package com.example.assisthome.controllers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SOController(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false
    private var capturedText: StringBuilder = StringBuilder()
    private var buttonReleased: Boolean = false // Para rastrear si el sensor sigue cubierto
    private var registerController: RegisterController = RegisterController(context)
    private var comunicadorController: ComunicadorController = ComunicadorController(context)


    // Mantener el micrófono activo mientras el botón esté presionado
    fun activarMicrofono() {
        buttonReleased = false // Reiniciamos la bandera
        registerController.starRegister()
        registerController.logEvent("Micrófono activado")

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SOController", "Micrófono listo para hablar")
                }

                override fun onBeginningOfSpeech() {
                    Log.d("SOController", "Comenzando a hablar")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d("SOController", "Fin de la conversación")
                    // Aquí no apagamos el micrófono inmediatamente. Esperamos a que el reconocimiento termine.
                }

                override fun onError(error: Int) {
                    Log.e("SOController", "Error durante el reconocimiento de voz: $error")
                    desactivarMicrofono()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.let {
                        capturedText.append(it[0]).append(" ") // Acumular el texto capturado
                    }
                    Log.d("SOController", "Texto capturado al cubrir sensor: ${capturedText.toString().trim()}")
                    finalizarCapturaVoz()
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
            isListening = true
            Log.d("SOController", "Micrófono activado")
        } else {
            Log.e("SOController", "El reconocimiento de voz no está disponible")
        }
    }

    fun desactivarMicrofono() {
        // Marcamos que el botón ha sido soltado
        buttonReleased = true
        Log.d("SOController", "Sensor descubierto, esperando a que termine el reconocimiento de voz")

        // Si ya terminó de capturar el texto, finalizamos la captura de voz
        if (!isListening) {
            finalizarCapturaVoz()
        }
    }

    // Método para devolver el texto capturado por el micrófono
    fun obtenerMensajeVoz(): String {
        return capturedText.toString().trim()  // Devuelve el texto capturado
    }

    private fun finalizarCapturaVoz() {
        if (isListening) {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            isListening = false
            Log.d("SOController", "Micrófono desactivado")

            // Texto capturado que será enviado al ComunicadorController
            val mensajeCapturado = capturedText.toString().trim()
            if (mensajeCapturado.isNotEmpty()) {
                comunicadorController.escuchar(mensajeCapturado)
                Log.d("SOController", "Texto capturado al descubrir el sensor: $mensajeCapturado")
            } else {
                Log.d("SOController", "No se capturó ningún texto")
            }
            capturedText.clear() // Limpiar el buffer de texto para la próxima vez
        }
    }
}
