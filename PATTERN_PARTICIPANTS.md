# ShopEase — GoF Pattern Participants

Companion to `BREAKPOINTS.md`. That file tells you *where to pause*; this file tells you
*what role each class is playing* — the GoF (Gang of Four) participant responsibilities,
and exactly which ShopEase method fulfills each one.

---

## 1. Singleton Pattern

### GoF participant: **Singleton**

> *Responsibility (GoF):* "Defines an `Instance` operation that lets clients access its
> unique instance. `Instance` is a class operation (i.e., a class method in Java). May be
> responsible for creating its own unique instance."

There is only one participant role in classic Singleton — the class is both the product
and the thing that controls its own construction. ShopEase has **three** classes playing
this role:

| Class | File |
|---|---|
| `ShopEaseDatabaseManager` | `code/com/shopease/singleton/ShopEaseDatabaseManager.java` |
| `ShopEaseCartSingleton` | `code/com/shopease/singleton/ShopEaseCartSingleton.java` |
| `ShopEaseWishlistSingleton` | `code/com/shopease/singleton/ShopEaseWishlistSingleton.java` |

### `ShopEaseDatabaseManager` — textbook (one instance, period)

- **Private constructor** (`private ShopEaseDatabaseManager() {}`) — blocks `new` from
  outside the class. This is what makes "uncontrolled instantiation" impossible.
- **Static field holds the one instance, created lazily**: `private static ShopEaseDatabaseManager instance;`
  starts out `null` — nothing is built until the first request.
- **Global access point with a visible create-or-reuse check**:
  ```java
  public static synchronized ShopEaseDatabaseManager getInstance() {
      if (instance == null) {
          instance = new ShopEaseDatabaseManager();
          System.out.println("[Memory] New ShopEaseDatabaseManager instance created.");
      } else {
          System.out.println("[Memory] Returning existing ShopEaseDatabaseManager instance.");
      }
      return instance;
  }
  ```
  Same shape as `ShopEaseCartSingleton.getInstance` — first caller builds it (and prints so),
  every caller after that just gets the same reference back (and prints that too). `synchronized`
  matters more here than on the cart: `getConnection()` is called from background `SwingWorker`
  threads (checkout, admin actions) as well as the EDT, so two threads racing to build the first
  instance has to be prevented explicitly — `synchronized` makes that impossible.
- **Why a Singleton here**: every DAO (`UserDAO`, `ProductDAO`, `OrderDAO`, ...) needs a
  SQLite `Connection`. If each DAO opened its own, you'd risk conflicting schema
  initialization and duplicate `PRAGMA` settings. One manager = one source of truth for
  `getConnection()` and `initializeDatabase()`.

### `ShopEaseCartSingleton` / `ShopEaseWishlistSingleton` — keyed variant (Singleton **per user**)

These extend the pattern: instead of one instance for the *entire app*, GoF's "unique
instance" constraint is scoped to "unique instance **per `userId`**":

- **Private constructor** — same idea, `new ShopEaseCartSingleton(userId)` can't be called
  from outside.
- **Static registry instead of a single field**: `private static final Map<String, ShopEaseCartSingleton> instances = new HashMap<>();`
  — this *is* still Singleton, just keyed. The invariant GoF cares about ("at most one
  instance reachable through the access point, for a given identity") still holds: call
  `getInstance("user-42")` a hundred times, you get the same object back every time.
- **Global access point with lazy creation**:
  - `ShopEaseCartSingleton.getInstance(String userId)` — checks `instances.containsKey(userId)`;
    creates-and-stores on first call, returns the cached one otherwise. Prints
    `[Memory] New Cart instance created...` vs. `[Memory] Returning existing cart instance...`
    so you can literally see the branch taken (this is breakpoint #1 in BREAKPOINTS.md).
  - `ShopEaseWishlistSingleton.getInstance(String userId)` does the same thing more tersely
    with `instances.computeIfAbsent(userId, ShopEaseWishlistSingleton::new)`.
- **Why per-user instead of one global instance**: a literal single cart for the whole
  app would mean every logged-in customer shares one shopping cart — wrong by design.
  Scoping the "uniqueness" to the user ID is what makes "one cart **per customer**,
  reused across that customer's session" true, which is the actual business rule.

---

## 2. Strategy Pattern

ShopEase has **two independent Strategy families** sharing the same shape: payment
methods, and admin inventory actions.

### GoF participants

| Role | GoF Responsibility |
|---|---|
| **Strategy** | Declares an interface common to all supported algorithms. `Context` uses this interface to call the algorithm defined by a `ConcreteStrategy`. |
| **ConcreteStrategy** | Implements the algorithm using the `Strategy` interface. |
| **Context** | Configured with a `ConcreteStrategy` object; maintains a reference to a `Strategy` object; may define an interface that lets `Strategy` access its data; forwards client requests to its strategy. |

### Family A — Payment

| Role | Class | File |
|---|---|---|
| Strategy | `ShopEasePaymentStrategy` | `code/com/shopease/strategy/ShopEasePaymentStrategy.java` |
| ConcreteStrategy | `ShopEaseCreditCardStrategy`, `ShopEaseDuitNowStrategy`, `ShopEaseMAEStrategy`, `ShopEaseTNGStrategy` | `code/com/shopease/strategy/*.java` |
| Context | `ShopEasePaymentContext` | `code/com/shopease/strategy/ShopEasePaymentContext.java` |

- **Strategy** — `ShopEasePaymentStrategy` is a one-method interface:
  `void algorithmInterface(double amount);` That single method *is* the entire contract
  every payment method must satisfy. Nothing about "how" payment happens leaks into
  the interface — only "given an amount, do it."
- **ConcreteStrategy** — each of the four classes implements `algorithmInterface` with
  its own (simulated) behavior, e.g. `ShopEaseDuitNowStrategy`:
  ```java
  public void algorithmInterface(double amount) {
      System.out.println("Processing Payment: RM" + ... + " via DuitNow QR.");
  }
  ```
  `ShopEaseCreditCardStrategy`, `ShopEaseMAEStrategy`, `ShopEaseTNGStrategy` are identical
  in shape, different in the printed method name — this interchangeability is the whole
  point: `ShopEasePaymentContext` never needs to know which one it's holding.
- **Context** — `ShopEasePaymentContext`:
  - Holds the reference: `private ShopEasePaymentStrategy strategy;`
  - Gets configured via constructor (`ShopEasePaymentContext(ShopEasePaymentStrategy strategy)`)
    or `setStrategy(...)`.
  - Forwards the client's request: `public void contextInterface(double amount) { strategy.algorithmInterface(amount); }`
    — it does no payment logic itself; it's pure delegation.
- **Who plays the client?** `CartDialog.paymentStrategyFor(String paymentName)` (a
  `switch` on the dropdown value) picks the `ConcreteStrategy`; `ShopEaseService.checkout(ShopEasePaymentStrategy strategy)`
  builds the `Context` around it and calls `contextInterface(total)`. The client (UI)
  decides *which* algorithm; the `Context` (service layer) decides *when* to run it.

### Family B — Admin Inventory Actions

| Role | Class | File |
|---|---|---|
| Strategy | `ShopEaseInventoryActionStrategy` | `code/com/shopease/strategy/ShopEaseInventoryActionStrategy.java` |
| ConcreteStrategy | `RestockInventoryStrategy`, `ReduceInventoryStrategy`, `UndoInventoryStrategy` | `code/com/shopease/strategy/*.java` |
| Context | `ShopEaseInventoryActionContext` | `code/com/shopease/strategy/ShopEaseInventoryActionContext.java` |

- **Strategy** — `ShopEaseInventoryActionStrategy` declares
  `boolean algorithmInterface(InventoryActionRequest request, InventoryActionResult result);`
  This is a richer signature than the payment family because inventory actions need more
  inputs (product ID, amount, remarks, current user, the last undoable action) and richer
  outputs (success message, the log entry created, whether an undo was consumed). The
  interface solves this with two **nested parameter/result objects** rather than a single
  primitive — same GoF role, just a fatter contract:
  - `InventoryActionRequest` — read-only bundle of everything a strategy might need
    (`productDAO`, `adminActivityDAO`, `inventorySubject`, `currentUser`, `productId`,
    `amount`, `remarks`, `lastUndoableAction`).
  - `InventoryActionResult` — mutable out-parameter (`message`, `newLog`, `clearedUndoAction`)
    the strategy fills in before returning.
- **ConcreteStrategy**:
  - `RestockInventoryStrategy.algorithmInterface` — delegates to a shared static helper
    `adjustStock(request, result, +amount, "RESTOCK", "")`.
  - `ReduceInventoryStrategy.algorithmInterface` — validates remarks are non-blank and
    amount > 0 first (this is the "remarks mandatory" rule from BREAKPOINTS.md), then
    calls the same `adjustStock` helper with a **negative** quantity and `"REDUCE"`.
  - `UndoInventoryStrategy.algorithmInterface` — doesn't touch `adjustStock` at all; it
    reverses `request.lastUndoableAction` directly, restoring the prior stock level and
    writing an `"UNDO"` log entry.
  - All three call `request.inventorySubject.setStock(...)` somewhere in their path —
    this is the seam where Strategy hands off to the **Observer** pattern (see below):
    changing stock is what triggers `LOW_STOCK` / `OUT_OF_STOCK` notifications.
- **Context** — `ShopEaseInventoryActionContext` is the same shape as the payment
  context: holds `strategy`, `contextInterface(request, result)` delegates to
  `strategy.algorithmInterface(request, result)` and returns its boolean.
- **Who plays the client?** `ShopEaseService.runInventoryAction(strategy, productId, amount, remarks)`
  builds the request/result objects, wraps the chosen strategy in a `Context`, and calls
  `contextInterface`. `reduceStockProduct(...)` and `restockProduct(...)` are the public
  entry points that pick *which* concrete strategy to pass in.

---

## 3. Observer Pattern

### GoF participants

| Role | GoF Responsibility |
|---|---|
| **Subject** | Knows its observers (any number may observe a subject). Provides an interface for attaching and detaching `Observer` objects. |
| **ConcreteSubject** | Stores state of interest to `ConcreteObserver` objects. Sends a notification to its observers when its state changes. |
| **Observer** | Defines an updating interface for objects that should be notified of changes in a subject. |
| **ConcreteObserver** | Maintains a reference to a `ConcreteSubject` object. Stores state that should stay consistent with the subject's. Implements the `Observer` updating interface to keep its state consistent with the subject's. |

| Role | Class | File |
|---|---|---|
| Subject | `Subject` (abstract) | `code/com/shopease/observer/Subject.java` |
| ConcreteSubject | `ShopEaseInventorySubject` | `code/com/shopease/observer/ShopEaseInventorySubject.java` |
| Observer | `Observer` (interface) | `code/com/shopease/observer/Observer.java` |
| ConcreteObserver | 7 classes — see table below | `code/com/shopease/observer/*.java` |

### Subject — `Subject.java`

- **Knows its observers**: `private final List<Observer> observers = new ArrayList<>();`
- **Attach/detach interface**:
  ```java
  public void attach(Observer observer) { ...; observers.add(observer); }
  public void detach(Observer observer) { observers.remove(observer); }
  ```
  `attach` has one extra responsibility beyond plain GoF: if the observer being attached
  is a `ShopEaseAbstractObserver`, it calls `abstractObserver.bindSubject(this)` so that
  observer can later pull state back from *this* subject (see ConcreteObserver below).
- **Notification loop**: `protected void notifyObservers() { for (Observer o : observers) o.update(); }`
  — note this lives in the **base class**, shared by every subject in the app (there's
  currently only one `ConcreteSubject`, but the loop itself doesn't know that).
- **`getState()` is abstract** — GoF's Subject doesn't mandate a specific state-access
  method, but this codebase standardizes one (`abstract SubjectState getState();`) so
  every `ConcreteObserver` can pull state the same way regardless of which subject it's
  attached to.

### ConcreteSubject — `ShopEaseInventorySubject.java`

- **Stores the state of interest**: `private SubjectState subjectState = SubjectState.empty();`
  plus `private int stock = 0;`. `SubjectState` is a small immutable value object
  (`event`, `productName`, `stockQuantity`) — the "interesting state" GoF refers to.
- **Returns it on demand**: `getState()` just returns `subjectState` — this is what
  observers read inside their `update()`.
- **Decides when state changes warrant notification** — this is the most important
  ConcreteSubject responsibility, and it's concentrated in `setStock(int qty, String productName)`:
  ```java
  if (qty == 0) {
      setSubjectState(ShopEaseAppEvents.OUT_OF_STOCK, productName);
      notifyObservers();
  } else if (qty < LOW_STOCK_THRESHOLD) {
      setSubjectState(ShopEaseAppEvents.LOW_STOCK, productName);
      notifyObservers();
  }
  ```
  i.e. not every stock write triggers a notification — only crossing into "low" or "out"
  does. There's also a generic escape hatch, `publishEvent(String event, String detail)`,
  used for events that aren't about stock at all (`CART_REMINDER`, `WISHLIST_RESTOCK`,
  `ADMIN_LOGIN_STOCK`, `DATA_CHANGED`) — same Subject, reused as a general event bus.

### Observer — `Observer.java`

- One method, no arguments: `void update();` GoF allows `update()` to optionally carry
  the changed state as parameters; this codebase deliberately keeps it parameterless and
  pushes state-retrieval onto the observer instead (`observer pulls`, doesn't get state
  pushed) — that pull happens through the next class.

### ConcreteObserver — shared base + 7 concrete classes

GoF's ConcreteObserver responsibilities ("hold a reference to the subject" + "implement
`update()` to stay consistent with it") are split here into a **template method** base
class plus the seven leaves that actually react:

- **`ShopEaseAbstractObserver`** (abstract) — does the GoF-mandated bookkeeping once for
  everybody:
  ```java
  protected Subject subject;
  void bindSubject(Subject subject) { this.subject = subject; }   // called by Subject.attach()
  public void update() { if (subject != null) onStateChanged(subject.getState()); }
  protected abstract void onStateChanged(SubjectState state);
  ```
  So "maintain a reference to the subject" = `bindSubject`; "implement `update()`" is
  done *once* here and simply forwards to `onStateChanged`, which is where each concrete
  observer's real logic lives.

| ConcreteObserver | Reacts to event | What it does |
|---|---|---|
| `ShopEaseCartStockSyncObserver` | `OUT_OF_STOCK` | Removes the matching item from **that specific customer's** cart (`ShopEaseCartSingleton`) so they can't check out with something no longer available. Bridges Observer → Singleton. |
| `ShopEaseInventoryAdminLogObserver` | `OUT_OF_STOCK`, `LOW_STOCK` | Prints `[ADMIN ALERT] ...` to the console — the always-on audit trail attached in `setupInventoryObservers()`. |
| `ShopEaseInventoryAdminAlertObserver` | `OUT_OF_STOCK`, `LOW_STOCK` | Pops a `JOptionPane` warning/error dialog in the admin's open window. |
| `ShopEaseAdminLoginStockAlertObserver` | `ADMIN_LOGIN_STOCK` | Shows a one-time summary popup of every low/out-of-stock product when an admin logs in. |
| `ShopEaseCustomerCartReminderObserver` | `CART_REMINDER` | Nudges a returning customer that they left items in their cart. |
| `ShopEaseCustomerWishlistRestockObserver` | `WISHLIST_RESTOCK` | Tells a customer their wishlisted item is back in stock. |
| `ShopEaseDataChangeRefreshObserver` | `DATA_CHANGED` | No popup — just re-runs a `Runnable` to repaint whatever UI screen is currently open. |

Every one of these starts `onStateChanged` with an early-return guard
(`if (!EXPECTED_EVENT.equals(state.getEvent())) return;`) — because all observers are
attached to the *same* subject and the *same* `update()` fires for every event type, each
observer is responsible for filtering down to the one event it actually cares about.

### Supporting types (not GoF roles, but load-bearing)

- **`SubjectState`** — the value object carried between Subject and Observer: `event`,
  `productName`, `stockQuantity`. Immutable, with a static `empty()` default so
  `ShopEaseInventorySubject` always has *something* to return from `getState()` even
  before any event has fired.
- **`ShopEaseAppEvents`** — `public static final String` constants (`OUT_OF_STOCK`,
  `LOW_STOCK`, `CART_REMINDER`, `WISHLIST_RESTOCK`, `ADMIN_LOGIN_STOCK`, `DATA_CHANGED`).
  Acts as the shared vocabulary both `ConcreteSubject` (when calling `setSubjectState`/
  `publishEvent`) and every `ConcreteObserver` (when filtering in `onStateChanged`) agree
  on, instead of hardcoding string literals everywhere.

---

## How the three patterns interlock (one concrete path)

A customer checking out the last unit of a product touches all three patterns in one call
stack:

1. **Singleton** — `ShopEaseCartSingleton.getInstance(userId)` is the cart being checked out.
2. **Strategy** — `ShopEaseService.checkout(strategy)` wraps the UI-selected
   `ConcreteStrategy` (e.g. `ShopEaseDuitNowStrategy`) in a `ShopEasePaymentContext` and
   runs it to "pay."
3. **Strategy → Observer handoff** — checkout then calls `updateProductStock(...)` →
   `ShopEaseInventorySubject.setStock(0, productName)`, which is a `ConcreteSubject`
   detecting `OUT_OF_STOCK` and calling `notifyObservers()`.
4. **Observer → Singleton handoff** — `ShopEaseCartStockSyncObserver` (a `ConcreteObserver`
   bound to that same customer's cart) reacts by removing the now-unavailable item from
   the very `ShopEaseCartSingleton` instance from step 1.

That loop (Singleton holds per-user state → Strategy executes the business action →
Observer keeps everything else in sync) is the actual architectural reason all three
patterns exist together here, not just three unrelated demo classes.
