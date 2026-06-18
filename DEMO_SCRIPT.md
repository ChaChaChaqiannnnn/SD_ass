# ShopEase — Breakpoint Demo Script

A presenter's script for live-demoing the three GoF patterns via the debugger.
Pairs with `BREAKPOINTS.md` (exact file/line reference) and `PATTERN_PARTICIPANTS.md`
(role definitions) — this file is the thing you actually read from while presenting.

Each breakpoint below has three parts:
- **DO** — the exact click/action to perform
- **SAY** — words to speak to the audience (read it, paraphrase it, whatever's natural)
- **EXPECT** — what should appear on screen / in the Debug Console, so you know it worked

---

## Before you go live (2-minute prep, do this off-camera)

1. Run `./compile.sh`.
2. Open **Run and Debug**, confirm **"ShopEase GUI (Mac/Windows)"** is selected.
3. Set **all 12 breakpoints now** (see table at the bottom of this file), then go to the
   **BREAKPOINTS** panel and **uncheck everything except the 2 Singleton ones**. You'll
   check the next group's boxes live, on camera, right before you need them — that's part
   of the demo, not a setup step to hide.
4. Know your seed data: **Gaming Laptop** starts at stock **5**, **Wireless Mouse** at
   stock **20**. Admin login is `admin@email.admin.my` / `adminpass`.
5. Decide your customer test account ahead of time (sign up `demo@customer.my` or reuse
   one) so you're not fumbling with the registration form on camera.

---

## Opening line

> "ShopEase uses three Gang of Four design patterns — Singleton, Strategy, and Observer.
> Rather than just point at the code, I'm going to pause real execution at the exact line
> where each pattern does its job, so you can see it actually run, not just read about it."

---

## Part 1 — Singleton (2 breakpoints)

**DO:** In the BREAKPOINTS panel, confirm only these two are checked:
- `ShopEaseCartSingleton.java:24`
- `ShopEaseDatabaseManager.java:24`

**SAY:**
> "Singleton guarantees a class hands out the same instance every time it's asked,
> instead of letting callers create new ones freely. ShopEase uses it twice: once for the
> database connection manager — one for the whole app — and once for each customer's
> shopping cart, scoped per user ID."

### Breakpoint #2 — `ShopEaseDatabaseManager.getInstance()`

**DO:** Press **F5** to launch.

**EXPECT:** Pauses almost immediately, before the login screen even renders.

**SAY:**
> "We haven't even logged in yet, and we're already paused inside
> `ShopEaseDatabaseManager.getInstance()`. The app needs a database connection to check
> for existing users before it can show the login screen — and this method is the single
> gateway every part of the app goes through to get one."

**DO:** Press **F5** repeatedly until the login screen actually appears (this method gets
hit a few more times during startup — that's expected, just keep going).

### Breakpoint #1 — `ShopEaseCartSingleton.getInstance(userId)`

**DO:** Log in as your customer account.

**EXPECT:** Pauses at line 24. Debug Console shows:
```
[Memory] New Cart instance created for user: ...
```

**SAY:**
> "First login for this user — no cart exists yet, so the singleton registry creates one
> and prints 'New Cart instance created.' Watch what happens when I log out and log back
> in as the *same* user."

**DO:** Press **F5**, add **Wireless Mouse** to the cart, then **log out**, then **log
back in as the same customer**.

**EXPECT:** Pauses at line 24 again. Console now shows:
```
[Memory] Returning existing cart instance for user: ...
```

**SAY:**
> "Same line, same method — but this time it returned the *existing* cart instance
> instead of creating a new one. That's the proof: one cart per user ID, reused for the
> life of that user's session, not recreated on every login."

---

## Part 2 — Strategy (6 breakpoints + 1 bonus)

**DO:** Uncheck the two Singleton breakpoints. Check these five:
- `CartDialog.java:273`
- `ShopEaseService.java:492`
- `ShopEaseService.java:493`
- `ShopEasePaymentContext.java:20`
- `ShopEaseDuitNowStrategy.java:6`

**SAY:**
> "Strategy lets you swap out an algorithm at runtime without the calling code knowing
> which one it's using. ShopEase has four payment methods — Credit Card, DuitNow, MAE,
> TNG — all implementing the same one-method interface. The checkout code calls that
> interface exactly once; it never has an if/else chain checking which payment type it
> got."

**DO:** Restart (Shift+F5, then F5). Log in as the customer, add an item to the cart, open
**View Cart**, select **DuitNow QR** from the payment dropdown, click **Pay & place
order**.

### Breakpoint #3 — `CartDialog` selects the ConcreteStrategy

**EXPECT:** Pauses at line 273, *before* the Yes/No confirm dialog appears.

**SAY:**
> "This is the client picking the concrete strategy. `paymentStrategyFor('DuitNow QR')`
> just ran a switch statement and handed back a `ShopEaseDuitNowStrategy` object — but
> from this point on, the rest of the app only ever calls it through the generic
> `ShopEasePaymentStrategy` interface."

**DO:** Press **F5**. Click **Yes** on the confirm dialog that appears in the app.

### Breakpoints #4 and #5 — `ShopEaseService` builds and runs the Context

**EXPECT:** Pauses at line 492.

**SAY:**
> "We're inside the service layer now, on a background thread so the UI doesn't freeze.
> This line wraps whatever strategy the UI picked inside a `ShopEasePaymentContext`. The
> context doesn't know or care that it's DuitNow — it just holds a reference to *a*
> `ShopEasePaymentStrategy`."

**DO:** Press **F10** (step over) to land on line 493.

**SAY:**
> "And here's the context being told to run it: `context.contextInterface(total)`. Let's
> step inside and see what that actually does."

### Breakpoint #6 — inside `ShopEasePaymentContext`

**DO:** Press **F11** (step into).

**EXPECT:** Lands on line 20 of `ShopEasePaymentContext.java`: `strategy.algorithmInterface(amount);`

**SAY:**
> "This is the entire Context class's job in one line — delegate to whatever strategy
> it's holding. No payment logic lives here at all. Let's step into the strategy itself."

### Breakpoint #7 — the ConcreteStrategy actually runs

**DO:** Press **F11** again.

**EXPECT:** Lands on line 6 of `ShopEaseDuitNowStrategy.java`. Press **F5** to let it finish.

**SAY:**
> "And this is the concrete strategy — the actual DuitNow-specific behavior. If I'd
> picked MAE or TNG from the dropdown instead, we'd have landed in a completely different
> class right here, with zero changes to `ShopEaseService` or `ShopEasePaymentContext`.
> That interchangeability is the whole point of Strategy."

**EXPECT:** Console shows:
```
Processing Payment: RMxx.xx via DuitNow QR.
```
Receipt dialog appears in the app.

### Bonus breakpoint #8 — Strategy reused for a second purpose

**DO:** Check `ReduceInventoryStrategy.java:9` in the BREAKPOINTS panel. Log out, log in
as **admin**, go to **Inventory**, pick a product, choose **Reduce**, enter an amount and
**remarks** (mandatory), confirm.

**EXPECT:** Pauses at line 9.

**SAY:**
> "This is a *second*, completely unrelated Strategy family — admin inventory actions:
> restock, reduce, undo. Same shape: one interface, one `algorithmInterface` method,
> swappable concrete implementations, run through a `ShopEaseInventoryActionContext`.
> Strategy isn't a one-off here, it's a reusable pattern for 'pick one of several
> interchangeable algorithms at runtime,' applied twice in this codebase."

**DO:** Press **F10** a couple of times to show the remarks/amount validation, then **F5**.

---

## Part 3 — Observer (4 breakpoints)

**DO:** Uncheck the Strategy breakpoints. Check just one for now:
- `ShopEaseService.java:99`

**SAY:**
> "Observer lets one object — the Subject — broadcast 'something changed' to any number
> of listeners, without those listeners being hardcoded into it. ShopEase's inventory
> system is the Subject; things like 'auto-remove from cart' and 'show the admin a popup'
> are Observers attached to it."

**DO:** Restart debugging. Log in as the customer.

### Breakpoint #11 — an observer gets attached

**EXPECT:** Pauses at line 99.

**SAY:**
> "The moment this customer logs in, we register a `ShopEaseCartStockSyncObserver` — bound
> to *their* cart specifically — onto the inventory subject. From now on, if any product
> goes out of stock, this observer will hear about it and can clean their cart."

**DO:** Press **F5**. Add **Gaming Laptop** to the cart. **Do not check out.**

**DO:** Uncheck `ShopEaseService.java:99`. Check:
- `ShopEaseInventorySubject.java:36`
- `Subject.java:29`

### Breakpoints #9 and #10 — the Subject notifies

**DO:** Log out, log in as **admin**, go to **Inventory**, select **Gaming Laptop**,
**Reduce** by **5** (its full stock), fill in remarks, confirm.

**EXPECT:** Pauses at line 36 of `ShopEaseInventorySubject.java`. Console already shows:
```
[Inventory] Gaming Laptop is now OUT OF STOCK.
```

**SAY:**
> "Stock just hit zero. The ConcreteSubject — `ShopEaseInventorySubject` — decided this
> crosses the 'out of stock' threshold, and is about to call `notifyObservers()`. Let's
> step into that."

**DO:** Press **F10**.

**EXPECT:** Pauses at line 29 of `Subject.java`.

**SAY:**
> "This is the generic notify loop, and notice it lives in the base `Subject` class, not
> in the inventory-specific subclass — every observer attached gets `update()` called in
> turn, with no idea in advance how many or which ones exist."

**DO:** Press **F10** a few times to step through the loop. Point out the console line as
it appears:
```
[ADMIN ALERT] Gaming Laptop requires immediate attention!
```

**SAY:**
> "That's `ShopEaseInventoryAdminLogObserver` reacting — one of seven different observer
> classes attached to this same subject, each one only caring about specific events."

**DO:** Press **F5** to finish.

### Breakpoint #12 — the customer-side observer (needs a different trigger)

**SAY:**
> "Now here's a subtlety. We just logged the customer out before the admin reduced stock
> — which means their personal `ShopEaseCartStockSyncObserver` from breakpoint #11 was
> never around to hear this notification, because logging out replaced the subject
> entirely. To actually see *that* observer fire, the customer has to deplete the stock
> *themselves*, while still logged in."

**DO:** Check `ShopEaseCartStockSyncObserver.java:18`. Log in as the customer again, add
all remaining units of some product to the cart (enough to bring stock to exactly 0),
check out with any payment method, confirm Yes.

**EXPECT:** Pauses at line 18 inside `ShopEaseCartStockSyncObserver`. Console shows:
```
[OBSERVER] SYSTEM ALERT: ... is now out of stock!
```

**SAY:**
> "This time the customer's own checkout triggered `OUT_OF_STOCK` while their observer
> was still attached, so it fired. Their cart was already cleared by the checkout itself
> before this ran, so you won't see the 'auto-removed' line this time — but the important
> part is this observer reacting *at all*, proving it really is bound to this specific
> customer's session."

**DO:** Press **F5** to finish.

---

## Closing line

> "To tie it together: Singleton kept each customer's cart and the database connection
> unique and reusable. Strategy let checkout run whichever payment method — or whichever
> admin inventory action — was selected, without branching logic. And Observer kept the
> rest of the app in sync whenever stock changed, without the inventory subject needing
> to know who was listening. These aren't three isolated examples — in the checkout flow
> we just watched, all three patterns ran in a single call stack: Strategy executed the
> payment, that triggered Observer's stock notification, and Observer reached back into
> that customer's Singleton cart to clean it up."

---

## Quick cue card — all 12 breakpoints

| # | Pattern | File | Line |
|---|---|---|---|
| 1 | Singleton | `ShopEaseCartSingleton.java` | 24 |
| 2 | Singleton | `ShopEaseDatabaseManager.java` | 24 |
| 3 | Strategy | `CartDialog.java` | 273 |
| 4 | Strategy | `ShopEaseService.java` | 492 |
| 5 | Strategy | `ShopEaseService.java` | 493 |
| 6 | Strategy | `ShopEasePaymentContext.java` | 20 |
| 7 | Strategy | `ShopEaseDuitNowStrategy.java` | 6 |
| 8 *(bonus)* | Strategy | `ReduceInventoryStrategy.java` | 9 |
| 11 | Observer | `ShopEaseService.java` | 99 |
| 9 | Observer | `ShopEaseInventorySubject.java` | 36 |
| 10 | Observer | `Subject.java` | 29 |
| 12 | Observer | `ShopEaseCartStockSyncObserver.java` | 18 |

If anything doesn't pause: check the breakpoint is a solid (not hollow/grey) dot, confirm
you launched with **F5** through the debug panel (not the terminal script), and confirm
only the breakpoints for the pattern you're currently demoing are checked.
