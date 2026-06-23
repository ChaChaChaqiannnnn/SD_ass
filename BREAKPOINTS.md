# ShopEase — Presentation Breakpoint Guide

## Setup
1. Run `./compile.sh` first.
2. **Run and Debug** (`Cmd+Shift+D` / `Ctrl+Shift+D`) → choose **ShopEase GUI (Mac/Windows)**.
3. Open each file below → click the gutter (left of line number) to create a solid red dot.
4. **CRITICAL:** Use the toolbar **▷ Continue (F5)** button, the **Step Over (F10)** button, or the **Step Into (F11)** button.
5. Make sure all breakpoints from old sessions are **UNCHECKED** when you start.

---

## All 9 Presentation Breakpoints

### PART 1 — Singleton (2 breakpoints)
**Goal:** Show that only one instance of `DatabaseManager` and `CartSingleton` is ever created.

| # | File | Line | Trigger |
|---|------|------|---------|
| 1 | `code/com/shopease/singleton/ShopEaseCartSingleton.java` | 24 | Customer login |
| 2 | `code/com/shopease/singleton/ShopEaseDatabaseManager.java` | 24 | F5 start app / Background DB processes |

**The Steps:**
1. Check **#1** and **#2**. Uncheck everything else.
2. **F5** — Launch the app.
3. Pauses at **#2**. Press **F10**, then **F5**. Console shows `New ShopEaseDatabaseManager instance created`.
4. Pauses at **#2** again. Press **F10**, then **F5**. Console shows `Returning existing...`.
5. Press **F5 repeatedly** until the login screen fully appears.
6. Log in as customer. Pauses at **#1**. Press **F10**, then **F5**. Console shows `New Cart instance created`.
7. **CRITICAL:** Uncheck **#2** (`DatabaseManager`) so it stops pausing.
8. Add an item to the cart, log out, and log back in as the same customer.
9. Pauses at **#1**. Press **F10**, then **F5**. Console shows `Returning existing cart instance`.
10. **CRITICAL:** Uncheck **#1** before continuing to Part 2!

---

### PART 2 — Strategy (5 breakpoints)
**Goal:** Show that the payment context dynamically switches to the chosen payment strategy.

| # | File | Line | Trigger |
|---|------|------|---------|
| 3 | `code/com/shopease/ui/CartDialog.java` | 273 | Customer checkout |
| 4 | `code/com/shopease/service/ShopEaseService.java` | 492 | Confirm Dialog (Yes) |
| 5 | `code/com/shopease/service/ShopEaseService.java` | 493 | F10 from line 492 |
| 6 | `code/com/shopease/strategy/ShopEasePaymentContext.java` | 20 | F11 from line 493 |
| 7 | `code/com/shopease/strategy/ShopEaseDuitNowStrategy.java` | 6 | F11 from line 20 |

**The Steps:**
1. Check **#3, #4, #5, #6, #7**. Uncheck everything else.
2. As the customer, go to **View Cart**. Select **DuitNow QR** and click **Pay & place order**.
3. Pauses at **#3**. Press **F5**.
4. Confirmation dialog appears. Click **Yes**.
5. Pauses at **#4**. Press **F10**. It moves to **#5**.
6. At **#5**: Press **F11 (Step Into)**. It jumps inside to **#6**.
   * *Explain: "We are stepping into the Context class that executes the strategy."*
7. At **#6**: Press **F11 (Step Into)**. It jumps directly to the strategy class (**#7**).
   * *Explain: "It dynamically routed the execution directly into the DuitNowStrategy based on the selection."*
8. Press **F5** to finish.

---

### PART 3 — Observer (2 breakpoints)
**Goal:** Show that changing the inventory state automatically notifies attached observers without tightly coupling them.

| # | File | Line | Trigger |
|---|------|------|---------|
| 8 | `code/com/shopease/observer/ShopEaseInventorySubject.java` | 36 | Admin reduces stock to 0 |
| 9 | `code/com/shopease/observer/Subject.java` | 30 | F5 from line 36 |

**The Steps:**
1. **UNCHECK ALL BREAKPOINTS.** Do not check the Observer breakpoints yet.
2. Log out of the customer account, and log in as **Admin**.
3. Go to the **Inventory** page. Select any product that has stock.
4. **CRITICAL:** NOW check **#8** and **#9**.
5. Click **Reduce** → reduce it by its **full stock** (so it reaches 0) → type remarks → click confirm.
6. Pauses at **#8**. (Console says `OUT OF STOCK`).
   * *Explain: "Subject state has changed, preparing to notify observers."*
7. Press **F5**. It jumps to **#9** (`observer.update();`).
   * *Explain: "Subject is now looping through its Observers and executing their update() methods."*
8. Press **F5** again. *(Note: If it stops at #9 again, just press F5 until the popup appears).*
9. The **"Stock alert" popup** appears on the screen!
   * *Explain: "The concrete Observer received the update() call and successfully triggered the UI alert!"*
