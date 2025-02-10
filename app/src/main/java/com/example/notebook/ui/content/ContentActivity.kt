package com.example.notebook.ui.content

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.notebook.data.database.NoteDatabase
import com.example.notebook.data.model.Note
import com.example.notebook.databinding.ActivityContentBinding
import com.example.notebook.ui.main.NoteViewModel

class ContentActivity : AppCompatActivity() {
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var binding: ActivityContentBinding
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var copyNote: Note;
    private lateinit var note: Note;

    private var isNoteUpdated = false;

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteDatabase = NoteDatabase.getDatabase(this)
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        noteViewModel.setNoteDatabase(noteDatabase)
        noteViewModel.start()

        note = if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("note", Note::class.java)!!
        } else {
            intent.getParcelableExtra<Note>("note")!!
        }

        //BACK BUTTON CLICK
        binding.backButton.setOnClickListener {
            backPressHandler()
        }

        //ROOT
        binding.root.setOnTouchListener { _, _ ->
            hideKeyboard(binding.root, binding.notEditTitle)
            hideKeyboard(binding.root, binding.notEditContent)
            false
        }

        copyNote = note.copy();

        note.let {
            binding.notEditTime.setText("${it.createdDate} ${it.time}")
            binding.notEditTitle.setText(it.title)
            binding.notEditContent.setText(it.content)
        }

        binding.notEditTitle.addTextChangedListener {
            note.title = binding.notEditTitle.text.toString()
            buttonControl(copyNote, note)
        }

        binding.notEditContent.addTextChangedListener {
            note.let { noteToUpdate ->
                noteToUpdate.content = binding.notEditContent.text.toString()
            }

            buttonControl(copyNote, note)
        }

        buttonControl(copyNote, note)

        binding.btnUpdateNote.setOnClickListener {
            note.let { noteToUpdate ->
                noteViewModel.updateNote(noteToUpdate)
                copyNote = noteToUpdate.copy();
                hideKeyboard(binding.root, binding.notEditTitle)
                buttonControl(copyNote, noteToUpdate)
                isNoteUpdated = true
            }
        }
    }

    private fun buttonControl(copyNote: Note, note: Note) {
        if (copyNote != note) {
            binding.btnUpdateNote.isEnabled = true
            binding.btnUpdateNote.alpha = 1f
        } else {
            binding.btnUpdateNote.isEnabled = false
            binding.btnUpdateNote.alpha = 0.3f
        }
    }

    private fun isSame(): Boolean {
        return copyNote == note
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun hideKeyboard(view: View, searchView: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        searchView.clearFocus()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backPressHandler()
    }

    fun backPressHandler() {
        if (isNoteUpdated) {
            val resultIntent = Intent()
            resultIntent.putExtra("isNoteUpdated", true)
            setResult(Activity.RESULT_OK, resultIntent)
        }
        finish()
    }
}