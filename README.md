# ShopEase E-Commerce System (Group 5)

A premium Java Swing e-commerce application prototype with complete business logic, SQLite database integration, and UI layers.

---

## 📋 Prerequisites

Before compiling or running the project, ensure you have the following installed and set up:

- **JDK 11+** (Tested and compatible with JDK 17, 21, and 24).
- **sqlite-jdbc.jar** located in the `lib/` directory.
  > [!IMPORTANT]
  > If the SQLite jar is missing or corrupted, download a fresh copy of the SQLite JDBC library from [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/) and place it in the `lib/` folder as `lib/sqlite-jdbc.jar`.

---

## 💻 Compilation & Running Guide

Choose the guide corresponding to your operating system below:

### 🍎 macOS & Linux

#### 1. Compile the Project
Open your Terminal, navigate to the project directory, make the scripts executable, and compile:
```bash
# Make scripts executable (only needed once)
chmod +x compile.sh run-gui.sh

# Run compilation script
./compile.sh
```
*Alternatively, you can compile manually without the script:*
```bash
find code -name "*.java" > sources.txt
mkdir -p bin
javac -encoding UTF-8 -d bin -cp "lib/sqlite-jdbc.jar" @sources.txt
```

#### 2. Run the Application (GUI)
Launch the graphical interface using:
```bash
./run-gui.sh
```
*Alternatively, run the application class manually:*
```bash
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

#### 3. Run System Tests & Smoke Tests
To verify all application systems, run the unit/integration tests:
```bash
# Full System test verification suite
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.FullSystemTest

# Basic Smoke test
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.SmokeTest
```

---

### 🪟 Windows

#### 1. Compile the Project
Open **Command Prompt** (cmd) or **PowerShell**, navigate to the project directory, and run:
```cmd
.\compile.bat
```
*Alternatively, you can compile manually without the batch script:*
```cmd
dir /s /b code\*.java > sources.txt
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar" @sources.txt
```

#### 2. Run the Application (GUI)
Launch the graphical interface using:
```cmd
.\run-gui.bat
```
*Alternatively, run the application class manually (note that Windows uses `;` as the classpath separator):*
```cmd
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

#### 3. Run System Tests & Smoke Tests
To run the automated tests on Windows:
```cmd
# Full System test verification suite
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.FullSystemTest

# Basic Smoke test
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.SmokeTest
```

---

## 🔑 Default Credentials

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
