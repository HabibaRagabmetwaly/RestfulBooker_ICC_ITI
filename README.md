# RestfulBooker_ICC_ITI
<p align="center">
  <img src="https://media.giphy.com/media/QBd2kLB5qDmysEXre9/giphy.gif" width="280" alt="API Testing"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Rest%20Assured-API%20Testing-green?style=for-the-badge" />
  <img src="https://img.shields.io/badge/TestNG-Framework-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Allure-Report-purple?style=for-the-badge&logo=allure" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/build-passing-brightgreen" />
  <img src="https://img.shields.io/badge/coverage-~90%25-yellow" />
</p>
Restful Booker API Automation | RestAssured + TestNG + Allure
Author: Habiba Ragab Abdelmoneam
This repository contains an API automation framework for the public **Restful Booker** API using:

- **Java**
- **RestAssured**
- **TestNG**
- **Maven**
- **Allure Reports**
- **Log4j2**

The goal of this project is to provide a clean, production-like example of API automation including:
- Happy-path booking flows
- Negative & edge cases
- Security-oriented scenarios (XSS, SQL injection)
- Full end-to-end booking lifecycle
- Rich reporting with Allure + logging

---

## 🌐 Target API

- **Base URL:** `https://restful-booker.herokuapp.com`
- **Main Resources:**
  - `POST /auth` – Get auth token
  - `GET /booking` – List bookings (with optional filters)
  - `GET /booking/{id}` – Get booking by ID
  - `POST /booking` – Create booking
  - `PUT /booking/{id}` – Update booking
  - `PATCH /booking/{id}` – Partial update
  - `DELETE /booking/{id}` – Delete booking

---

## 🛠 Tech Stack

| Component      | Technology                              |
|----------------|------------------------------------------|
| Language       | Java (configured via Maven compiler)     |
| Test Framework | TestNG                                   |
| API Client     | RestAssured                             |
| Reporting      | Allure (allure-testng, allure-rest-assured) |
| Logging        | Log4j2                                   |
| Build Tool     | Maven                                   |

Key dependencies (see `pom.xml` for full list):
- `io.rest-assured:rest-assured:5.5.6`
- `org.testng:testng:7.11.0`
- `io.qameta.allure:allure-testng` / `allure-rest-assured`
- `com.fasterxml.jackson.core:jackson-databind`
- `org.apache.logging.log4j:log4j-core`, `log4j-api`

---

## 📂 Project Structure

```text
RestfulBooker
├─ run-tests-quick.bat
├─ run-tests-with-allure.bat
├─ pom.xml
├─ src
│  ├─ main
│  │  └─ java
│  │     └─ org
│  │        └─ example
│  │           └─ Main.java
│  ├─ resources
│  │  ├─ log4j2.xml          # Logging configuration
│  │  └─ testng.xml          # TestNG suites: Auth, Happy Path, Negative, E2E
│  └─ test
│     └─ java
│        ├─ base
│        │  └─ BaseTest.java
│        ├─ models
│        │  ├─ AuthRequest.java
│        │  ├─ AuthResponse.java
│        │  ├─ Booking.java
│        │  ├─ BookingDates.java
│        │  └─ BookingResponse.java
│        ├─ services
│        │  ├─ AuthService.java
│        │  └─ BookingService.java
│        └─ tests
│           ├─ AuthTests.java
│           ├─ BookingTests.java
│           ├─ NegativeBookingTests.java
│           └─ BookingE2ETest.java
## 📚 Documentation

- 📘 [API Documentation (DOCX)](docs/restful_booker_api_documentation.docx)
- ✅ [Test Cases Matrix (Excel)](docs/restful_booker_test_cases.xlsx)
- 🌐 [Static Docs Site (HTML)](docs/restful_booker_docs_site.html)
