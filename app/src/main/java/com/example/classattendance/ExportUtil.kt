package com.example.classattendance

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.classattendance.data.*
import java.io.File
import java.time.LocalDate

object ExportUtil {
    suspend fun exportClass(context: Context, schoolClass: SchoolClass, students: List<Student>, db: AppDatabase): Intent {
        val file = File(context.cacheDir, "attendance_${schoolClass.name}_${LocalDate.now()}.csv")
        file.bufferedWriter(Charsets.UTF_8).use { out ->
            out.write("\uFEFFنام دانش‌آموز,تاریخ,وضعیت\n")
            val from = LocalDate.now().withDayOfMonth(1).toString()
            val to = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).toString()
            for (s in students) {
                db.attendanceDao().byStudentAndRange(s.id, from, to).forEach {
                    out.write("${s.name.csv()},${it.date},${it.status.csv()}\n")
                }
            }
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "گزارش حضور و غیاب ${schoolClass.name}")
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                context, context.packageName + ".fileprovider", file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    private fun String.csv() = "\"" + replace("\"", "\"\"") + "\""
}
