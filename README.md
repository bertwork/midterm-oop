# Store Ordering System

A simple console-based Java application for managing product orders and viewing sales reports. Users can browse a product catalog, build an order, receive automatic promotional discounts, and later review aggregated sales data — all backed by plain text files instead of a database.

**Who is "the user"?** The app has no login or role system — whoever is running the console at the time can access both menu options. In practice that's usually:

- **A cashier/staff member**, using menu option 1 ("Start New Order") to take a customer's order at the counter, or
- **The owner/manager**, using menu option 2 ("View Sales Summary") to check how much has been sold and earned so far.

Both roles use the same single terminal session; the menu doesn't distinguish between them.

## Project Structure

```
├── data/
│   ├── products.psv               # Product catalog (name|price)
│   └── orders.psv                 # Saved order history (one line per sold item)
└── src/
    ├── storesystem/
    │   ├── Main.java                  # Application entry point
    │   ├── App.java                   # Main menu loop and orchestration
    │   ├── discount/
    │   │   ├── Discount.java          # Abstract base class for discounts
    │   │   ├── DiscountPolicy.java    # Business rule: decides which discount applies
    │   │   ├── FixedAmountDiscount.java
    │   │   └── PercentageDiscount.java
    │   └── order/
    │       ├── Product.java           # A catalog product (name + price)
    │       ├── OrderItem.java         # A product + quantity line item
    │       ├── Order.java             # A collection of OrderItems + optional Discount
    │       ├── OrderFileHandler.java  # Reads/writes products.psv and orders.psv
    │       ├── SoldItem.java          # Flattened sold-item record used for reporting
    │       └── SalesReport.java       # Aggregates SoldItems into sales statistics
    └── utils/
        ├── ConsoleUI.java             # Header/separator printing helpers
        └── InputReader.java           # Console input helpers (prompts, validated ints)
```

## Architecture

The application follows a simple layered structure with a clear separation of concerns:

```
┌──────────────────────────────────────────────┐
│                 Main.java                    │
│              (entry point)                   │
└────────────────────┬─────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────┐
│                  App.java                    │
│   Menu loop • order flow • sales summary     │
│   flow (the "controller" of the app)         │
└───────┬───────────────────┬──────────────────┘
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────────────┐
│ DiscountPolicy│   │  OrderFileHandler     │
│ (discount     │   │  (all file I/O:       │
│  business     │   │   load products,      │
│  rules)       │   │   save/load orders)   │
└───────┬───────┘   └──────────┬────────────┘
        │                      │
        ▼                      ▼
┌────────────────┐   ┌──────────────────────┐
│   Discount     │   │  Product / Order /   │
│  (abstract)    │   │  OrderItem / SoldItem│
│  ├ Fixed       │   │  (plain data models) │
│  └ Percentage  │   └───────────┬──────────┘
└────────────────┘               │
                                 ▼
                        ┌───────────────────┐
                        │   SalesReport     │
                        │ (aggregates       │
                        │  SoldItems)       │
                        └───────────────────┘

ConsoleUI / InputReader (utils package)
   → used throughout App.java for all console output/input
```

**Package responsibilities:**

| Package                | Responsibility                                                                                                                                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `storesystem`          | Application entry point and the top-level controller (`App`) that drives the console menu and ties everything together.                                                                                      |
| `storesystem.discount` | Discount logic: an abstract `Discount` type, two concrete strategies (`FixedAmountDiscount`, `PercentageDiscount`), and a `DiscountPolicy` that decides which discount (if any) applies to a given subtotal. |
| `storesystem.order`    | Core domain models (`Product`, `OrderItem`, `Order`, `SoldItem`) and their supporting services: `OrderFileHandler` (persistence) and `SalesReport` (reporting/aggregation).                                  |
| `utils`                | Generic, reusable console helpers not tied to store business logic.                                                                                                                                          |

### Key design points

- **Strategy pattern for discounts.** `Discount` is an abstract class with `apply(double)`. `FixedAmountDiscount` and `PercentageDiscount` are interchangeable strategies. `DiscountPolicy` is the single place that decides _which_ strategy to use based on business rules (currently: 10% off orders of ₱500+), so new discount types or rules can be added without touching `Order` or `App`.
- **Separation of persistence from domain models.** `Product`, `Order`, and `OrderItem` know nothing about how they're saved. All file reading/writing lives in `OrderFileHandler`, so the storage format (currently pipe-delimited text) could be swapped out (e.g. for a database or JSON) by changing only that one class.
- **`SoldItem` vs `OrderItem`.** `OrderItem` is used while building a live order and references a full `Product` object. `SoldItem` is a flattened, storage-oriented record (just name/quantity/subtotal) reconstructed from saved order files, used only for sales reporting — the two are intentionally decoupled.
- **Fail-soft file parsing.** `OrderFileHandler` skips malformed lines/fields (bad prices, incomplete lines, unparsable numbers) rather than crashing, so a single corrupted line doesn't take down the whole catalog or order history.
- **Consistent, single-delimiter file format.** `orders.psv` writes one flat, pipe-delimited line per sold item (grouped by a shared `orderId`) instead of nesting a comma/colon sub-format inside one pipe-delimited column. Every field on every line uses the same `|` delimiter, matching what a `.psv` extension implies and making the file simpler to parse, edit, or import elsewhere.

## OOP Concepts Used

| Concept                                                                                       | Where it's used                                                                                                                                                       | Explanation                                                                                                                                                                                                                                                                                                                                     |
| --------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Encapsulation**                                                                             | `Product`, `OrderItem`, `Order`, `SoldItem`, `Discount` and its subclasses                                                                                            | All fields are `private` and only exposed through getters (and, where appropriate, controlled setters like `Order.setDiscount()`). Internal state can't be modified directly from outside the class — e.g. `OrderItem` and `Product` validate their constructor arguments (`quantity > 0`, `price >= 0`) so invalid objects can never exist.    |
| **Abstraction**                                                                               | `Discount` (abstract class)                                                                                                                                           | `Discount` defines _what_ a discount does (`apply(double subtotal)`, `getLabel()`) without specifying _how_ the discount amount is calculated. Callers like `Order.getTotal()` and `App` work with the abstract `Discount` type and don't need to know which concrete kind of discount is in use.                                               |
| **Inheritance**                                                                               | `FixedAmountDiscount extends Discount`, `PercentageDiscount extends Discount`                                                                                         | Both subclasses inherit the shared `label` field/`getLabel()` behavior from `Discount` via `super(label)`, and only add the calculation logic specific to their discount type.                                                                                                                                                                  |
| **Polymorphism**                                                                              | `Discount discount = discountPolicy.determineDiscount(subtotal); discount.apply(subtotal);` in `App`/`Order`                                                          | Code that calls `discount.apply(subtotal)` doesn't know (or care) whether `discount` is actually a `FixedAmountDiscount` or a `PercentageDiscount` — the correct `apply()` implementation is chosen at runtime (dynamic method dispatch). This is what lets `DiscountPolicy` return different discount types through one common reference type. |
| **Composition ("has-a" relationships)**                                                       | `Order` has-a `List<OrderItem>` and has-a `Discount`; `OrderItem` has-a `Product`                                                                                     | Rather than inheriting from each other, these classes are built by combining simpler objects. An `Order` is composed of `OrderItem`s, and each `OrderItem` is composed with a `Product` — this models the real-world "an order contains items, an item references a product" relationship.                                                      |
| **Single Responsibility (class design principle, not a pure OOP pillar but closely related)** | `OrderFileHandler` (persistence only), `SalesReport` (aggregation only), `DiscountPolicy` (discount rule decision only), `ConsoleUI`/`InputReader` (console I/O only) | Each class is responsible for exactly one concern, which is why, for example, `Order` and `Product` never touch file I/O — that's delegated entirely to `OrderFileHandler`.                                                                                                                                                                     |

## Currency

All monetary values in this application — product prices, order subtotals, discounts, and totals — are assumed to be in **Philippine Peso (₱ / PHP)**. This is a documentation-only convention: the console output and the `.psv` data files intentionally print plain numbers (e.g. `278.98`) without a currency symbol or code, so no console-output code was changed for this. The examples below use `₱` purely to make the intended unit clear when reading this document.

## Data Files

The app reads/writes two plain-text data files (created relative to the working directory):

### `data/products.psv`

One product per line: `name|price`

```
Bluetooth Mouse|29.99
Wireless Keyboard|49.50
27in Monitor|219.00
```

(Prices shown as plain numbers in the file; per the [Currency](#currency) note above, treat these as ₱29.99, ₱49.50, and ₱219.00.)

### `data/orders.psv`

**One line per sold item** (not per order), all fields separated by a single, consistent `|` delimiter with no nested sub-formats:

```
orderId|productName|quantity|itemSubtotal|orderSubtotal|discountLabel|discountAmount|orderTotal
```

An order's `orderId` is a timestamp (`System.currentTimeMillis()`) generated once when the order is saved; every item line belonging to that order repeats the same `orderId`, `orderSubtotal`, `discountLabel`, `discountAmount`, and `orderTotal` values, so an order with 3 items produces 3 lines. Example — an order of 2 mice and 1 monitor, no discount:

```
1732650000123|Bluetooth Mouse|2|59.98|278.98|none|0.0|278.98
1732650000123|27in Monitor|1|219.00|278.98|none|0.0|278.98
```

(Again, the numeric fields carry no symbol in the file itself — per the [Currency](#currency) note, `59.98` and `278.98` represent ₱59.98 and ₱278.98.)

If an order qualifies for a discount, `discountLabel` and `discountAmount` are populated instead of `none`/`0.0` on every one of that order's lines.

> **Note:** Both files must exist under a `data/` folder relative to where the app is run, or the app will start with an empty catalog / empty sales history and warn accordingly. Also, since `|` is the delimiter, product names must not contain a `|` character.
