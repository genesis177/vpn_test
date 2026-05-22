# TunVPN

Android VPN-клиент и TLS VPN-сервер на Java с использованием TUN-интерфейса и packet forwarding.

---

# О проекте

**TunVPN** — это собственная реализация VPN-сервиса для Android.

Приложение создаёт виртуальный TUN-интерфейс через Android `VpnService`, перехватывает сетевой трафик устройства и передаёт его через защищённый TLS-туннель на собственный VPN-сервер.

Сервер принимает пакеты, проксирует TCP-соединения в интернет и возвращает ответы обратно клиенту.

---

# Основные возможности

- VPN на базе Android `VpnService`
- Работа через TUN-интерфейс
- TLS 1.3 туннель
- Перехват IPv4 TCP/UDP пакетов
- NAT session tracking
- TCP relay сервер
- Docker-поддержка
- Многопоточная обработка пакетов
- Собственный packet parser

---

#  Архитектура

```text
Android Apps
      │
      ▼
VpnService (TUN)
      │
      ▼
PacketReader
      │
      ▼
TLS Tunnel
      │
════════ INTERNET ════════
      │
      ▼
VPN Server
      │
      ▼
TcpRelay
      │
      ▼
Remote Internet Server
```

---

#  Android-клиент

Клиентская часть:

- создаёт TUN-интерфейс
- читает IP-пакеты
- отслеживает NAT-сессии
- отправляет трафик через TLS
- принимает ответы от сервера
- возвращает пакеты обратно в Android networking stack

## Основные компоненты

| Компонент | Назначение |
|---|---|
| `MyVpnService` | Главный VPN-сервис |
| `VpnConfigurator` | Настройка TUN |
| `PacketReader` | Чтение пакетов |
| `PacketWriter` | Запись пакетов |
| `TunnelClient` | TLS-клиент |
| `SessionManager` | Управление NAT |
| `NatTable` | Хранилище сессий |

---

#  VPN-сервер

Серверная часть:

- принимает TLS-соединения
- читает туннельные пакеты
- создаёт TCP relay
- проксирует соединения
- возвращает ответы клиенту

## Основные компоненты

| Компонент | Назначение |
|---|---|
| `VpnServer` | TLS VPN сервер |
| `ClientHandler` | Обработка клиента |
| `TcpRelay` | TCP proxy |
| `SessionStore` | Хранилище relay |
| `SslContextProvider` | TLS контекст |

---

# Безопасность

Используется:

- TLS 1.3
- SSL sockets
- encrypted tunnel
- self-signed certificates

---

# Структура проекта

```text
tun-vpn/
│
├── android-client/      # Android VPN клиент
├── vpn-server/          # VPN сервер
├── shared/              # Общие классы
├── docker/              # Docker конфигурация
│
├── build.gradle
└── settings.gradle
```

---

# Запуск Android-клиента

## 1. Указать IP сервера

Файл:

```java
TunnelConstants.java
```

Изменить:

```java
public static final String SERVER_HOST = "your-server-ip";
```

---

## 2. Собрать APK

```bash
./gradlew assembleDebug
```

---

## 3. Установить APK

```bash
adb install app-debug.apk
```

---

## 4. Запустить VPN

1. Открыть приложение
2. Нажать `Connect`
3. Подтвердить VPN permission
4. VPN начнёт перехватывать трафик

---

# Запуск VPN-сервера

## 1. Сгенерировать сертификат

```bash
keytool -genkeypair \
-alias vpnserver \
-keyalg RSA \
-keysize 2048 \
-validity 3650 \
-keystore keystore.jks \
-storepass changeit \
-dname "CN=vpnserver, O=TunVPN, C=RU"
```

---

## 2. Собрать сервер

```bash
./gradlew :vpn-server:jar
```

---

## 3. Запустить сервер

```bash
java -jar vpn-server/build/libs/vpn-server-1.0-SNAPSHOT.jar
```

---

# Docker

## Сборка

```bash
docker-compose build
```

## Запуск

```bash
docker-compose up -d
```

---

# Как работает VPN

## Исходящий трафик

```text
App → TUN → PacketReader → TLS Tunnel → VPN Server
```

## Входящий трафик

```text
VPN Server → TLS Tunnel → PacketWriter → TUN → App
```

---

# NAT и Session Tracking

Для отслеживания соединений используется `NatTable`.

Каждая сессия хранит:

- source IP
- source port
- destination IP
- destination port
- protocol
- state
- last activity timestamp

Неактивные сессии автоматически удаляются по TTL.

---

# Используемые технологии

- Java 17
- Android SDK 34
- Android VpnService API
- TLS / SSL
- TCP/IP
- Docker
- Gradle
