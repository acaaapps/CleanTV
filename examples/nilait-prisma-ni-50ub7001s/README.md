# Nilait Prisma NI-50UB7001S 50" (Android TV 14)

Configuración probada y verificada en una **Nilait Prisma NI-50UB7001S** de 50" con Android TV 14 oficial (firmware `UKNV.260428.001`, chasis Vestel `BODRUM`, dispositivo `finch`, SoC MediaTek MT7332).

---

## Archivos de esta configuración

- `debloat.txt`: Lista de 24 paquetes de telemetría Vestel, publicidad Google y componentes residuales desactivados con seguridad.
- `tv.json`: Metadatos del dispositivo para el catálogo de televisores probados.
- `restore.bat` y `restore.ps1`: Scripts directos en la raíz del repositorio para restaurar el televisor al estado de fábrica.
- `build-and-install.bat` y `build-and-install.ps1`: Scripts directos para compilar e instalar el launcher y sus servicios.
- `README_NILAIT.md`: Guía completa y detallada de personalización (dock, columnas, salvapantallas, frases y servicio de Home).

---

## Cómo volver a activar cualquier aplicación

Todos los paquetes fueron **desactivados** (`pm disable-user --user 0`), nunca desinstalados. Se pueden recuperar en cualquier momento:

### Reactivar una app específica:
```powershell
tools\android-sdk\platform-tools\adb.exe connect 192.168.1.18:5555
tools\android-sdk\platform-tools\adb.exe shell pm enable com.disney.disneyplus
```

### Restaurar todo al estado original:
Ejecuta con doble clic en `restore.bat` o por PowerShell:
```powershell
.\restore.ps1
```

---

## Cómo cambiar las apps del Dock o el número de columnas

Las preferencias del launcher se configuran en `launcher/app/src/main/res/values/config.xml`:

1. Abre `launcher/app/src/main/res/values/config.xml`.
2. Para cambiar el orden o las apps fijadas en el dock:
   ```xml
   <string-array name="dock_apps">
       <item>com.mediatek.wwtv.tvcenter</item>
       <item>com.google.android.youtube.tv</item>
       <item>com.netflix.ninja</item>
       <item>org.courville.nova</item>
       <item>com.android.vending</item>
   </string-array>
   ```
3. Para cambiar el número de columnas (4, 5, 6 o 7):
   ```xml
   <integer name="columns">5</integer>
   ```
4. Reinstala en la TV ejecutando simplemente:
   ```cmd
   build-and-install.bat
   ```

---

## Notas de hardware importantes para este modelo
- **Botón Input del mando:** Requiere mantener activos `com.vestel.tv.eib` y `fusion.android.tv.demo`. No desactivar nunca.
- **Middleware TV:** Requiere `com.mediatek.android.leanbacklauncher.partnercustomizer` activo para evitar reinicios de `com.mediatek.tv.service`.
- **Rampa de volumen vs Tecla Home:** La pulsación prolongada de 3 segundos de Home en el mando requiere el servicio `HomeKeyService`. En Android TV 14 este servicio hace que el volumen suba/baje de 1 en 1 al mantenerlo pulsado. Para recuperar la aceleración rápida continua, se puede desactivar el servicio de accesibilidad desde el menú de la tele o mediante `adb shell settings put secure accessibility_enabled 0`.
