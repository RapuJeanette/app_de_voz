import 'dart:convert';
import 'package:http/http.dart' as http;

class BackendService {
  static const String _baseUrl = "http://192.168.0.8:5000"; // ← Cámbialo

  /// Envía el texto al backend (que a su vez llama a Dialogflow) y retorna respuesta + estado
  static Future<Map<String, dynamic>> enviarTexto(String mensaje) async {
    final url = Uri.parse("$_baseUrl/dialogflow");

    try {
      final response = await http.post(
        url,
        headers: {"Content-Type": "application/json"},
        body: jsonEncode({"mensaje": mensaje}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return {
          "respuesta": data["respuesta"] ?? "Sin respuesta",
          "accion": data["accion"] ?? "",
          "estado": data["estado"] ?? false,
        };
      } else {
        return {
          "respuesta": "Error al comunicarse con el servidor.",
          "accion": "",
          "estado": false,
        };
      }
    } catch (e) {
      return {
        "respuesta": "Error de red: $e",
        "accion": "",
        "estado": false,
      };
    }
  }
}
