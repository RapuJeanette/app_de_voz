import 'package:http/http.dart' as http;
import '../utils/constants.dart';

class LedService {
  Future<void> encender() async {
    await http.get(Uri.parse(ledOnUrl));
  }

  Future<void> apagar() async {
    await http.get(Uri.parse(ledOffUrl));
  }
}
