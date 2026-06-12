package com.eim.callapp.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CallRecordingDao {

    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): LiveData<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE isRecorded = 1 ORDER BY timestamp DESC")
    fun getRecordedCalls(): LiveData<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE phoneNumber = :number ORDER BY timestamp DESC")
    fun getCallsByNumber(number: String): LiveData<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE callType = :type ORDER BY timestamp DESC")
    fun getCallsByType(type: String): LiveData<List<CallRecording>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: CallRecording): Long

    @Update
    suspend fun update(recording: CallRecording)

    @Delete
    suspend fun delete(recording: CallRecording)

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM call_recordings")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM call_recordings WHERE isRecorded = 1")
    suspend fun getRecordedCount(): Int

    @Query("SELECT SUM(duration) FROM call_recordings")
    suspend fun getTotalDuration(): Long?
}
