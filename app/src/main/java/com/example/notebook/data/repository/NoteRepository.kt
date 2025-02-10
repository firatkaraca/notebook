package com.example.notebook.data.repository

import com.example.notebook.data.dao.NoteDao
import com.example.notebook.data.model.Note

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun getAllNotes(): MutableList<Note> {
        return noteDao.getAllNotes()
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
}