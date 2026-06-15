# ShopEase E-Commerce System (Group 5)

A Java Swing e-commerce application with SQLite database, Observer / Singleton / Strategy design patterns, and separate setup for **Windows** and **macOS (Apple)**.

> **Windows users:** see **`WINDOWS_HOW_TO_RUN.txt`** in the project root for step-by-step compile & run instructions (Command Prompt / PowerShell).

---

## Works on both Windows and Mac

| Step | macOS / Apple (Terminal) | Windows (Command Prompt) |
|------|--------------------------|---------------------------|
| **1. Compile** | `./compile.sh` | `compile.bat` |
| **2. Run GUI** | `./run-gui.sh` | `run-gui.bat` or `setup-windows.bat` *(compile + run)* |

**Requirements (both platforms):**
- JDK 11 or newer (17, 21, 24 tested)
- `lib/sqlite-jdbc.jar` (or `lib\sqlite-jdbc.jar` on Windows) — included in this repo

**First time on Mac only** — make scripts executable:
```bash
chmod +x compile.sh run-gui.sh   # once only
```

**Windows users in Git Bash** can also run `./compile.sh` and `./run-gui.sh` like Mac users.

**Database:** `shopease.db` is created automatically in the project root on first run (same folder as `compile.sh` / `compile.bat`).

---

## macOS / Apple — detailed steps

### 1. Prerequisites
- Install JDK 11+ ([Adoptium](https://adoptium.net/) or Oracle JDK)
- Ensure `lib/sqlite-jdbc.jar` exists

### 2. Compile
```bash
chmod +x compile.sh run-gui.sh   # once only
./compile.sh
```

Manual compile:
```bash
find code -name "*.java" > sources.txt
mkdir -p bin
javac -encoding UTF-8 -d bin -cp "lib/sqlite-jdbc.jar" @sources.txt
```

### 3. Run GUI
```bash
./run-gui.sh
```

Manual run (classpath uses `:` on Mac):
```bash
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

---

## Windows — detailed steps

### 1. Prerequisites
- Install JDK 11+ and add `java` / `javac` to PATH ([Adoptium](https://adoptium.net/))
- Ensure `lib\sqlite-jdbc.jar` exists

### 2. Compile
Open **Command Prompt** or **PowerShell** in the project folder:
```cmd
compile.bat
```

**One-click compile + run:**
```cmd
setup-windows.bat
```

Manual compile:
```cmd
for /r code %f in (*.java) do @echo %f>>sources.txt
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar" @sources.txt
```

### 3. Run GUI
```cmd
run-gui.bat
```

Manual run (classpath uses `;` on Windows):
```cmd
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

---

## VS Code / Cursor (Mac or Windows)

1. Open the project root folder (the one containing `code/` and `lib/`)
2. Install the **Extension Pack for Java**
3. Settings in `.vscode/settings.json` already point to `code/` and `lib/**/*.jar`
4. Run **ShopEase GUI (Mac/Windows)** from the Run and Debug panel

---

## Default login

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@email.admin.my` | `adminpass` |
| **Customer** | Register via Sign Up in the GUI | *(your choice)* |

---

## Project structure

```
TT2L_G5_ShopEaseEcommerceSystem/
├── code/com/shopease/
│   ├── dao/        # SQLite database access
│   ├── observer/   # Observer pattern (alerts, UI refresh)
│   ├── singleton/  # One cart / wishlist per customer
│   ├── strategy/   # Payment, profile, admin user CRUD
│   ├── service/    # Business logic (ShopEaseService)
│   └── ui/         # Swing GUI (entry: ShopEaseApp)
├── lib/sqlite-jdbc.jar
├── compile.sh / compile.bat
├── setup-windows.bat   ← Windows: compile + run in one step
└── run-gui.sh / run-gui.bat
```

---

## Design patterns (Assignment 2)

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Singleton** | `ShopEaseCartSingleton`, `ShopEaseWishlistSingleton` | One cart/wishlist per customer session |
| **Observer** | `ShopEaseInventorySubject` + `*Observer` classes | Stock alerts, login popups, live UI refresh |
| **Strategy** | `ShopEasePaymentContext`, payment/profile/admin strategy classes | Swap payment method or business rules at runtime |

See `code/com/shopease/DesignPatterns.java` for a full class list.
