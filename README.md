# 🎯 SledgerAI v2

<div align="center">
  <img src="logo/Logo1.png" alt="SledgerAI Logo" width="200" />
</div>

<h3 align="center">AI-Driven Financial Management & Virtual CA Service</h3>

<div align="center">
  
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-green.svg)](https://spring.io/projects/spring-boot)
[![Python FastAPI](https://img.shields.io/badge/Python_AI-FastAPI-teal.svg)](https://fastapi.tiangolo.com/)
[![Firebase](https://img.shields.io/badge/Firebase-Integration-orange.svg)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)

</div>

---

## 🌟 Overview

**SledgerAI** has evolved into a next-generation personal finance management platform. We provide a comprehensive, multi-tiered ecosystem powered by a React frontend, a Spring Boot API, and an advanced AI virtual CA service to automate your accounting and provide actionable financial advice.

### 🎯 Key Highlights
- **🤖 Virtual CA & AI Parsing**: Built-in RAG service parses PDF bank statements, categorizes transactions intelligently, and provides CA-level tax advice (e.g., Section 80C, 80D optimizations).
- **🔒 Secure Authentication & Data**: Powered by Firebase Auth and Firestore for real-time synchronization, with role-based access control and admin dashboard capabilities.
- **🎓 Student Verification System**: Specialized gating and administrative workflows for verifying student subscriptions via Firebase Storage document uploads.
- **💳 Payment Integration**: Fully integrated Razorpay subscription gating and billing pipeline.
- **🎨 Glassmorphic Modern UI**: Premium, dark-themed responsive UI built with Tailwind/Vanilla CSS, React, and beautiful micro-animations.

---

## 🏗️ Architecture

SledgerAI relies on a modern microservices-inspired architecture:

1. **Frontend (`/frontend`)**
   - **Tech Stack**: React 19, Vite, Firebase SDK, Lucide Icons.
   - **Features**: Stunning landing page with glassmorphism pricing cards, secure user dashboard, admin user management panel, and file upload capabilities.
2. **Backend (`/backend`)**
   - **Tech Stack**: Spring Boot (Java 21), Maven, Firebase Admin SDK.
   - **Features**: Secure REST APIs, business logic validation, and robust data management for core system entities.
3. **AI Service (`/ai_service`)**
   - **Tech Stack**: Python, FastAPI, Google Gemini 3.5 Flash, Razorpay SDK.
   - **Features**: AI-driven PDF statement parsing, intelligent categorization, zero-cost static CA rules Engine, audit logging, and payment verification handling.
4. **Legacy Desktop Application (`/src`, `/db`, `/auth`)**
   - **Tech Stack**: Java Swing, JDBC, MySQL.
   - **Features**: The original V1 desktop client for local usage. *Currently maintained in the root directory for legacy support.*

---

## 🚀 Quick Start

### 1. Frontend Setup (React/Vite)
Navigate to the `frontend` directory:
```bash
cd frontend
npm install
npm run dev
```
*The app will be available at `sledgerai.com`.*

### 2. AI Service Setup (Python/FastAPI)
Ensure you have Python installed, then navigate to `ai_service`:
```bash
cd ai_service
pip install -r requirements.txt
# Set your GEMINI_API_KEY in the .env file
uvicorn main:app --reload --port 8000
```

### 3. Backend Setup (Spring Boot)
Ensure you have Java 21+ and Maven installed:
```bash
cd backend
./mvnw spring-boot:run
```

---

## 💳 Subscription Tiers

SledgerAI offers flexible subscription plans managed through our sophisticated billing API:
- **Free Plan**: Basic transaction tracking and limited AI insights.
- **Student Plan**: Discounted tier accessible upon successful upload and administrative verification of valid student ID.
- **Pro Tier**: Full access to the Virtual CA, automated portfolio parsing, unlimited tax advisory, and premium insights.

---

## 🤝 Contributing

We welcome contributions!
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**SledgerAI** - *Revolutionizing Financial Management*  
*Developed with ❤️ by adxconnect*

</div>
