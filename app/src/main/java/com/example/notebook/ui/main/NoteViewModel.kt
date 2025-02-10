package com.example.notebook.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notebook.data.database.NoteDatabase
import com.example.notebook.data.model.Note
import com.example.notebook.data.model.NoteState
import com.example.notebook.data.model.Response
import com.example.notebook.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteViewModel() : ViewModel() {
    private lateinit var noteRepository: NoteRepository
    lateinit var allNotes: MutableList<Note>
    private lateinit var noteDatabase: NoteDatabase

    fun start() {
        viewModelScope.launch {
            noteRepository = NoteRepository(noteDatabase.noteDao())
            allNotes = noteRepository.getAllNotes()
        }
    }

    fun setNoteDatabase(noteDatabase: NoteDatabase) {
        this.noteDatabase = noteDatabase
    }

    private val _noteState: MutableStateFlow<Response<MutableList<Note>>> = MutableStateFlow(Response.Loading)
    private var originalNoteState: MutableList<Note> = mutableListOf()
    var noteState = _noteState.asStateFlow()
    
    //GET ALL NOTES
    fun getAllNotes() {
        viewModelScope.launch {
            _noteState.value = Response.Loading
            try {
                val notes = noteRepository.getAllNotes()
                _noteState.value = Response.Success(notes)
                originalNoteState = notes;
            } catch (e: Exception) {
                _noteState.value = Response.Error(e.message ?: "Unknown Error")
            }
        }
    }

    //SEARCH NOTES
    fun searchNotes(text: String?) {
        val filteredNotes = originalNoteState.filter { note ->
            note.title!!.contains(text!!, ignoreCase = true)
        }

        _noteState.value = Response.Success(filteredNotes.toMutableList())
    }

    //SAVE NEW NOTE
    fun saveNewNote(note: Note) {
        viewModelScope.launch {
            noteRepository.insert(note)
            getAllNotes()
        }
    }

    //UPDATE NOTE
    fun updateNote(note: Note){
        viewModelScope.launch {
            noteRepository.update(note)
        }
    }

    //DELETE NOTE
    fun deleteNote(note: Note){
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }
}