const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_spv_01";
const BOB_UID = "bob_worker_02";
const WORKSPACE_ID = "scm_eii";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles, shared workspace, or photos", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("workspaces").doc(WORKSPACE_ID).get());
  await assertFails(
    unauthDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("work_orders")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );
  await assertFails(
    unauthDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("photos")
      .doc("photo_1")
      .set({
        workspaceId: WORKSPACE_ID,
        userId: ALICE_UID,
        contentBase64: "dGVzdHBob3RvYnl0ZXM=",
        mimeType: "image/jpeg",
        createdAt: now,
        updatedAt: now,
      })
  );
});

test("Personal profiles remain strictly owner-isolated while shared workspace & cloud photos are readable by all signed-in users", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const updated = new Date(Date.now() - 1000);

  // 1. Personal WorkspaceProfile
  const aliceProfileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(
    aliceProfileRef.set({
      userId: ALICE_UID,
      displayName: "Andi Pratama",
      jobTitle: "Senior Manager",
      activeRole: "Supervisor",
      activeMemberName: "Andi Pratama",
      initialized: true,
      profileCompleted: true,
      createdAt: created,
      updatedAt: created,
    })
  );
  // Bob cannot read or modify Alice's personal profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked",
      updatedAt: updated,
    })
  );

  // 2. Shared Workspace Root + Editable TeamMembers
  const sharedWsRef = aliceDb.collection("workspaces").doc(WORKSPACE_ID);
  await assertSucceeds(
    sharedWsRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      initialized: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  const memberRef = sharedWsRef.collection("team_members").doc("member_0");
  await assertSucceeds(
    memberRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      name: "Budi Santoso",
      role: "Senior Manager",
      specialty: "SCM & Electrical Instrumentation",
      status: "Hadir",
      lastTime: "07:45",
      sortOrder: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob can query and edit TeamMember (name, role, specialty)
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("team_members")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("team_members")
      .doc("member_0")
      .update({
        name: "Budi Santoso, ST",
        role: "Manager",
        specialty: "Lead SCM dan EII Team",
        updatedAt: updated,
      })
  );

  // 3. Alice uploads a cloud photo -> Bob can read it; Alice creates WorkOrder with new categories referencing fbphoto://
  const photoRef = sharedWsRef.collection("photos").doc("photo_before_1");
  await assertSucceeds(
    photoRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      contentBase64: "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQg=",
      mimeType: "image/jpeg",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("photos")
      .doc("photo_before_1")
      .get()
  );

  // Photos are immutable against update
  await assertFails(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("photos")
      .doc("photo_before_1")
      .update({ contentBase64: "AAAA", updatedAt: updated })
  );

  const woRef = sharedWsRef.collection("work_orders").doc("wo_001");
  await assertSucceeds(
    woRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      woCode: "WO-001",
      category: "Function Test",
      equipmentTag: "PT-101A (Suction Pressure)",
      technician: "Budi Santoso, ST",
      description: "Function test loop 4-20mA dan dokumen instalasi.",
      status: "Open",
      reportDate: "19 Nov 2026",
      beforePhotoUri: "fbphoto://photo_before_1",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob sees Alice's WorkOrder + can edit category to Pembelian / Instalasi / Meeting / Persiapan
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("work_orders")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("work_orders")
      .doc("wo_001")
      .update({
        category: "Instalasi",
        equipmentTag: "PT-101B",
        technician: "Budi Santoso, ST",
        description: "Instalasi dan wiring selesai.",
        status: "Done",
        afterPhotoUri: "fbphoto://photo_before_1",
        updatedAt: updated,
      })
  );

  // 4. ScmJob with expanded categories (Mob, Demob, Plan, Forecast, Meeting)
  const scmRef = sharedWsRef.collection("scm_jobs").doc("scm_001");
  await assertSucceeds(
    scmRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      jobCode: "SCM-001",
      category: "Mob",
      title: "Mobilisasi Tool Calibration Kit ke Site B",
      referenceNumber: "MOB-2026-01",
      picName: "Admin SCM",
      destinationOrVendor: "Site Plant B",
      description: "Mobilisasi peralatan kalibrasi dan toolbox.",
      status: "Open",
      jobDate: "19 Nov 2026",
      beforePhotoUri: "fbphoto://photo_before_1",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("scm_jobs")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("scm_jobs")
      .doc("scm_001")
      .update({
        category: "Forecast",
        title: "Forecast Spare Part Q4",
        referenceNumber: "FC-2026-04",
        picName: "Supervisor SCM",
        destinationOrVendor: "Gudang Utama",
        description: "Perencanaan kebutuhan stok Q4.",
        status: "Proses",
        updatedAt: updated,
      })
  );

  // 5. Briefing & AttendanceLog
  await assertSucceeds(
    sharedWsRef.collection("briefings").doc("b_1").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      badge: "SHIFT BRIEFING",
      timeTag: "08:00 - 17:00",
      title: "Briefing Pagi SCM dan EII",
      summary: "Koordinasi jadwal mob/demob dan instalasi.",
      isSafetyAlert: false,
      sortOrder: 0,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("briefings")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );
  await assertSucceeds(
    sharedWsRef.collection("attendance_logs").doc("att_1").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      personName: "Budi Santoso, ST",
      attendanceType: "Masuk",
      timeLabel: "07:45 WIB",
      gpsCoordinates: "-6.4521, 106.6892",
      status: "Verified GPS",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Rejects unfiltered list queries, invalid workspaces, @DocumentId leakage, ghost fields, and invalid enums", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const wsRef = aliceDb.collection("workspaces").doc(WORKSPACE_ID);

  // Unfiltered list query without .where("workspaceId", "==", WORKSPACE_ID) must fail
  await assertFails(wsRef.collection("work_orders").get());
  await assertFails(wsRef.collection("scm_jobs").get());
  await assertFails(wsRef.collection("team_members").get());

  // Other workspaceId outside 'scm_eii' is blocked
  await assertFails(
    aliceDb.collection("workspaces").doc("other_ws").set({
      workspaceId: "other_ws",
      userId: ALICE_UID,
      initialized: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject 'id' field inside WorkOrder document
  await assertFails(
    wsRef.collection("work_orders").doc("wo_bad_id").set({
      id: "wo_bad_id",
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      woCode: "WO-002",
      category: "Meeting",
      equipmentTag: "Weekly Sync",
      technician: "Andi",
      description: "Coordinasi mingguan",
      status: "Open",
      reportDate: "19 Nov 2026",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject ghost field
  await assertFails(
    wsRef.collection("scm_jobs").doc("scm_ghost").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      jobCode: "SCM-999",
      category: "Demob",
      title: "Demob Alat",
      referenceNumber: "DM-01",
      picName: "Admin",
      destinationOrVendor: "Gudang",
      description: "Return tools",
      status: "Open",
      jobDate: "19 Nov 2026",
      isApproved: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject invalid category enum
  await assertFails(
    wsRef.collection("work_orders").doc("wo_bad_cat").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      woCode: "WO-003",
      category: "Kategori Ngawur",
      equipmentTag: "PT-101",
      technician: "Andi",
      description: "Test",
      status: "Open",
      reportDate: "19 Nov 2026",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    wsRef.collection("photos").doc("photo_future").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      contentBase64: "/9j/4AAQSkZJRgABAQAAAQABAAD=",
      mimeType: "image/jpeg",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );
});

test("Allows Status Posisi Kerja logs & TeamMember position updates and enforces required destination notes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const updated = new Date(Date.now() - 1000);
  const wsRef = aliceDb.collection("workspaces").doc(WORKSPACE_ID);

  await assertSucceeds(
    wsRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      initialized: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // TeamMember with Kantor + updating to Dinas Luar Kota with workNote
  await assertSucceeds(
    wsRef.collection("team_members").doc("member_achmad").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      name: "Achmad Rohadi",
      role: "Supervisor E&II",
      specialty: "SCM & Electrical Instrumentation",
      status: "Kantor",
      workNote: "",
      lastTime: "08:00",
      sortOrder: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("team_members")
      .doc("member_achmad")
      .update({
        status: "Dinas Luar Kota",
        workNote: "Plant Cilacap",
        lastTime: "09:15 • 19 Nov",
        updatedAt: updated,
      })
  );

  // Valid WorkStatusLog without note (Kantor, Workshop, Cuti, Sakit)
  await assertSucceeds(
    wsRef.collection("work_status_logs").doc("ws_kantor").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      personName: "Achmad Rohadi",
      workStatus: "Kantor",
      destinationNote: "",
      timeLabel: "08:00 • 19 Nov",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid WorkStatusLog with note (Luar Area Kerja, Dinas Luar Kota, Izin)
  await assertSucceeds(
    wsRef.collection("work_status_logs").doc("ws_dinas").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      personName: "Achmad Rohadi",
      workStatus: "Dinas Luar Kota",
      destinationNote: "Surabaya - Commissioning",
      timeLabel: "09:15 • 19 Nov",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject Luar Area Kerja / Dinas Luar Kota / Izin without destinationNote
  await assertFails(
    wsRef.collection("work_status_logs").doc("ws_bad_luar").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      personName: "Achmad Rohadi",
      workStatus: "Luar Area Kerja",
      destinationNote: "",
      timeLabel: "09:20 • 19 Nov",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertFails(
    wsRef.collection("work_status_logs").doc("ws_bad_izin").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      personName: "Achmad Rohadi",
      workStatus: "Izin",
      destinationNote: "",
      timeLabel: "09:25 • 19 Nov",
      createdAt: created,
      updatedAt: created,
    })
  );

  // All signed-in users can query shared work_status_logs
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("work_status_logs")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );
});

test("Allows up to 4 before and 4 after photos in WorkOrder and ScmJob and rejects > 4 photos", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const updated = new Date(Date.now() - 1000);
  const wsRef = aliceDb.collection("workspaces").doc(WORKSPACE_ID);

  await assertSucceeds(
    wsRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      initialized: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  const fourBefore = [
    "fbphoto://p1",
    "fbphoto://p2",
    "fbphoto://p3",
    "fbphoto://p4",
  ];
  const fourAfter = [
    "fbphoto://a1",
    "fbphoto://a2",
    "fbphoto://a3",
    "fbphoto://a4",
  ];

  await assertSucceeds(
    wsRef.collection("work_orders").doc("wo_multi").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      woCode: "WO-020",
      category: "Preventive Maintenance",
      equipmentTag: "PT-201",
      technician: "Achmad Rohadi",
      description: "Multi-photo test",
      status: "In Progress",
      reportDate: "19 Nov 2026",
      beforePhotoUri: "fbphoto://p1",
      beforePhotoUris: fourBefore,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update adding 4 after photos
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("work_orders")
      .doc("wo_multi")
      .update({
        afterPhotoUri: "fbphoto://a1",
        afterPhotoUris: fourAfter,
        updatedAt: updated,
      })
  );

  // ScmJob with 4 before & 4 after photos
  await assertSucceeds(
    wsRef.collection("scm_jobs").doc("scm_multi").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      jobCode: "SCM-020",
      category: "Pengiriman Barang",
      title: "Pengiriman Valve",
      referenceNumber: "SJ-020",
      picName: "Admin SCM",
      destinationOrVendor: "Site A",
      description: "4 foto dokumentasi",
      status: "Proses",
      jobDate: "19 Nov 2026",
      beforePhotoUri: "fbphoto://p1",
      afterPhotoUri: "fbphoto://a1",
      beforePhotoUris: fourBefore,
      afterPhotoUris: fourAfter,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject 5 photos
  await assertFails(
    wsRef.collection("work_orders").doc("wo_too_many").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      woCode: "WO-021",
      category: "Preventive Maintenance",
      equipmentTag: "PT-202",
      technician: "Achmad Rohadi",
      description: "Too many photos",
      status: "Open",
      reportDate: "19 Nov 2026",
      beforePhotoUris: [
        "fbphoto://p1",
        "fbphoto://p2",
        "fbphoto://p3",
        "fbphoto://p4",
        "fbphoto://p5",
      ],
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Allows shared DailyWorkReport CRUD with all 9 categories and rejects invalid category or unfiltered query", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const updated = new Date(Date.now() - 1000);
  const wsRef = aliceDb.collection("workspaces").doc(WORKSPACE_ID);

  await assertSucceeds(
    wsRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      initialized: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid DailyWorkReport with Calibration & documentation photos
  await assertSucceeds(
    wsRef.collection("daily_work_reports").doc("dwr_1").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      reportCode: "DWR-001",
      workDate: "22 Nov 2026",
      technicianName: "Andi Pratama",
      category: "Calibration",
      equipmentName: "TT-201 Temperature Transmitter",
      workLocation: "Skid Compressor Unit 02",
      activityDescription: "Kalibrasi temperature transmitter range 0-150 C",
      startTime: "10.00",
      endTime: "12.00",
      workResult: "Deviasi 0.1%, hasil kalibrasi normal",
      status: "Selesai",
      issuesFound: "-",
      photoUris: ["fbphoto://p1", "fbphoto://p2"],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob can query with workspaceId filter and update status
  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("daily_work_reports")
      .where("workspaceId", "==", WORKSPACE_ID)
      .get()
  );

  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("daily_work_reports")
      .doc("dwr_1")
      .update({
        status: "Proses",
        updatedAt: updated,
      })
  );

  // Unfiltered query without workspaceId fails
  await assertFails(wsRef.collection("daily_work_reports").get());

  // Invalid category fails
  await assertFails(
    wsRef.collection("daily_work_reports").doc("dwr_bad").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      reportCode: "DWR-002",
      workDate: "22 Nov 2026",
      technicianName: "Andi Pratama",
      category: "Random Invalid Category",
      equipmentName: "PT-101",
      workLocation: "Plant A",
      activityDescription: "Cek pressure",
      startTime: "08.00",
      endTime: "09.00",
      workResult: "OK",
      status: "Selesai",
      issuesFound: "-",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("TeamMember supports optional whatsappNumber up to 30 chars and rejects oversized values", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const updated = new Date(Date.now() - 1000);
  const wsRef = aliceDb.collection("workspaces").doc(WORKSPACE_ID);

  await assertSucceeds(
    wsRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      initialized: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  const memberRef = wsRef.collection("team_members").doc("member_wa_1");
  await assertSucceeds(
    memberRef.set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      name: "Andi Pratama",
      role: "Teknisi Electrical & Instrumentation",
      specialty: "Instrument",
      whatsappNumber: "08988319566",
      status: "Kantor",
      workNote: "",
      lastTime: "08:00",
      sortOrder: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    bobDb
      .collection("workspaces")
      .doc(WORKSPACE_ID)
      .collection("team_members")
      .doc("member_wa_1")
      .update({
        name: "Andi Pratama",
        role: "Teknisi Electrical & Instrumentation",
        specialty: "Instrument & Calibration",
        whatsappNumber: "081234567890",
        updatedAt: updated,
      })
  );

  await assertFails(
    wsRef.collection("team_members").doc("member_wa_bad").set({
      workspaceId: WORKSPACE_ID,
      userId: ALICE_UID,
      name: "Invalid WA",
      role: "Admin SCM",
      specialty: "SCM",
      whatsappNumber: "081234567890123456789012345678901",
      status: "Kantor",
      workNote: "",
      lastTime: "08:00",
      sortOrder: 1,
      createdAt: created,
      updatedAt: created,
    })
  );
});



