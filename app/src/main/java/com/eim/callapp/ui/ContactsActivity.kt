package com.eim.callapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eim.callapp.databinding.ActivityContactsBinding
import com.eim.callapp.utils.ContactUtils
import kotlinx.coroutines.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: ContactAdapter
    private var allContacts = listOf<ContactUtils.Contact>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        loadContacts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Contacts"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter { contact ->
            makeCall(contact.phoneNumber)
        }
        binding.recyclerContacts.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = this@ContactsActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = if (query.isEmpty()) allContacts
                else allContacts.filter {
                    it.name.lowercase().contains(query) || it.phoneNumber.contains(query)
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadContacts() {
        scope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            allContacts = withContext(Dispatchers.IO) {
                ContactUtils.getAllContacts(this@ContactsActivity)
            }
            adapter.submitList(allContacts)
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvCount.text = "${allContacts.size} contacts"
        }
    }

    private fun makeCall(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
        } else {
            Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
