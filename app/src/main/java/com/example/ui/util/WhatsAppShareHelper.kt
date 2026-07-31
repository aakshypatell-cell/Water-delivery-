package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Customer

object WhatsAppShareHelper {

    fun sendBillOnWhatsApp(
        context: Context,
        customer: Customer,
        monthName: String,
        deliveredDays: Int,
        totalCans: Int,
        totalAmount: Double,
        paidAmount: Double,
        pendingAmount: Double,
        upiId: String
    ) {
        val cleanPhone = customer.phone.replace("[^0-9]".toRegex(), "")
        val formattedPhone = if (cleanPhone.startsWith("91") || cleanPhone.length > 10) {
            cleanPhone
        } else {
            "91$cleanPhone"
        }

        val message = StringBuilder().apply {
            append("💧 *AQUA PURE WATER SERVICES*\n")
            append("📋 *Monthly Bill Statement - $monthName*\n\n")
            append("Hello *${customer.name}*,\n")
            append("Here is your water camper delivery summary for $monthName:\n\n")
            append("• *Camper Size:* ${customer.camperSize}\n")
            append("• *Price/Camper:* ₹${customer.pricePerCamper.toInt()}\n")
            append("• *Delivered Days:* $deliveredDays days\n")
            append("• *Total Campers:* $totalCans cans\n")
            append("-----------------------------------\n")
            append("• *Total Bill Amount:* ₹${totalAmount.toInt()}\n")
            append("• *Amount Received:* ₹${paidAmount.toInt()}\n")
            append("• *NET PENDING DUE:* ₹${pendingAmount.toInt()}\n")
            append("-----------------------------------\n\n")
            if (pendingAmount > 0) {
                append("💳 *UPI Payment ID:* `$upiId`\n")
                append("Please pay via UPI or Cash at your earliest convenience. Thank you for your business! 🙏")
            } else {
                append("Thank you! Your bill for this month is fully settled. Page updated. ✅")
            }
        }.toString()

        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback general share
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Bill via"))
        }
    }
}
