package com.eim.callapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.eim.callapp.model.AppDatabase
import com.eim.callapp.model.CallRecording
import com.eim.callapp.model.CallRecordingDao
import kotlinx.coroutines.launch

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: CallRecordingDao = AppDatabase.getDatabase(application).callRecordingDao()

    val allCalls: LiveData<List<CallRecording>> = dao.getAllRecordings()
    val recordedCalls: LiveData<List<CallRecording>> = dao.getRecordedCalls()

    private val _filterType = MutableLiveData<String?>(null)
    val filteredCalls: LiveData<List<CallRecording>> = _filterType.switchMap { type ->
        if (type == null) dao.getAllRecordings()
        else dao.getCallsByType(type)
    }

    fun setFilter(type: String?) {
        _filterType.value = type
    }

    fun deleteRecording(recording: CallRecording) = viewModelScope.launch {
        if (recording.filePath.isNotEmpty()) {
            com.eim.callapp.utils.FileUtils.deleteFile(recording.filePath)
        }
        dao.delete(recording)
    }

    fun updateNotes(recording: CallRecording, notes: String) = viewModelScope.launch {
        dao.update(recording.copy(notes = notes))
    }
}
