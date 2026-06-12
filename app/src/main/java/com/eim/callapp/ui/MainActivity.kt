package com.eim.callapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.eim.callapp.R
import com.eim.callapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CallViewModel
    private lateinit var adapter: CallLogAdapter

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.PROCESS_OUTGOING_CALLS
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CallViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupBottomNav()
        observeData()

        if (!hasAllPermissions()) {
            requestPermissions()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "EIM Call Manager"
    }

    private fun setupRecyclerView() {
        adapter = CallLogAdapter(
            onItemClick = { recording ->
                if (recording.isRecorded) {
                    val intent = Intent(this, RecordingsActivity::class.java)
                    intent.putExtra("filter_number", recording.phoneNumber)
                    startActivity(intent)
                }
            },
            onCallClick = { recording ->
                makeCall(recording.phoneNumber)
            },
            onDeleteClick = { recording ->
                viewModel.deleteRecording(recording)
            }
        )
        binding.recyclerCallLog.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabDial.setOnClickListener {
            startActivity(Intent(this, DialerActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recents -> {
                    viewModel.setFilter(null)
                    true
                }
                R.id.nav_recordings -> {
                    startActivity(Intent(this, RecordingsActivity::class.java))
                    true
                }
                R.id.nav_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun observeData() {
        viewModel.filteredCalls.observe(this) { calls ->
            adapter.submitList(calls)
            binding.tvEmptyState.visibility =
                if (calls.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun makeCall(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = android.net.Uri.parse("tel:$number")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Phone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val denied = permissions.zip(grantResults.toList())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }
            if (denied.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Some permissions denied. Call recording may not work properly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
