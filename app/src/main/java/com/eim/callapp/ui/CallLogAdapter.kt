package com.eim.callapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eim.callapp.R
import com.eim.callapp.databinding.ItemCallLogBinding
import com.eim.callapp.model.CallRecording
import com.eim.callapp.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.*

class CallLogAdapter(
    private val onItemClick: (CallRecording) -> Unit,
    private val onCallClick: (CallRecording) -> Unit,
    private val onDeleteClick: (CallRecording) -> Unit
) : ListAdapter<CallRecording, CallLogAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CallRecording>() {
            override fun areItemsTheSame(a: CallRecording, b: CallRecording) = a.id == b.id
            override fun areContentsTheSame(a: CallRecording, b: CallRecording) = a == b
        }
        private val DATE_FORMAT = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    }

    inner class ViewHolder(private val binding: ItemCallLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CallRecording) {
            binding.tvContactName.text =
                if (item.contactName == item.phoneNumber || item.contactName == "Unknown")
                    item.phoneNumber
                else item.contactName

            binding.tvPhoneNumber.text = item.phoneNumber
            binding.tvTime.text = DATE_FORMAT.format(Date(item.timestamp))
            binding.tvDuration.text = if (item.duration > 0)
                FileUtils.formatDuration(item.duration) else ""

            // Call type icon and color
            val (iconRes, colorRes) = when (item.callType) {
                "INCOMING" -> Pair(R.drawable.ic_call_incoming, R.color.incoming_call)
                "OUTGOING" -> Pair(R.drawable.ic_call_outgoing, R.color.outgoing_call)
                "MISSED" -> Pair(R.drawable.ic_call_missed, R.color.missed_call)
                else -> Pair(R.drawable.ic_call_incoming, R.color.incoming_call)
            }
            binding.ivCallType.setImageResource(iconRes)
            binding.ivCallType.setColorFilter(
                binding.root.context.getColor(colorRes)
            )

            // Recording indicator
            binding.ivRecorded.visibility =
                if (item.isRecorded) android.view.View.VISIBLE else android.view.View.GONE

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnCall.setOnClickListener { onCallClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
