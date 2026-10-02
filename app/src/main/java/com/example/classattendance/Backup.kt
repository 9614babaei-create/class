package com.example.classattendance

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.classattendance.data.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object Backup {
    suspend fun create(context: Context, db: AppDatabase): Intent {
        val classes = db.classDao().observeAll().first()
        val out = JSONObject()
        val ca = JSONArray()
        for (c in classes) {
            val co = JSONObject().put("id",c.id).put("name",c.name).put("description",c.description)
            val sa = JSONArray()
            val students = db.studentDao().observeByClass(c.id).first()
            for (s in students) {
                val so = JSONObject().put("id",s.id).put("name",s.name).put("studentNumber",s.studentNumber)
                val aa = JSONArray()
                db.attendanceDao().observeByStudent(s.id).first().forEach { a ->
                    aa.put(JSONObject().put("date",a.date).put("status",a.status))
                }
                so.put("attendance",aa); sa.put(so)
            }
            co.put("students",sa); ca.put(co)
        }
        out.put("classes",ca)
        val file=File(context.cacheDir,"attendance_backup.json")
        file.writeText(out.toString(2),Charsets.UTF_8)
        return Intent(Intent.ACTION_SEND).apply {
            type="application/json"
            putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(context,context.packageName+".fileprovider",file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
