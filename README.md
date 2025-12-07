# BeaTosu

<div align="center">

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.6-blue?style=for-the-badge&logo=java)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A full-featured osu! rhythm game clone built with Java and JavaFX**

[Features](#-features) • [Architecture](#-architecture) • [Installation](#-installation) • [Usage](#-usage) • [Screenshots](#-screenshots)

</div>

---

## Overview

BeaTosu is a comprehensive clone of the popular rhythm game [osu!](https://osu.ppy.sh/), developed entirely in Java using JavaFX for the graphical user interface. This project implements the core osu! Standard gameplay mechanics, multiplayer functionality, and a complete client-server architecture.

## Features

### Gameplay
- **Full osu! Standard Mode Support** - Hit circles, sliders, and spinners with accurate timing mechanics
- **Beatmap Parsing** - Complete `.osu` file parser supporting all hit object types
- **Scoring System** - Accurate scoring with combo multipliers, accuracy calculation, and grading (SS, S, A, B, C, D)
- **HP Drain System** - Dynamic health bar with drain and recovery mechanics
- **Break Periods** - Support for mid-song breaks with pass/fail indicators
- **Input Overlay** - Real-time visualization of key presses

### Multiplayer
- **Match Lobbies** - Create and join multiplayer matches with password protection
- **Real-time Synchronization** - Synchronized game start and live score updates
- **Chat System** - Public channels and private messaging
- **Spectator Mode** - Watch other players in real-time
- **Win Conditions** - Score, accuracy, and combo-based win conditions

### Audio & Visual
- **BGM Playback** - Background music with volume control
- **Sound Effects** - Hit sounds, UI feedback, and gameplay audio
- **Custom Backgrounds** - Dynamic background dimming and beatmap backgrounds
- **Smooth Animations** - Fluid transitions and visual feedback

### User System
- **Authentication** - User registration and login with secure password hashing
- **User Profiles** - Profile pictures, country flags, and statistics
- **Leaderboards** - Global and per-beatmap score rankings
- **Replay System** - Record and playback gameplay sessions

---

## Architecture

BeaTosu follows a **multi-module Gradle project structure** with a clean separation between client, server, and shared components.

```
BeaTosu/
├── client/                 # JavaFX Desktop Application
├── server/                 # TCP Socket Server
├── shared/                 # Common DTOs, Models & Enums
├── gradle/                 # Gradle Wrapper
├── diagram/                # Architecture Diagrams (VPP)
├── build.gradle            # Root Build Configuration
└── settings.gradle         # Module Definitions
```

### Module Details

#### Client Module (`client/`)
The desktop application built with JavaFX featuring:

| Package | Description |
|---------|-------------|
| `view/` | JavaFX UI components organized by screen (landing, home, game, lobby, match) |
| `controller/` | Client-side controllers handling user actions and server communication |
| `service/` | Singleton service for server connection management |
| `connection/` | TCP socket connection with request-response and real-time message handling |
| `helper/` | Manager classes (Game, Audio, Background, Scene, Input, etc.) |
| `model/` | Client-side models (Beatmap, HitObject, TimingPoint, etc.) |
| `utils/` | Utilities including the `.osu` file parser |
| `events/` | Game event system with listeners |
| `factory/` | Factory pattern for HitObject creation |

#### Server Module (`server/`)
The multi-threaded TCP socket server featuring:

| Package | Description |
|---------|-------------|
| `handler/` | Client connection handlers and message processors |
| `router/` | Message routing based on type and action |
| `service/` | Business logic services (Auth, Match, Score, Channel, etc.) |
| `repositories/` | Data access layer for database operations |
| `entities/` | Server-side entity models |
| `database/` | MySQL connection management |
| `config/` | Server configuration management |

#### Shared Module (`shared/`)
Common code shared between client and server:

| Package | Description |
|---------|-------------|
| `dto/` | Data Transfer Objects for client-server communication |
| `models/` | Message models (Request, Response, Realtime) |
| `enums/` | Shared enumerations (MessageType, PlayerStatus, etc.) |
| `common/` | Common utilities (Result, Error handling) |

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 11+ | Primary Language |
| JavaFX | 17.0.6 | UI Framework |
| Gradle | 8.x | Build System |
| MySQL | 8.0 | Database |
| Lombok | 1.18.30 | Boilerplate Reduction |
| ControlsFX | 11.2.1 | Enhanced UI Controls |
| Ikonli | 12.3.1 | Icon Library |
| BootstrapFX | 0.4.0 | CSS Styling |

---

## Installation

### Prerequisites

- **JDK 11 or higher** - [Download OpenJDK](https://adoptium.net/)
- **MySQL 8.0+** - [Download MySQL](https://dev.mysql.com/downloads/)
- **Gradle** (or use the included wrapper)

### Database Setup

1. Create a new MySQL database:
```sql
CREATE DATABASE beatosu;
```

2. Import the schema:
```bash
mysql -u root -p beatosu < server/src/main/resources/migration.sql
```

3. Update database credentials in `server/src/main/java/beat/osu/server/database/Connect.java`:
```java
private final String USERNAME = "your_username";
private final String PASSWORD = "your_password";
private final String HOST = "localhost:3306";
private final String DATABASE = "beatosu";
```

### Build & Run

1. **Clone the repository:**
```bash
git clone https://github.com/Wenfuuu/BeaTosu.git
cd BeaTosu
```

2. **Build the project:**
```bash
# Windows
.\gradlew.bat build

# Linux/macOS
./gradlew build
```

3. **Start the server:**
```bash
# Windows
.\gradlew.bat :server:run

# Linux/macOS
./gradlew :server:run
```

4. **Start the client (in a new terminal):**
```bash
# Windows
.\gradlew.bat :client:run

# Linux/macOS
./gradlew :client:run
```

### Configuration

**Client Configuration** (`client/config.properties`):
```properties
server.host=localhost
server.port=8081
keybind.1=Z
keybind.2=X
sfx.volume=0.2
bgm.volume=0.1
background.dim=0.8
```

**Server Configuration** (`server/config.properties`):
```properties
server.host=localhost
server.port=8081
connection.timeout=5000
```

---

## Usage

### Adding Beatmaps

1. Download beatmaps from [osu! website](https://osu.ppy.sh/beatmapsets) (`.osz` files)
2. Upload the `.osz` file in the home page (it's a ZIP archive)
3. The game will detect, extract and parse `.osu` files automatically

### Controls

| Key | Action |
|-----|--------|
| `Z` | Primary Hit |
| `X` | Secondary Hit |
| `Mouse` | Cursor Movement |
| `Escape` | Pause/Exit |

### Multiplayer

1. Navigate to **Lobby** from the home screen
2. **Create Match** with a name and optional password
3. Other players can join from the lobby
4. **Host** selects the beatmap
5. Players set status to **Ready**
6. Host starts the match

---

## Development

### Building Shadow JARs

Create executable JAR files with all dependencies:

```bash
# Client
.\gradlew.bat :client:shadowJar

# Server
.\gradlew.bat :server:shadowJar
```

Output locations:
- `client/build/libs/beatosu-client.jar`
- `server/build/libs/beatosu-server.jar`

### Running JARs

```bash
# Start server
java -jar server/build/libs/beatosu-server.jar

# Start client
java -jar client/build/libs/beatosu-client.jar
```

---

## License

This project is for educational purposes. osu! is a registered trademark of ppy Pty Ltd.

---

## Acknowledgments

- [osu!](https://osu.ppy.sh/) - Original game by peppy
- [JavaFX](https://openjfx.io/) - UI Framework
- [Project Lombok](https://projectlombok.org/) - Java Library

---

<div align="center">
Made with ❤️ and ☕ by <a href="https://github.com/Wenfuuu">Wenfuuu</a> and <a href="https://github.com/Artificed">Artificed</a>
</div>
