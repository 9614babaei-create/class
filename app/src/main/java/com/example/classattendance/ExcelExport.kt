package com.example.classattendance

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.classattendance.data.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

object ExcelExport {
    suspend fun create(
        context: Context, schoolClass: SchoolClass, students: List<Student>,
        db: AppDatabase, school: String, teacher: String
    ): Intent {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("حضور و غیاب")
        var r = 0
        sheet.createRow(r++).createCell(0).setCellValue("کلاس: ${schoolClass.name}")
        sheet.createRow(r++).createCell(0).setCellValue("مدرسه: $school")
        sheet.createRow(r++).createCell(0).setCellValue("معلم: $teacher")
        r++
        val h = sheet.createRow(r++)
        listOf("نام دانش‌آموز","تاریخ","وضعیت").forEachIndexed { i,v -> h.createCell(i).setCellValue(v) }
        for (s in students) {
            val list = db.attendanceDao().observeByStudent(s.id).kotlinx.coroutines.flow.first()
            for (a in list) {
                val row = sheet.createRow(r++)
                row.createCell(0).setCellValue(s.name)
                row.createCell(1).setCellValue(a.date)
                row.createCell(2).setCellValue(a.status)
            }
        }
        val file = File(context.cacheDir, "attendance_${schoolClass.name}.xlsx")
        file.outputStream().use { wb.write(it) }
        wb.close()
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.packageName+".fileprovider", file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
