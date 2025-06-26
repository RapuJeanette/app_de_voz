package com.example.assisthome.controllers

import android.content.Context
import com.example.assisthome.R
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class DialogflowController(private val context: Context) {
    private suspend fun getSession(): SessionsClient? {
        val stream: InputStream = context.resources.openRawResource(R.raw.dialogflow_credentials) // Mantiene el nombre original
        val credentials = GoogleCredentials.fromStream(stream)
        val settings = SessionsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()
        return SessionsClient.create(settings)
    }

    // Definir una interfaz para el callback
    interface DialogflowCallback {
        fun onResponseReceived(response: String?)
    }

    // Enviar el mensaje a Dialogflow y procesar la respuesta usando un callback
    public fun sendMessageToBot(message: String, callback: DialogflowCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = sendMessageToDialogflow(message)

            // Llamar al callback con la respuesta obtenida
            callback.onResponseReceived(response)
        }
    }

    suspend fun sendMessageToDialogflow(message: String): String? {
        // Verificar si el mensaje es vacío o nulo antes de enviarlo
        Log.d("DialogflowController", "Mensaje enviado a control de voz: $message")
        if (message.isBlank()) {
            Log.e("DialogflowController", "El mensaje de entrada está vacío")
            return null
        }

        return withContext(Dispatchers.IO) {
            val sessionClient = getSession() ?: return@withContext null
            val session = SessionName.of("controlvoz-vwye", "1")  // Actualizado con el nuevo project_id
            val queryInput = QueryInput.newBuilder().apply {
                text = TextInput.newBuilder().setText(message).setLanguageCode("es-ES").build()
            }.build()

            val response = sessionClient.detectIntent(session, queryInput)
            Log.d("DialogflowController", "Respuesta de Dialogflow: ${response.queryResult.fulfillmentText}")
            response.queryResult.fulfillmentText
        }
    }
}