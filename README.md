# ShopEase E-Commerce System (Group 5)

Java Swing prototype with UI, business logic, and SQLite database layers.

## Requirements

- **JDK 11+** (tested with JDK 21/24)
- **sqlite-jdbc.jar** in `lib/` (~10–14 MB). If compile fails with `zip END header not found`, the JAR is corrupted — replace it from [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/).

## Build

```bash
chmod +x compile.sh run-gui.sh
./compile.sh
```

Manual compile:

```bash
find code -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin -cp "lib/sqlite-jdbc.jar" @sources.txt
```

## Verify (no GUI)

```bash
./compile.sh
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.SmokeTest
```

## Run (GUI — use for assignment demo)

```bash
./run-gui.sh
```

In Cursor/VS Code: open `code/com/shopease/ui/ShopEaseApp.java` → **Run** above `main`, or F5 → **ShopEase GUI**.

Or:

```bash
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
```

## Run (console demo — optional)

```bash
java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.Main
```

## Default login

| Role     | Email                   | Password    |
|----------|-------------------------|-------------|
| Admin    | admin@email.admin.my    | adminpass   |
| Customer | Register on login screen | (your choice) |

Database file `shopease.db` is created in the project root on first run.

## Project structure

| Layer    | Package / folder        |
|----------|-------------------------|
| UI       | `code/com/shopease/ui/` |
| Business | `code/com/shopease/service/` |
| Data     | `code/com/shopease/dao/` |
| Patterns | `observer/`, `strategy/`, `singleton/` |

## Design patterns

- **Singleton** — `ShopEaseCartSingleton` (one cart per user)
- **Strategy** — payment methods (Credit Card, DuitNow, MAE, Touch 'n Go)
- **Observer** — inventory low/out-of-stock notifications
