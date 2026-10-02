package com.example.classattendance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY name")
    fun observeAll(): Flow<List<SchoolClass>>
    @Insert suspend fun insert(item: SchoolClass): Long
    @Update suspend fun update(item: SchoolClass)
    @Delete suspend fun delete(item: SchoolClass)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name")
    fun observeByClass(classId: Long): Flow<List<Student>>
    @Insert suspend fun insert(item: Student): Long
    @Update suspend fun update(item: Student)
    @Delete suspend fun delete(item: Student)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun observeByDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun observeByStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date BETWEEN :from AND :to ORDER BY date")
    suspend fun byStudentAndRange(studentId: Long, from: String, to: String): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Attendance)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteForStudent(studentId: Long)
}
