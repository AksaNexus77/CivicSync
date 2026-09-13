package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechAndHapticHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isPlayingTts = MutableStateFlow(false)
    val isPlayingTts: StateFlow<Boolean> = _isPlayingTts.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("SpeechHelper", "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlayingTts.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isPlayingTts.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isPlayingTts.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isPlayingTts.value = false
                }
            })
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isTtsInitialized || tts == null) return

        stopSpeech()

        // Configure locale
        val locale = if (language == AppLanguage.URDU) {
            Locale.forLanguageTag("ur-PK")
        } else {
            Locale.forLanguageTag("en-PK")
        }

        val available = tts?.isLanguageAvailable(locale)
        if (available == TextToSpeech.LANG_AVAILABLE || available == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            tts?.language = locale
        } else {
            // Fallback to English or default
            tts?.language = Locale.ENGLISH
        }

        // Clean markdown symbols for natural speech
        val cleanedText = text
            .replace("#", "")
            .replace("*", "")
            .replace("_", "")
            .replace("[Name]", "Muhammad Ali")
            .replace("[CNIC Number]", "CNIC")
            .replace("[Address]", "Pakistan")

        _isPlayingTts.value = true
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "civic_advocacy_tts")
    }

    fun stopSpeech() {
        if (isTtsInitialized) {
            tts?.stop()
        }
        _isPlayingTts.value = false
    }

    fun performHapticFeedback(pattern: Long = 40L) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(pattern, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(pattern, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern)
                }
            }
        } catch (e: Exception) {
            Log.w("HapticHelper", "Vibration failed: ${e.message}")
        }
    }

    fun shutdown() {
        stopSpeech()
        tts?.shutdown()
        tts = null
    }
}
