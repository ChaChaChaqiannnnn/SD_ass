# ShopEase E-Commerce System (Group 5)

A premium Java Swing e-commerce application prototype with complete business logic, SQLite database integration, and UI layers.

---

## 🍎 macOS & Linux Setup & Guide

Follow these steps if you are running macOS or Linux.

### 1. Prerequisites (macOS/Linux)
- **JDK 11+** installed (Tested and compatible with JDK 17, 21, and 24).
- **sqlite-jdbc.jar** located in the `lib/` directory.
  > [!IMPORTANT]
  > If the SQLite jar is missing or corrupted, download a fresh copy of the SQLite JDBC library from [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/) and place it in the `lib/` folder as `lib/sqlite-jdbc.jar`.

### 2. Compile the Project (macOS/Linux)
Open your Terminal, navigate to the project directory, and compile the code using:
```bash
# Make scripts executable (only needed once)
chmod +x compile.sh run-gui.sh

# Run compilation script
./compile.sh
```

*Alternatively, compile manually without using the script:*
```bash
find code -name "*.java" > sources.txt
mkdir -p bin
javac -encoding UTF-8 -d bin -cp "lib/sqlite-jdbc.jar" @sources.txt
```

### 3. Run the GUI (macOS/Linux)
Launch the main application interface using:
```bash
./run-gui.sh
```

*Alternatively, run the application class manually:*
```bash
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

### 4. Run Tests (macOS/Linux)
To verify all systems and run automated unit/integration tests:
```bash
# Full System test verification suite
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.FullSystemTest

# Basic Smoke test
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.SmokeTest
```

---

## 🪟 Windows Setup & Guide

Follow these steps if you are running Windows.

### 1. Prerequisites (Windows)
- **JDK 11+** installed (Tested and compatible with JDK 17, 21, and 24).
- **sqlite-jdbc.jar** located in the `lib\` directory.
  > [!IMPORTANT]
  > If the SQLite jar is missing or corrupted, download a fresh copy of the SQLite JDBC library from [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/) and place it in the `lib\` folder as `lib\sqlite-jdbc.jar`.

### 2. Compile the Project (Windows)
Open **Command Prompt** (cmd) or **PowerShell**, navigate to the project directory, and compile:
```cmd
.\compile.bat
```

*Alternatively, compile manually without using the script:*
```cmd
dir /s /b code\*.java > sources.txt
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar" @sources.txt
```

### 3. Run the GUI (Windows)
Launch the main application interface using:
```cmd
.\run-gui.bat
```

*Alternatively, run the application class manually (note that Windows uses `;` as the classpath separator):*
```cmd
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

### 4. Run Tests (Windows)
To run the automated tests on Windows:
```cmd
# Full System test verification suite
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.FullSystemTest

# Basic Smoke test
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.SmokeTest
```

---

## 🔑 Default Credentials (Shared)

The database file `shopease.db` is automatically created in the root directory upon the first run. The system comes pre-seeded with the following admin account:

| Role | Email / Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@email.admin.my` | `adminpass` |
| **Customer** | *Register a new account via the GUI signup panel* | *(custom)* |

---

## 📂 Project Architecture

```
TT2L_G5_ShopEaseEcommerceSystem/
├── code/
│   └── com/
│       └── shopease/
│           ├── dao/        # Database access objects (SQLite interactions)
│           ├── observer/   # Observer design pattern interfaces
│           ├── service/    # Core business and service logic 
│           ├── strategy/   # Payment strategies (Credit Card, MAE, TNG, DuitNow)
│           └── ui/         # Java Swing GUI Panels and Dialogs
├── lib/
│   └── sqlite-jdbc.jar     # SQLite JDBC driver
├── compile.sh / .bat       # OS-specific compilation scripts
└── run-gui.sh / .bat       # OS-specific GUI runners
```

---

## 🎨 Implemented Design Patterns

* **Singleton**: Applied to [DatabaseConnection](file:///Users/serenehong/Desktop/TT2L_G5_ShopEaseEcommerceSystem/code/com/shopease/dao/DatabaseConnection.java) to manage a single connection instance and `ShopEaseCartSingleton` to maintain a single cart context per customer session.
* **Strategy Pattern**: Used to encapsulate different payment methods (Credit Card, MAE, DNG, Touch 'n Go) with a unified factory interface in [ShopEasePaymentStrategyFactory](file:///Users/serenehong/Desktop/TT2L_G5_ShopEaseEcommerceSystem/code/com/shopease/strategy/ShopEasePaymentStrategyFactory.java).
* **Observer Pattern**: Implements low-stock alerts dynamically notifying admin systems when stock is reduced below thresholds.
