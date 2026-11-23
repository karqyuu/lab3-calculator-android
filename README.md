# Лабораторна 3. Науковий калькулятор для Android (варіант 7)

Мова: Java. Графічний інтерфейс, тригонометрія (градуси/радіани), логарифми,
корінь, степінь, факторіал, відсотки, константи π і e, живий попередній
перегляд результату.

## Як відкрити

1. Android Studio → **File → Open** → папка `lab3-calculator-android`.
2. Дочекайтеся **Gradle sync** (перший раз довго, іде завантаження).
   Якщо Studio питає про оновлення Android Gradle Plugin, натисніть **Update**.
   Якщо Studio просить встановити SDK Platform 34, погодьтеся.
3. Оберіть пристрій (емулятор або телефон) і натисніть **Run ▶**.

### Якщо проєкт не відкривається
Створіть новий проєкт: **New Project → Empty Views Activity**, Language
**Java**, package `com.student.calculator`, Minimum SDK 24. Потім скопіюйте
з цього архіву в новий проєкт папку `app/src/main` (java + res +
AndroidManifest.xml) і додайте в `app/build.gradle` залежності:

```
implementation 'com.google.android.material:material:1.12.0'
implementation 'com.jakewharton.timber:timber:5.0.1'
```
а в блок `android { }` рядок `buildFeatures { buildConfig true }`.

## Тести (без телефона)

Ядро обчислень (`engine`) не залежить від Android. Тести запускаються
командою `./gradlew test` або зеленою стрілкою біля класу в Android Studio
(`app/src/test`).

## Вимоги лабораторної

- **Kotlin/Java**: Java.
- **Логування**: Timber. `CalculatorApp` садить `DebugTree` у debug-збірці та
  `ReleaseTree` (лише WARN і ERROR) у release. Перегляд: вікно **Logcat**.
- **i18n**: англійська (за замовчуванням, `values/`), українська
  (`values-uk/`), польська (`values-pl/`). Усі тексти в `strings.xml`,
  повідомлення про помилки передаються типом помилки, а не текстом.
  Мову можна змінити кнопкою **Language** у застосунку
  (`AppCompatDelegate.setApplicationLocales`, `locales_config.xml`) або в
  налаштуваннях Android для цього застосунку.
- **Git**: `.gitignore` налаштований для Android Studio.

## Структура

| Пакет | Призначення |
|---|---|
| `engine` | `ExpressionEvaluator` (рекурсивний спуск), `ResultFormatter`, `AngleMode`, `CalculationException` |
| `ui` | `MainActivity`, `KeySpec` (опис клавіш) |
| `logging` | `ReleaseTree` |
