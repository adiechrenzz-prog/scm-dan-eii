package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CobaltBright
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.SinercoTeal
import com.example.ui.theme.SlateCanvas
import com.example.ui.theme.SlateCanvasDeep

@Composable
fun SinercoWatermarkBackdrop(
    modifier: Modifier = Modifier,
    topHeroAccent: Boolean = false
) {
    val backgroundBrush = if (topHeroAccent) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1452B0),
                CobaltBright,
                Color(0xFF3A8DF2),
                SlateCanvasDeep,
                SlateCanvas
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                SlateCanvas,
                Color(0xFFEAF2FE),
                SlateCanvasDeep,
                SlateCanvas
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                drawCircle(
                    color = CobaltBright.copy(alpha = if (topHeroAccent) 0.14f else 0.055f),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.92f, size.height * 0.16f)
                )
                drawCircle(
                    color = SinercoTeal.copy(alpha = if (topHeroAccent) 0.12f else 0.055f),
                    radius = size.width * 0.58f,
                    center = Offset(size.width * 0.08f, size.height * 0.78f)
                )
            }
            .testTag("sinerco_watermark_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        // Large faded Sinerco logo + bold SINERCO watermark centered across canvas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = if (topHeroAccent) 60.dp else 18.dp)
                .rotate(-14f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_sinerco),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(118.dp)
                    .alpha(if (topHeroAccent) 0.11f else 0.085f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SINERCO",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 9.sp,
                textAlign = TextAlign.Center,
                color = CobaltPrimary.copy(alpha = if (topHeroAccent) 0.09f else 0.065f)
            )
            Text(
                text = "SCM & EII DEPARTMENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.5.sp,
                textAlign = TextAlign.Center,
                color = SinercoTeal.copy(alpha = if (topHeroAccent) 0.11f else 0.08f)
            )
        }
    }
}
