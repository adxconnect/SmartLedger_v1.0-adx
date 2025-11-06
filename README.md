# Finance Manager - FinanceHub
## Comprehensive Project Documentation

---

## 📋 Table of Contents
<<<<<<< HEAD
1.  [Project Overview](#project-overview)
2.  [Technology Stack](#technology-stack)
3.  [Architecture]    (#architecture)
4.  [Features & Modules](#features--modules)
5.  [UI/UX Design](#uiux-design)
6.  [Database Schema](#database-schema)
7.  [Project Structure](#project-structure)
8.  [Progress & Status](#progress--status)
9.  [Installation & Setup](#installation--setup)
10. [Usage Guide](#usage-guide)
=======
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture](#architecture)
4. [Features & Modules](#features--modules)
5. [UI/UX Design](#uiux-design)
6. [Database Schema](#database-schema)
7. [Project Structure](#project-structure)
8. [Progress & Status](#progress--status)
9. [Installation & Setup](#installation--setup)
10.[Usage Guide](#usage-guide)
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df

---

## 🎯 Project Overview

**FinanceHub** is a comprehensive personal finance management application built with Java Swing. The application enables users to track and manage all aspects of their financial life including transactions, bank accounts, deposits, investments, loans, credit cards, and taxation profiles.

### Key Highlights
- **Multi-user Support**: Secure authentication with account-scoped data
- **Comprehensive Finance Tracking**: 8 major financial modules
- **Modern UI**: Custom-built modern design system with dark mode support
- **Real-time Data**: MySQL database integration for persistent storage
- **Export Capabilities**: CSV and PDF export functionality
- **Recycle Bin**: Soft delete for all major entities

---

## 💻 Technology Stack

### Core Technologies
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | Java 8+ | Primary programming language |
| **Swing** | Built-in | GUI framework |
| **MySQL** | 5.7+ | Database management system |
| **JDBC** | 9.4.0 | Database connectivity |

### External Libraries
| Library | Version | Purpose |
|---------|---------|---------|
| `mysql-connector-j` | 9.4.0 | MySQL database driver |
| `itextpdf` | 5.5.13.4 | PDF generation and export |
| `poi` | 5.4.1 | Excel file operations |
| `poi-ooxml` | 5.4.1 | Excel OOXML format support |
| `gson` | 2.10.1 | JSON serialization/deserialization |
| `commons-compress` | 1.28.0 | Archive file handling |
| `commons-collections4` | 4.5.0 | Enhanced Java collections |
| `xmlbeans` | 5.3.0 | XML processing |
| `log4j-api` | 3.0.0-beta2 | Logging framework |

### Build & Compilation
- **Compiler**: javac with UTF-8 encoding
- **Build Command**: `javac -encoding UTF-8 -d bin -cp "lib/*;src" @sources.txt`
- **Run Command**: `java -cp "bin;lib/*" src.UI.FinanceManagerApp`

---

## 🏗️ Architecture

### Design Pattern
- **MVC Architecture**: Model-View-Controller separation
- **Data Access Layer**: DBHelper and FinanceManager classes
- **Authentication Layer**: AuthManager with password hashing and salting
- **Session Management**: SessionContext for user state management

### Package Structure
```
src/
├── auth/                    # Authentication & Authorization
│   ├── Account.java
│   ├── AuthManager.java
│   ├── EmailService.java
│   ├── OtpService.java
│   ├── PasswordHasher.java
│   ├── SessionContext.java
│   └── UserPreferencesCache.java
├── db/                      # Database Layer
│   └── DBHelper.java
├── UI/                      # User Interface
│   ├── LoginDialog.java
│   ├── FinanceManagerFullUI.java
│   ├── ModernTheme.java
│   ├── ModernIcons.java
│   ├── LogoPanel.java
│   └── [Dialog classes...]
├── FinanceManager.java      # Core Business Logic
└── [Model classes...]       # Data Models
```

---

## 🎨 Features & Modules

### 1. **Authentication System**
- **User Registration**: Name, email, phone, account type, password
- **Secure Login**: Password hashing with SHA-256 and salt
- **Password Recovery**: OTP-based forgot password functionality
- **Account Types**: Personal, Business, Joint, Student
- **Session Management**: Automatic session tracking
- **Login Audit**: Activity logging with timestamps

### 2. **Transaction Management**
- **Add Transactions**: Income and expense tracking
- **Payment Methods**: UPI, Cash, Card, Bank Transfer, Others
- **Categories**: Customizable transaction categories
- **Monthly View**: Transactions organized by month tabs
- **Search & Filter**: Live search across all transaction fields
- **Year Filter**: View transactions by year
- **Column Sorting**: Sort by any column (date, amount, category, etc.)
- **Bulk Operations**: Select all and delete multiple transactions
- **Recycle Bin**: Recover deleted transactions
- **Export**: CSV and table export functionality

### 3. **Bank Accounts**
- **Account Types**: Savings, Current, Salary, Business
- **Multi-bank Support**: Track accounts from different banks
- **Balance Tracking**: Real-time balance calculations
- **Account Details**: Holder name, account number, IFSC, bank name
- **Master-Detail View**: List view with detailed information panel
- **CRUD Operations**: Create, read, update, delete accounts

### 4. **Deposits**
- **Fixed Deposits (FD)**: Principal, interest rate, tenure, maturity
- **Recurring Deposits (RD)**: Monthly amount, tenure, maturity calculation
- **Gullak (Piggy Bank)**: 
  - Cash denomination tracking (₹500, ₹200, ₹100, ₹50, ₹20, ₹10, ₹5, ₹2, ₹1)
  - Real-time total calculation
  - Due amount tracking
- **Maturity Tracking**: Auto-calculated maturity amounts
- **Interest Calculation**: Automatic interest computation
- **Recycle Bin**: Soft delete with recovery option

### 5. **Investments**
- **Investment Types**:
  - Gold/Silver
  - Mutual Funds
  - Stocks
  - Bonds
  - Real Estate
  - Cryptocurrency
  - Others
- **Price Tracking**: Current price and purchase price
- **Gain/Loss Calculation**: Automatic profit/loss computation
- **Quantity Management**: Units/quantity tracking
- **Investment Details**: Purchase date, current value, returns
- **Recycle Bin**: Investment recovery system

### 6. **Loans & EMI**
- **Loan Types**: Personal, Home, Car, Education, Business, Credit Card EMI
- **EMI Calculation**: Automatic EMI computation based on principal, rate, tenure
- **Principal Tracking**: Outstanding and paid amounts
- **Repayment Schedule**: Tenure in months/years
- **Status Management**: Active, Paid Off, Defaulted
- **Interest Rate**: Flexible rate configuration
- **Total Repayable**: Auto-calculated total amount
- **Recycle Bin**: Loan recovery functionality

### 7. **Credit Cards**
- **Card Management**: Store multiple credit cards
- **Secure Storage**: Encrypted card details (number, CVV)
- **Card Types**: Credit, Debit, Prepaid
- **Validity Tracking**: Valid from and valid through dates
- **Credit Limit**: Maximum credit limit tracking
- **Expense Tracking**: Current expenses and utilization
- **Payment Due**: Amount to pay and days left
- **Card Images**: Front and back image storage capability
- **OTP Verification**: View sensitive details only after OTP verification
- **Recycle Bin**: Card recovery system

### 8. **Taxation**
- **Tax Profiles**: Multiple tax profiles per financial year
- **Profile Types**: Individual, Business, HUF, Trust
- **Financial Year**: Year-wise tax management (e.g., FY 2024-25)
- **Income Tracking**: Gross income recording
- **Deductions**: Total deductions tracking
<<<<<<< HEAD
- **Taxable Income**: Auto-calculated taxable income (highlighted in bordered section)
- **Tax Paid**: Record of taxes paid (TDS tracking)
- **Notes**: Additional tax-related notes with HTML rendering
- **CRUD Operations**: Full profile management
- **Compact Layout**: Single-page view with 2x2 grid layout
  - Header section: Profile name, year, and type
  - Financial summary panel with bordered sections
  - Color-coded values (red for deductions, green for tax paid, blue for taxable income)
  - Highlighted taxable income in central position
  - No scroll bar required - everything fits on one page
=======
- **Taxable Income**: Auto-calculated taxable income
- **Tax Paid**: Record of taxes paid
- **Notes**: Additional tax-related notes
- **CRUD Operations**: Full profile management
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df

### 9. **Summary & Reports**
- **Comprehensive Dashboard**:
  - Transaction summary (income, expenses, balance)
  - Bank account totals
  - Deposit summaries (FD, RD, Gullak)
  - Investment portfolio value
  - Loan outstanding amounts
  - Credit card utilization
  - Tax profile overview
- **Export Options**:
  - CSV export for spreadsheet analysis
  - PDF export for professional reports
- **Customization**: Company name, designation, holder name
- **Year Selection**: Generate reports for specific years
- **Real-time Updates**: Auto-refresh on data changes

---

## 🎨 UI/UX Design

### Design System - ModernTheme

#### Color Palette

**Primary Colors**
- Primary Blue: `#4361EE` (RGB: 67, 97, 238)
- Primary Dark: `#3049BF` (RGB: 48, 73, 191)
- Primary Light: `#8C9EFF` (RGB: 140, 158, 255)

**Accent Colors**
- Success Green: `#2ED573` (RGB: 46, 213, 115)
- Danger Red: `#FF4757` (RGB: 255, 71, 87)
- Warning Orange: `#FFB822` (RGB: 255, 184, 34)
- Info Cyan: `#34ACE0` (RGB: 52, 172, 224)

**Light Mode**
- Background: `#F8F9FC` (RGB: 248, 249, 252)
- Surface: `#FFFFFF` (RGB: 255, 255, 255)
- Text Primary: `#212529` (RGB: 33, 37, 41)
- Text Secondary: `#6C757D` (RGB: 108, 117, 125)
- Border: `#DEE2E6` (RGB: 222, 226, 230)

**Dark Mode**
- Background: `#121212` (RGB: 18, 18, 18)
- Surface: `#1E1E1E` (RGB: 30, 30, 30)
- Text Primary: `#FFFFFF` (RGB: 255, 255, 255)
- Text Secondary: `#AAAAAA` (RGB: 170, 170, 170)
- Border: `#3C3C3C` (RGB: 60, 60, 60)

#### Typography
- **Font Family**: Segoe UI (System font)
- **Font Sizes**:
  - Logo: 28pt Bold
  - Title: 24pt Bold
  - Subtitle: 18pt Bold
  - Header: 16pt Bold
  - Body: 14pt Regular
  - Small: 12pt Regular
  - Button: 14pt Bold

#### Component Styling

**Buttons**
- Border Radius: 8px
- Padding: 10px 20px
- Types:
  - Primary: Blue background, white text
  - Secondary: White background, blue border
  - Success: Green background, white text
  - Danger: Red background, white text
  - Warning: Orange background, dark text

**Input Fields**
- Border Radius: 8px
- Padding: 8px 12px
- Border: 1px light gray
- Focus: Blue border with shadow

<<<<<<< HEAD
**Placeholder Labels**
- Font: 16pt Bold (FONT_HEADER)
- Color: Theme-aware (TEXT_PRIMARY)
- Alignment: Center
- Usage: All empty state messages ("Select a...")
- Dark Mode Compatible: Auto-switches to white text

=======
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df
**Tables**
- Row Height: 40px
- Header: Bold with bottom border
- Alternating rows for readability
- Grid lines: Light gray (1px)
- Selection: Light blue background

**Cards & Panels**
- Border Radius: 16px
- Padding: 16px
- Elevation: Subtle shadow
- Background: White (light mode) / Dark gray (dark mode)

**Scrollbars**
- Width: 10px
- Border Radius: 10px (pill-shaped)
- Thumb: Blue color
- Track: Light gray background
- No arrow buttons (modern look)

#### Icons System - ModernIcons

**19 Custom Vector Icons**:
1. Dashboard
2. Transactions
3. Bank
4. Credit Card
5. Investment
6. Loan
7. Tax
8. Deposit
9. Summary
10. Add (Plus sign)
11. Delete (Trash)
12. Edit (Pencil)
13. Recycle (Circular arrows)
14. Export (Download)
15. Search (Magnifying glass)
16. Settings (Gear)
17. User Profile
18. Logout
19. Filter

**Icon Features**:
- SVG-style programmatic drawing
- Scalable to any size
- Color customizable
- Crisp rendering with antialiasing
- Consistent 16px size on buttons with 8px gap

#### Layout Components

**Login Screen**
- Centered card layout (620×750px)
- Logo at top
- Tabbed interface (Sign In / Sign Up)
- Form fields with modern styling
- Dark mode toggle button (44×44px round button)
- Forgot password link

**Main Application**
- **Header Bar**:
  - Logo on left
  - User info in center (name, account type badge)
  - Dark mode toggle + Logout on right
  - Height: ~70px
  - Background: White surface

- **Tab Navigation**:
  - 8 main tabs (Transactions, Bank Accounts, Deposits, Investments, Loans, Cards, Taxation, Summary)
  - Rounded tab design (12px radius)
  - Active tab: Blue background, white text
  - Inactive tab: Gray background, dark text

- **Content Panels**:
  - Master-Detail view for most tabs
  - Split pane with 4px thin divider
  - List on left (200-220px width)
  - Detail panel on right
  - Action buttons at bottom

### Dark Mode Implementation
<<<<<<< HEAD
- **Toggle Button**: Round 44×44px button in header and login dialog
- **Icon**: Moon (☾) in light mode, Sun (☀) in dark mode
- **Color Switching**: Instant theme update
- **Supported Areas**: All UI components including login dialog
- **Session Preservation**: Theme toggle in login dialog no longer logs out users
- **In-Place Refresh**: Components update without recreating dialogs
- **Recursive Update**: All nested components automatically themed
- **Placeholder Labels**: Bold text adapts to theme (black in light, white in dark)
=======
- **Toggle Button**: Round 44×44px button in header
- **Icon**: Moon (☾) in light mode, Sun (☀) in dark mode
- **Color Switching**: Instant theme update
- **Supported Areas**: All UI components
- **Persistence**: State maintained across dialogs
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df

---

## 🗄️ Database Schema

### Database: `finance_manager`

#### Core Tables

**1. accounts**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_name (VARCHAR(100))
- account_type (VARCHAR(20)) [Personal, Business, Joint, Student]
- email (VARCHAR(255), UNIQUE, NOT NULL)
- phone (VARCHAR(20))
- password_hash (VARCHAR(512), NOT NULL)
- password_salt (VARCHAR(256), NOT NULL)
- security_question (VARCHAR(255))
- security_answer (VARCHAR(255))
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
- last_login_at (TIMESTAMP)
```

**2. login_audit**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- event_type (VARCHAR(50))
- ip_address (VARCHAR(64))
- user_agent (VARCHAR(255))
- created_at (TIMESTAMP)
```

**3. transactions**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- date (DATE, NOT NULL)
- timestamp (TIMESTAMP)
- day (VARCHAR(20))
- payment_method (VARCHAR(20)) [UPI, CASH, CARD, etc.]
- category (VARCHAR(100))
- type (VARCHAR(20)) [Income, Expense]
- payee (VARCHAR(255))
- description (TEXT)
- amount (DECIMAL(15, 2))
```

**4. recycle_bin_transactions**
```sql
- Same structure as transactions
- deleted_on (TIMESTAMP)
```

**5. bank_accounts**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- holder_name (VARCHAR(100))
- account_number (VARCHAR(50), UNIQUE)
- account_type (VARCHAR(50)) [Savings, Current, Salary, Business]
- ifsc_code (VARCHAR(20))
- bank_name (VARCHAR(100))
- balance (DECIMAL(15, 2))
- last_updated (TIMESTAMP)
```

**6. deposits**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- unique_id (VARCHAR(36), UNIQUE)
- deposit_name (VARCHAR(100))
- deposit_type (VARCHAR(20)) [Fixed Deposit, Recurring Deposit, Gullak]
- bank_name (VARCHAR(100))
- account_number (VARCHAR(50))
- principal_amount (DECIMAL(15, 2))
- monthly_amount (DECIMAL(15, 2))
- interest_rate (DECIMAL(5, 2))
- tenure (INT)
- tenure_unit (VARCHAR(10)) [Months, Years]
- start_date (DATE)
- current_total (DECIMAL(15, 2))
- last_updated (TIMESTAMP)
- count_500, count_200, count_100... (INT) [Gullak denominations]
- gullak_due_amount (DECIMAL(15, 2))
```

**7. recycle_bin_deposits**
```sql
- Same structure as deposits
- deleted_on (TIMESTAMP)
```

**8. investments**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- unique_id (VARCHAR(36), UNIQUE)
- investment_name (VARCHAR(100))
- investment_type (VARCHAR(50)) [Gold/Silver, Mutual Fund, Stock, etc.]
- quantity (DECIMAL(15, 4))
- purchase_price (DECIMAL(15, 2))
- current_price (DECIMAL(15, 2))
- purchase_date (DATE)
- notes (TEXT)
- last_updated (TIMESTAMP)
```

**9. recycle_bin_investments**
```sql
- Same structure as investments
- deleted_on (TIMESTAMP)
```

**10. loans**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- unique_id (VARCHAR(36), UNIQUE)
- loan_name (VARCHAR(100))
- loan_type (VARCHAR(50)) [Personal, Home, Car, Education, Business, Credit Card EMI]
- lender_name (VARCHAR(100))
- principal_amount (DECIMAL(15, 2))
- interest_rate (DECIMAL(5, 2))
- tenure (INT)
- tenure_unit (VARCHAR(10)) [Months, Years]
- monthly_emi (DECIMAL(15, 2))
- principal_outstanding (DECIMAL(15, 2))
- principal_paid (DECIMAL(15, 2))
- total_repayable (DECIMAL(15, 2))
- repayable_outstanding (DECIMAL(15, 2))
- start_date (DATE)
- status (VARCHAR(20)) [Active, Paid Off, Defaulted]
- notes (TEXT)
- last_updated (TIMESTAMP)
```

**11. recycle_bin_loans**
```sql
- Same structure as loans
- deleted_on (TIMESTAMP)
```

**12. cards**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- unique_id (VARCHAR(36), UNIQUE)
- card_name (VARCHAR(100))
- card_type (VARCHAR(20)) [Credit, Debit, Prepaid]
- card_number (VARCHAR(16))
- valid_from (VARCHAR(5)) [MM/YY]
- valid_through (VARCHAR(5)) [MM/YY]
- cvv (VARCHAR(4))
- front_image_path (VARCHAR(255))
- back_image_path (VARCHAR(255))
- credit_limit (DECIMAL(15, 2))
- current_expenses (DECIMAL(15, 2))
- amount_to_pay (DECIMAL(15, 2))
- days_left_to_pay (INT)
- creation_date (DATE)
```

**13. recycle_bin_cards**
```sql
- Same structure as cards
- deleted_on (TIMESTAMP)
```

**14. tax_profiles**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- account_id (INT, FOREIGN KEY → accounts.id)
- profile_name (VARCHAR(100))
- profile_type (VARCHAR(50)) [Individual, Business, HUF, Trust]
- financial_year (VARCHAR(10)) [e.g., "2024-25"]
- gross_income (DECIMAL(15, 2))
- total_deductions (DECIMAL(15, 2))
- taxable_income (DECIMAL(15, 2))
- tax_paid (DECIMAL(15, 2))
- notes (TEXT)
- last_updated (TIMESTAMP)
```

### Data Relationships
- **One-to-Many**: Each account can have multiple transactions, bank accounts, deposits, investments, loans, cards, and tax profiles
- **Cascade Delete**: Deleting an account removes all associated records
- **Soft Delete**: All major entities support recycle bin functionality
- **Account Scoping**: All data is automatically filtered by logged-in account ID

---

## 📁 Project Structure

```
Finance-Manager--Adx/
│
├── .git/                           # Git version control
├── .vscode/                        # VS Code settings
│
├── bin/                            # Compiled .class files
│   ├── org/
│   └── src/
│
├── data/                           # Data files (legacy file-based storage)
│   ├── creditcards.txt
│   ├── fixeddeposits.txt
│   ├── goldsilverinvestments.txt
│   ├── investments.txt
│   ├── mutualfunds.txt
│   ├── recurringdeposits.txt
│   └── transactions.txt
│
├── lib/                            # External JAR dependencies
│   ├── commons-collections4-4.5.0.jar
│   ├── commons-compress-1.28.0.jar
│   ├── gson-2.10.1.jar
│   ├── itextpdf-5.5.13.4.jar
│   ├── log4j-api-3.0.0-beta2.jar
│   ├── mysql-connector-j-9.4.0.jar
│   ├── poi-5.4.1.jar
│   ├── poi-ooxml-5.4.1.jar
│   └── xmlbeans-5.3.0.jar
│
├── src/                            # Source code
│   ├── auth/                       # Authentication package
│   │   ├── Account.java           # User account model
│   │   ├── AuthManager.java       # Authentication service
│   │   ├── EmailService.java      # Email functionality
│   │   ├── OtpService.java        # OTP generation/verification
│   │   ├── PasswordHasher.java    # Password hashing utility
│   │   ├── SessionContext.java    # Session management
│   │   └── UserPreferencesCache.java # User preference caching
│   │
│   ├── db/                         # Database package
│   │   └── DBHelper.java          # Database connection helper
│   │
│   ├── UI/                         # User interface package
│   │   ├── FinanceManagerApp.java          # Application entry point
│   │   ├── LoginDialog.java                # Login screen
│   │   ├── ForgotPasswordDialog.java       # Password recovery
│   │   ├── FinanceManagerFullUI.java       # Main application window
│   │   ├── ModernTheme.java                # Design system & theme
│   │   ├── ModernIcons.java                # Icon system
│   │   ├── LogoPanel.java                  # Logo component
│   │   ├── AutoCompleteTextField.java      # Autocomplete input
│   │   │
│   │   ├── AddEditCardDialog.java          # Card form dialog
│   │   ├── AddEditDepositDialog.java       # Deposit form dialog
│   │   ├── AddEditInvestmentDialog.java    # Investment form dialog
│   │   ├── AddEditLoanDialog.java          # Loan form dialog
│   │   ├── AddEditTaxProfileDialog.java    # Tax profile form dialog
│   │   │
│   │   ├── CardRecycleBinDialog.java       # Card recycle bin
│   │   ├── DepositRecycleBinDialog.java    # Deposit recycle bin
│   │   ├── InvestmentRecycleBinDialog.java # Investment recycle bin
│   │   ├── LoanRecycleBinDialog.java       # Loan recycle bin
│   │   ├── RecycleBinDialog.java           # Transaction recycle bin
│   │   │
│   │   ├── GullakDialog.java               # Gullak management
│   │   ├── SensitiveCardDetailsDialog.java # Card details viewer
│   │   ├── ShowOtpDialog.java              # OTP display
│   │   └── EnterOtpDialog.java             # OTP input
│   │
│   ├── BankAccount.java            # Bank account model
│   ├── Card.java                   # Credit/debit card model
│   ├── Deposit.java                # Deposit model (FD/RD/Gullak)
│   ├── Investment.java             # Investment model
│   ├── Loan.java                   # Loan model
│   ├── TaxProfile.java             # Tax profile model
│   ├── Transaction.java            # Transaction model
│   ├── SummaryData.java            # Summary aggregation model
│   │
│   ├── FinanceManager.java         # Core business logic
│   ├── Main.java                   # Console app entry point
│   │
│   ├── GoldSilverInvestment.java   # (Legacy)
│   ├── MutualFund.java             # (Legacy)
│   ├── RecycleBinDialog.java       # (Legacy)
│   └── UnsynchronizedByteArrayOutputStream.java
│
├── README.md                       # Project readme
├── sources.txt                     # List of all source files
└── PROJECT_DOCUMENTATION.md        # This file

Total Files: 94+ Java files
```

---

## 📊 Progress & Status

### ✅ Completed Features (100%)

#### 1. Authentication & User Management ✓
- [x] User registration with account types
- [x] Secure login with password hashing (SHA-256 + salt)
- [x] Forgot password with OTP
- [x] Session management
- [x] Login audit trail
- [x] Multi-account support

#### 2. Transaction Management ✓
- [x] Add/Edit/Delete transactions
- [x] Monthly tab organization
- [x] Year filtering
- [x] Live search across all fields
- [x] Column sorting
- [x] Bulk operations (select all, delete multiple)
- [x] Transaction recycle bin
- [x] CSV export

#### 3. Bank Accounts ✓
- [x] Multiple account types (Savings, Current, Salary, Business)
- [x] CRUD operations
- [x] Balance tracking
- [x] Master-detail view
- [x] Bank account details panel

#### 4. Deposits ✓
- [x] Fixed Deposits (FD)
- [x] Recurring Deposits (RD)
- [x] Gullak (Piggy Bank) with denomination tracking
- [x] Interest calculation
- [x] Maturity tracking
- [x] Deposit recycle bin
- [x] Add/Edit/Delete operations

#### 5. Investments ✓
- [x] Multiple investment types
- [x] Price tracking (purchase vs current)
- [x] Gain/Loss calculation
- [x] Quantity management
- [x] Price update functionality
- [x] Investment recycle bin

#### 6. Loans & EMI ✓
- [x] Multiple loan types
- [x] EMI calculation
- [x] Principal tracking (outstanding and paid)
- [x] Status management
- [x] Repayment schedule
- [x] Loan recycle bin

#### 7. Credit Cards ✓
- [x] Card storage with secure details
- [x] Credit limit tracking
- [x] Expense and payment tracking
- [x] Card validity management
- [x] OTP-based sensitive detail viewing
- [x] Card recycle bin

#### 8. Taxation ✓
- [x] Multiple tax profiles per year
- [x] Income and deduction tracking
- [x] Taxable income calculation
- [x] Financial year management
- [x] Profile types support

#### 9. Summary & Reports ✓
- [x] Comprehensive financial dashboard
- [x] All module summaries
- [x] CSV export
- [x] PDF export
- [x] Year-based filtering
- [x] Custom header (company, designation, holder)

#### 10. UI/UX ✓
- [x] Modern design system (ModernTheme)
- [x] Custom icon system (19 icons)
- [x] Logo branding (FinanceHub)
- [x] Responsive layouts
- [x] Master-detail views
- [x] Custom styled components
- [x] Modern scrollbars (thin, rounded)
- [x] Rounded tab design
- [x] Grid lines in tables
- [x] Dark mode implementation
- [x] Dark mode toggle button (round, 44×44px)
- [x] Theme switching for all components

### 🚧 Known Issues & Limitations
1. Database warning: `Unknown column 'last_login_at'` (benign, table created on first run)
2. Some lint warnings for unused lambda parameters (cosmetic only)
3. File-based storage methods still present (legacy code, not in use)

<<<<<<< HEAD
### ✨ Recent Enhancements (November 2025)

#### Dark Mode Improvements
- **Login Dialog Dark Mode Fix**: Fixed issue where toggling dark mode in login dialog would log out users
  - Changed from `dispose()` and recreate to in-place theme refresh
  - Added `refreshDialogTheme()` and `updateComponentTheme()` methods
  - Maintains user session and authentication state during theme changes
  
#### UI Enhancements
- **Bold Placeholder Labels**: All "Select a..." placeholder texts are now bold and theme-aware
  - Created `ModernTheme.createPlaceholderLabel()` utility method
  - Implemented across all 7 main sections (Bank Accounts, Deposits, Investments, Taxation, Loans, Lending, Cards)
  - Light mode: Bold black text for high contrast
  - Dark mode: Bold white text for excellent visibility
  - Centralized styling eliminates code duplication

#### Layout Improvements
- **Taxation Section Redesign**: Completely restructured for compact, single-page view
  - Eliminated scroll bar requirement
  - Implemented 2x2 grid layout for financial data
  - Highlighted taxable income with colored border and larger font
  - Compact notes section using HTML rendering
  - Professional visual hierarchy with bordered sections
  - Color-coded information (red for deductions, green for tax paid, blue for taxable income)

#### Bug Fixes
- Fixed Loans/EMI section placeholder text not showing bold styling
- Fixed Lending section placeholder text inconsistencies
- Corrected all refresh methods to use theme-aware placeholders
- Resolved dark mode toggle causing logout in login dialog

=======
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df
### 📈 Code Statistics
- **Total Java Files**: 94+
- **Total Lines of Code**: ~30,000+
- **Packages**: 3 (auth, db, UI)
- **UI Components**: 40+
- **Data Models**: 8
- **Dialog Classes**: 15+
- **Database Tables**: 14+
<<<<<<< HEAD
- **Custom Theme Methods**: 15+ (including new placeholder and refresh methods)
=======
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df

---

## 🚀 Installation & Setup

### Prerequisites
1. **Java Development Kit (JDK)**: Version 8 or higher
2. **MySQL Server**: Version 5.7 or higher
3. **IDE** (Optional): IntelliJ IDEA, Eclipse, or VS Code

### Database Setup

1. **Install MySQL** and start the service

2. **Create Database**:
```sql
CREATE DATABASE finance_manager;
```

3. **Update Database Credentials** in `src/db/DBHelper.java`:
```java
String url = "jdbc:mysql://localhost:3306/finance_manager";
String user = "root";          // Your MySQL username
String password = "your_pass";  // Your MySQL password
```

4. **Tables Auto-Creation**: Tables will be created automatically on first run

### Project Setup

1. **Clone or Extract** the project to your local machine

2. **Navigate to Project Directory**:
```bash
cd Finance-Manager--Adx
```

3. **Verify Libraries** in `lib/` folder:
   - All 9 JAR files should be present
   - If missing, download from Maven Central

### Compilation

#### Windows PowerShell:
```powershell
javac -encoding UTF-8 -d bin -cp "lib/*;src" `@sources.txt
```

#### Linux/Mac:
```bash
javac -encoding UTF-8 -d bin -cp "lib/*:src" @sources.txt
```

### Running the Application

#### Windows:
```powershell
java -cp "bin;lib/*" src.UI.FinanceManagerApp
```

#### Linux/Mac:
```bash
java -cp "bin:lib/*" src.UI.FinanceManagerApp
```

### First Run

1. Application launches with **Login Screen**
2. Click on **"Sign Up"** tab
3. Fill in registration details:
   - Account Name
   - Email (unique)
   - Phone Number
   - Account Type
   - Password (strong password recommended)
4. Click **"Create Account"**
5. Login with your credentials
6. Start managing your finances!

---

## 📖 Usage Guide

### Login & Authentication
1. **Login**: Enter email and password
2. **Forgot Password**: Click "Forgot Password?" → Enter email → OTP verification → Reset
3. **Dark Mode**: Click moon/sun icon in header to toggle theme
4. **Logout**: Click "Logout" button in top-right corner

### Managing Transactions
1. Navigate to **Transactions** tab
2. Select year from dropdown
3. Click **"Add Transaction"** button
4. Fill in details: Date, Category, Type, Amount, Payment Method, Payee, Description
5. Click **"Save"**
6. **Search**: Use search field to filter transactions
7. **Delete**: Select transaction → Click "Delete Selected"
8. **Recycle Bin**: Recover deleted transactions

### Managing Bank Accounts
1. Navigate to **Bank Accounts** tab
2. Click **"Add New Account"**
3. Enter: Holder Name, Account Number, Account Type, Bank Name, IFSC, Balance
4. Click **"Save"**
5. Select account from list to view details
6. Edit or Delete as needed

### Managing Deposits
1. Navigate to **Deposits** tab
2. Click **"Add New Deposit"**
3. Select Type: Fixed Deposit, Recurring Deposit, or Gullak
4. Fill in respective fields
5. **For Gullak**: Click "Manage Gullak" to track cash denominations
6. Click **"Save"**

### Managing Investments
1. Navigate to **Investments** tab
2. Click **"Add Investment"**
3. Select investment type
4. Enter quantity, purchase price, current price
5. Click **"Save"**
6. **Update Price**: Select investment → Click "Update Price"

### Managing Loans
1. Navigate to **Loans / EMI** tab
2. Click **"Add New Loan"**
3. Enter principal, interest rate, tenure
4. EMI is auto-calculated
5. Track repayment status

### Managing Cards
1. Navigate to **Cards** tab
2. Click **"Add New Card"**
3. Enter card details (name, number, validity, CVV, credit limit)
4. Click **"Save"**
5. **View Sensitive Details**: Requires OTP verification
6. Track expenses and payments

### Managing Tax Profiles
1. Navigate to **Taxation** tab
2. Click **"Add New Tax Profile"**
3. Enter financial year and profile type
4. Input gross income and deductions
5. Taxable income is auto-calculated

### Generating Reports
1. Navigate to **Summary & Reports** tab
2. Enter Company Name, Designation, Report Holder
3. Select Transaction Year
4. Click **"Generate Summary"**
5. Review comprehensive financial overview
6. **Export**: Click "Export CSV" or "Export PDF"

---

## 🔐 Security Features

### Password Security
- **Hashing Algorithm**: SHA-256
- **Salting**: Unique salt per user
- **Storage**: Only hash and salt stored, never plain text

### Card Security
- **Sensitive Data Protection**: Card number and CVV encrypted
- **OTP Verification**: Required to view sensitive card details
- **Limited Access**: Only account owner can view

### Session Security
- **Session Context**: User ID stored in session
- **Auto-logout**: On application close
- **Account Scoping**: All queries filtered by account ID

### Data Integrity
- **Foreign Key Constraints**: Maintain referential integrity
- **Cascade Delete**: Cleanup related records
- **Recycle Bin**: Prevent accidental data loss
- **Timestamp Tracking**: All records have creation/update timestamps

---

## 🎨 UI Screenshots Reference

### Main Screens
1. **Login Screen**: Modern card with logo, tabbed sign-in/sign-up, dark mode toggle
2. **Main Dashboard**: Header with logo, user info, tabs, dark mode toggle
3. **Transactions Tab**: Year filter, search, monthly tabs, table with grid lines, scrollbars
4. **Bank Accounts Tab**: Split pane, list on left, details on right, action buttons
5. **Other Tabs**: Similar master-detail layout with consistent styling

### Design Elements
- **Round Dark Mode Button**: 44×44px, moon/sun icon, yellow/dark background
- **Modern Tables**: Grid lines, vertical scrollbar always visible, horizontal as needed
- **Styled Buttons**: Blue primary, red danger, white secondary with icons
- **Custom Scrollbars**: 10px width, rounded, blue thumb, no arrows
- **Rounded Tabs**: 12px radius, blue selection, smooth transitions

---

## 🤝 Contributing

This is a personal finance management project. Future enhancements could include:
- Multi-currency support
- Budget planning and alerts
- Recurring transaction automation
- Data visualization (charts and graphs)
- Mobile app integration
- Cloud sync functionality
- Financial goal tracking
- Bill reminders
- Receipt scanning and attachment

---

## 📄 License

This project is created for educational and personal use.

---

## 👤 Author

**Project**: Finance Manager (FinanceHub)  
**Developer**: Adx Connect  
**Repository**: Finance-Manager--Adx  
**Last Updated**: October 31, 2025

---

## 📞 Support

For issues, questions, or suggestions:
1. Check this documentation
2. Review code comments
3. Test with sample data
4. Verify database connection

---

**End of Documentation**

*Generated on: October 31, 2025*  
<<<<<<< HEAD
*Last Updated: November 6, 2025*  
*Version: 2.0*  
*Status: Production Ready* ✅

### Changelog
- **v2.0 (November 6, 2025)** - Major UI/UX Overhaul:
  - **Ultra-Modern Sidebar Redesign**:
    - Gradient blue selection highlight (12px rounded, vibrant #0078D4 to #005BA1)
    - Borderless design with enhanced icon visibility
    - Slim modern scrollbar (8px width, rounded thumb, transparent track)
    - Collapsible sidebar with icon-only mode and tooltips
    - Action command preservation across collapsed/expanded states
  
  - **Dark Mode Synchronization**:
    - Fixed filter button icons not updating on theme toggle
    - Fixed export button icons not syncing in dark mode
    - Fixed column menu item icons not reflecting theme changes
    - Enhanced theme update logic to refresh all dynamic UI elements
  
  - **Transaction UI Improvements**:
    - Right-aligned search field using BoxLayout with horizontal glue
    - Completely borderless transaction table for cleaner appearance
    - Modern scrollbar styling for table viewport
  
  - **Profile Management System**:
    - New EditProfileDialog with modern card-based design
    - Real-time email validation with ✓/✗ indicators
    - Editable fields: Account Name, Account Type, Email, Phone
    - Database integration with automatic session context refresh
    - Responsive layout with proper spacing (12px margins, 16px gaps)
  
  - **Login Experience Enhancement**:
    - Password field eye icon properly positioned using BorderLayout
    - Consistent field sizing (490×42px matching email field)
    - Fixed eye toggle button overlapping with password text
    - 45px right padding for eye button placement
    - Improved visual hierarchy and form alignment

- **v1.1 (November 5, 2025)**:
  - Fixed dark mode toggle in login dialog (no longer logs out users)
  - Implemented bold, theme-aware placeholder labels across all sections
  - Redesigned taxation section with compact single-page layout
  - Fixed placeholder text styling in Loans and Lending sections
  - Enhanced visual hierarchy with color-coded financial data
  - Improved user experience with in-place theme refresh

- **v1.0 (October 31, 2025)**:
  - Initial documentation
  - Complete feature set implementation
  - Full database schema documentation
=======
*Version: 1.0*  
*Status: Production Ready* ✅
>>>>>>> 674ea5741c636a7ca63b8abf2bdbb9192b2a48df
