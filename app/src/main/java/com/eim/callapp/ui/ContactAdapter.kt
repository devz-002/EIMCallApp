package com.eim.callapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eim.callapp.databinding.ItemContactBinding
import com.eim.callapp.utils.ContactUtils

class ContactAdapter(
    private val onCallClick: (ContactUtils.Contact) -> Unit
) : ListAdapter<ContactUtils.Contact, ContactAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContactUtils.Contact>() {
            override fun areItemsTheSame(a: ContactUtils.Contact, b: ContactUtils.Contact) =
                a.id == b.id
            override fun areContentsTheSame(a: ContactUtils.Contact, b: ContactUtils.Contact) =
                a == b
        }
    }

    inner class ViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContactUtils.Contact) {
            binding.tvName.text = item.name
            binding.tvNumber.text = item.phoneNumber

            // Generate avatar letter
            binding.tvAvatar.text = item.name.firstOrNull()?.uppercase() ?: "?"

            binding.btnCall.setOnClickListener { onCallClick(item) }
            binding.root.setOnClickListener { onCallClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
