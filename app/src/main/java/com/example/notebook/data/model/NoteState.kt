package com.example.notebook.data.model

data class NoteState(
    val noteList: MutableList<Note> = mutableListOf(),
    val searchText: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    var showAddDialog: Boolean = false,
)