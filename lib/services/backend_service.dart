import 'dart:convert';
import 'package:http/http.dart' as http;

class BackendResponse {
  final String mensaje;
  final String? accion;

  BackendResponse({required this.mensaje, this.accion});
}

class BackendService {
  final String backendUrl = 'http://192.168.250.21:5000/dialogflow';

  Future<BackendResponse> enviarMensaje(String texto) async {
    try {
      final response = await http.post(
        Uri.parse(backendUrl),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'mensaje': texto}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return BackendResponse(
          mensaje: data['respuesta'] ?? 'Sin respuesta',
          accion: data['accion'],
        );
      } else {
        return BackendResponse(mensaje: 'Error del servidor');
      }
    } catch (_) {
      return BackendResponse(mensaje: 'No se pudo conectar');
    }
  }
}
