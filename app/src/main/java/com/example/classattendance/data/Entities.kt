package com.example.classattendance.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val name: String,
    val studentNumber: String = ""
)

@Entity(
    tableName = "attendance",
    primaryKeys = ["studentId", "date"]
)
data class Attendance(
    val studentId: Long,
    val date: String,
    val status: String
)
