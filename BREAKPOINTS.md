# ShopEase — Breakpoint Reference Guide

## Setup

1. Run `./compile.sh` first.
2. **Run and Debug** (`Cmd+Shift+D`) → choose **ShopEase GUI (Mac/Windows)**.
3. Open each file below → click the gutter (left of line number) → solid red dot.
4. Press **F5** to start debug (not `./run-gui.sh`).
5. Open **Debug Console** for `[Memory]`, `[OBSERVER]`, and payment messages.
6. Enable **one pattern at a time** — uncheck other breakpoints in the **BREAKPOINTS** panel.

### Key Shortcuts
| Key | Action |
|-----|--------|
| F5  | Continue |
| F10 | Step Over |
| F11 | Step Into (use at `PaymentContext` line 20) |

---

## All 11 Breakpoints

### Pattern 1 — Singleton (2 breakpoints)

| # | File | Line | Click on this line | Trigger |
|---|------|------|--------------------|---------|
| 1 | `code/com/shopease/singleton/ShopEaseCartSingleton.java` | **24** | `public static synchronized ShopEaseCartSingleton getInstance(String userId) {` | Customer login (Sign Up or existing). Login again as same customer after adding item + logout. |
| 2 | `code/com/shopease/singleton/ShopEaseDatabaseManager.java` | **24** | `public static synchronized ShopEaseDatabaseManager getInstance() {` | F5 start app — hits immediately on startup (before login). Also hits on every DB access (browsing products, login, checkout, admin actions, ...). |

**Proves:** One cart per user ID; one DB manager for the entire app — both now use the same
create-or-reuse `if/else` shape, so you can step through line 24 with F10 and show your
teacher the exact branch: first call builds it, every call after just returns the same object.

**Expected console:**
```
First login:        [Memory] New Cart instance created for user: ...
Second login:        [Memory] Returning existing cart instance for user: ...

First DB call ever:  [Memory] New ShopEaseDatabaseManager instance created.
Every DB call after: [Memory] Returning existing ShopEaseDatabaseManager instance.
```

---

### Pattern 2 — Strategy (6 breakpoints)

| # | File | Line | Click on this line | Trigger |
|---|------|------|--------------------|---------|
| 3 | `code/com/shopease/ui/CartDialog.java` | **273** | `final ShopEasePaymentStrategy strategy = paymentStrategyFor(paymentName);` | Customer → Cart → select **DuitNow QR** → click **Pay & place order**. *(Fires immediately, before the Yes/No confirm dialog — selection now happens up front in `doCheckout()`.)* |
| 4 | `code/com/shopease/service/ShopEaseService.java` | **492** | `ShopEasePaymentContext context = new ShopEasePaymentContext(strategy);` | Click **Yes** on the confirm dialog (runs in a background `SwingWorker` thread) |
| 5 | `code/com/shopease/service/ShopEaseService.java` | **493** | `context.contextInterface(total);` | F10 from line 492 |
| 6 | `code/com/shopease/strategy/ShopEasePaymentContext.java` | **20** | `strategy.algorithmInterface(amount);` | F10 from line 493 → then F11 to step into strategy |
| 7 | `code/com/shopease/strategy/ShopEaseDuitNowStrategy.java` | **6** | `public void algorithmInterface(double amount) {` | Lands here after F11 from #6 |
| 8 *(bonus)* | `code/com/shopease/strategy/ReduceInventoryStrategy.java` | **9** | `public boolean algorithmInterface(InventoryActionRequest request, InventoryActionResult result) {` | Admin login → Inventory → Reduce (amount + remarks required) |

**Proves:** Payment and inventory use interchangeable strategy classes (`algorithmInterface`) via `ShopEasePaymentContext` / `ShopEaseInventoryActionContext`.

**Expected console (DuitNow):**
```
Processing Payment: RMxx.xx via DuitNow QR.
```

---

### Pattern 3 — Observer (4 breakpoints)

> **Note:** the Subject/Observer base classes were refactored into a template-method style (`Subject`, `Observer`, `ShopEaseAbstractObserver`). The notify loop now lives in `Subject.java`, not in `ShopEaseInventorySubject.java`.

| # | File | Line | Click on this line | Trigger |
|---|------|------|--------------------|---------|
| 11 | `code/com/shopease/service/ShopEaseService.java` | **99** | `inventorySystem.attach(new ShopEaseCartStockSyncObserver(userCart));` | Customer login |
| 9  | `code/com/shopease/observer/ShopEaseInventorySubject.java` | **36** | `notifyObservers();` *(inside the `qty == 0` branch of `setStock`)* | Admin → Inventory → Reduce to 0 (+ remarks) |
| 10 | `code/com/shopease/observer/Subject.java` | **29** | `for (Observer observer : observers) {` | F10 from line 36 above |
| 12 | `code/com/shopease/observer/ShopEaseCartStockSyncObserver.java` | **18** | `protected void onStateChanged(SubjectState state) {` | *(See important note below)* |

**Proves:** Subject notifies observers on `OUT_OF_STOCK`; cart sync removes invalid items.

**Expected console:**
```
[Inventory] Gaming Laptop is now OUT OF STOCK.
[ADMIN ALERT] Gaming Laptop requires immediate attention!     (from ShopEaseInventoryAdminLogObserver)
[OBSERVER] SYSTEM ALERT: Gaming Laptop is now out of stock!   (from ShopEaseCartStockSyncObserver)
[OBSERVER] Auto-removed 'Gaming Laptop' from your cart to prevent checkout errors.
```

> **⚠️ Important — Breakpoint #12 (cart sync observer)**
>
> If you logout customer → admin reduce, breakpoint #12 often will **NOT** hit. `ShopEaseService.logout()` replaces `inventorySystem` with a brand-new `ShopEaseInventorySubject()`, so the customer's observer is no longer attached to anything. That is OK:
> - **#9 and #10** still prove Observer (subject + notify loop).
> - Cart cleanup after re-login is handled by `syncCartWithDatabase()` in `ShopEaseService`.
> - To hit #12 in the **same session**: customer must still be logged in when `OUT_OF_STOCK` fires (e.g. checkout that depletes stock to 0 while customer is active).

---

## Demo Order

### Part 1 — Singleton
1. Enable only **#1** → customer login twice (add Wireless Mouse between logins).
2. Enable only **#2** → F5 restart → pause on startup → F5 to login screen.

### Part 2 — Strategy
1. Enable **#3, #4, #5, #6, #7** → customer checkout with **DuitNow QR**.
2. *(Optional)* Enable **#8** → admin reduce stock.

### Part 3 — Observer
1. Enable **#11** → customer login → F5.
2. Add **Gaming Laptop** to cart → do **not** checkout.
3. Enable **#9, #10** (disable #11 if cluttered).
4. Logout → admin login → reduce that product to **0** with remarks.
5. F10 through line 36 (`ShopEaseInventorySubject`) → line 29 (`Subject`).
6. Logout → same customer login → open Cart → laptop should be gone.

---

## 5-Minute Version (Minimum Breakpoints)

| Pattern  | File | Line | Action |
|----------|------|------|--------|
| Singleton | `ShopEaseCartSingleton.java` | 24 | Customer login twice |
| Strategy  | `ShopEaseService.java` | 492–493 | DuitNow checkout |
| Strategy  | `ShopEasePaymentContext.java` | 20 | F11 into `algorithmInterface` |
| Strategy  | `ShopEaseDuitNowStrategy.java` | 6 | Concrete strategy lands |
| Observer  | `ShopEaseInventorySubject.java` | 36 | Admin reduce to 0 |

---

## Troubleshooting

| Check | Fix |
|-------|-----|
| Grey dot (unverified breakpoint) | Run `./compile.sh` then Shift+F5 → F5 again |
| No pause | Must use **F5 Debug**, not Terminal run |
| Wrong role | Cart = customer; reduce = admin |
| Reduce fails | Remarks field is mandatory |
| Too many breakpoints | Enable only one pattern's breakpoints at a time |

---

## Quick Reference — File Paths

```
code/com/shopease/singleton/ShopEaseCartSingleton.java
code/com/shopease/singleton/ShopEaseDatabaseManager.java
code/com/shopease/ui/CartDialog.java
code/com/shopease/service/ShopEaseService.java
code/com/shopease/strategy/ShopEasePaymentContext.java
code/com/shopease/strategy/ShopEaseDuitNowStrategy.java
code/com/shopease/strategy/ReduceInventoryStrategy.java
code/com/shopease/observer/ShopEaseInventorySubject.java
code/com/shopease/observer/Subject.java
code/com/shopease/observer/ShopEaseCartStockSyncObserver.java
```

> **Optional cheat sheet during demo:** `code/com/shopease/DesignPatterns.java` — lists all pattern class names for Q&A.
