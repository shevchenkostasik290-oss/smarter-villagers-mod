# Инструкции по сборке Villager Behavior Mod

## Проблема
Локальная Java повреждена (SunJSSE provider error), поэтому сборка через Gradle невозможна даже с VPN.

## Решение 1: Сборка в Docker контейнере

Создайте файл `Dockerfile` в корне проекта:

```dockerfile
FROM openjdk:8-jdk

WORKDIR /app

# Копируем проект
COPY . /app

# Устанавливаем Gradle wrapper
RUN chmod +x gradlew

# Собираем проект
RUN ./gradlew build --no-daemon

# Копируем результат
RUN cp build/libs/*.jar /output/

# Создаем папку для output
RUN mkdir -p /output
```

Запустите сборку:
```bash
docker build -t villager-mod-build .
docker run --rm -v $(pwd)/output:/output villager-mod-build
```

## Решение 2: Сборка в онлайн-среде

1. Загрузите проект на GitHub/GitLab
2. Используйте GitHub Actions или GitLab CI для сборки
3. Скачайте готовый .jar файл

Пример GitHub Actions (`.github/workflows/build.yml`):
```yaml
name: Build Villager Mod
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 8
        uses: actions/setup-java@v2
        with:
          java-version: '8'
          distribution: 'adopt'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Upload artifact
        uses: actions/upload-artifact@v2
        with:
          name: villager-mod-jar
          path: build/libs/*.jar
```

## Решение 3: Ручная сборка (требует Forge зависимости)

1. Скачайте Forge MDK 1.16.5-36.2.34 с https://files.minecraftforge.net/
2. Извлеките `forge-1.16.5-36.2.34-universal.jar`
3. Скопируйте все необходимые JAR файлы в папку `libs/`
4. Запустите `build-manual.bat`

## Решение 4: Использование онлайн-компилятора

Используйте такие сервисы как:
- Replit (https://replit.com)
- GitHub Codespaces
- Gitpod

Загрузите проект туда и соберите через терминал.

## Текущий статус проекта

**Готовые файлы:**
- `src/main/java/com/villagermod/VillagerBehaviorMod.java` - главный класс
- `src/main/java/com/villagermod/VillagerEventHandler.java` - обработчик событий
- `src/main/resources/META-INF/mods.toml` - метаданные мода
- `build.gradle` - конфигурация сборки
- `build-manual.bat` - скрипт ручной сборки

**После успешной сборки:**
JAR файл будет находиться в: `build/libs/villager-behavior-mod-1.0.0.jar`

## Рекомендуемое действие

Используйте **Решение 1 (Docker)** или **Решение 2 (GitHub Actions)**, так как они не требуют исправления локальной Java и работают в изолированной среде с правильной конфигурацией SSL.
