package com.example.util

import android.content.Context
import com.example.data.model.EiSeedDefaults
import com.example.data.model.TeamMember
import org.json.JSONArray

object CustomJobTitleStore {
    private const val PREFS_NAME = "custom_job_titles_prefs"
    private const val KEY_TITLES = "custom_job_titles_json"

    fun getCustomTitles(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TITLES, null)
            .orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val value = arr.optString(i).trim().take(80)
                    if (value.isNotEmpty()) add(value)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun addCustomTitle(context: Context, title: String): List<String> {
        val clean = title.trim().take(80)
        if (clean.isEmpty()) return getCustomTitles(context)

        val current = getCustomTitles(context)
        val updated = deduplicateCaseInsensitive(current + clean)
        val arr = JSONArray().apply {
            updated.forEach { put(it) }
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TITLES, arr.toString())
            .apply()
        return updated
    }

    fun mergedJobTitles(
        context: Context,
        teamMembers: List<TeamMember> = emptyList()
    ): List<String> {
        val combined = EiSeedDefaults.jobTitleOptions +
            getCustomTitles(context) +
            teamMembers.map { it.role }
        return deduplicateCaseInsensitive(combined)
    }

    private fun deduplicateCaseInsensitive(items: List<String>): List<String> {
        val seen = HashSet<String>()
        val result = mutableListOf<String>()
        items.forEach { raw ->
            val trimmed = raw.trim().take(80)
            if (trimmed.isNotEmpty() && seen.add(trimmed.lowercase())) {
                result.add(trimmed)
            }
        }
        return result
    }
}
