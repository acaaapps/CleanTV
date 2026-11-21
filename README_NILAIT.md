# Guía de Configuración y Personalización del Televisor Nilait Prisma NI-50UB7001S

Este documento explica de forma clara y detallada cómo personalizar cada aspecto del televisor, cómo recompilar e instalar los cambios, y cómo funciona el sistema de apagado, reinicio y accesibilidad.

---

## Índice

1. [Scripts Rápidos (Compilación y Restauración)](#1-scripts-rápidos)
2. [Cómo Personalizar el Dock y el Número de Columnas](#2-cómo-personalizar-el-dock-y-el-número-de-columnas)
3. [Cómo Configurar el Salvapantallas (Imágenes y Tiempos)](#3-cómo-configurar-el-salvapantallas)
4. [Cómo Modificar o Añadir Frases y Poemas](#4-cómo-modificar-o-añadir-frases-y-poemas)
5. [Servicio de la Tecla Home y Menú de Energía](#5-servicio-de-la-tecla-home-y-menú-de-energía)
   - [Funciones del Menú (Apagar, Reiniciar, Salvapantallas)](#funciones-del-menú)
   - [Compromiso de Accesibilidad y Repetición de Volumen en Android 14](#compromiso-de-accesibilidad-y-volumen)
   - [Cómo Activar o Desactivar el Servicio de Home](#cómo-activar-o-desactivar-el-servicio-de-home)

---

## 1. Scripts Rápidos

En la carpeta principal del proyecto dispones de scripts listos para usar con doble clic:

- **`build-and-install.bat`** (o `.\build-and-install.ps1`):
  Compila automáticamente el código fuente con Gradle, instala el APK en el televisor por red, desactiva el launcher pesado de fábrica, establece el nuevo launcher como predeterminado y activa el servicio del botón Home.
- **`restore.bat`** (o `.\restore.ps1`):
  Restaura el televisor al estado 100% de fábrica. Vuelve a habilitar todos los paquetes desactivados de Google y Vestel, restablece el launcher oficial de Google TV y desactiva los servicios de accesibilidad.

---

## 2. Cómo Personalizar el Dock y el Número de Columnas

Toda la distribución de aplicaciones en la pantalla de inicio se controla desde:
`launcher/app/src/main/res/values/config.xml`

### Modificar las aplicaciones fijadas en el Dock inferior:
Busca la sección `<string-array name="dock_apps">`:
```xml
<string-array name="dock_apps">
    <item>com.mediatek.wwtv.tvcenter</item>       <!-- TDT / Canales en directo -->
    <item>com.google.android.youtube.tv</item>     <!-- YouTube -->
    <item>com.netflix.ninja</item>                 <!-- Netflix -->
    <item>org.courville.nova</item>                <!-- Nova Video Player -->
    <item>com.android.vending</item>               <!-- Google Play Store -->
</string-array>
```
Puedes cambiar el orden, añadir o quitar elementos usando los nombres de paquete de Android.
> **Consejo**: Para conocer el nombre de paquete de cualquier aplicación instalada en el televisor, puedes ejecutar:
> `tools\android-sdk\platform-tools\adb.exe shell pm list packages -3`

### Cambiar el número de columnas de la cuadrícula:
En el mismo archivo `config.xml`, localiza:
```xml
<integer name="columns">5</integer>
```
Puedes configurarlo en **4, 5, 6 o 7 columnas**.

### Aplicar los cambios:
Tras guardar el archivo `config.xml`, ejecuta simplemente **`build-and-install.bat`**.

---

## 3. Cómo Configurar el Salvapantallas

El televisor incluye un salvapantallas cinematográfico con fondos de alta definición, reloj flotante minimalista y degradados suaves optimizados para bajo consumo de memoria.

### Dónde se ubican las imágenes en el televisor:
Las imágenes se organizan por carpetas en la memoria interna:
- `/sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature/` (Modo Naturaleza / Paisajes)
- `/sdcard/Android/data/com.gathin.tvhome/files/screensaver/art/` (Modo Arte / Pinturas clásicas)

### Cómo añadir tus propias imágenes:
1. Prepara las imágenes en formato JPG o PNG (resolución recomendada 1920x1080 o 3840x2160).
2. Opcionalmente, crea o edita un archivo `captions.txt` con el nombre de cada foto, su título y autor separados por tabulación (`\t`):
   ```
   foto1.jpg	Parque Nacional de Ordesa	Pirineos, Huesca
   foto2.jpg	El valle del Rin	Alemania
   ```
3. Cópialas al televisor con el comando:
   ```cmd
   tools\android-sdk\platform-tools\adb.exe push mi_foto.jpg /sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature/
   ```

### Modos disponibles:
- **Naturaleza**: Fotografías de paisajes y naturaleza con autor y localización.
- **Arte**: Obras de arte clásicas de dominio público en alta definición.
- **Mezcla**: Combina automáticamente todas las fotografías de naturaleza y obras de arte en una rotación aleatoria fluida.
- **Solo reloj**: Modo minimalista tipo OLED (reloj y fecha flotantes sobre fondo negro con iluminación ambiental sutil).

### Orden aleatorio de imágenes:
Las imágenes se cargan y barajan aleatoriamente cada vez que se inicia el salvapantallas, garantizando que nunca empiece por la misma imagen y que no se repitan de forma predecible.

### Inicio automático y botones de activación:
En **Ajustes del Launcher** (icono de engranaje en Home) dispones de 4 botones para el salvapantallas:
1. **Iniciar ahora**: Ejecuta el modo salvapantallas inmediatamente según el modo seleccionado.
2. **30 segundos**: Se activa de forma automática tras 30 segundos de inactividad en la pantalla de inicio.
3. **1 minuto**: Se activa de forma automática tras 1 minuto de inactividad.
4. **5 minutos**: Se activa de forma automática tras 5 minutos de inactividad.

*(Nota: El salvapantallas únicamente se ejecuta cuando estés en la pantalla de inicio del launcher; nunca interrumpirá la reproducción de un vídeo, una película o una aplicación abierta).*

### Activación manual adicional:
- **Desde la barra superior del Home**: Pulsa sobre el icono de encendido (a la derecha de Ajustes) y selecciona **Salvapantallas** (botón verde).
- **Desde el mando a distancia**: Mantén pulsado el botón **HOME** durante 3 segundos y pulsa sobre el botón verde **Salvapantallas**.

---

## 4. Cómo Modificar o Añadir Frases y Poemas

La pantalla de inicio rota citas célebres y poesías de forma elegante en la esquina superior derecha.

### Archivo de frases:
- En tu ordenador: `work/quotes.json`
- En el televisor: `/sdcard/Android/data/com.gathin.tvhome/files/quotes.json`

### Formato de cada entrada en `quotes.json`:
Cada frase o poema tiene la siguiente estructura:
```json
{
  "id": "mi_cita_001",
  "category": "estoicismo",
  "type": "quote",
  "text": "No nos afecta lo que nos sucede, sino lo que pensamos acerca de lo que nos sucede.",
  "author": "Epicteto",
  "source": "Enquiridión",
  "edition": "Filosofía Clásica"
}
```

- **Para citas o frases normales**: Usa `"type": "quote"`. Se mostrará en tipografía sans-serif en cursiva («...») con el autor y fuente.
- **Para poemas o textos líricos**: Usa `"type": "poem"`. Se mostrará en tipografía serif clásica con una barra vertical de acento estético.

### Sincronizar las frases con el televisor:
Tras editar `work/quotes.json`, cárgalo en el televisor ejecutando:
```cmd
tools\android-sdk\platform-tools\adb.exe push work\quotes.json /sdcard/Android/data/com.gathin.tvhome/files/quotes.json
```
*(O ejecuta `build-and-install.bat`, que lo sincroniza automáticamente).*

### Opciones desde la interfaz del televisor:
Entra en **Ajustes del Launcher -> Frases** para:
- Activar o desactivar las frases.
- Cambiar la velocidad de rotación: Rápida (15s/30s), Normal (25s/50s) o Lenta (45s/90s).
- Filtrar el tipo: Mostrar todo, solo citas o solo poemas.
- Habilitar o deshabilitar autores/categorías individuales.

---

## 5. Servicio de la Tecla Home y Menú de Energía

### Funciones del Menú

El televisor cuenta con un menú de energía completo con tres opciones:
1. **Apagar (Rojo)**: Apaga el televisor por completo.
2. **Reiniciar (Amarillo)**: Realiza un reinicio completo del sistema de forma silenciosa y automática.
3. **Salvapantallas (Verde)**: Inicia inmediatamente el modo salvapantallas sin esperas.

Este menú se puede invocar de dos formas:
- **Desde la interfaz Home**: Pulsando el botón con icono de encendido situado en la esquina superior derecha (al lado del botón Ajustes).
- **Desde cualquier aplicación con el mando a distancia**: Manteniendo pulsado el botón **HOME** durante **3 segundos** continuos. Se mostrará una ventana flotante translúcida sobre la aplicación en curso. Si decides cancelar, pulsar el botón **ATRÁS** te devuelve a la aplicación sin enviarte al Home.

---

### Compromiso de Accesibilidad y Volumen

En Android TV 14, el sistema operativo implementa una política interna estricta:
- Cuando un servicio de accesibilidad captura eventos de teclas para detectar pulsaciones largas (mediante la propiedad `canFilterKeyEvents`), el subsistema del sistema (`PhoneWindowManager` / `AccessibilityInputFilter`) consume los eventos de repetición continua de las teclas de volumen.
- Esto significa que al mantener pulsados los botones de **Subir/Bajar volumen** en el mando, el volumen cambia **paso a paso (de 1 en 1)** en lugar de deslizarse rápidamente en rampa continua.
- Este comportamiento es inherente a la arquitectura de Android TV 14 y afecta a cualquier aplicación de accesibilidad.

---

### Cómo Activar o Desactivar el Servicio de Home

Puedes elegir qué comportamiento prefieres según tu uso:

#### Opción A: Mantener el servicio activo (Configuración actual recomendada)
- **Ventajas**: Menú de 3 botones accesible con pulsación larga de Home desde cualquier app, y reinicio silencioso con un clic.
- **Compromiso**: La tecla de volumen sube/baja de uno en uno al mantenerla pulsada.

Para activarlo (si estuviera desactivado):
```powershell
tools\android-sdk\platform-tools\adb.exe shell settings put secure enabled_accessibility_services com.gathin.tvhome/.HomeKeyService
tools\android-sdk\platform-tools\adb.exe shell settings put secure accessibility_enabled 1
```
*(O desde la tele: Ajustes -> Preferencias del dispositivo -> Accesibilidad -> TV Home Key Service -> Activar).*

#### Opción B: Desactivar el servicio
- **Ventajas**: El volumen recupera la aceleración continua rápida al mantener pulsados Vol+ o Vol-.
- **Compromiso**: No se abrirá el menú flotante al mantener pulsado el botón Home en el mando (el botón de encendido de la pantalla de inicio sigue disponible para apagar y salvapantallas; el reinicio se realizaría desde Ajustes de la tele).

Para desactivarlo:
```powershell
tools\android-sdk\platform-tools\adb.exe shell settings put secure enabled_accessibility_services '""'
tools\android-sdk\platform-tools\adb.exe shell settings put secure accessibility_enabled 0
```
*(O desde la tele: Ajustes -> Preferencias del dispositivo -> Accesibilidad -> TV Home Key Service -> Desactivar).*
