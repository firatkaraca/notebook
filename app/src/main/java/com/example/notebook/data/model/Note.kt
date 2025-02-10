package com.example.notebook.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    var title: String? = "",
    var content: String? = "",
    val createdDate: String? = "",
    val changedDate: String? = "",
    val time: String? = "",
    val color: String? = ""
) : Parcelable