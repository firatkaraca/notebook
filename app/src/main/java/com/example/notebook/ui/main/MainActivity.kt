package com.example.notebook.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notebook.data.model.Note
import com.example.notebook.data.model.Response
import com.example.notebook.databinding.ActivityMainBinding
import com.example.notebook.ui.content.ContentActivity
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.data.database.NoteDatabase
import com.example.notebook.databinding.DialogNewNoteBinding
import com.example.notebook.utils.Utils

class MainActivity : AppCompatActivity() {
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    noteViewModel.getAllNotes()
                }
            }

        noteDatabase = NoteDatabase.getDatabase(this)

        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        noteViewModel.setNoteDatabase(noteDatabase)
        noteViewModel.start()

        //NEW NOTE DIALOG
        binding.addNewNote.setOnClickListener {
            showNoteDialog()
            hideKeyboard(binding.root, binding.searchView)
        }

        //SEARCH
        binding.searchView.addTextChangedListener { text ->
            noteViewModel.searchNotes(text.toString())
        }

        //ROOT
        binding.root.setOnTouchListener { _, _ ->
            hideKeyboard(binding.root, binding.searchView)
            false
        }

        lifecycleScope.launch {
            noteViewModel.noteState.collect { response ->
                when (response) {
                    is Response.Success -> {
                        binding.notesProgress.visibility = View.GONE
                        updateRecyclerView(response.data)
                    }

                    is Response.Error -> {
                        println("ERROR: ${response.message}")
                    }

                    else -> {
                        binding.notesProgress.visibility = View.VISIBLE
                    }
                }
            }
        }

        noteViewModel.getAllNotes()
    }

    private fun textChange(editText: EditText, otherText: EditText, btn: Button) {
        btn.isEnabled = false
        btn.alpha = 0.3f

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val note = editText.text?.toString().orEmpty()
                val content = otherText.text?.toString().orEmpty()

                if (note.isNotEmpty() && content.isNotEmpty()) {
                    btn.isEnabled = true
                    btn.alpha = 1f
                } else {
                    btn.isEnabled = false
                    btn.alpha = 0.3f
                }
            }
        }

        editText.addTextChangedListener(textWatcher)
        otherText.addTextChangedListener(textWatcher)
    }

    private fun showNoteDialog() {
        val binding = DialogNewNoteBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
        builder.setView(binding.root)

        val dialog = builder.create()

        binding.btnSave.setOnClickListener {
            val note = Note(
                id = 0,
                title = binding.note.text.toString(),
                content = binding.content.text.toString(),
                createdDate = Utils.getCurrentDate(),
                changedDate = Utils.getCurrentDate(),
                time = Utils.getCurrentTime(),
                color = Utils.getRandomColor()
            )

            noteViewModel.saveNewNote(note)
            dialog.dismiss()
        }

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        textChange(binding.note, binding.content, binding.btnSave)
        textChange(binding.content, binding.note, binding.btnSave)

        dialog.show()
        binding.note.requestFocusFromTouch()

        binding.note.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.note, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun updateRecyclerView(noteList: MutableList<Note>) {
        if (noteList.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.noDataText.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.noDataText.visibility = View.GONE

            val adapter = NoteAdapter(noteList) { note ->
                val intent = Intent(this, ContentActivity::class.java)
                intent.putExtra("note", note)
                resultLauncher.launch(intent)
            }

            binding.recyclerView.apply {
                this.adapter = adapter
                this.layoutManager = LinearLayoutManager(this@MainActivity)
            }

            //DELETE ITEM
            val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    noteViewModel.deleteNote(noteList[position])
                    noteList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    if (isCurrentlyActive && dX < 0) {
                        val itemView = viewHolder.itemView
                        val paint = Paint().apply { color = Color.parseColor("#FF0000") }

                        c.drawRect(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat(),
                            paint
                        )

                        val textPaint = Paint().apply {
                            color = Color.WHITE
                            textSize = 40f
                            textAlign = Paint.Align.CENTER
                        }
                        val centerY = (itemView.top + itemView.bottom) / 2f - ((textPaint.descent() + textPaint.ascent()) / 2)
                        c.drawText("Sil", itemView.right.toFloat() - 100, centerY, textPaint)
                    }
                }
            }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(binding.root, binding.searchView)
    }

    private fun hideKeyboard(view: View, searchView: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        searchView.clearFocus()
    }
}