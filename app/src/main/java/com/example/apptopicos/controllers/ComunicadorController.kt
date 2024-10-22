package com.example.apptopicos.controllers

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale
import com.example.apptopicos.controllers.DialogflowController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ComunicadorController(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var dialogflowController: DialogflowController

    init {
        // Inicializar TextToSpeech para que el dispositivo pueda "hablar"
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                Log.d("ComunicadorController", "TextToSpeech inicializado")
            } else {
                Log.e("ComunicadorController", "Error al inicializar TextToSpeech")
            }
        }

        // Inicializar DialogflowController
        dialogflowController = DialogflowController(context)
    }

    // Función para "escuchar" lo que el usuario diga
    fun escuchar(mensaje: String) {
        Log.d("MiApp", "Escuchando: $mensaje")
        responder(mensaje)
    }

    // Función para "responder" con base en el mensaje escuchado
    private fun responder(mensaje: String) {
        // Ejecutar dentro de una corutina
        Log.d("ComunicadorController", "Respondiendo: $mensaje")
        CoroutineScope(Dispatchers.Main).launch {
            val respuesta = dialogflowController.sendMessageToDialogflow(mensaje)
            // Maneja la respuesta después de la llamada
            hablar(respuesta);
        }
    }

    // Función para que el dispositivo "hable"
    private fun hablar(texto: String?) {
        textToSpeech?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("ComunicadorController", "Respondiendo: $texto")
    }

    // Función para apagar la "escucha" (puedes personalizar lo que hace aquí)
    private fun desactivar_escucha() {
        Log.d("ComunicadorController", "Desactivando escucha")
        val intent = Intent("com.example.apptopicos.DESACTIVAR_ESCUCHA")
        context.sendBroadcast(intent)
    }

    // Función para activar la cámara del dispositivo
    private fun activarCamara() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d("ComunicadorController", "Cámara abierta correctamente")
                    Toast.makeText(context, "Cámara activada", Toast.LENGTH_SHORT).show()
                    //camera.close() // Cerrar la cámara si es necesario
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("ComunicadorController", "Cámara desconectada")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("ComunicadorController", "Error al abrir la cámara: $error")
                }
            }, null)
        } catch (e: CameraAccessException) {
            Log.e("ComunicadorController", "Error al acceder a la cámara: ${e.message}")
        } catch (e: SecurityException) {
            Log.e("ComunicadorController", "Permiso de cámara no concedido: ${e.message}")
        }
    }

    // Liberar recursos de TextToSpeech cuando ya no se necesite
    fun liberarRecursos() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
