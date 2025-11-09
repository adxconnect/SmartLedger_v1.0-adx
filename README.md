# 🎯 SmartLedger v1.0 - Modern Personal Finance Manager

<div align="center">

![SmartLedger Logo](logo/Logo1.png)

**Your Complete Financial Companion for 2025**  
*Track, Manage, and Optimize Your Financial Life with Style*

[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-blue.svg)](https://www.mysql.com/)
[![Swing](https://img.shields.io/badge/Swing-Modern%20UI-green.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![License](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0-brightgreen.svg)]()

*✨ Modern Dark Mode • 🔒 Secure Authentication • 📊 Real-time Analytics • 📱 Cross-Platform*

[📥 Download](#-installation--setup) • [🚀 Quick Start](#-quick-start) • [📖 Documentation](#-features-overview) • [🎨 Screenshots](#-ui-showcase)

</div>

---

## 🌟 What's New in v1.0 (November 2025)

- **🎨 Ultra-Modern UI**: Complete redesign with gradient themes, custom icons, and fluid animations
- **🌙 Advanced Dark Mode**: Seamless theme switching with intelligent color adaptation
- **🔐 Enhanced Security**: OTP-based card protection, SHA-256 password hashing, account isolation
- **📊 Smart Analytics**: Real-time financial insights, automated calculations, comprehensive reporting
- **♻️ Recycle Bin System**: Soft delete for all financial data with easy recovery
- **📱 Responsive Design**: Optimized for all screen sizes with modern scrollbars and layouts
- **⚡ Performance Optimized**: Fast loading, efficient database queries, background processing
- **🔄 Multi-User Support**: Secure account-scoped data with session management

---

## 📋 Table of Contents

- [🌟 Overview](#-overview)
- [✨ Key Features](#-key-features)
- [💻 Technology Stack](#-technology-stack)
- [🏗️ Architecture](#-architecture)
- [📁 Project Structure](#-project-structure)
- [⚡ Installation & Setup](#-installation--setup)
- [🚀 Quick Start](#-quick-start)
- [🎨 UI Showcase](#-ui-showcase)
- [📖 Features Overview](#-features-overview)
- [🔧 Configuration](#-configuration)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [👤 Author](#-author)

---

## 🌟 Overview

**SmartLedger** is a cutting-edge personal finance management application designed for the modern user. Built with Java Swing and featuring a stunning custom UI, SmartLedger provides comprehensive financial tracking across 8 major modules with enterprise-grade security and user experience.

### 🎯 Why SmartLedger?

- **🔒 Bank-Level Security**: Multi-user authentication, encrypted data, OTP verification
- **🎨 Beautiful Interface**: Modern design with dark mode, custom icons, smooth animations
- **📊 Complete Financial Picture**: Track every aspect of your financial life in one place
- **⚡ Lightning Fast**: Optimized performance with real-time calculations and updates
- **📱 Cross-Platform**: Runs on Windows, macOS, and Linux with native look and feel
- **🔄 Data Safety**: Recycle bin system prevents accidental data loss
- **📈 Smart Analytics**: Automated EMI calculations, investment tracking, tax optimization

### 📊 Financial Modules

| Module | Description | Key Features |
|--------|-------------|--------------|
| 💳 **Transactions** | Income & expense tracking | Monthly tabs, live search, bulk operations |
| 🏦 **Bank Accounts** | Multi-bank account management | Balance tracking, account types, IFSC validation |
| 💰 **Deposits** | FD, RD, Gullak management | Auto-calculations, maturity tracking, denomination counting |
| 📈 **Investments** | Portfolio management | 7 investment types, gain/loss tracking, price updates |
| 💸 **Loans & EMI** | Loan management | EMI calculator, repayment tracking, status monitoring |
| 🎴 **Credit Cards** | Card management | Secure storage, limit tracking, OTP protection |
| 📋 **Taxation** | Tax profile management | Multi-year support, deduction tracking, auto-calculations |
| 📊 **Summary & Reports** | Financial dashboard | PDF/CSV export, comprehensive analytics, year filtering |

---

## ✨ Key Features

### 🔐 Security & Authentication
- **Multi-User Support** with account-scoped data isolation
- **SHA-256 Password Hashing** with unique salt per user
- **OTP-Based Recovery** for forgotten passwords
- **Session Management** with automatic logout
- **Login Audit Trail** for security monitoring
- **Card Data Protection** with OTP verification for sensitive details

### 🎨 Modern UI/UX
- **Custom Design System** with 19 vector icons
- **Dark/Light Mode Toggle** with instant theme switching
- **Responsive Layouts** with master-detail views
- **Modern Components**: Rounded buttons, custom scrollbars, gradient themes
- **Professional Branding** with custom logo and typography
- **Smooth Animations** and transitions throughout

### 📊 Financial Intelligence
- **Real-Time Calculations**: EMI, interest, maturity amounts
- **Automated Analytics**: Gain/loss tracking, balance summaries
- **Export Capabilities**: PDF reports, CSV data export
- **Recycle Bin System**: Soft delete with recovery for all entities
- **Search & Filter**: Live search across all financial data
- **Bulk Operations**: Select multiple items for batch actions

### 🔧 Technical Excellence
- **MySQL Database** with optimized queries and foreign key constraints
- **JDBC Integration** for reliable data persistence
- **External Libraries**: PDF generation, Excel operations, JSON handling
- **Modular Architecture** with clean separation of concerns
- **Error Handling** with user-friendly messages
- **Performance Monitoring** and optimization

---

## 💻 Technology Stack

### Core Technologies
```java
Java 8+           // Primary language
Swing             // Modern GUI framework
MySQL 5.7+        // Database management
JDBC 9.4.0        // Database connectivity
```

### External Dependencies
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

### Development Tools
- **IDE**: VS Code, IntelliJ IDEA, Eclipse
- **Build Tool**: javac with UTF-8 encoding
- **Version Control**: Git
- **Package Manager**: Manual JAR management

---

## 🏗️ Architecture

### Three-Layer Architecture

```
┌─────────────────┐
│   UI Layer      │  🎨 Modern Swing Components
│   (src/UI/)     │  • LoginDialog, FinanceManagerFullUI
│                 │  • ModernTheme, ModernIcons
└─────────────────┘
         │
┌─────────────────┐
│  Auth Layer     │  🔐 Authentication & Security
│  (src/auth/)    │  • AuthManager, SessionContext
│                 │  • PasswordHasher, OtpService
└─────────────────┘
         │
┌─────────────────┐
│  Data Layer     │  💾 Database Operations
│  (src/db/)      │  • DBHelper, FinanceManager
│                 │  • Business Logic & Queries
└─────────────────┘
```

### Key Design Patterns
- **MVC Architecture**: Clear separation of Model-View-Controller
- **Singleton Pattern**: Database connections, session management
- **Factory Pattern**: UI component creation
- **Observer Pattern**: Theme change notifications
- **Strategy Pattern**: Different export formats (PDF, CSV)

### Database Design
- **14 Tables** with proper normalization
- **Foreign Key Constraints** for data integrity
- **Account Scoping** on all queries
- **Recycle Bin Tables** for soft deletes
- **Audit Trails** for security logging

---

## 📁 Project Structure

```
Finance-Manager--Adx/
│
├── 📁 bin/                          # Compiled Java classes
│   ├── org/apache/...              # External library classes
│   └── src/...                     # Application classes
│
├── 📁 lib/                          # External JAR dependencies
│   ├── mysql-connector-j-9.4.0.jar
│   ├── itextpdf-5.5.13.4.jar
│   ├── poi-5.4.1.jar
│   ├── gson-2.10.1.jar
│   └── ... (9 total JARs)
│
├── 📁 logo/                         # Application logos and branding
│   └── smartledger-logo.png
│
├── 📁 src/                          # Source code
│   ├── 📁 auth/                     # Authentication package
│   │   ├── Account.java            # User account model
│   │   ├── AuthManager.java        # Authentication service
│   │   ├── EmailService.java       # Email functionality
│   │   │   ├── OtpService.java         # OTP generation/verification
│   │   ├── PasswordHasher.java     # Password hashing utility
│   │   ├── SessionContext.java     # Session management
│   │   └── UserPreferencesCache.java # User preferences
│   │
│   ├── 📁 db/                       # Database package
│   │   └── DBHelper.java           # Database connection helper
│   │
│   ├── 📁 UI/                       # User interface package
│   │   ├── FinanceManagerApp.java   # Application entry point
│   │   ├── LoginDialog.java         # Login screen
│   │   ├── FinanceManagerFullUI.java # Main application window
│   │   ├── ModernTheme.java         # Design system & theming
│   │   ├── ModernIcons.java         # Custom icon system
│   │   ├── LogoPanel.java           # Logo component
│   │   ├── EditProfileDialog.java   # Profile management
│   │   └── ... (25+ dialog classes)
│   │
│   ├── BankAccount.java            # Bank account model
│   ├── Card.java                   # Credit/debit card model
│   ├── Deposit.java                # Deposit model (FD/RD/Gullak)
│   ├── Investment.java             # Investment model
│   ├── Loan.java                   # Loan model
│   ├── TaxProfile.java             # Tax profile model
│   ├── Transaction.java            # Transaction model
│   ├── SummaryData.java            # Summary aggregation model
│   ├── FinanceManager.java         # Core business logic
│   └── Main.java                   # Console app entry point
│
├── 📁 data/                         # Legacy data files (not used)
│   ├── transactions.txt
│   ├── creditcards.txt
│   └── ... (6 files)
│
├── 📄 README.md                     # This file
├── 📄 sources.txt                   # Compilation source list
├── 📄 PROJECT_DOCUMENTATION.md      # Detailed documentation
└── 📄 .gitignore                    # Git ignore rules
```

**File Count**: 94+ Java files, 9 JARs, 6 data files
**Lines of Code**: ~30,000+ lines
**Packages**: 3 main packages (auth, db, UI)

---

## ⚡ Installation & Setup

### 📋 Prerequisites

| Requirement | Version | Download Link |
|-------------|---------|---------------|
| **Java JDK** | 8 or higher | [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) |
| **MySQL Server** | 5.7 or higher | [MySQL Downloads](https://dev.mysql.com/downloads/mysql/) |
| **Git** | Latest | [Git Downloads](https://git-scm.com/downloads) |

### 🗄️ Database Setup

1. **Install MySQL Server** and start the service
2. **Open MySQL Command Line** or MySQL Workbench
3. **Create Database**:
   ```sql
   CREATE DATABASE finance_manager;
   ```
4. **Update Credentials** in `src/db/DBHelper.java`:
   ```java
   String url = "jdbc:mysql://localhost:3306/finance_manager";
   String user = "root";              // Your MySQL username
   String password = "your_password";  // Your MySQL password
   ```

### 📦 Project Setup

1. **Clone Repository**:
   ```bash
   git clone https://github.com/adxconnect/Finance-Manager--Adx.git
   cd Finance-Manager--Adx
   ```

2. **Verify Dependencies**:
   - Check that all 9 JAR files exist in `lib/` folder
   - If missing, download from Maven Central Repository

3. **Compile Application**:
   ```powershell
   # Windows PowerShell
   javac -encoding UTF-8 -d bin -cp "lib/*;src" @sources.txt
   ```
   ```bash
   # Linux/macOS
   javac -encoding UTF-8 -d bin -cp "lib/*:src" @sources.txt
   ```

4. **Run Application**:
   ```powershell
   # Windows PowerShell
   java -cp "bin;lib/*" src.UI.FinanceManagerApp
   ```
   ```bash
   # Linux/macOS
   java -cp "bin:lib/*" src.UI.FinanceManagerApp
   ```

### 🎯 First Run Setup

1. **Launch Application** - Login screen appears
2. **Create Account**:
   - Click "Sign Up" tab
   - Enter: Account Name, Email, Phone, Account Type
   - Set strong password
   - Click "Create Account"
3. **Login** with your credentials
4. **Start Managing** your finances!

---

## 🚀 Quick Start

### Adding Your First Transaction
1. Navigate to **Transactions** tab
2. Select year from dropdown
3. Click **"Add Transaction"**
4. Fill details: Date, Category, Type, Amount, Payment Method
5. Click **"Save"**

### Setting Up Bank Accounts
1. Go to **Bank Accounts** tab
2. Click **"Add New Account"**
3. Enter account details: Holder name, Account number, Bank, IFSC
4. Set opening balance
5. Click **"Save"**

### Creating Investment Portfolio
1. Navigate to **Investments** tab
2. Click **"Add Investment"**
3. Select type: Stocks, Mutual Funds, Gold, etc.
4. Enter quantity, purchase price, current price
5. Click **"Save"**

---

## 🎨 UI Showcase

### Modern Login Experience
- **Clean Card Design** with centered logo
- **Tabbed Interface** for Sign In/Sign Up
- **Dark Mode Toggle** with smooth animations
- **Form Validation** with real-time feedback
- **Professional Branding** throughout

### Main Dashboard
- **Header Bar** with logo, user info, and controls
- **8 Main Tabs** with modern rounded design
- **Dark Mode Toggle** (44×44px round button)
- **Responsive Layout** adapting to screen size

### Transaction Management
- **Monthly Tab Organization** for easy navigation
- **Live Search** across all transaction fields
- **Modern Table Design** with grid lines and custom scrollbars
- **Bulk Operations** for efficient data management
- **Export Capabilities** to CSV and PDF

### Financial Analytics
- **Comprehensive Dashboard** showing all financial data
- **Real-time Calculations** for balances and totals
- **Color-coded Categories** for easy identification
- **Export Options** for professional reporting

---

## 📖 Features Overview

### 💳 Transaction Management
- **Complete CRUD Operations**: Add, edit, delete transactions
- **Monthly Organization**: Transactions grouped by month in tabs
- **Advanced Search**: Live filtering across date, amount, category, payee
- **Bulk Operations**: Select all, delete multiple, export selected
- **Recycle Bin**: Recover accidentally deleted transactions
- **Export Support**: CSV export for spreadsheet analysis

### 🏦 Bank Account Management
- **Multi-Bank Support**: Track accounts from different banks
- **Account Types**: Savings, Current, Salary, Business accounts
- **Balance Tracking**: Real-time balance calculations
- **Secure Storage**: Account numbers, IFSC codes, bank details
- **Master-Detail View**: List view with detailed information panel

### 💰 Deposit Management
- **Fixed Deposits (FD)**: Principal, interest rate, tenure tracking
- **Recurring Deposits (RD)**: Monthly deposits with maturity calculation
- **Gullak (Piggy Bank)**: Cash denomination tracking system
  - Track ₹500, ₹200, ₹100, ₹50, ₹20, ₹10, ₹5, ₹2, ₹1 notes
  - Real-time total calculation
  - Due amount tracking
- **Auto-Calculations**: Maturity amounts, interest earned

### 📈 Investment Portfolio
- **7 Investment Types**: Gold/Silver, Mutual Funds, Stocks, Bonds, Real Estate, Crypto, Others
- **Price Tracking**: Purchase price vs current price
- **Gain/Loss Calculation**: Automatic profit/loss computation
- **Quantity Management**: Units/shares tracking
- **Portfolio Overview**: Total investment value, returns percentage

### 💸 Loans & EMI Management
- **Loan Types**: Personal, Home, Car, Education, Business, Credit Card EMI
- **EMI Calculator**: Automatic calculation using standard formula
- **Repayment Tracking**: Principal paid, outstanding balance
- **Status Management**: Active, Paid Off, Defaulted loans
- **Flexible Tenure**: Months or years configuration

### 🎴 Credit Card Management
- **Secure Card Storage**: Encrypted card numbers and CVV
- **Card Types**: Credit, Debit, Prepaid cards
- **Limit Tracking**: Credit limit, current expenses, utilization %
- **Payment Management**: Amount due, days left to pay
- **OTP Protection**: Secure access to sensitive card details

### 📋 Taxation Management
- **Multi-Year Support**: Financial year-wise tax profiles
- **Profile Types**: Individual, Business, HUF, Trust
- **Income Tracking**: Gross income recording
- **Deduction Management**: Total deductions tracking
- **Auto-Calculations**: Taxable income computation
- **Notes Support**: Additional tax-related information

### 📊 Summary & Reports
- **Financial Dashboard**: Complete overview of all finances
- **Real-time Totals**: Transaction balances, account sums, investment values
- **Export Options**: Professional PDF reports, CSV data export
- **Customization**: Company name, designation, report holder details
- **Year Filtering**: Generate reports for specific years

---

## 🔧 Configuration

### Database Configuration
Edit `src/db/DBHelper.java`:
```java
// Update these values according to your MySQL setup
private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_manager";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password";
```

### Theme Customization
Modify `src/UI/ModernTheme.java` for custom colors:
```java
// Primary colors
public static final Color PRIMARY = new Color(67, 97, 238);
public static final Color PRIMARY_DARK = new Color(48, 73, 191);

// Accent colors
public static final Color SUCCESS = new Color(46, 213, 115);
public static final Color DANGER = new Color(255, 71, 87);
```

### Font Configuration
Update font loading in `src/UI/ModernTheme.java`:
```java
// Register Google Fonts (Poppins)
try {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/Poppins-Regular.ttf")));
} catch (Exception e) {
    // Fallback to system fonts
}
```

---

## 🤝 Contributing

We welcome contributions to SmartLedger! Here's how you can help:

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and test thoroughly
4. Commit your changes: `git commit -m 'Add amazing feature'`
5. Push to the branch: `git push origin feature/amazing-feature`
6. Open a Pull Request

### Code Standards
- Follow Java naming conventions
- Add comments for complex logic
- Test all new features
- Update documentation for API changes
- Maintain backward compatibility

### Feature Requests
- Open an issue with the "enhancement" label
- Provide detailed description and use cases
- Include mockups if UI-related

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Permissions**: Commercial use, modification, distribution, private use
**Limitations**: No liability, no warranty
**Conditions**: Include copyright notice

---

## 👤 Author

**SmartLedger v1.0**  
**Developer**: Adx Connect  
**Repository**: [Finance-Manager--Adx](https://github.com/adxconnect/Finance-Manager--Adx)  
**Email**: adxconnect@outlook.com  
**GitHub Profile** (dynamic owner link): [Adx Connect](../..)  
**LinkedIn**: [linkedin.com/in/adxconnect](https://linkedin.com/in/adxconnect)  

### Acknowledgments
- **Java Swing Community** for GUI framework
- **MySQL Team** for database management
- **Apache POI** for Excel operations
- **iTextPDF** for PDF generation
- **Google Fonts** for typography

---

## 📞 Support & Contact

### Getting Help
1. **Check Documentation**: Review this README and PROJECT_DOCUMENTATION.md
2. **Search Issues**: Look for existing GitHub issues
3. **Create Issue**: Open new issue for bugs or questions
4. **Community**: Join discussions in GitHub Discussions

### Common Issues
- **Database Connection**: Verify MySQL credentials in DBHelper.java
- **Missing Libraries**: Ensure all JAR files are in lib/ folder
- **Font Issues**: Check resources/ folder for TTF files
- **Compilation Errors**: Use UTF-8 encoding flag

### System Requirements
- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **RAM**: 512MB minimum, 1GB recommended
- **Storage**: 50MB for application, plus database space
- **Display**: 1024×768 minimum resolution

---

<div align="center">

**🎉 Thank you for choosing SmartLedger! 🎉**

*Transform your financial management experience with modern technology and beautiful design.*

---

**SmartLedger v1.0** - *Released November 2025*  
*Made with ❤️ by Adx Connect*

</div>

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
**Taxable Income**: Auto-calculated (highlighted in bordered section)
**Tax Paid**: Record of taxes paid (incl. TDS tracking)
**Notes**: Additional tax-related notes (HTML supported)
**CRUD Operations**: Full profile management
**Compact Layout**: Single-page 2×2 grid (no scrolling; color-coded values)

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

**Placeholder Labels**
* Font: 16pt Bold (FONT_HEADER)
* Theme-aware (TEXT_PRIMARY)
* Center aligned empty-state ("Select a…")
* Auto switches for dark mode
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
* Toggle Button: Round 44×44px (header + login)
* Icons: Moon (light) / Sun (dark)
* Instant theme update; recursive component refresh
* Session preserved (no dialog recreation)
* Placeholder labels adapt color automatically

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
- [x] Logo branding (SmartLedger)
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
* Loans/EMI placeholder styling corrected
* Lending placeholder inconsistencies fixed
* Refresh methods unified to use theme-aware placeholders
* Dark mode toggle no longer logs out users (login dialog)
### 📈 Code Statistics
- **Total Java Files**: 94+
- **Total Lines of Code**: ~30,000+
- **Packages**: 3 (auth, db, UI)
- **UI Components**: 40+
- **Data Models**: 8
- **Dialog Classes**: 15+
- **Database Tables**: 14+
**Custom Theme Methods**: 15+ (including new placeholder & refresh helpers)

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

=======
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

---

## 👤 Author

**SmartLedger v1.0**  
**Developer**: AdxConnect  
**Repository**: [Finance-Manager--Adx](https://github.com/adxconnect/Finance-Manager--Adx)  
**Email**: soft.link.reg@outlook.com | banerjeeaniket003@gmail.com
**LinkedIn**: [Aniket Banerjee]([https://linkedin.com/in/adxconnect](https://www.linkedin.com/in/aniket-banerjee-a36191295/))  

### Acknowledgments
- **Java Swing Community** for GUI framework
- **MySQL Team** for database management
- **Apache POI** for Excel operations
- **iTextPDF** for PDF generation
- **Google Fonts** for typography

---

## 📞 Support & Contact

### Getting Help
1. **Check Documentation**: Review this README and PROJECT_DOCUMENTATION.md
2. **Search Issues**: Look for existing GitHub issues
3. **Create Issue**: Open new issue for bugs or questions
4. **Community**: Join discussions in GitHub Discussions

### Common Issues
- **Database Connection**: Verify MySQL credentials in DBHelper.java
- **Missing Libraries**: Ensure all JAR files are in lib/ folder
- **Font Issues**: Check resources/ folder for TTF files
- **Compilation Errors**: Use UTF-8 encoding flag

### System Requirements
- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **RAM**: 512MB minimum, 1GB recommended
- **Storage**: 50MB for application, plus database space
- **Display**: 1024×768 minimum resolution

---

<div align="center">

**🎉 Thank you for choosing SmartLedger! 🎉**

*Transform your financial management experience with modern technology and beautiful design.*

---

**SmartLedger v1.0** - *Released November 2025*  
*Made with ❤️ by Adx Connect*

</div>
>>>>>>> d965d71877fe7f12493a76d4f43a98f620b739e8
