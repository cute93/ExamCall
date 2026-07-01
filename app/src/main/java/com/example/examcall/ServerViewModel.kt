package com.example.examcall

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examcall.network.EmbeddedWebSocketServer
import com.example.examcall.network.UdpDiscovery
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class ServerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("examcall_server", Context.MODE_PRIVATE)

    private val _serverName = MutableStateFlow(prefs.getString("serverName", "") ?: "")
    val serverName: StateFlow<String> = _serverName

    private val _names = MutableStateFlow<List<String>>(emptyList())
    val names: StateFlow<List<String>> = _names

    private val _calledName = MutableStateFlow<String?>(null)
    val calledName: StateFlow<String?> = _calledName

    private val _absentNames = MutableStateFlow<Set<String>>(emptySet())
    val absentNames: StateFlow<Set<String>> = _absentNames

    private val _clientCount = MutableStateFlow(0)
    val clientCount: StateFlow<Int> = _clientCount

    private val _serverIp = MutableStateFlow("")
    val serverIp: StateFlow<String> = _serverIp

    private var wsServer: EmbeddedWebSocketServer? = null
    private var udpJob: Job? = null
    private var dismissJob: Job? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("ko", "KR"))
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        val saved = prefs.getString("names", null)
        if (!saved.isNullOrBlank()) {
            _names.value = saved.split(",").filter { it.isNotBlank() }
        }
        val savedAbsent = prefs.getString("absentNames", null)
        if (!savedAbsent.isNullOrBlank()) {
            _absentNames.value = savedAbsent.split(",").filter { it.isNotBlank() }.toSet()
        }
    }

    fun startServer(name: String) {
        _serverName.value = name
        prefs.edit().putString("serverName", name).apply()

        val ip = UdpDiscovery.getDeviceIpAddress() ?: return
        _serverIp.value = ip

        wsServer?.stop()
        wsServer = EmbeddedWebSocketServer(
            onCallReceived = { calledName -> handleCall(calledName) },
            onClientCountChanged = { _clientCount.value = it }
        ).also {
            it.broadcastNameList(_names.value)
            it.broadcastAbsentList(_absentNames.value.toList())
            it.start()
        }

        udpJob?.cancel()
        udpJob = UdpDiscovery.startBroadcasting(ip, name, viewModelScope)
    }

    fun addName(name: String) {
        val updated = _names.value + name
        _names.value = updated
        wsServer?.broadcastNameList(updated)
        saveNames(updated)
    }

    fun deleteName(name: String) {
        val updated = _names.value.filter { it != name }
        _names.value = updated
        wsServer?.broadcastNameList(updated)
        saveNames(updated)
        val updatedAbsent = _absentNames.value - name
        _absentNames.value = updatedAbsent
        wsServer?.broadcastAbsentList(updatedAbsent.toList())
        prefs.edit().putString("absentNames", updatedAbsent.joinToString(",")).apply()
    }

    fun toggleAbsent(name: String) {
        val updated = if (name in _absentNames.value) _absentNames.value - name
                      else _absentNames.value + name
        _absentNames.value = updated
        wsServer?.broadcastAbsentList(updated.toList())
        prefs.edit().putString("absentNames", updated.joinToString(",")).apply()
    }

    fun dismissCalledName() {
        dismissJob?.cancel()
        _calledName.value = null
    }

    private fun handleCall(name: String) {
        _calledName.value = name
        dismissJob?.cancel()
        dismissJob = viewModelScope.launch {
            delay(10_000)
            _calledName.value = null
        }

        val mp = MediaPlayer.create(getApplication(), R.raw.ding_dong) ?: run {
            speakTts(name)
            return
        }
        mp.setOnCompletionListener {
            it.release()
            speakTts(name)
        }
        mp.start()
    }

    private fun speakTts(name: String) {
        if (ttsReady) {
            val text = "${name}선생님 호출입니다."
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, name)
        }
    }

    private fun saveNames(names: List<String>) {
        prefs.edit().putString("names", names.joinToString(",")).apply()
    }

    override fun onCleared() {
        super.onCleared()
        udpJob?.cancel()
        wsServer?.stop()
        tts?.shutdown()
    }
}
