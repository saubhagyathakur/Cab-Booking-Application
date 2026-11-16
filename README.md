# CabGo – Enterprise Cab Booking & Ride-Hailing Platform
> **Production-grade full-stack ride-hailing architecture built with Java 17+, Spring Boot 3, Spring Security (JWT), PostgreSQL / Supabase, WebSockets (STOMP), Stripe SDK, and React + Tailwind CSS.**

---

## 1️⃣ Project Overview

**CabGo** is an enterprise-scale full-stack ride-hailing platform inspired by modern systems like Uber, Ola, and Lyft. It handles the entire ride lifecycle seamlessly:
- **Riders** can check distance/time estimates across multiple vehicle tiers (Economy, Comfort, Premier, XL), apply promo codes, book rides, track nearby drivers on live maps, view start-ride security PINs (OTP), trigger emergency SOS broadcasts, and download official PDF tax invoices.
- **Drivers** have a dedicated operations dashboard to toggle online/offline availability, receive real-time ride request alerts, accept/decline rides, verify rider OTPs, execute trips with real-time GPS location simulation, and monitor lifetime earnings and star ratings.
- **Admins** have a comprehensive operations control room with real-time platform KPIs, trip monitoring, fleet roster management, emergency SOS alert handling, and support ticket resolution.
- **Security & Payments**: Stateless JWT token authentication, BCrypt password hashing, role-based authorization (`ROLE_RIDER`, `ROLE_DRIVER`, `ROLE_ADMIN`), and Stripe payment intent integration with receipt generation.

---

## 2️⃣ Technology Stack

| Domain | Technologies |
|---|---|
| **Backend Framework** | Java 17 (Temurin), Spring Boot 3.2.4, Spring Web |
| **Security & Auth** | Spring Security 6, JJWT (HMAC-SHA256), BCrypt, Role-Based Access Control |
| **Persistence & Database** | Hibernate 6 / Spring Data JPA, PostgreSQL / Supabase (Prod), H2 In-Memory (Dev fallback) |
| **Real-Time Communication** | Spring WebSocket, STOMP over SockJS, SimpMessagingTemplate |
| **Payments** | Stripe Java SDK (v24.20.0), PaymentIntent API, Webhooks |
| **Invoicing & Documents** | OpenPDF (iText LGPL standard) for downloadable PDF receipts, Spring Mail |
| **Frontend** | React 18, Vite 5, Tailwind CSS 3, Lucide React, Axios |
| **Interactive Maps** | Leaflet with CartoDB & OpenStreetMap tile layers (zero external API cost) |
| **DevOps & Deployment** | Docker (Multi-stage), Docker Compose, Nginx, GitHub Actions CI/CD |

---

## 3️⃣ System Architecture

```
[ Frontend: React + Tailwind CSS + Leaflet ]
                  │
     HTTP REST    │    STOMP / SockJS WebSockets
     (JWT Bearer) │    (/ws, /topic/*)
                  ▼
[ Spring Boot 3 Security & REST Gateway (Port 8080) ]
        │
        ├── AuthController (`/api/v1/auth/**`)
        ├── RideController (`/api/v1/rides/**`)
        ├── DriverController (`/api/v1/drivers/**`)
        ├── PaymentController (`/api/v1/payments/**`)
        ├── ReceiptController (`/api/v1/receipts/**`)
        ├── RatingController (`/api/v1/ratings/**`)
        └── AdminController (`/api/v1/admin/**`)
        │
        ▼
[ Service Layer & Concurrency Engine ]
        ├── RideService (Haversine Distance, Dynamic Pricing, OTP Lifecycle)
        ├── DriverService (Fleet availability, Live GPS updates)
        ├── PaymentService (Stripe PaymentIntents & Webhooks)
        ├── ReceiptService (OpenPDF Invoice Generation)
        └── WebSocketService (STOMP Topic Broadcaster)
        │
        ▼
[ Spring Data JPA / Hibernate Layer ]
        │
        ▼
[ Database: PostgreSQL / Supabase or Embedded H2 ]
```

---

## 4️⃣ Database Schema & Entity Relationships

The relational model includes 7 core tables optimized with indexes:

1. **`users`**: Identity, credentials, role (`ROLE_RIDER`, `ROLE_DRIVER`, `ROLE_ADMIN`), contact information.
2. **`drivers`**: One-to-one with `users`, vehicle details (model, plate, type), driver status (`AVAILABLE`, `BUSY`, `OFFLINE`), live coordinates (`current_lat`, `current_lng`), rating score, and earnings.
3. **`rides`**: Many-to-one with rider and driver, pickup/drop coordinates, vehicle type, distance (km), estimated duration, base fare, discount, final fare, status (`REQUESTED`, `ACCEPTED`, `ARRIVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), 4-digit security PIN (`otp`), and payment status.
4. **`payments`**: One-to-one with `rides`, amount, currency, Stripe PaymentIntent ID, status (`PENDING`, `PAID`, `REFUNDED`).
5. **`ratings`**: One-to-one with `rides`, 1–5 star rating, feedback comment, automatic driver score recalculation.
6. **`promo_codes`**: Active promotional discounts (e.g. `SAVE20`, `WELCOME50`) with percentage and max caps.
7. **`support_tickets`**: Customer inquiries, ride disputes, and admin resolution workflows.

---

## 5️⃣ Real-Time WebSocket Topics

The application uses STOMP over WebSocket with endpoint `/ws`:

| Topic / Channel | Direction | Description |
|---|---|---|
| `/topic/rides/{rideId}` | Server &rarr; Client | Real-time status changes for a specific ride |
| `/topic/rider/{riderId}/rides` | Server &rarr; Rider | Active ride updates pushed directly to the rider |
| `/topic/driver/{driverId}/requests` | Server &rarr; Driver | Push notification for nearby available drivers when a ride is requested |
| `/topic/driver/{driverId}/rides` | Server &rarr; Driver | Active trip updates for the assigned driver |
| `/topic/drivers/nearby` | Server &rarr; Map | Live coordinates stream of moving drivers on the map |
| `/topic/admin/alerts` | Server &rarr; Admin | High-priority operational alerts and SOS distress signals |
| `/topic/sos` | Broadcast | Emergency distress signal broadcasting |
| `/app/driver/{driverId}/location` | Driver &rarr; Server | Inbound driver location updates |
| `/app/rides/{rideId}/sos` | Client &rarr; Server | Inbound emergency SOS trigger |

---

## 6️⃣ Quick Start & Running Locally

### Prerequisites
- **Java 17+** (`java -version`)
- **Maven 3.8+** (`mvn -v`)
- **Node.js 18+** (`node -v` and `npm -v`)
- *(Optional)* Docker & Docker Compose

### 1. Launch Backend (Spring Boot)
The default development profile is pre-configured with H2 in PostgreSQL mode, automatically seeding demo accounts, active drivers, promo codes, and completed rides:

```bash
cd backend
mvn clean spring-boot:run
```
The backend starts on `http://localhost:8080`.
- H2 Web Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:cabbooking`, User: `sa`, Password: *blank*)

### 2. Launch Frontend (React + Vite)
In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

### 3. Pre-Seeded Demo Accounts (1-Click Switcher Available in Navbar)
| Persona | Email | Password | Role |
|---|---|---|---|
| **Rider (Alex Johnson)** | `rider@example.com` | `password123` | `ROLE_RIDER` |
| **Driver (John Doe)** | `driver@example.com` | `password123` | `ROLE_DRIVER` |
| **Driver (Sarah Connor)** | `driver2@example.com` | `password123` | `ROLE_DRIVER` |
| **Admin (Super Admin)** | `admin@example.com` | `admin123` | `ROLE_ADMIN` |

---

## 7️⃣ Running with Docker Compose (Production Setup)

To launch the complete containerized stack (PostgreSQL 16 + Spring Boot + React/Nginx):

```bash
docker-compose up --build
```
- Frontend: `http://localhost` (or `http://localhost:80`)
- Backend API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

---

## 8️⃣ REST API Documentation

### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/register`: Register new rider or driver
- `POST /api/v1/auth/login`: Authenticate and receive JWT Bearer token
- `GET /api/v1/auth/me`: Get current authenticated user profile

### Rides (`/api/v1/rides`)
- `POST /api/v1/rides/estimate`: Calculate distance, duration, and fares across vehicle tiers
- `POST /api/v1/rides/request`: Request a new ride (generates 4-digit OTP PIN and notifies nearby drivers)
- `GET /api/v1/rides/active`: Fetch rider's active ride
- `GET /api/v1/rides/my-rides`: Fetch trip history
- `POST /api/v1/rides/{id}/cancel`: Cancel trip

### Drivers (`/api/v1/drivers`)
- `GET /api/v1/drivers/nearby`: Fetch available drivers within radius
- `PATCH /api/v1/drivers/status`: Toggle `AVAILABLE` or `OFFLINE`
- `PATCH /api/v1/drivers/location`: Update GPS latitude/longitude
- `GET /api/v1/drivers/available-requests`: View pending ride requests
- `POST /api/v1/drivers/rides/{id}/accept`: Accept ride request
- `POST /api/v1/drivers/rides/{id}/arrived`: Driver arrives at pickup
- `POST /api/v1/drivers/rides/{id}/start?otp={otp}`: Verify rider PIN and start ride
- `POST /api/v1/drivers/rides/{id}/complete`: Complete ride and collect fare

### Payments & Invoicing
- `POST /api/v1/payments/create-intent`: Create Stripe PaymentIntent
- `POST /api/v1/payments/confirm`: Confirm Stripe payment
- `GET /api/v1/receipts/{id}/download`: Download official PDF tax invoice
- `POST /api/v1/receipts/{id}/email`: Email PDF receipt to rider

---

## 9️⃣ Two-Week Development Plan & Milestones

- **Week 1 – Core Backend & Foundations**:
  - *Day 1–2*: Project setup, Spring Security + JWT authentication, role modeling.
  - *Day 3*: JPA entities (`User`, `Driver`, `Ride`, `Payment`, `Rating`), repository layer, DDL script.
  - *Day 4*: Haversine distance engine, tiered fare calculation, promo code verification.
  - *Day 5*: Driver matching algorithm and state machine.
  - *Day 6*: WebSocket STOMP broker setup, ride update topic broadcasting.
  - *Day 7*: Unit tests with MockMvc and `@SpringBootTest`.
- **Week 2 – Advanced Features & Deployment**:
  - *Day 8*: Stripe Java SDK payment intent creation and webhook verification.
  - *Day 9*: OpenPDF invoice generation and receipt emailing.
  - *Day 10*: 1–5 star rating system and dynamic driver score recalculation.
  - *Day 11*: Security hardening, CORS filters, input validation.
  - *Day 12*: React + Tailwind dashboard with Leaflet map and live GPS simulation.
  - *Day 13*: End-to-end integration testing and bug fixing.
  - *Day 14*: Multi-stage Docker builds, Docker Compose, and GitHub Actions CI/CD.

---

## 🔟 Resume Highlights

Add these bullet points to your resume:
- *Architected a full-stack, enterprise-grade ride-hailing platform using Java 17, Spring Boot 3, Hibernate/JPA, and PostgreSQL, supporting concurrent ride requests and real-time fleet coordination.*
- *Engineered a low-latency real-time event pipeline using Spring WebSockets and STOMP over SockJS to broadcast live GPS driver locations, trip state transitions, and emergency SOS alerts.*
- *Implemented a secure authentication and authorization pipeline using Spring Security 6, JWT, and BCrypt, enforcing strict Role-Based Access Control (RBAC) across Rider, Driver, and Admin boundaries.*
- *Integrated Stripe Java SDK for seamless PaymentIntent processing, and automated OpenPDF tax invoice generation for trip receipts.*
- *Dockerized services with multi-stage builds and container orchestration via Docker Compose, automated through GitHub Actions CI/CD.*
