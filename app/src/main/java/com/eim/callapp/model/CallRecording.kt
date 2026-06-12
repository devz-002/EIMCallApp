package com.eim.callapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_recordings")
data class CallRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String = "Unknown",
    val callType: String,          // INCOMING / OUTGOING / MISSED
    val duration: Long = 0,        // in seconds
    val filePath: String = "",
    val fileSize: Long = 0,        // in bytes
    val timestamp: Long = System.currentTimeMillis(),
    val isRecorded: Boolean = false,
    val notes: String = ""
)
