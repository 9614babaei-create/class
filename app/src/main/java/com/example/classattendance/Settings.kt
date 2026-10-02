package com.example.classattendance

import android.content.Context

class Settings(context: Context) {
    private val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var schoolName: String
        get() = p.getString("schoolName", "") ?: ""
        set(v) = p.edit().putString("schoolName", v).apply()
    var teacherName: String
        get() = p.getString("teacherName", "") ?: ""
        set(v) = p.edit().putString("teacherName", v).apply()
}
