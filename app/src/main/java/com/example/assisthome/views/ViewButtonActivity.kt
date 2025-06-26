package com.example.assisthome.views

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.assisthome.R
import com.example.assisthome.controllers.SOController
import java.util.*

class ViewButtonActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var soController: SOController
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private var isNear = false
    private var isMicrophoneActive = false
    private lateinit var statusIndicator: TextView
    private var lastSensorChange = 0L // Para evitar cambios muy rápidos

    // TextToSpeech para mensajes de voz
    private var textToSpeech: TextToSpeech? = null
    private var ttsInitialized = false

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()  // Cierra el Activity
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soController = SOController(this)

        // Configurar ventana overlay
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        setContentView(R.layout.activity_view_button)

        // Obtener referencia del indicador de estado
        statusIndicator = findViewById(R.id.status_indicator)

        // Inicializar TextToSpeech
        initializeTextToSpeech()

        // Configurar sensor de proximidad
        setupProximitySensor()

        // Registrar el receptor de broadcast para cerrar el Activity
        registerReceiver(closeReceiver, IntentFilter("com.example.assisthome.CLOSE_ACTIVITY"), Context.RECEIVER_NOT_EXPORTED)

        Log.d("ViewButtonActivity", "Activity iniciada con sensor de proximidad")
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                ttsInitialized = true
                Log.d("ViewButtonActivity", "TextToSpeech inicializado correctamente")

                // Hablar el mensaje inicial cuando esté listo
                speakMessage("Modo escucha activo. Cubre el sensor de proximidad para hablar.")
            } else {
                Log.e("ViewButtonActivity", "Error al inicializar TextToSpeech")
                ttsInitialized = false
            }
        }
    }

    private fun speakMessage(message: String) {
        if (ttsInitialized && textToSpeech != null) {
            textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("ViewButtonActivity", "Mensaje hablado: $message")
        } else {
            Log.w("ViewButtonActivity", "TextToSpeech no está listo para hablar")
        }
    }

    private fun setupProximitySensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Log.e("ViewButtonActivity", "Sensor de proximidad no disponible")
            statusIndicator.text = "Sensor de proximidad no disponible\nEste dispositivo no soporta esta función"
            speakMessage("Error: Sensor de proximidad no disponible en este dispositivo")
        } else {
            Log.d("ViewButtonActivity", "Sensor de proximidad configurado correctamente")
        }
    }

    override fun onResume() {
        super.onResume()
        // IMPORTANTE: Resetear variables al reanudar
        isNear = false
        isMicrophoneActive = false

        // Registrar listener del sensor
        proximitySensor?.let {
            sensorManager.unregisterListener(this) // Primero desregistrar si existe
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("ViewButtonActivity", "Sensor de proximidad activado y reseteado")
        }

        // Actualizar indicador
        updateStatusIndicator("Modo Escucha Activo\nCubre el sensor de proximidad para hablar")
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar listener del sensor
        sensorManager.unregisterListener(this)

        // Si el micrófono está activo, desactivarlo
        if (isMicrophoneActive) {
            soController.desactivarMicrofono()
            isMicrophoneActive = false
        }

        Log.d("ViewButtonActivity", "Sensor de proximidad desactivado")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val currentTime = System.currentTimeMillis()

            // Evitar cambios muy rápidos (debounce)
            if (currentTime - lastSensorChange < 300) {
                return
            }
            lastSensorChange = currentTime

            val distance = event.values[0]
            val maxRange = event.sensor.maximumRange

            // Determinar si el sensor está cubierto (cerca)
            val wasNear = isNear
            isNear = distance < maxRange && distance < 3.0f // Ajuste más estricto

            Log.d("ViewButtonActivity", "Sensor proximidad - Distancia: $distance, MaxRange: $maxRange, wasNear: $wasNear, isNear: $isNear")

            // Solo actuar cuando cambia el estado
            if (wasNear != isNear) {
                if (isNear && !isMicrophoneActive) {
                    // Sensor cubierto - Activar micrófono
                    Log.d("ViewButtonActivity", "Sensor cubierto - Activando micrófono")
                    soController.activarMicrofono()
                    isMicrophoneActive = true
                    updateStatusIndicator("🎤 Grabando... Descubre el sensor para parar")
                    speakMessage("Grabando") // Mensaje corto mientras graba

                } else if (!isNear && isMicrophoneActive) {
                    // Sensor descubierto - Desactivar micrófono
                    Log.d("ViewButtonActivity", "Sensor descubierto - Desactivando micrófono")
                    soController.desactivarMicrofono()
                    isMicrophoneActive = false
                    updateStatusIndicator("Modo Escucha Activo\nCubre el sensor de proximidad para hablar")
                    // No hablar aquí para no interferir con el procesamiento del comando
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("ViewButtonActivity", "Precisión del sensor cambiada: $accuracy")
    }

    private fun updateStatusIndicator(text: String) {
        runOnUiThread {
            statusIndicator.text = text
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Liberar TextToSpeech
        textToSpeech?.stop()
        textToSpeech?.shutdown()

        // Desregistrar el sensor explícitamente
        sensorManager.unregisterListener(this)

        // Desregistrar el receptor para evitar fugas de memoria
        try {
            unregisterReceiver(closeReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w("ViewButtonActivity", "Receiver ya desregistrado")
        }

        // Asegurar que el micrófono esté desactivado
        if (isMicrophoneActive) {
            soController.desactivarMicrofono()
        }

        Log.d("ViewButtonActivity", "Activity destruida completamente")
    }
}