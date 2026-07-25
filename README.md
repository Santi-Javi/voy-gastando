# Voy Gastando

Aplicación Android nativa en Kotlin para llevar rápidamente el total de una compra mientras se agregan productos al carrito.

## Estado actual

Fase 1 implementada:

- Proyecto Android base con Gradle Kotlin DSL.
- Jetpack Compose, Material 3 y Navigation Compose.
- Arquitectura inicial MVVM.
- Room como fuente única de verdad para compra activa e ítems.
- DataStore Preferences para configuración monetaria básica.
- Cálculos monetarios centralizados con `Long`.
- Inicio de compra con presupuesto opcional.
- Restauración automática de compra activa al abrir la app.
- Ícono provisional original.
- Pruebas unitarias del cálculo monetario y formato de moneda.

## Tecnología

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- DataStore Preferences
- Coroutines y Flow/StateFlow

## Estructura

- `app/src/main/java/com/voygastando/app/data/local`: base Room, entidades, DAO y conversores.
- `app/src/main/java/com/voygastando/app/data/repository`: repositorios concretos.
- `app/src/main/java/com/voygastando/app/domain`: modelos, contratos y reglas de dominio.
- `app/src/main/java/com/voygastando/app/ui`: tema, navegación y pantallas Compose.
- `app/src/main/java/com/voygastando/app/util`: utilidades compartidas.

## Permisos

La app no declara permiso `INTERNET`.

La Fase 1 no declara permisos porque todavía no usa notificación ni vibración.

Permisos previstos para fases posteriores:

- `POST_NOTIFICATIONS`: necesario desde Android 13 para mostrar la notificación persistente de compra activa. Se implementará en la fase de notificaciones.
- `VIBRATE`: necesario para la vibración breve al sumar productos. Se usará en la fase de ingreso rápido.

No se solicitan ubicación, cámara, micrófono, contactos, accesibilidad, superposiciones ni almacenamiento general.

## Compilar

Abrir el proyecto en Android Studio y ejecutar:

```powershell
./gradlew assembleDebug
```

Si la máquina no tiene JDK, Gradle ni Android SDK instalados, se puede preparar un entorno portable dentro del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File tools/setup-android-env.ps1
powershell -ExecutionPolicy Bypass -File tools/build-local.ps1 assembleDebug
```

Para usar `gradlew.bat` con el JDK/SDK portable en una terminal:

```powershell
. .\tools\env-local.ps1
.\gradlew.bat assembleDebug
```

## Pruebas

```powershell
./gradlew testDebugUnitTest
```

Con el entorno portable:

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-local.ps1 testDebugUnitTest
```

## Decisiones técnicas

Los importes se guardan como `Long`, en la unidad monetaria configurada. En la configuración inicial los centavos están desactivados, por lo que `1000` representa mil pesos. Cuando se activen centavos, `1000` representará diez pesos.

Room mantiene una única compra activa. Si Android mata el proceso o el usuario vuelve a abrir la app, la pantalla observa la sesión `ACTIVE` y navega directamente a la compra en curso.

La notificación persistente, el Quick Settings Tile, historial completo, edición de ítems y configuración avanzada quedan preparados para las fases siguientes.
