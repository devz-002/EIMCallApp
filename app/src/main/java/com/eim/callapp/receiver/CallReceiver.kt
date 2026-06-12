package com.eim.callapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.eim.callapp.service.CallRecordingService
import com.eim.callapp.utils.ContactUtils

class CallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallReceiver"
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Long = 0
        private var isIncoming = false
        private var savedNumber = ""
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // Handle outgoing call
        if (action == Intent.ACTION_NEW_OUTGOING_CALL) {
            savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            isIncoming = false
            Log.d(TAG, "Outgoing call to: $savedNumber")
            return
        }

        // Handle phone state change
        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        val state = when (stateStr) {
            TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            else -> return
        }

        onCallStateChanged(context, state, number)
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                savedNumber = number ?: ""
                Log.d(TAG, "Incoming call from: $savedNumber")
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call answered or outgoing call connected
                callStartTime = System.currentTimeMillis()
                val callType = if (isIncoming) "INCOMING" else "OUTGOING"
                val contactName = ContactUtils.getContactName(context, savedNumber)

                Log.d(TAG, "Call connected: $callType - $savedNumber ($contactName)")

                // Start recording service
                val serviceIntent = Intent(context, CallRecordingService::class.java).apply {
                    action = CallRecordingService.ACTION_START_RECORDING
                    putExtra(CallRecordingService.EXTRA_PHONE_NUMBER, savedNumber)
                    putExtra(CallRecordingService.EXTRA_CONTACT_NAME, contactName)
                    putExtra(CallRecordingService.EXTRA_CALL_TYPE, callType)
                }
                context.startForegroundService(serviceIntent)
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                when (lastState) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        // Missed call - log but don't record
                        Log.d(TAG, "Missed call from: $savedNumber")
                        logMissedCall(context, savedNumber)
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        // Call ended
                        Log.d(TAG, "Call ended: $savedNumber")
                        val stopIntent = Intent(context, CallRecordingService::class.java).apply {
                            action = CallRecordingService.ACTION_STOP_RECORDING
                        }
                        context.startForegroundService(stopIntent)
                    }
                }
                savedNumber = ""
            }
        }

        lastState = state
    }

    private fun logMissedCall(context: Context, number: String) {
        val contactName = ContactUtils.getContactName(context, number)
        // Use coroutine scope for DB operation - via Application or WorkManager in production
        val db = com.eim.callapp.model.AppDatabase.getDatabase(context)
        val recording = com.eim.callapp.model.CallRecording(
            phoneNumber = number,
            contactName = contactName,
            callType = "MISSED",
            duration = 0,
            isRecorded = false
        )
        // Fire-and-forget using a background thread
        Thread {
            kotlinx.coroutines.runBlocking {
                db.callRecordingDao().insert(recording)
            }
        }.start()
    }
}
