package com.eim.callapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eim.callapp.databinding.ItemRecordingBinding
import com.eim.callapp.model.CallRecording
import com.eim.callapp.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.*

class RecordingAdapter(
    private val onPlayClick: (CallRecording) -> Unit,
    private val onDeleteClick: (CallRecording) -> Unit,
    private val onShareClick: (CallRecording) -> Unit
) : ListAdapter<CallRecording, RecordingAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CallRecording>() {
            override fun areItemsTheSame(a: CallRecording, b: CallRecording) = a.id == b.id
            override fun areContentsTheSame(a: CallRecording, b: CallRecording) = a == b
        }
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    inner class ViewHolder(private val binding: ItemRecordingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CallRecording) {
            binding.tvContactName.text = item.contactName
            binding.tvPhoneNumber.text = item.phoneNumber
            binding.tvDate.text = DATE_FORMAT.format(Date(item.timestamp))
            binding.tvDuration.text = FileUtils.formatDuration(item.duration)
            binding.tvFileSize.text = FileUtils.formatFileSize(item.fileSize)
            binding.tvCallType.text = item.callType

            binding.btnPlay.setOnClickListener { onPlayClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
            binding.btnShare.setOnClickListener { onShareClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
