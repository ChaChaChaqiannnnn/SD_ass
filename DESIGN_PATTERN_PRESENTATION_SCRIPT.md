# ShopEase — Complete Design Pattern Presentation Script

**Project:** TT2L Group 5 — ShopEase E-Commerce System  
**Patterns:** Gang of Four (GoF) — **Singleton**, **Strategy**, **Observer**  
**Code root:** `code/com/shopease/`  
**Suggested time:** 20–25 minutes (patterns only) + Q&A  
**Demo admin login:** `admin@email.admin.my` / `adminpass`

---

## How to Read This Script

| Marker | Meaning |
|--------|---------|
| **[SAY]** | Speak this aloud to your lecturer |
| **[SHOW]** | Open the file and point at the line numbers |
| **[DEMO]** | Run the app and perform the action |
| **[GOF]** | Gang of Four role name — say this when mapping code to the textbook |

**Tip:** When your lecturer asks “where is the Subject?”, answer with **GoF role + class name + file + line number**.

---

## Opening (30 seconds)

**[SAY]**  
"Good morning/afternoon. I will explain how ShopEase implements **three Gang of Four design patterns** in real, runnable Java code — not just on UML slides. For each pattern I will name the **GoF role** — Subject, ConcreteStrategy, getInstance, and so on — and point to the **exact line of code** where that role lives. Our patterns sit in three packages: `singleton/`, `strategy/`, and `observer/`, coordinated by `ShopEaseService`."

**[SHOW]** `code/com/shopease/DesignPatterns.java` — quick reference map of all GoF roles (lines 10–45).

---

# PATTERN 1 — SINGLETON (GoF)

## 1.1 What Problem Singleton Solves

**[SAY]**  
"In GoF terms, **Singleton** ensures a class has only **one instance** and provides a **global access point** — `getInstance()`. In ShopEase we use Singleton in **three ways**:

1. **One database manager** for the whole application — lazy initialization.
2. **One cart per customer** — we extend classic Singleton into a **multiton** (one instance per `userId`).
3. **One wishlist session per customer** — same multiton idea.

Without this, two cart objects for the same user would show different totals — that is a real bug, not a theoretical one."

---

## 1.2 GoF Singleton Structure (Textbook)

**[SAY]**  
"The GoF Singleton structure has four parts:

| GoF Part | Meaning |
|----------|---------|
| **Singleton** | The class itself |
| **static instance** | Holds the single object |
| **private constructor** | Blocks `new` from outside |
| **getInstance()** | Global access — creates on first call if null |

We follow this exactly in `ShopEaseDatabaseManager`. Cart and wishlist add a `Map<userId, instance>` because we need one object **per user**, not one for the whole JVM."

---

## 1.3 Singleton Type A — Database Manager (Lazy, Global)

**[SHOW]** `code/com/shopease/singleton/ShopEaseDatabaseManager.java`

**[SAY — walk line by line]**

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **13** | **Singleton class** | "This is our Singleton class — one connection manager for SQLite." |
| **15** | **static instance** | "GoF `instance` field — starts as `null` until first use. This is **lazy initialization**, not eager." |
| **21** | **private constructor** | "Private constructor — nobody can write `new ShopEaseDatabaseManager()` outside this class. That is mandatory for Singleton." |
| **24–31** | **getInstance()** | "GoF global access point. Line 25: `if (instance == null)` — create once. Line 26: `instance = new ...`. Line 31: always return the same reference. `synchronized` makes it thread-safe." |
| **34–39** | **Operations** | "Instance methods — `getConnection()` opens JDBC to `shopease.db`. All DAOs share this one manager." |
| **42–133** | **Operations** | "`initializeDatabase()` creates all seven tables once at startup." |

**[SHOW]** `code/com/shopease/dao/DatabaseConnection.java`

**[SAY]**  
"DAO code does not create its own manager. Line **17** calls `ShopEaseDatabaseManager.getInstance().getConnection()`. Line **21** calls `getInstance().initializeDatabase()`. The **Singleton is the manager**; `DatabaseConnection` is just a thin delegate."

**[SHOW]** `code/com/shopease/service/ShopEaseService.java` line **57**

**[SAY]**  
"When the app starts, `ShopEaseService` constructor line 57 calls `DatabaseConnection.initializeDatabase()` — that triggers the **first** `getInstance()`, so you will see `[Memory] New ShopEaseDatabaseManager instance created.` in the console only once."

**[DEMO]** Run app twice in one JVM session — second `getInstance()` prints `Returning existing ShopEaseDatabaseManager instance.` (line **29**).

---

## 1.4 Singleton Type B — Cart (Multiton per userId)

**[SHOW]** `code/com/shopease/singleton/ShopEaseCartSingleton.java`

**[SAY — walk line by line]**

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **14** | **Registry of instances** | "Instead of one `static instance`, we use `Map<String, ShopEaseCartSingleton>` — one cart per `userId`. This is Singleton **extended** to multiton." |
| **18–22** | **private constructor** | "Private constructor takes `userId` — only `getInstance` can create carts." |
| **24–30** | **getInstance(userId)** | "GoF access point with a key. Line 25: if no cart for this user, create at line 26. Line 30: return existing reference for same user." |
| **33–35** | **Operations** | "`addItem`, `getItems` — all cart mutations go through this single object per user." |

**[SHOW]** `code/com/shopease/service/ShopEaseService.java`

| Line(s) | What to Say |
|---------|-------------|
| **39** | "`userCart` field holds the Singleton reference for the logged-in customer." |
| **96** | "On customer login: `ShopEaseCartSingleton.getInstance(user.getUserId())` — **this is where Singleton is used at runtime**." |
| **336–338** | "`getCart()` returns the same Singleton instance for the session." |
| **374–384** | "`addToCart` mutates `userCart` — always the same object." |
| **508** | "After checkout, `userCart.getItems().clear()` clears the Singleton cart." |

**[DEMO]** Customer login → add item → logout → login same user → cart items still in memory (same Singleton in the static map).

---

## 1.5 Singleton Type C — Wishlist (Multiton per userId)

**[SHOW]** `code/com/shopease/singleton/ShopEaseWishlistSingleton.java`

**[SAY]**

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **16** | **Registry** | "Same multiton pattern as cart — `Map<String, ShopEaseWishlistSingleton>`." |
| **19–22** | **private constructor** | "Private — only created via `getInstance`." |
| **24–26** | **getInstance(userId)** | "Line 25: `computeIfAbsent` — idiomatic Java for create-if-missing. Same GoF idea as cart." |
| **32–46** | **Operations** | "Wishlist product IDs persist in SQLite via `WishlistDAO`; this Singleton is the in-memory handle per user." |

**[SHOW]** `ShopEaseService.java` line **97** — `ShopEaseWishlistSingleton.getInstance(user.getUserId())` on customer login.

---

## 1.6 Singleton — One Paragraph to Memorise

**[SAY]**  
"`ShopEaseDatabaseManager` line 15 is the GoF `instance`, line 21 is the private constructor, line 24 is `getInstance()` with lazy creation. `ShopEaseCartSingleton` line 14 is a per-user instance registry; line 96 in `ShopEaseService` is where the client obtains the cart Singleton at login. Three Singleton **types**, all following GoF: private constructor, controlled creation, single access point."

---

# PATTERN 2 — STRATEGY (GoF)

## 2.1 What Problem Strategy Solves

**[SAY]**  
"GoF **Strategy** defines a family of **algorithms**, encapsulates each one, and makes them **interchangeable**. The **Context** delegates to a **Strategy interface** without knowing which concrete algorithm runs. This follows the **Open/Closed Principle** — we add GrabPay by adding one class, not by editing checkout with more `if-else`."

**GoF names in our code:** We deliberately use `algorithmInterface()` on Strategy and `contextInterface()` on Context — same names as the GoF structure diagram.

---

## 2.2 GoF Strategy Structure (Textbook)

| GoF Role | Our Code |
|----------|----------|
| **Strategy** | Interface — e.g. `ShopEasePaymentStrategy` |
| **ConcreteStrategy** | Implementation — e.g. `ShopEaseDuitNowStrategy` |
| **Context** | Holds strategy, delegates — e.g. `ShopEasePaymentContext` |
| **Client** | Selects strategy — e.g. `CartDialog.paymentStrategyFor()` |

---

## 2.3 Strategy Family 1 — Payment (4 Concrete Strategies)

### Strategy interface

**[SHOW]** `code/com/shopease/strategy/ShopEasePaymentStrategy.java`

**[SAY]**  
"Line **6–7**: GoF **Strategy** interface. Single method `algorithmInterface(double amount)` — this is the interchangeable algorithm contract."

### Concrete Strategies

**[SHOW]** Each file — point at `algorithmInterface`:

| File | Line | GoF Role | Algorithm |
|------|------|----------|-----------|
| `ShopEaseCreditCardStrategy.java` | **6–8** | **ConcreteStrategy** | Credit Card payment |
| `ShopEaseDuitNowStrategy.java` | **6–8** | **ConcreteStrategy** | DuitNow QR |
| `ShopEaseMAEStrategy.java` | **6–8** | **ConcreteStrategy** | MAE |
| `ShopEaseTNGStrategy.java` | **6–8** | **ConcreteStrategy** | Touch 'n Go |

**[SAY]**  
"Each ConcreteStrategy implements the same interface with different console output — simulating different payment gateways without real bank APIs."

### Context

**[SHOW]** `code/com/shopease/strategy/ShopEasePaymentContext.java`

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **7–8** | **Context** | "Context class holds a reference to Strategy." |
| **8** | **strategy field** | "Private `ShopEasePaymentStrategy strategy` — the algorithm we will run." |
| **10–12** | **Constructor** | "Context is constructed with a ConcreteStrategy — dependency injection." |
| **14–16** | **setStrategy** | "Optional runtime swap — GoF allows changing strategy after construction." |
| **19–21** | **contextInterface** | "**This is the key line.** GoF Context delegates: `strategy.algorithmInterface(amount)`. Context does not know if it is DuitNow or MAE." |

### Client selects ConcreteStrategy

**[SHOW]** `code/com/shopease/ui/CartDialog.java`

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **24–31** | **Client labels** | "Payment dropdown options — user-visible names." |
| **106** | **Client UI** | "`JComboBox` — customer picks payment method." |
| **272–273** | **Client selection** | "Line 272: read selected label. Line 273: `paymentStrategyFor()` returns the right **ConcreteStrategy**." |
| **334–343** | **Client factory logic** | "`paymentStrategyFor` — switch maps label to `new ShopEaseDuitNowStrategy()` etc. This is the **Client** in GoF — it chooses which algorithm to pass to Context." |
| **299** | **Delegation to service** | "`service.checkout(strategy)` — passes Strategy interface, not a string." |

### Service runs Context

**[SHOW]** `code/com/shopease/service/ShopEaseService.java` lines **466–518**

**[SAY — checkout sequence]**

| Line(s) | What to Say |
|---------|-------------|
| **466** | "`checkout(ShopEasePaymentStrategy strategy)` — accepts **Strategy interface**, not concrete class. Open/Closed." |
| **474–489** | "Validate cart and stock — business rules before payment." |
| **492–493** | "**Strategy pattern execution.** Line 492: `new ShopEasePaymentContext(strategy)` — create Context with chosen ConcreteStrategy. Line 493: `context.contextInterface(total)` — Context calls `algorithmInterface` on the strategy." |
| **502–506** | "Persist order to SQLite — payment is simulated, order is real." |
| **508** | "Clear Singleton cart." |
| **510–514** | "Reduce stock — triggers Observer (covered later)." |

**[DEMO]** Checkout with DuitNow, then Credit Card — console shows different `algorithmInterface` output.

### Full payment call chain (say while stepping debugger)

```
CartDialog.doCheckout()           line 273  → paymentStrategyFor()     line 334–343
  → ShopEaseService.checkout()    line 466
    → new ShopEasePaymentContext  line 492  [Context]
    → contextInterface(total)     line 493
      → strategy.algorithmInterface(total)   [ConcreteStrategy line 6–8]
```

---

## 2.4 Strategy Family 2 — Inventory Actions (3 Concrete Strategies)

### Strategy interface

**[SHOW]** `code/com/shopease/strategy/ShopEaseInventoryActionStrategy.java`

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **12** | **Strategy interface** | "`ShopEaseInventoryActionStrategy` — inventory algorithms." |
| **14** | **algorithmInterface** | "GoF method name — takes `InventoryActionRequest` and `InventoryActionResult`." |
| **17–50** | **Request/Result DTOs** | "Data passed into algorithms — keeps strategy methods clean." |

### Concrete Strategies

| File | Line | GoF Role | Algorithm |
|------|------|----------|-----------|
| `RestockInventoryStrategy.java` | **14–16** | **ConcreteStrategy** | Add stock |
| `ReduceInventoryStrategy.java` | **9–20** | **ConcreteStrategy** | Reduce stock (requires remarks) |
| `UndoInventoryStrategy.java` | **13–54** | **ConcreteStrategy** | Undo last admin action |

**[SHOW]** `RestockInventoryStrategy.java` line **45**

**[SAY]**  
"After updating the database, line 45 calls `inventorySubject.setStock(stockAfter, product.getName())` — this is where **Strategy hands off to Observer**. Stock algorithm finishes, then Subject notifies listeners."

### Context

**[SHOW]** `code/com/shopease/strategy/ShopEaseInventoryActionContext.java`

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **8–9** | **Context** | "Holds `ShopEaseInventoryActionStrategy`." |
| **20–22** | **contextInterface** | "Delegates to `strategy.algorithmInterface(request, result)`." |

### Service wires strategies at construction

**[SHOW]** `ShopEaseService.java`

| Line(s) | What to Say |
|---------|-------------|
| **49–51** | "Three ConcreteStrategy fields: `restockStrategy`, `reduceStrategy`, `undoStrategy` — created once." |
| **273–278** | "Public methods `restockProduct`, `reduceStockProduct` — UI calls these." |
| **281–311** | "`runInventoryAction` — **generic Context runner**. Line 296: `new ShopEaseInventoryActionContext(strategy)`. Line 297: `contextInterface`. Same pattern as payment, different Strategy family." |
| **318–324** | "`undoLastRestock` passes `undoStrategy` into same `runInventoryAction`." |

**[DEMO]** Admin → Inventory → select Gaming Laptop → Reduce by 2 with remarks → stock 3 → Observer fires (next section).

---

## 2.5 Strategy Family 3 — Login Validation

**[SHOW]** `code/com/shopease/strategy/ShopEaseLoginValidationStrategy.java` line **10–12** — **Strategy interface**  
**[SHOW]** `code/com/shopease/strategy/DefaultLoginValidationStrategy.java` line **13–25** — **ConcreteStrategy**

**[SHOW]** `ShopEaseService.java`

| Line(s) | What to Say |
|---------|-------------|
| **48** | "`loginValidationStrategy = new DefaultLoginValidationStrategy()` — ConcreteStrategy wired at construction." |
| **84** | "`loginValidationStrategy.validate(...)` — Strategy runs on every login." |

**[SAY]**  
"We could add `LdapLoginValidationStrategy` implementing the same interface without changing `login()` — that is Strategy + Open/Closed."

---

## 2.6 Strategy Family 4 — Profile Update & Registration

| GoF Role | File | Line |
|----------|------|------|
| **Strategy** | `ShopEaseProfileUpdateStrategy.java` | **11–13** |
| **ConcreteStrategy** | `CustomerProfileUpdateStrategy.java` | **14–46** |
| **Strategy** | `ShopEaseRegisterCustomerStrategy.java` | **7–9** |
| **ConcreteStrategy** | `RegisterCustomerStrategy.java` | **16–58** |

**[SHOW]** `ShopEaseService.java`

| Line(s) | What to Say |
|---------|-------------|
| **43, 47** | "ConcreteStrategy instances for profile and registration." |
| **111** | "`registerCustomerStrategy.register(...)` in `registerCustomer()`." |
| **592** | "`profileUpdateStrategy.update(...)` in `updateProfile()`." |

---

## 2.7 Strategy Family 5 — Admin User CRUD

| GoF Role | File | Line |
|----------|------|------|
| **Strategy** | `ShopEaseCreateCustomerStrategy.java` | interface |
| **ConcreteStrategy** | `CreateCustomerUserStrategy.java` | **17–52** |
| **Strategy** | `ShopEaseUpdateCustomerStrategy.java` | interface |
| **ConcreteStrategy** | `UpdateCustomerUserStrategy.java` | **14–54** |
| **Strategy** | `ShopEaseAdminUserActionStrategy.java` | **10–11** |
| **ConcreteStrategy** | `DeleteCustomerUserStrategy.java` | **14–37** |

**[SHOW]** `ShopEaseService.java` lines **44–46**, **631**, **644**, **657** — service delegates create/update/delete to ConcreteStrategies.

---

## 2.8 Strategy — One Paragraph to Memorise

**[SAY]**  
"In GoF Strategy, the **Client** `CartDialog` line 273 selects a **ConcreteStrategy** via `paymentStrategyFor` lines 334–343. The **Context** `ShopEasePaymentContext` line 19 `contextInterface` delegates to **Strategy** `ShopEasePaymentStrategy` line 7 `algorithmInterface`. `ShopEaseService.checkout` line 492–493 runs this without knowing the payment brand. We have **five Strategy families** — payment, inventory, login, profile/register, admin CRUD — all using the same GoF structure."

---

# PATTERN 3 — OBSERVER (GoF, Pull Model)

## 3.1 What Problem Observer Solves

**[SAY]**  
"GoF **Observer** defines a one-to-many dependency: when one object — the **Subject** — changes state, all **Observers** are notified automatically. The Subject does not know whether the reaction is a popup, console log, or cart sync — **loose coupling**.

We use the **pull model**: `update()` has **no parameters**. Each observer calls `subject.getState()` to read `SubjectState`. GoF also describes a push model where data is passed into `update(event)` — we deliberately do **not** use that."

---

## 3.2 GoF Observer Structure (Textbook)

| GoF Role | Our Class | File |
|----------|-----------|------|
| **Subject** | `Subject` | `observer/Subject.java` |
| **ConcreteSubject** | `ShopEaseInventorySubject` | `observer/ShopEaseInventorySubject.java` |
| **Observer** | `Observer` | `observer/Observer.java` |
| **ConcreteObserver** | 7 classes (see §3.6) | `observer/ShopEase*Observer.java` |
| **State** | `SubjectState` | `observer/SubjectState.java` |

---

## 3.3 Subject (Abstract) — attach, detach, notify, getState

**[SHOW]** `code/com/shopease/observer/Subject.java`

**[SAY — walk line by line]**

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **14** | **Subject** | "Abstract Subject — GoF role. Defines the observer registry and notification protocol." |
| **15** | **observers list** | "Private list of `Observer` — all registered listeners." |
| **17–22** | **attach(Observer)** | "GoF `Attach` — register listener. Line 18–19: bind Subject reference into `ShopEaseAbstractObserver` for pull model." |
| **24–26** | **detach(Observer)** | "GoF `Detach` — unregister on logout." |
| **28–31** | **notifyObservers()** | "**Core of Observer.** Line 29: for-each observer. Line 30: `observer.update()` — no data passed. This is pull, not push." |
| **35** | **getState()** | "Abstract — ConcreteSubject implements. Observers pull state after `update()`." |

---

## 3.4 Observer Interface

**[SHOW]** `code/com/shopease/observer/Observer.java`

**[SAY]**  
"Line **8–9**: GoF **Observer** interface. Single method `void update()` — no event parameter. That design choice forces pull."

---

## 3.5 ConcreteSubject — state + notify triggers

**[SHOW]** `code/com/shopease/observer/ShopEaseInventorySubject.java`

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **7** | **ConcreteSubject** | "Extends abstract `Subject` — real subject for inventory and app events." |
| **8–9** | **subjectState + stock** | "Internal state observers will pull." |
| **12–15** | **getState()** | "GoF: `return subjectState` — observers read this in pull model." |
| **17–19** | **setSubjectState** | "Builds new `SubjectState(event, productName, stock)`." |
| **22–24** | **publishEvent** | "Generic event bus — login reminders, UI refresh. Line 23: set state. Line 24: `notifyObservers()`." |
| **31–41** | **setStock** | "Called after restock/reduce/checkout. Line 33–36: qty==0 → OUT_OF_STOCK → notify. Line 37–40: qty&lt;5 → LOW_STOCK → notify. Normal stock (5+) does **not** notify." |

**[SHOW]** `code/com/shopease/model/InventoryStockStatus.java` line **9**

**[SAY]**  
"`LOW_STOCK_THRESHOLD = 5` — shared constant. Stock 1–4 triggers LOW_STOCK; 0 triggers OUT_OF_STOCK."

---

## 3.6 SubjectState — data observers pull

**[SHOW]** `code/com/shopease/observer/SubjectState.java`

| Line(s) | What to Say |
|---------|-------------|
| **7–12** | "Immutable state object: `event`, `productName`, `stockQuantity`." |
| **24–33** | "Getters — observers read these after `getState()`." |

---

## 3.7 Abstract Observer — pull model implementation

**[SHOW]** `code/com/shopease/observer/ShopEaseAbstractObserver.java`

**[SAY — this is the most important Observer demo]**

| Line(s) | GoF Role | What to Say |
|---------|----------|-------------|
| **7** | **Base ConcreteObserver** | "Implements `Observer` — shared pull logic for all concrete observers." |
| **8** | **subject reference** | "Reference to Subject — set in `attach` via `bindSubject` line 10." |
| **14–18** | **update()** | "**GoF `Update`.** Line 15: `update()` called with no args. Line 17: **`subject.getState()`** — this is the **pull**. Line 17: pass state to `onStateChanged`." |
| **21** | **onStateChanged** | "Subclass hook — each ConcreteObserver filters by event type here." |

**[SAY]**  
"When the lecturer asks push vs pull, open line **17** and say: 'Observer pulls — `getState()` is called inside `update()`, not passed in by Subject.'"

---

## 3.8 Event Types

**[SHOW]** `code/com/shopease/observer/ShopEaseAppEvents.java` lines **8–13**

| Constant | When Fired |
|----------|------------|
| `DATA_CHANGED` | Cart/profile/checkout writes — UI refresh |
| `LOW_STOCK` | Stock 1–4 |
| `OUT_OF_STOCK` | Stock 0 |
| `ADMIN_LOGIN_STOCK` | Admin login with low/out products |
| `CART_REMINDER` | Customer login with items in cart |
| `WISHLIST_RESTOCK` | Wishlist item was 0, now back in stock |

---

## 3.9 All Seven ConcreteObservers

| # | GoF Role | Class | File | Key Lines | Reacts To | Effect |
|---|----------|-------|------|-----------|-----------|--------|
| 1 | **ConcreteObserver** | `ShopEaseInventoryAdminLogObserver` | `observer/ShopEaseInventoryAdminLogObserver.java` | **9–16** | LOW_STOCK, OUT_OF_STOCK | Console log |
| 2 | **ConcreteObserver** | `ShopEaseInventoryAdminAlertObserver` | `observer/ShopEaseInventoryAdminAlertObserver.java` | **18–35** | LOW_STOCK, OUT_OF_STOCK | Admin popup |
| 3 | **ConcreteObserver** | `ShopEaseAdminLoginStockAlertObserver` | `observer/ShopEaseAdminLoginStockAlertObserver.java` | **18–26** | ADMIN_LOGIN_STOCK | Login summary popup |
| 4 | **ConcreteObserver** | `ShopEaseCustomerCartReminderObserver` | `observer/ShopEaseCustomerCartReminderObserver.java` | **18–25** | CART_REMINDER | Customer popup |
| 5 | **ConcreteObserver** | `ShopEaseCustomerWishlistRestockObserver` | `observer/ShopEaseCustomerWishlistRestockObserver.java` | **18–27** | WISHLIST_RESTOCK | Wishlist popup |
| 6 | **ConcreteObserver** | `ShopEaseDataChangeRefreshObserver` | `observer/ShopEaseDataChangeRefreshObserver.java` | **16–19** | DATA_CHANGED | Reload UI lists |
| 7 | **ConcreteObserver** | `ShopEaseCartStockSyncObserver` | `observer/ShopEaseCartStockSyncObserver.java` | **18–33** | OUT_OF_STOCK | Remove item from Singleton cart |

**[SAY]**  
"Every ConcreteObserver extends `ShopEaseAbstractObserver`, so all share pull logic lines 14–18. Each filters in `onStateChanged` by `state.getEvent()` — they receive every notify but ignore unrelated events."

**Example — admin alert popup:**

**[SHOW]** `ShopEaseInventoryAdminAlertObserver.java` lines **18–35**

**[SAY]**  
"Line 19: read event from pulled state. Line 21: ignore if not LOW or OUT. Line 24–34: `JOptionPane` on UI thread — ConcreteObserver reaction. Subject at line 40 of `ShopEaseInventorySubject` never mentions JOptionPane — loose coupling."

---

## 3.10 Where Subject Is Created and Observers Attach

**[SHOW]** `code/com/shopease/service/ShopEaseService.java`

| Line(s) | What to Say |
|---------|-------------|
| **41** | "`inventorySystem` — our **ConcreteSubject** instance field." |
| **63** | "Constructor creates `new ShopEaseInventorySubject()`." |
| **90** | "Fresh Subject on each login — clean observer list." |
| **99** | "Customer login: attach `ShopEaseCartStockSyncObserver` — ConcreteObserver #7." |
| **103** | "`setupInventoryObservers()` — attach console log observer." |
| **145** | "Attach `ShopEaseInventoryAdminLogObserver` — always on login." |
| **149–152** | "`attachObserver` — UI registers extra ConcreteObservers." |
| **155–158** | "`detachObserver` — cleanup on logout." |
| **182** | "`publishEvent(ADMIN_LOGIN_STOCK, ...)` — login notification." |
| **204** | "`publishEvent(WISHLIST_RESTOCK, ...)`." |
| **218** | "`publishEvent(CART_REMINDER, ...)`." |
| **248** | "`publishEvent(DATA_CHANGED, ...)` after writes." |
| **270** | "`setStock` after checkout stock reduction — may fire LOW/OUT." |

**[SHOW]** `code/com/shopease/ui/ShopEaseApp.java`

| Line(s) | What to Say |
|---------|-------------|
| **83–84** | "Admin login: create `ShopEaseAdminLoginStockAlertObserver`, attach line 84." |
| **85** | "`publishAdminLowStockOnLogin()` — triggers notify." |
| **93–96** | "Customer login: attach cart reminder + wishlist observers." |
| **87, 100–101** | "Detach on logout — GoF Detach." |

**[SHOW]** `code/com/shopease/ui/AdminDashboard.java` lines **39–41**

**[SAY]**  
"Admin dashboard attaches `ShopEaseInventoryAdminAlertObserver` line 39–41 — real-time stock popup while admin works."

**[SHOW]** `code/com/shopease/ui/CartDialog.java` lines **46, 148**

**[SAY]**  
"Cart dialog attaches `ShopEaseDataChangeRefreshObserver` — refreshes cart when DATA_CHANGED fires."

---

## 3.11 Full Observer Call Chain (Admin Reduce Stock Demo)

**[DEMO]** Admin login → Inventory → Gaming Laptop → Reduce to 0 with remarks → debugger or explain:

```text
AdminInventoryPanel → reduceStockProduct()
  ... (Strategy pattern execution) ...
          inventorySubject.setStock(0, name)             ShopEaseInventorySubject line 31
            setSubjectState(OUT_OF_STOCK, name)          line 35
            notifyObservers()                            line 36  ◄── ConcreteSubject notifies
              Subject.notifyObservers()                    Subject line 29
                observer.update()                          Subject line 30
                  ... (inside Observer) ...
                      AdminAlertObserver                 shows JOptionPane popup
```

**[SAY while stepping]**

1. **Line 36** `ShopEaseInventorySubject` — "ConcreteSubject changed state to OUT OF STOCK, calls notify."
2. **Line 30** `Subject` — "Subject loops all attached observers and calls update() on each."
3. **Popup appears** — "The ConcreteObserver received the update and triggered the UI alert."

---

## 3.12 Observer — One Paragraph to Memorise

**[SAY]**  
"`Subject` line 28 `notifyObservers` loops observers and calls `update()` with no data. `ShopEaseInventorySubject` line 7 is the **ConcreteSubject**; line 40 is where stock changes trigger notify. `ShopEaseAbstractObserver` line 17 `subject.getState()` is the **pull**. Seven **ConcreteObservers** each filter events in `onStateChanged`. The Subject never imports Swing — only observers do. That is GoF Observer with loose coupling."

---

# HOW THE THREE PATTERNS WORK TOGETHER

## Integrated Demo Script (8 minutes)

| Step | Action | Pattern | Line to Mention |
|------|--------|---------|-----------------|
| 1 | Run `ShopEaseApp` | Singleton | `ShopEaseService:57` → DB `getInstance()` |
| 2 | Customer login | Singleton | `ShopEaseCartSingleton:24` → cart `getInstance(userId)` |
| 3 | Add to cart, checkout DuitNow | Strategy | `CartDialog:273`; `checkout:492–493` |
| 4 | Logout, admin login | Observer | `ShopEaseInventorySubject:36` |
| 5 | Reduce stock to 0 | Observer | `setStock:33–36` → `notifyObservers:36` |
| 6 | Stock alert popup | Observer | `Subject:30` |

**[SAY]**  
"Singleton ensures one database and one cart object. Strategy handles the dynamic checkout payment. Observer notifies the admin immediately when stock runs out. All three patterns work seamlessly together."

---

# LECTURER Q&A — QUICK ANSWERS WITH LINE PROOF

| Question | Answer with line proof |
|----------|------------------------|
| Where is the **Subject**? | Abstract: `Subject.java:14`. Concrete: `ShopEaseInventorySubject.java:7`. Runtime field: `ShopEaseService.java:41`. |
| Where is the **Observer** interface? | `Observer.java:8–9`. |
| Where is **notifyObservers**? | `Subject.java:28–31`. Called from `ShopEaseInventorySubject.java:24, 36, 40`. |
| Push or pull? | **Pull.** `ShopEaseAbstractObserver.java:17` — `getState()` inside `update()`. |
| Where is **ConcreteSubject**? | `ShopEaseInventorySubject.java:7`. |
| Name all **ConcreteObservers**. | Seven classes in §3.9; registry in `DesignPatterns.java:17–23`. |
| Where is **Strategy** interface? | Payment: `ShopEasePaymentStrategy.java:6`. Inventory: `ShopEaseInventoryActionStrategy.java:14`. |
| Where is **Context**? | Payment: `ShopEasePaymentContext.java:19`. Inventory: `ShopEaseInventoryActionContext.java:20`. |
| Where is **getInstance**? | DB: `ShopEaseDatabaseManager.java:24`. Cart: `ShopEaseCartSingleton.java:24`. Wishlist: `ShopEaseWishlistSingleton.java:24`. |
| Lazy or eager Singleton? | **Lazy** — `ShopEaseDatabaseManager.java:25` checks `instance == null`. |
| Singleton vs multiton? | DB = one global. Cart/Wishlist = `Map` at lines 14/16 — one per `userId`. |
| Strategy vs State? | Strategy = client **chooses** algorithm (payment dropdown). State = behavior changes with internal state — we don't use State. |
| Open/Closed example? | New payment = new class implementing `ShopEasePaymentStrategy`; `checkout` line 466 unchanged. |
| How to prove patterns at runtime? | Breakpoints: `getInstance:25`, `contextInterface:19`, `notifyObservers:29`, `getState:17`. |

---

# CLOSING (30 seconds)

**[SAY]**  
"To summarise: ShopEase implements three GoF patterns with explicit role names in code. **Singleton** — `ShopEaseDatabaseManager` line 24 `getInstance`, cart multiton line 96. **Strategy** — `algorithmInterface` and `contextInterface` at payment line 492–493 and inventory line 296–297. **Observer** — pull model at `ShopEaseAbstractObserver` line 17, seven ConcreteObservers, ConcreteSubject `setStock` line 40. All patterns are wired through `ShopEaseService` and demonstrated in the live Swing application. Thank you — we welcome questions."

---

# APPENDIX — Master GoF Role Map

## Singleton

| GoF Part | Location |
|----------|----------|
| Singleton class | `ShopEaseDatabaseManager:13`, `ShopEaseCartSingleton:13`, `ShopEaseWishlistSingleton:15` |
| static instance | `ShopEaseDatabaseManager:15`, `ShopEaseCartSingleton:14` |
| private constructor | `ShopEaseDatabaseManager:21`, `ShopEaseCartSingleton:18`, `ShopEaseWishlistSingleton:19` |
| getInstance() | `ShopEaseDatabaseManager:24`, `ShopEaseCartSingleton:24`, `ShopEaseWishlistSingleton:24` |
| Client usage | `ShopEaseService:96–97`, `DatabaseConnection:17` |

## Strategy (all families)

| GoF Part | Payment | Inventory |
|----------|---------|-----------|
| Strategy | `ShopEasePaymentStrategy:6` | `ShopEaseInventoryActionStrategy:14` |
| ConcreteStrategy | `ShopEaseDuitNowStrategy:6` etc. | `ReduceInventoryStrategy:9` etc. |
| Context | `ShopEasePaymentContext:19` | `ShopEaseInventoryActionContext:20` |
| Client | `CartDialog:334` | `ShopEaseService:281` |
| Runtime delegate | `ShopEaseService:492–493` | `ShopEaseService:296–297` |

## Observer

| GoF Part | Location |
|----------|----------|
| Subject | `Subject:14` |
| ConcreteSubject | `ShopEaseInventorySubject:7` |
| Observer | `Observer:8` |
| ConcreteObserver (×7) | See §3.9 |
| attach / detach | `Subject:17, 24` |
| notifyObservers | `Subject:28` |
| getState (pull) | `ShopEaseAbstractObserver:17` |
| State object | `SubjectState:7` |

---

*TT2L Group 5 — ShopEase E-Commerce System — Design Pattern Presentation Script*
