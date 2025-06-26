package com.example.assisthome.controllers

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ComunicadorController(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var dialogflowController: DialogflowController

    init {
        // Inicializar TextToSpeech para respuestas de voz
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                Log.d("ComunicadorController", "TextToSpeech inicializado para control de voz")
            } else {
                Log.e("ComunicadorController", "Error al inicializar TextToSpeech")
            }
        }

        // Inicializar DialogflowController
        dialogflowController = DialogflowController(context)
    }

    // Función para procesar comandos de voz
    fun escuchar(comando: String) {
        Log.d("ComunicadorController", "Procesando comando: $comando")
        responder(comando)
    }

    // Función para responder con base en el comando recibido
    private fun responder(comando: String) {
        Log.d("ComunicadorController", "Enviando comando a Dialogflow: $comando")
        // Ejecutar dentro de una corutina
        CoroutineScope(Dispatchers.Main).launch {
            val respuesta = dialogflowController.sendMessageToDialogflow(comando)
            // Procesar la respuesta de Dialogflow
            procesarRespuesta(respuesta, comando)
        }
    }

    // Procesar respuesta de Dialogflow y ejecutar acciones
    private fun procesarRespuesta(respuesta: String?, comandoOriginal: String) {
        Log.d("ComunicadorController", "Respuesta de Dialogflow: $respuesta")

        // Aquí se podrán agregar las acciones específicas para controlar dispositivos
        // Por ejemplo: si la respuesta contiene "encender_luz", ejecutar función para encender

        // Por ahora, solo respondemos con voz
        hablar(respuesta)
    }

    // Función para que el dispositivo responda por voz
    private fun hablar(texto: String?) {
        if (!texto.isNullOrBlank()) {
            textToSpeech?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("ComunicadorController", "Respondiendo por voz: $texto")
        }
    }

    // Función para desactivar la escucha (mantener funcionalidad original)
    private fun desactivar_escucha() {
        Log.d("ComunicadorController", "Desactivando control de voz")
        val intent = Intent("com.example.assisthome.DESACTIVAR_ESCUCHA")
        context.sendBroadcast(intent)
    }

    // Liberar recursos de TextToSpeech cuando ya no se necesite
    fun liberarRecursos() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        Log.d("ComunicadorController", "Recursos de TextToSpeech liberados")
    }
}