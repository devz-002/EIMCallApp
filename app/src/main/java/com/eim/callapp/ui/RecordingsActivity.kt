package com.eim.callapp.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.eim.callapp.databinding.ActivityRecordingsBinding
import com.eim.callapp.model.CallRecording
import com.eim.callapp.utils.FileUtils
import java.io.IOException

class RecordingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingsBinding
    private lateinit var viewModel: CallViewModel
    private lateinit var adapter: RecordingAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlaying: CallRecording? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CallViewModel::class.java]
        val filterNumber = intent.getStringExtra("filter_number")

        setupToolbar()
        setupRecyclerView()
        observeData(filterNumber)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Recordings"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = RecordingAdapter(
            onPlayClick = { recording -> playRecording(recording) },
            onDeleteClick = { recording -> confirmDelete(recording) },
            onShareClick = { recording -> shareRecording(recording) }
        )
        binding.recyclerRecordings.apply {
            layoutManager = LinearLayoutManager(this@RecordingsActivity)
            adapter = this@RecordingsActivity.adapter
        }
    }

    private fun observeData(filterNumber: String?) {
        viewModel.recordedCalls.observe(this) { recordings ->
            val filtered = if (filterNumber != null) {
                recordings.filter { it.phoneNumber == filterNumber }
            } else recordings
            adapter.submitList(filtered)
            binding.tvCount.text = "${filtered.size} recording(s)"
        }
    }

    private fun playRecording(recording: CallRecording) {
        if (currentlyPlaying?.id == recording.id && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            return
        }

        stopPlayback()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(recording.filePath)
                prepare()
                start()
            }
            currentlyPlaying = recording
            Toast.makeText(this, "Playing: ${recording.contactName}", Toast.LENGTH_SHORT).show()

            mediaPlayer?.setOnCompletionListener {
                currentlyPlaying = null
                adapter.notifyDataSetChanged()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Cannot play this recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        currentlyPlaying = null
    }

    private fun confirmDelete(recording: CallRecording) {
        AlertDialog.Builder(this)
            .setTitle("Delete Recording")
            .setMessage("Delete recording of ${recording.contactName} (${recording.phoneNumber})?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecording(recording)
                Toast.makeText(this, "Recording deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareRecording(recording: CallRecording) {
        val file = java.io.File(recording.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "audio/m4a"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(intent, "Share Recording"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        handler.removeCallbacksAndMessages(null)
    }
}
