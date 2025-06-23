import 'dart:async';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin = FlutterLocalNotificationsPlugin();

Future<void> initializeService() async {
  final service = FlutterBackgroundService();

  await service.configure(
    androidConfiguration: AndroidConfiguration(
      onStart: onStart,
      isForegroundMode: true,
      autoStart: true,
      notificationChannelId: 'my_foreground',
      initialNotificationTitle: 'Servicio activo',
      initialNotificationContent: 'Ejecutándose...',
    ),
    iosConfiguration: IosConfiguration(),
  );

  await service.startService();
}

@pragma('vm:entry-point')
void onStart(ServiceInstance service) async {
  if (service is AndroidServiceInstance) {
    const AndroidNotificationChannel channel = AndroidNotificationChannel(
      'my_foreground', // debe coincidir con notificationChannelId
      'Servicio de Voz',
      description: 'Canal para servicio foreground',
      importance: Importance.high,
    );

    // Inicializa el plugin
    await flutterLocalNotificationsPlugin
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>()
        ?.createNotificationChannel(channel);

    await service.setForegroundNotificationInfo(
      title: 'Servicio de Voz',
      content: 'Corriendo en segundo plano...',
    );

    service.setAsForegroundService();
  }

  Timer.periodic(const Duration(seconds: 15), (timer) async {
    if (service is AndroidServiceInstance && !(await service.isForegroundService())) {
      timer.cancel();
    }

    service.invoke('update', {
      "time": DateTime.now().toIso8601String(),
    });
  });
}
