package com.example.apptopicos.controllers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

class SOController(private val context: Context) {

    private var isCameraInUse: Boolean = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false
    private var capturedText: StringBuilder = StringBuilder()
    private var buttonReleased: Boolean = false // Para rastrear si el botón sigue presionado
    private var registerController: RegisterController = RegisterController(context)
    private var comunicadorController: ComunicadorController = ComunicadorController(context)

    // Mantener la cámara activa o apagarla
    fun isCameraActive(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SOController", "Permiso de cámara no concedido")
            return false
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0] // Usar el primer ID de cámara disponible
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d("SOController", "Cámara abierta correctamente.")
                    camera.close() // Cerramos la cámara después de verificar que se puede abrir
                    isCameraInUse = false
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("SOController", "Cámara desconectada.")
                    isCameraInUse = false
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("SOController", "Error al abrir la cámara: $error")
                    if (error == CameraDevice.StateCallback.ERROR_CAMERA_IN_USE) {
                        Log.e("SOController", "La cámara está en uso por otra aplicación o servicio.")
                        isCameraInUse = true
                    }
                }
            }, null)
        } catch (e: CameraAccessException) {
            Log.e("SOController", "Error al acceder a la cámara: ${e.message}")
        } catch (e: SecurityException) {
            Log.e("SOController", "Excepción de seguridad: ${e.message}")
        }
        return isCameraInUse
    }

    // Método para desactivar la cámara si está activa
    fun CamaraOff() {
        if (isCameraActive()) {
            Log.d("SOController", "Desactivando la cámara...")
        } else {
            Log.d("SOController", "La cámara no está activa, no se requiere desactivación.")
        }
    }

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
                    Log.d("SOController", "Texto capturado sin soltar boton: ${capturedText.toString().trim()}")
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
        Log.d("SOController", "Botón soltado, esperando a que termine el reconocimiento de voz")

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
                Log.d("SOController", "Texto capturado al solatar boton: $mensajeCapturado")
            } else {
                Log.d("SOController", "No se capturó ningún texto")
            }
            capturedText.clear() // Limpiar el buffer de texto para la próxima vez
        }
    }
}
