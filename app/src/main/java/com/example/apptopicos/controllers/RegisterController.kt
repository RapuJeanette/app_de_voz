package com.example.apptopicos.controllers

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class RegisterController(private val context: Context) {

    private var isRegistering: Boolean = false
    private val eventLog = mutableListOf<Event>()
    private val fileName = "event_log.txt"
    private var autoDesactivityController: AutoDesactivityController = AutoDesactivityController(context, this)

    data class Event(val description: String, val timestamp: Long)

    fun starRegister() {
        if (!isRegistering) {
            isRegistering = true
            Log.d("RegisterController", "Registro iniciado")
            logEvent("Registro iniciado")
        }
    }

    fun offRegister() {
        if (isRegistering) {
            logEvent("Registro detenido")
            isRegistering = false
            Log.d("RegisterController", "Registro detenido")
        }
    }

    fun logEvent(description: String) {
        autoDesactivityController.resetInactivityCheck() // Reiniciar el contador de inactividad
        if (isRegistering) {
            val timestamp = System.currentTimeMillis()
            val event = Event(description, timestamp)
            eventLog.add(event)
            Log.d("MiApp", "Evento registrado: $description a las ${formatDate(timestamp)}")
            writeEventToFile(event) // Guardar el evento en el archivo
        }
    }

    fun getLastEvent(): Event? {
        val eventsFromFile = readEventsFromFile() // Leer todos los eventos desde el archivo
        return eventsFromFile.lastOrNull() // Retornar el último evento
    }

    // Función para guardar un evento en el archivo
    private fun writeEventToFile(event: Event) {
        try {
            val file = File(context.filesDir, fileName)
            file.appendText("${event.description},${event.timestamp}\n") // Guardar en formato CSV
            Log.d("MiApp", "Se escribio el registro en $fileName")
        } catch (e: Exception) {
            Log.d("MiApp", "Error al escribir el archivo: ${e.message}")
        }
    }

    // Función para leer todos los eventos desde el archivo
    private fun readEventsFromFile(): List<Event> {
        val events = mutableListOf<Event>()
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.forEachLine { line ->
                    val data = line.split(",")
                    if (data.size == 2) {
                        val description = data[0]
                        val timestamp = data[1].toLongOrNull() ?: 0L
                        events.add(Event(description, timestamp))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RegisterController", "Error al leer el archivo: ${e.message}")
        }
        return events
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
