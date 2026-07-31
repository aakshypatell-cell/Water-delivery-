package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.DeliveryRecord
import com.example.data.model.DeliveryStatus
import com.example.data.model.Payment
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateCustomerMonthlyPdf(
        context: Context,
        businessName: String,
        monthName: String,
        customer: Customer,
        deliveries: List<DeliveryRecord>,
        payments: List<Payment>,
        totalAmount: Double,
        totalPaid: Double,
        pendingAmount: Double
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()

        // Background header
        paint.color = Color.parseColor("#0288D1")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Header Text
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 22f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(businessName.uppercase(), 30f, 45f, titlePaint)

        titlePaint.textSize = 14f
        titlePaint.typeface = Typeface.DEFAULT
        canvas.drawText("WATER CAMPER DELIVERY INVOICE - $monthName", 30f, 75f, titlePaint)

        // Customer Info
        paint.color = Color.parseColor("#333333")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        var yPos = 140f

        canvas.drawText("CUSTOMER DETAILS", 30f, yPos, paint)
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 12f
        yPos += 20f
        canvas.drawText("Name: ${customer.name}", 30f, yPos, paint)
        canvas.drawText("Phone: ${customer.phone}", 320f, yPos, paint)
        yPos += 18f
        canvas.drawText("Address: ${customer.address} (${customer.area})", 30f, yPos, paint)
        canvas.drawText("Camper Size: ${customer.camperSize} @ ₹${customer.pricePerCamper.toInt()}/can", 320f, yPos, paint)

        // Divider
        yPos += 25f
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, yPos, 565f, yPos, paint)

        // Table Header
        yPos += 25f
        paint.color = Color.parseColor("#F0F4F8")
        canvas.drawRect(30f, yPos - 15f, 565f, yPos + 10f, paint)

        paint.color = Color.parseColor("#0288D1")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("DATE", 40f, yPos, paint)
        canvas.drawText("STATUS", 140f, yPos, paint)
        canvas.drawText("QUANTITY", 260f, yPos, paint)
        canvas.drawText("RATE", 380f, yPos, paint)
        canvas.drawText("AMOUNT", 480f, yPos, paint)

        yPos += 20f
        paint.color = Color.parseColor("#444444")
        paint.typeface = Typeface.DEFAULT

        var totalCans = 0
        var deliveredDays = 0

        val sortedDeliveries = deliveries.sortedBy { it.date }
        for (del in sortedDeliveries) {
            if (yPos > 700f) break // Simple page limit handling
            canvas.drawText(del.date, 40f, yPos, paint)
            
            val statusStr = when (del.status) {
                DeliveryStatus.DELIVERED -> {
                    deliveredDays++
                    totalCans += del.quantityDelivered
                    "Delivered"
                }
                DeliveryStatus.NOT_DELIVERED -> "Not Delivered"
                DeliveryStatus.HOLIDAY -> "Holiday"
                DeliveryStatus.SKIP -> "Skipped"
            }
            
            canvas.drawText(statusStr, 140f, yPos, paint)
            canvas.drawText("${del.quantityDelivered} cans", 260f, yPos, paint)
            canvas.drawText("₹${del.priceAtTime.toInt()}", 380f, yPos, paint)
            val lineAmt = del.quantityDelivered * del.priceAtTime
            canvas.drawText("₹${lineAmt.toInt()}", 480f, yPos, paint)

            yPos += 18f
        }

        // Summary Box
        yPos += 20f
        paint.color = Color.parseColor("#E1F5FE")
        canvas.drawRect(30f, yPos, 565f, yPos + 100f, paint)

        paint.color = Color.parseColor("#01579B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f

        yPos += 25f
        canvas.drawText("Delivered Days: $deliveredDays", 50f, yPos, paint)
        canvas.drawText("Total Cans: $totalCans", 300f, yPos, paint)

        yPos += 22f
        canvas.drawText("Total Bill Amount: ₹${totalAmount.toInt()}", 50f, yPos, paint)
        canvas.drawText("Total Paid Amount: ₹${totalPaid.toInt()}", 300f, yPos, paint)

        yPos += 25f
        paint.textSize = 14f
        paint.color = if (pendingAmount > 0) Color.RED else Color.parseColor("#2E7D32")
        canvas.drawText("NET PENDING BALANCE: ₹${pendingAmount.toInt()}", 50f, yPos, paint)

        pdfDocument.finishPage(page)

        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoice_${customer.name.replace(" ", "_")}_$monthName.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_SHORT).show()

            // Share PDF
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Bill PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
