package com.eim.callapp.utils

import android.content.Context
import android.provider.ContactsContract

object ContactUtils {

    fun getContactName(context: Context, phoneNumber: String): String {
        if (phoneNumber.isBlank()) return "Unknown"
        return try {
            val uri = android.net.Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(phoneNumber)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else phoneNumber
            } ?: phoneNumber
        } catch (e: Exception) {
            phoneNumber
        }
    }

    data class Contact(
        val id: String,
        val name: String,
        val phoneNumber: String
    )

    fun getAllContacts(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                contacts.add(
                    Contact(
                        id = it.getString(idIdx) ?: "",
                        name = it.getString(nameIdx) ?: "Unknown",
                        phoneNumber = it.getString(numIdx) ?: ""
                    )
                )
            }
        }
        return contacts
    }
}
