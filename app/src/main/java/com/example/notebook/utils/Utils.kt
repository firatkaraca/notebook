package com.example.notebook.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Utils {
    companion object {
        fun getCurrentDate(format: String = "dd/MM/yyyy"): String {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            return sdf.format(Date(System.currentTimeMillis()))
        }

        fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(System.currentTimeMillis()))
        }

        fun String.isToday(): Boolean {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val inputDate = sdf.parse(this)
            val today = sdf.parse(getCurrentDate())

            return inputDate != null && today != null && inputDate.compareTo(today) == 0
        }

        fun getRandomColor(): String {
            val random = java.util.Random()
            val color = String.format("#%06X", (random.nextInt(0xFFFFFF + 1)))
            return color
        }

        fun String.getShortText(len: Int): String{
            return if (this.length > len) {
                this.take(len - 3) + "..."
            } else {
                this
            }
        }
    }
}