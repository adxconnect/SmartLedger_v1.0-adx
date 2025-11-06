# Finance Manager - AI Coding Agent Instructions

## Project Overview
**FinanceHub** is a comprehensive personal finance management desktop application built with Java Swing. Multi-user support with account-scoped data, modern dark mode UI, and 8 major financial modules (Transactions, Bank Accounts, Deposits, Investments, Loans, Credit Cards, Taxation, Summary/Reports).

## Architecture Fundamentals

### Three-Layer Architecture
- **Data Layer**: `src/db/DBHelper.java` (MySQL connection), `src/FinanceManager.java` (business logic & data operations)
- **Auth Layer**: `src/auth/` package (authentication, session management, password hashing)
- **UI Layer**: `src/UI/` package (Swing dialogs & main UI)

### Critical Session Management
All database operations REQUIRE an authenticated session via `SessionContext.getCurrentAccountId()`. Every query must be account-scoped:
```java
// REQUIRED pattern for all DB queries
int accountId = SessionContext.getCurrentAccountId();
String sql = "SELECT * FROM transactions WHERE account_id = ?";
```
Never write queries without `account_id` filtering - this prevents cross-account data leakage.

### Authentication Flow
1. Entry point: `src/UI/FinanceManagerApp.java` → forces login via `LoginDialog`
2. On success: `AuthManager.authenticate()` → `SessionContext.setCurrentAccount()`
3. All subsequent operations use `SessionContext.getCurrentAccountId()` for account isolation
4. On logout/exit: `SessionContext.clear()` wipes session data

## Build & Run Commands

### Compilation (Windows PowerShell)
```powershell
javac -encoding UTF-8 -d bin -cp "lib/*;src" `@sources.txt
```

### Run Application (Windows PowerShell)
```powershell
java -cp "bin;lib/*" src.UI.FinanceManagerApp
```

### Database Setup
MySQL connection hardcoded in `src/db/DBHelper.java`:
```java
String url = "jdbc:mysql://localhost:3306/finance_manager";
String user = "root";  // Update before running
String password = "2003";  // Update before running
```
Tables auto-create on first run via `FinanceManager.createTables()`.

## Project-Specific Conventions

### Modern Theme System (`src/UI/ModernTheme.java`)
ALL UI components must use ModernTheme constants:
- Colors: `ModernTheme.PRIMARY`, `ModernTheme.SURFACE`, `ModernTheme.TEXT_PRIMARY`, etc.
- Fonts: `ModernTheme.FONT_BODY`, `ModernTheme.FONT_HEADER` (uses "Poppins" via Google Fonts)
- Buttons: Use `ModernTheme.createPrimaryButton()`, `ModernTheme.createDangerButton()`, etc.
- Dark mode: Theme colors auto-switch via `ModernTheme.toggleDarkMode()` - all components refresh recursively

**Critical**: Font registration happens BEFORE UI initialization in `FinanceManager.registerGoogleFonts()`. If fonts fail to load, system falls back to defaults but UI looks inconsistent.

### Icon System (`src/UI/ModernIcons.java`)
19 custom vector icons drawn programmatically (not image files). Usage:
```java
JButton btn = ModernTheme.createPrimaryButton("Add");
btn.setIcon(ModernIcons.getIcon(ModernIcons.IconType.ADD, 16, ModernTheme.TEXT_WHITE));
```
Never use external icon files - extend `ModernIcons` enum for new icons.

### Dialog Patterns
Every entity has paired dialogs:
- **Add/Edit Dialog**: `AddEdit<Entity>Dialog.java` (modal dialog for CRUD operations)
- **Recycle Bin Dialog**: `<Entity>RecycleBinDialog.java` (soft delete recovery)

Example: Cards have `AddEditCardDialog.java` + `CardRecycleBinDialog.java`. Follow this pattern for new entities.

### Sensitive Data Handling
Credit card details (number, CVV) require OTP verification:
```java
// Pattern: Show OTP → Verify → Display sensitive data
ShowOtpDialog otpDialog = new ShowOtpDialog(parentFrame);
if (otpDialog.isVerified()) {
    // Show card details in SensitiveCardDetailsDialog
}
```
Never display card numbers/CVVs without OTP gate.

### Recycle Bin System
All major entities support soft delete with `recycle_bin_*` shadow tables:
- `transactions` → `recycle_bin_transactions`
- `deposits` → `recycle_bin_deposits`
- `cards` → `recycle_bin_cards` (etc.)

Deletes move records to recycle bin with `deleted_on` timestamp. Restore copies back and removes from recycle bin.

## Key Data Flows

### Transaction Management
1. User adds transaction via `FinanceManagerFullUI` → opens `AddEditTransactionDialog`
2. Dialog calls `FinanceManager.addTransactionToDB(accountId, ...)` with form data
3. FinanceManager inserts to `transactions` table with `account_id = SessionContext.getCurrentAccountId()`
4. UI refreshes monthly tabs (transactions grouped by month in `JTabbedPane monthTabs`)

### Investment Tracking
Supports 7 investment types (Gold/Silver, Mutual Funds, Stocks, Bonds, Real Estate, Crypto, Others). Each investment has:
- `purchase_price`, `current_price`, `quantity`
- Auto-calculated gain/loss: `(current_price - purchase_price) * quantity`

### Loan/EMI Calculations
`Loan.java` auto-calculates:
- Monthly EMI using: `P * r * (1+r)^n / ((1+r)^n - 1)` formula
- Total Repayable: `monthly_emi * tenure_months`
- Outstanding Balance: `principal_amount - principal_paid`

## External Dependencies

### Library Usage
- **mysql-connector-j-9.4.0.jar**: Database driver (JDBC)
- **itextpdf-5.5.13.4.jar**: PDF export for summary reports
- **poi-5.4.1.jar + poi-ooxml-5.4.1.jar**: Excel operations (CSV export uses built-in Java, XLSX removed)
- **gson-2.10.1.jar**: JSON serialization (used in `UserPreferencesCache`)

### Database Schema Notes
- All tables have `account_id` foreign key to `accounts.id` (except `accounts` and `login_audit`)
- `unique_id` (UUID) used for deposits/investments/loans to prevent duplicates
- Timestamps: `created_at`, `updated_at`, `deleted_on` track lifecycle

## Common Pitfalls

1. **Forgetting account_id filtering**: Always use `SessionContext.getCurrentAccountId()` in WHERE clauses
2. **Hardcoded database credentials**: Remind users to update `DBHelper.java` before first run
3. **Font loading failure**: If Poppins fonts don't load, UI renders but looks wrong. Check `src/resources/` for TTF files
4. **Path separators**: Use `;` for classpath on Windows (`bin;lib/*`), `:` on Linux/Mac (`bin:lib/*`)
5. **Theme refresh**: After `toggleDarkMode()`, call `SwingUtilities.updateComponentTreeUI(frame)` to repaint all components
6. **Dialog modality**: All Add/Edit dialogs are modal - parent frame waits for dialog closure

## Testing & Debugging

- No automated tests currently - manual testing via UI
- Main entry: Run `FinanceManagerApp.main()` → triggers login → launches `FinanceManagerFullUI`
- Console debugging: `FinanceManager` prints font loading and DB errors to `System.err`
- MySQL issues: Check `DBHelper` connection params, ensure `finance_manager` database exists

## Adding New Financial Modules

1. Create entity model class (e.g., `NewEntity.java`) with getters/setters
2. Add table creation SQL in `FinanceManager.createTables()`
3. Create `AddEditNewEntityDialog.java` and `NewEntityRecycleBinDialog.java` in `src/UI/`
4. Add tab to `FinanceManagerFullUI.tabs` JTabbedPane
5. Implement CRUD methods in `FinanceManager` with account_id scoping
6. Add icon to `ModernIcons` enum if needed
7. Update `PROJECT_DOCUMENTATION.md` with new module details
