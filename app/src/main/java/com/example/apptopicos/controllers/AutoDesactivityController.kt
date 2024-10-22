package com.example.apptopicos.controllers

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

class AutoDesactivityController(
    private val context: Context, // Agregar contexto como parámetro
    private val registerController: RegisterController // Instancia de RegisterController
) {

    private var isActive: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 5000 // Verifica cada 5 segundos
    private val inactivityThreshold: Long = 30000 // 30 segundos de inactividad

    fun starAutodesactivity() {
        if (!isActive) {
            isActive = true
            Log.d("MiApp", "Actividad automática iniciada")
            startInactivityCheck() // Inicia el chequeo de inactividad
        }
    }

    fun offAutodesactivity() {
        if (isActive) {
            isActive = false
            Log.d("MiApp", "Actividad automática detenida")
            handler.removeCallbacks(inactivityCheckRunnable)
        }
    }

    private fun startInactivityCheck() {
        handler.removeCallbacks(inactivityCheckRunnable) // Elimina cualquier chequeo anterior
        handler.postDelayed(inactivityCheckRunnable, checkInterval) // Inicia el chequeo
    }

    private val inactivityCheckRunnable = object : Runnable {
        override fun run() {
            if (isActive) {
                val lastEvent = registerController.getLastEvent()
                val currentTime = System.currentTimeMillis()

                // Si existe un último evento, compara el tiempo con el temporizador de inactividad
                if (lastEvent != null) {
                    val timeSinceLastActivity = currentTime - lastEvent.timestamp

                    if (timeSinceLastActivity > inactivityThreshold) {
                        Log.d("MiApp", "Inactividad detectada: Desactivando escucha")

                        // Enviar broadcast para desactivar escucha
                        val intent = Intent("com.example.apptopicos.DESACTIVAR_ESCUCHA")
                        context.sendBroadcast(intent)
                    } else {
                        Log.d("MiApp", "Última actividad hace ${timeSinceLastActivity / 1000} segundos.")
                        handler.postDelayed(this, checkInterval) // Sigue verificando
                    }
                } else {
                    // Si no hay eventos, se asume que ya ha pasado mucho tiempo (ejemplo: inactividad desde el inicio)
                    Log.d("MiApp", "No se encontraron eventos previos. Desactivando escucha.")
                    val intent = Intent("com.example.apptopicos.DESACTIVAR_ESCUCHA")
                    context.sendBroadcast(intent)
                }
            }
        }
    }

    // Método llamado desde RegisterController para reiniciar el chequeo de inactividad
    fun resetInactivityCheck() {
        if (isActive) {
            handler.removeCallbacks(inactivityCheckRunnable) // Asegúrate de eliminar el anterior
            handler.postDelayed(inactivityCheckRunnable, checkInterval) // Inicia un nuevo chequeo
            Log.d("MiApp", "Chequeo de inactividad reiniciado.")
        }
    }
}
