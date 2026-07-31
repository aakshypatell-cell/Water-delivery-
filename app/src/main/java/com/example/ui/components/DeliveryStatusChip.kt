package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.ui.theme.StatusDeliveredGreen
import com.example.ui.theme.StatusDeliveredGreenContainer
import com.example.ui.theme.StatusHolidayPurple
import com.example.ui.theme.StatusHolidayPurpleContainer
import com.example.ui.theme.StatusSkippedOrange
import com.example.ui.theme.StatusSkippedOrangeContainer

@Composable
fun DeliveryStatusChip(
    status: DeliveryStatus,
    quantity: Int = 1,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor, icon, label) = when (status) {
        DeliveryStatus.DELIVERED -> Quad(
            StatusDeliveredGreenContainer,
            StatusDeliveredGreen,
            Icons.Default.CheckCircle,
            "Delivered ($quantity)"
        )
        DeliveryStatus.SKIP -> Quad(
            StatusSkippedOrangeContainer,
            StatusSkippedOrange,
            Icons.Default.Block,
            "Skipped"
        )
        DeliveryStatus.HOLIDAY -> Quad(
            StatusHolidayPurpleContainer,
            StatusHolidayPurple,
            Icons.Default.Weekend,
            "Holiday"
        )
        DeliveryStatus.NOT_DELIVERED -> Quad(
            Color(0xFFEEEEEE),
            Color(0xFF616161),
            Icons.Default.DoNotDisturb,
            "Not Marked"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
