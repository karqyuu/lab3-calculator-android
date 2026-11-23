# Лабораторна 3. Науковий калькулятор для Android (варіант 7)

Мова: Java. Графічний інтерфейс, тригонометрія (градуси/радіани), логарифми,
корінь, степінь, факторіал, відсотки, константи π і e, живий попередній
перегляд результату.

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
