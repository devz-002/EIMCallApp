package com.eim.callapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.eim.callapp.R
import com.eim.callapp.model.AppDatabase
import com.eim.callapp.model.CallRecording
import com.eim.callapp.ui.MainActivity
import com.eim.callapp.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallRecordingService : Service() {

    companion object {
        private const val TAG = "CallRecordingService"
        const val CHANNEL_ID = "eim_call_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"

        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_CALL_TYPE = "extra_call_type"

        var isRecording = false
    }

    private var mediaRecorder: MediaRecorder? = null
    private var outputFilePath: String = ""
    private var recordingStartTime: Long = 0
    private var currentPhoneNumber: String = ""
    private var currentContactName: String = ""
    private var currentCallType: String = "INCOMING"

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                currentPhoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
                currentContactName = intent.getStringExtra(EXTRA_CONTACT_NAME) ?: "Unknown"
                currentCallType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "INCOMING"
                startForegroundService()
                startRecording()
            }
            ACTION_STOP_RECORDING -> {
                stopRecording()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EIM Call Recorder")
            .setContentText("Recording: $currentContactName ($currentPhoneNumber)")
            .setSmallIcon(R.drawable.ic_record)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startRecording() {
        try {
            val recordingsDir = FileUtils.getRecordingsDirectory(this)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "EIM_${currentPhoneNumber}_${timestamp}.m4a"
            outputFilePath = File(recordingsDir, fileName).absolutePath

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                // Use VOICE_COMMUNICATION for best call recording quality
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFilePath)
                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            Log.d(TAG, "Recording started: $outputFilePath")

        } catch (e: IOException) {
            Log.e(TAG, "Recording failed to start", e)
            isRecording = false
        } catch (e: IllegalStateException) {
            Log.e(TAG, "MediaRecorder state error", e)
            isRecording = false
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val duration = (System.currentTimeMillis() - recordingStartTime) / 1000
            val file = File(outputFilePath)
            val fileSize = if (file.exists()) file.length() else 0L

            // Save to database
            serviceScope.launch {
                val recording = CallRecording(
                    phoneNumber = currentPhoneNumber,
                    contactName = currentContactName,
                    callType = currentCallType,
                    duration = duration,
                    filePath = outputFilePath,
                    fileSize = fileSize,
                    isRecorded = file.exists() && fileSize > 0,
                    timestamp = recordingStartTime
                )
                database.callRecordingDao().insert(recording)
                Log.d(TAG, "Recording saved to DB: duration=${duration}s, size=${fileSize}B")
            }

        } catch (e: RuntimeException) {
            Log.e(TAG, "Error stopping recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for call recording notifications"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) stopRecording()
        serviceScope.cancel()
    }
}
