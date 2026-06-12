package com.eim.callapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eim.callapp.databinding.ActivityDialerBinding

class DialerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialerBinding
    private val dialedNumber = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDialpad()
        setupButtons()
    }

    private fun setupDialpad() {
        val buttons = mapOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9",
            binding.btnStar to "*",
            binding.btnHash to "#"
        )

        buttons.forEach { (button, digit) ->
            button.setOnClickListener {
                dialedNumber.append(digit)
                updateDisplay()
            }
        }
    }

    private fun setupButtons() {
        binding.btnBackspace.setOnClickListener {
            if (dialedNumber.isNotEmpty()) {
                dialedNumber.deleteCharAt(dialedNumber.length - 1)
                updateDisplay()
            }
        }

        binding.btnBackspace.setOnLongClickListener {
            dialedNumber.clear()
            updateDisplay()
            true
        }

        binding.btnCall.setOnClickListener {
            val number = dialedNumber.toString()
            if (number.isNotEmpty()) {
                makeCall(number)
            } else {
                Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDisplay() {
        binding.tvDialedNumber.text = formatNumber(dialedNumber.toString())
    }

    private fun formatNumber(number: String): String {
        // Basic Indian number formatting
        return when {
            number.length <= 5 -> number
            number.length <= 10 -> "${number.substring(0, 5)}-${number.substring(5)}"
            else -> "+${number.substring(0, 2)} ${number.substring(2, 7)}-${number.substring(7)}"
        }
    }

    private fun makeCall(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show()
        }
    }
}
