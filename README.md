# 🏥 MediCare — Hospital Appointment & Patient Management System

A production-grade, full-stack application for managing hospital operations including
patient registration, doctor management, appointment booking, prescriptions, and
medical records — with role-based dashboards and analytics.

---

## 📋 Tech Stack

| Layer        | Technology                                           |
| ------------ | ---------------------------------------------------- |
| **Backend**  | Java 21, Spring Boot 3.3.5, Maven                   |
| **Security** | Spring Security + JWT + Google OAuth2.0              |
| **ORM**      | Spring Data JPA + Hibernate                          |
| **Database** | MySQL 8+                                             |
| **Frontend** | React 18 (Vite), React Router, Bootstrap 5           |
| **API calls**| Axios (with JWT interceptor)                         |
| **Charts**   | Recharts                                             |
| **Testing**  | JUnit 5 + Mockito (backend), Postman (API)           |

---

## 🏗 Architecture

```
┌─────────────────┐       REST/JSON        ┌──────────────────────┐       JPA        ┌───────┐
│  React (Vite)   │  ────────────────────►  │  Spring Boot 3.x    │  ──────────────► │ MySQL │
│  Port 5173      │  ◄────────────────────  │  Port 8080          │  ◄────────────── │       │
└─────────────────┘    Axios + JWT token    └──────────────────────┘                  └───────┘
```

Backend layered pattern:
```
Controller → Service (interface + impl) → Repository → Entity → MySQL
     ↕              ↕
   DTOs        Business Rules
     ↕              ↕
  Validation    Exceptions
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** (JDK)
- **Maven 3.9+**
- **MySQL 8+**
- **Node.js 18+** & npm
- (Optional) Eclipse IDE with STS 5.0

### Java 21 toolchain

The Maven build requires a JDK in the `[21,22)` range and compiles with the Java 21 release target. Install the Windows x64 JDK (not the JRE) from [Eclipse Temurin 21 releases](https://adoptium.net/temurin/releases/?version=21). Select **Windows**, **x64**, and **JDK**, then install the MSI. This machine has JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot`; if you install another version or choose a different directory, use that JDK's path in the commands below.

In PowerShell, set the installed path for the current terminal and verify both tools use it:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version ; mvn -version
```

Both commands should report Java 21. Then run the backend from `hospital-backend` with `mvn spring-boot:run`.

In Eclipse/STS, open **Window > Preferences > Java > Installed JREs**, add/select the JDK 21 directory as a JDK, and set **Compiler compliance level** to 21. Then open **Project > Properties > Java Compiler**, enable project-specific settings if needed, and set compliance to 21. Also configure the run configuration to use the JDK 21 JRE.

Maven Enforcer checks the Maven JVM version and fails early with a clear message if Java 21 is not selected. Lombok's annotation processor is pinned explicitly for reliable builds.

### 1. Database Setup

```sql
CREATE DATABASE hospital_db;
-- The app auto-creates tables via hibernate ddl-auto=update
```

### 2. Backend Setup

```bash
cd hospital-backend

# Configure MySQL credentials using environment variables (PowerShell):
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<your-local-MySQL-password>"
$jwtBytes = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
# Persist JWT_SECRET for later PowerShell sessions if desired:
[Environment]::SetEnvironmentVariable("JWT_SECRET", $env:JWT_SECRET, "User")

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run
```

The backend starts at **http://localhost:8080**.  
On first startup, the **DataSeeder** auto-creates:
- Admin: `admin@hospital.com` / `Admin@1234`
- 4 Doctors (dr.sharma, dr.patel, dr.mehta, dr.gupta) / `Doctor@1234`
- 1 Patient: `patient@hospital.com` / `Patient@1234`
- 10 departments

### 3. Frontend Setup

```bash
cd hospital-frontend

# Install dependencies
npm install

# Start Vite dev server
npm run dev
```

Frontend runs at **http://localhost:5173**.

### 4. Google sign-in (Optional)

The project uses Google Identity Services to return an ID token, which the backend verifies against Google's tokeninfo endpoint and the configured client audience. A real OAuth client must be created in your Google Cloud project; placeholder or revoked client IDs cause Google's `Error 401: invalid_client` page.

1. In [Google Cloud Console](https://console.cloud.google.com), select the intended project and create an OAuth client with application type **Web application**.
2. Add both `http://localhost` and `http://localhost:5173` under **Authorized JavaScript origins**. Add redirect URIs only if you later change the frontend to a redirect-based OAuth flow.
3. Copy the OAuth **Client ID** (never put the client secret in the frontend).
4. Set the same Client ID for the frontend and backend. In PowerShell:

   ```powershell
   $env:VITE_GOOGLE_CLIENT_ID="<client-id>.apps.googleusercontent.com"
   $env:GOOGLE_CLIENT_ID=$env:VITE_GOOGLE_CLIENT_ID
   ```

   Or set `VITE_GOOGLE_CLIENT_ID` in `hospital-frontend/.env` and `GOOGLE_CLIENT_ID` in the backend environment. Restart both processes after changing either value.

5. If Google's consent screen is in testing mode, add the account (for example, `dipalikarad25@gmail.com`) as a test user in the OAuth consent screen's **Audience/Test users** settings.

### 5. Email (Forgot Password)

Configure your SMTP credentials in `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 6. GitHub source and deployment

This repository includes a GitHub Actions workflow that runs the backend tests on Java 21 and builds the frontend on pushes and pull requests. GitHub Pages only serves static frontend files; it cannot run this Spring Boot backend or the MySQL database. For a working public deployment, host the React build on a static host, deploy Spring Boot on a Java-capable application host, and use a managed MySQL database. Set `VITE_API_BASE_URL` to the deployed API URL at frontend build time, and configure the backend's `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, and mail settings in the backend host's secret/environment settings. Never commit `.env` files or database/JWT/OAuth credentials.

To push this project to a new GitHub repository after creating an empty repository in your account:

```powershell
cd C:\Users\Dipali\Desktop\Medicare
git init
git add .
git status --short
# Confirm .env files, node_modules, dist, target, and .m2-repository are ignored.
git commit -m "Prepare MediCare project"
git branch -M main
git remote add origin https://github.com/<your-user>/<your-repository>.git
git push -u origin main
```

After the push, check the repository's **Actions** tab for the build and test results. See [GitHub Actions build/test guidance](https://docs.github.com/en/actions/tutorials/build-and-test-code) and the [GitHub Pages hosting limits](https://docs.github.com/en/pages/getting-started-with-github-pages/creating-a-github-pages-site).

---

## 👥 Roles & Default Credentials

| Role      | Email                     | Password       |
| --------- | ------------------------- | -------------- |
| Admin     | admin@hospital.com        | Admin@1234     |
| Doctor    | dr.sharma@hospital.com    | Doctor@1234    |
| Doctor    | dr.patel@hospital.com     | Doctor@1234    |
| Doctor    | dr.mehta@hospital.com     | Doctor@1234    |
| Doctor    | dr.gupta@hospital.com     | Doctor@1234    |
| Patient   | patient@hospital.com      | Patient@1234   |

---

## 🔑 API Endpoints Summary

### Auth (Public)
| Method | Endpoint                       | Description            |
| ------ | ------------------------------ | ---------------------- |
| POST   | `/api/auth/register`           | Patient registration   |
| POST   | `/api/auth/login`              | Email/password login   |
| POST   | `/api/auth/google`             | Google OAuth2 login    |
| POST   | `/api/auth/forgot-password`    | Send reset email       |
| POST   | `/api/auth/reset-password`     | Reset with token       |

### Admin (`ROLE_ADMIN`)
| Method | Endpoint                                   | Description              |
| ------ | ------------------------------------------ | ------------------------ |
| GET    | `/api/admin/dashboard`                     | Dashboard stats          |
| POST   | `/api/admin/departments`                   | Create department        |
| GET    | `/api/admin/departments`                   | List departments         |
| PUT    | `/api/admin/departments/{id}`              | Update department        |
| DELETE | `/api/admin/departments/{id}`              | Delete department        |
| POST   | `/api/admin/doctors`                       | Create doctor account    |
| GET    | `/api/admin/doctors`                       | List/search doctors      |
| PUT    | `/api/admin/doctors/{profileId}`           | Update doctor profile    |
| PATCH  | `/api/admin/doctors/{userId}/toggle-status`| Activate/deactivate      |
| GET    | `/api/admin/patients`                      | List all patients        |
| PATCH  | `/api/admin/patients/{userId}/toggle-status`| Toggle patient status   |
| GET    | `/api/admin/appointments`                  | Filter all appointments  |
| GET    | `/api/admin/reports/appointments-per-doctor`| Report by doctor        |
| GET    | `/api/admin/reports/appointments-per-department`| Report by dept       |
| GET    | `/api/admin/reports/daily`                 | Daily appointment chart  |

### Doctor (`ROLE_DOCTOR`)
| Method | Endpoint                                      | Description              |
| ------ | --------------------------------------------- | ------------------------ |
| GET    | `/api/doctor/dashboard`                       | Doctor dashboard         |
| GET    | `/api/doctor/profile`                         | Get own profile          |
| PUT    | `/api/doctor/profile`                         | Update own profile       |
| POST   | `/api/doctor/availability`                    | Add availability slot    |
| GET    | `/api/doctor/availability`                    | List availability        |
| PUT    | `/api/doctor/availability/{id}`               | Update slot              |
| DELETE | `/api/doctor/availability/{id}`               | Remove slot              |
| GET    | `/api/doctor/appointments`                    | My appointments (filter) |
| PATCH  | `/api/doctor/appointments/{id}/status`        | Confirm/Complete/No-show |
| POST   | `/api/doctor/prescriptions`                   | Write prescription       |
| GET    | `/api/doctor/prescriptions`                   | List my prescriptions    |
| POST   | `/api/doctor/medical-records`                 | Add medical record       |
| GET    | `/api/doctor/patients/{profileId}/history`    | Patient medical history  |

### Patient (`ROLE_PATIENT`)
| Method | Endpoint                                      | Description              |
| ------ | --------------------------------------------- | ------------------------ |
| GET    | `/api/patient/profile`                        | Get own profile          |
| PUT    | `/api/patient/profile`                        | Update profile           |
| GET    | `/api/patient/doctors`                        | Search/browse doctors    |
| GET    | `/api/patient/doctors/{profileId}`            | Doctor detail            |
| GET    | `/api/patient/doctors/{profileId}/slots`      | Available slots by date  |
| POST   | `/api/patient/appointments`                   | Book appointment         |
| GET    | `/api/patient/appointments`                   | My appointments (filter) |
| PATCH  | `/api/patient/appointments/{id}/cancel`       | Cancel appointment       |
| PATCH  | `/api/patient/appointments/{id}/reschedule`   | Reschedule               |
| GET    | `/api/patient/prescriptions`                  | My prescriptions         |
| GET    | `/api/patient/history`                        | My medical history       |

---

## 📊 Business Rules Enforced

1. **No double-booking** — DB unique constraint + service check
2. **Availability-only booking** — slots generated from doctor schedule
3. **No past bookings** — date validation
4. **Weekend defaults** — Sat/Sun blocked unless doctor adds availability
5. **2-hour cancellation window** — time-based check
6. **Doctor ownership** — only assigned doctor manages their appointments
7. **Prescription rules** — COMPLETED only, assigned doctor only, one per appointment
8. **Data access control** — doctors see only their patients' history
9. **Account deactivation** — blocked from login
10. **Patient overlap check** — prevents double-booked patient time

---

## 📬 Postman Collection

Import `Hospital-Management-API.postman_collection.json` from the project root.

**Setup:**
1. Import into Postman
2. Run "Login (Admin)" to get a token
3. Copy `accessToken` from the response
4. Set the collection variable `token` to that value
5. All subsequent requests auto-attach the Bearer token

---

## 🧪 Running Tests

```bash
cd hospital-backend
mvn test
```

Tests cover:
- Appointment service (all 10 business rules)
- Prescription service (rule 7: COMPLETED-only, assigned-doctor-only, one-per-appointment)

---

## 📁 Project Structure

```
Medicare/
├── hospital-backend/
│   ├── pom.xml
│   └── src/main/java/com/hospital/
│       ├── HospitalApplication.java
│       ├── config/           # SecurityConfig, AppConfig, DataSeeder
│       ├── security/         # JWT filter, provider, UserDetailsService
│       ├── controller/       # Auth, Admin, Doctor, Patient, Department
│       ├── service/          # Interfaces
│       ├── service/impl/     # Implementations (business logic)
│       ├── repository/       # JpaRepositories
│       ├── entity/           # JPA entities
│       ├── dto/request/      # Request DTOs with validation
│       ├── dto/response/     # Response DTOs
│       ├── enums/            # Role, Status, Gender, etc.
│       ├── exception/        # Global handler + custom exceptions
│       └── util/             # SecurityUtils
│
├── hospital-frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── .env
│   └── src/
│       ├── main.jsx          # Entry point (providers, bootstrap CSS)
│       ├── App.jsx           # Router
│       ├── api/              # Axios instance + API modules
│       ├── context/          # AuthContext (JWT decode, auto-logout)
│       ├── layouts/          # Admin, Doctor, Patient (sidebar + navbar)
│       ├── routes/           # ProtectedRoute
│       └── pages/
│           ├── auth/         # Login, Register, Forgot/Reset Password
│           ├── admin/        # Dashboard, Doctors, Patients, Depts, Appts, Reports
│           ├── doctor/       # Dashboard, Appointments, Availability, Prescriptions, Profile
│           └── patient/      # Dashboard, Doctors, Book, Appointments, Prescriptions, History, Profile
│
└── Hospital-Management-API.postman_collection.json
```

---

## 📌 Eclipse IDE Import

1. **Backend**: File → Import → Maven → Existing Maven Projects → select `hospital-backend`
2. **Frontend**: File → Import → General → Existing Projects → select `hospital-frontend`
3. Install Lombok plugin for Eclipse (Help → Eclipse Marketplace → "Lombok")
4. Run `HospitalApplication.java` as Spring Boot App

---

## GitHub build and deployment

The GitHub Actions workflow builds the Java 21 backend and React frontend on pushes and pull requests. It packages the backend without running database integration tests, so GitHub does not need access to your local MySQL credentials.

GitHub Pages can host static frontend files, but it cannot run the Spring Boot API or MySQL database. A full deployment needs a Java application host and a managed MySQL database. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, and the production frontend/API origins in the hosting provider's secret/environment settings. Never commit `.env` files or production credentials.

To publish this project, create an empty GitHub repository, then add it as the `origin` remote and push the `main` branch. Keep the repository private if it contains coursework or personal project data.

## 📄 License

This project is for educational and demonstration purposes.
