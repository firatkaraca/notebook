package com.example.notebook.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.data.model.Note
import com.example.notebook.databinding.ItemNoteBinding
import com.example.notebook.utils.Utils.Companion.getShortText
import com.example.notebook.utils.Utils.Companion.isToday

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onItemClick: (Note) -> Unit,
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.shortText.setTextColor(Color.parseColor(note.color))

            binding.shortText.text =
                if (!note.title.isNullOrEmpty()) note.title?.firstOrNull().toString().uppercase()
                    ?: "?" else "?"
            binding.noteTitle.text = note.title.toString().getShortText(20)
            binding.noteContent.text = note.content.toString().getShortText(30)

            val isToday = note.changedDate.toString().isToday()
            binding.noteDateOrTime.text = if (isToday) note.time else note.changedDate

            //Click event
            itemView.setOnClickListener {
                onItemClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    fun removeItem(position: Int) {
        if (position in notes.indices) {
            notes.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, notes.size)
        }
    }
}