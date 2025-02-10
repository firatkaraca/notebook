package com.example.notebook.data.dao

import androidx.room.*
import com.example.notebook.data.model.Note

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAllNotes(): MutableList<Note>
}