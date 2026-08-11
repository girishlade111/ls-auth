package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppIconPack
import com.example.util.MonogramGenerator

@Composable
fun ServiceIconView(
    issuer: String,
    accountName: String,
    modifier: Modifier = Modifier,
    iconKey: String? = null,
    size: Dp = 44.dp
) {
    val matchedIcon = AppIconPack.resolveIcon(issuer = issuer, accountName = accountName, iconKey = iconKey)

    if (matchedIcon != null) {
        val isLightBg = matchedIcon.id == "snapchat" || matchedIcon.id == "binance"
        val iconTint = if (isLightBg) Color.Black else Color.White

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(matchedIcon.brandColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = matchedIcon.iconVector,
                contentDescription = "${matchedIcon.name} icon",
                tint = iconTint,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    } else {
        // Deterministic Monogram Generator for accounts without a matched icon
        val monogram = MonogramGenerator.generateMonogram(issuer = issuer, accountName = accountName)

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(monogram.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monogram.initials,
                color = monogram.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }
    }
}

