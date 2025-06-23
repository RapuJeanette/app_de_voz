import 'package:flutter/material.dart';
import 'package:speech_to_text/speech_to_text.dart' as stt;
import 'package:flutter_tts/flutter_tts.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

import 'background_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeService(); // Inicializa el servicio al arrancar
  runApp(const VoiceApp());
}

class VoiceApp extends StatelessWidget {
  const VoiceApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Control de Luces',
      home: const VoiceHomePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class VoiceHomePage extends StatefulWidget {
  const VoiceHomePage({super.key});

  @override
  State<VoiceHomePage> createState() => _VoiceHomePageState();
}

class _VoiceHomePageState extends State<VoiceHomePage> {
  late stt.SpeechToText _speech;
  bool _isListening = false;
  String _text = '';
  String _respuestaBot = '';
  final FlutterTts _flutterTts = FlutterTts();

  @override
  void initState() {
    super.initState();
    _speech = stt.SpeechToText();
    _requestPermission();
  }

  Future<void> _requestPermission() async {
    await Permission.microphone.request();
  }

  void _startListening() async {
    bool available = await _speech.initialize();
    if (available) {
      setState(() => _isListening = true);
      _speech.listen(
        onResult: (result) {
          setState(() {
            _text = result.recognizedWords;
          });
        },
        // ignore: deprecated_member_use
        listenMode: stt.ListenMode.dictation,
        localeId: 'es_ES',
      );
    }
  }

  void _stopListening() async {
    await _speech.stop();
    setState(() => _isListening = false);

    if (_text.trim().isNotEmpty) {
      _enviarADialogflow(_text);
    }
  }

  Future<void> _enviarADialogflow(String texto) async {
    final response = await http.post(
      Uri.parse('http://192.168.0.100:5000/dialogflow'), // Cambia esto por tu IP o backend
      headers: {
        'Content-Type': 'application/json',
      },
      body: json.encode({"mensaje": texto}),
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      final resultado = data['respuesta'];

      setState(() {
        _respuestaBot = resultado ?? "No hubo respuesta.";
      });

      _speakText(_respuestaBot);
    } else {
      setState(() {
        _respuestaBot = "Error al conectar con el backend.";
      });
      _speakText(_respuestaBot);
    }
  }

  void _speakText(String text) async {
    await _flutterTts.setLanguage('es-ES');
    await _flutterTts.setSpeechRate(0.5);
    await _flutterTts.speak(text);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Control de Luces por Voz')),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            Expanded(
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      _text.isEmpty ? 'Presiona y habla...' : '📣 Tú: $_text',
                      style: const TextStyle(fontSize: 20),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 20),
                    Text(
                      _respuestaBot.isEmpty ? '' : '🤖 Bot: $_respuestaBot',
                      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                GestureDetector(
                  onLongPress: _startListening,
                  onLongPressUp: _stopListening,
                  child: Container(
                    padding: const EdgeInsets.all(25),
                    decoration: BoxDecoration(
                      color: _isListening ? Colors.red : Colors.blue,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.mic, color: Colors.white, size: 40),
                  ),
                ),
                const SizedBox(width: 20),
                if (_respuestaBot.trim().isNotEmpty)
                  ElevatedButton.icon(
                    onPressed: () => _speakText(_respuestaBot),
                    icon: const Icon(Icons.replay),
                    label: const Text("Repetir"),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
