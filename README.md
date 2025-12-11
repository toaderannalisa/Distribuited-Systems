# Smart Energy Management System

Platformă microservicii distribuită pentru monitorizarea în timp real a consumului energetic, gestionarea utilizatorilor și dispozitivelor IoT.



## Arhitectura

Sistemul este compus din 3 microservicii backend independente, un frontend React, și infrastructură de messaging și persistență:

### Componente Principale

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (React)                         │
│                    http://localhost:80 (Nginx)                   │
└────────────┬────────────────────────────────────────────────────┘
             │
             ├─────────────────────────────────────────────────────┐
             │                                                     │
             ▼                                                     ▼
┌────────────────────────┐                          ┌────────────────────────┐
│   Person Backend       │                          │   Device Backend       │
│   Port: 8081           │                          │   Port: 8082           │
│   DB: person_db (3307) │                          │   DB: device_db (3309) │
└───────┬────────────────┘                          └────────┬───────────────┘
        │                                                    │
        │  RabbitMQ (sync.queue)                            │
        │  ┌─────────────────────────────────────────────┐  │
        └──►     Message Broker (Port: 5672/15672)      ◄──┘
           └───────────────┬─────────────────────────────┘
                          │
                          │ (sync.queue + energy.data.queue)
                          │
                          ▼
              ┌────────────────────────────┐
              │  Monitoring Backend        │
              │  Port: 8083                │
              │  DB: monitoring_db (3310)  │
              │  WebSocket: /ws-energy     │
              └────────────────────────────┘
```

### Flux de Date

1. **Autentificare & Gestionare Utilizatori**
   - Frontend → Person Backend (`/auth/login`, `/api/persons/`)
   - Person Backend → RabbitMQ (`sync.queue`) → Monitoring Backend (sync users)

2. **Gestionare Dispozitive**
   - Frontend → Device Backend (`/api/devices/`)
   - Device Backend → RabbitMQ (`sync.queue`) → Monitoring Backend (sync devices)

3. **Monitorizare Energie**
   - Dispozitiv IoT → RabbitMQ (`energy.data.queue`) → Monitoring Backend
   - Monitoring Backend → Database (energy_measurements)
   - Frontend → Monitoring Backend (`/api/energy/measurements/{id}/hourly`) → Chart Display
   - Monitoring Backend → WebSocket → Frontend (real-time updates)

## Tehnologii Utilizate

### Backend
- **Java 21** - Limbaj de programare
- **Spring Boot 3.4.0** - Framework pentru microservicii
  - Spring Web
  - Spring Data JPA
  - Spring AMQP (RabbitMQ)
  - Spring WebSocket
- **MySQL 8.0** - Baze de date relaționale (3 instanțe separate)
- **RabbitMQ 3.13** - Message broker pentru comunicare asincronă
- **Maven** - Build tool

### Frontend
- **React 18** - UI framework
- **TypeScript** - Limbaj tipizat
- **Material-UI** - Componente UI
- **Recharts** - Vizualizare date (charts)
- **Nginx** - Web server și reverse proxy

### Infrastructure
- **Docker & Docker Compose** - Containerizare și orchestrare
- **Traefik** - Reverse proxy (opțional)

### Development Tools
- **Python 3.x** - Pentru simulator de dispozitive
- **Node.js 20+** - Pentru build frontend (development)

## Prerequisite

- **Docker Desktop** instalat și pornit
- **Porturi disponibile**: 80, 3307, 3309, 3310, 5672, 8080, 8081, 8082, 8083, 15672
- **Sistem de operare**: Windows/Linux/MacOS cu minim 8GB RAM
- **(Opțional) Python 3.x** - Pentru rularea simulatorului de dispozitive
- **(Opțional) Node.js 20+** - Pentru development frontend


- `docker-compose.yml` - Orchestrare servicii
- `demodevice/Dockerfile` - Device backend
- `demoperson/Dockerfile` - Person backend
- `demomonitoring/Dockerfile` - Monitoring backend
- `react-demo/Dockerfile` - Frontend
- `traefik.yml` - Configurare Traefik

### 3. Build și Start Servicii

# Build toate imaginile și pornire servicii
docker-compose up --build

# SAU pentru rulare în background
docker-compose up -d --build

**Așteptați** ~2-3 minute pentru ca toate serviciile să pornească complet.

### 4. Verificare Servicii Pornite


Ar trebui să vedeți 9 containere active:
- `energy-frontend` (port 80)
- `person-backend` (port 8081)
- `device-backend` (port 8082)
- `monitoring-backend` (port 8083)
- `reverse-proxy` (port 8080)
- `rabbitmq` (port 5672, 15672)
- `person-db` (port 3307)
- `device-db` (port 3309)
- `monitoring-db` (port 3310)

## Utilizare

### Accesare Aplicație

1. **Frontend Web**: Deschideți browser la `http://localhost`

2. **Login Implicit**:
   - **Admin**: `anna` / `anna`
   - **Client**: `anto` / `anto`

3. **Workflow de Bază**:
   - Login ca admin
   - Creare utilizatori noi (User Management)
   - Creare dispozitive (Device Management)
   - Asociere dispozitive la utilizatori
   - Vizualizare consumuri (selectați dispozitiv → afișare grafic)

### Accesare Servicii Directe

- **Frontend**: http://localhost
- **Person API**: http://localhost:8081/api/people
- **Device API**: http://localhost:8082/api/devices
- **Monitoring API**: http://localhost:8083/api/energy
- **RabbitMQ Management**: http://localhost:15672 (user: `anna`, pass: `anna`)
- **Traefik Dashboard**: http://localhost:8080

### Stop Servicii

```bash
# Stop și păstrare volume (date persistente)
docker-compose down

# Stop și ștergere volume (resetare completă)
docker-compose down -v
```

## Simulator de Dispozitive

Pentru testare, sistemul include un simulator Python care generează date de consum energetic.

### Prerequisite Simulator

```bash
pip install pika
```

### Utilizare Simulator

```bash
cd simulator
python run_simulator.py <device-uuid> <interval-seconds>

# Exemplu: Trimite măsurători la fiecare 10 secunde pentru dispozitivul specificat
python run_simulator.py 82a4c8e1-68d8-404b-b115-c72a6232ae48 10
```

### Parametri
- `device-uuid`: ID-ul dispozitivului (din baza de date)
- `interval-seconds`: Interval între măsurători (recomandat: 10-60 secunde)

### Output Așteptat

```
Connecting to RabbitMQ...
Connected successfully
Starting energy simulation for device: 82a4c8e1-68d8-404b-b115-c72a6232ae48
Sent measurement: timestamp=2025-12-09 17:30:45, value=0.75 kWh
Sent measurement: timestamp=2025-12-09 17:31:45, value=0.82 kWh
...
```

Datele vor apărea în:
1. RabbitMQ queue `energy.data.queue`
2. Monitoring backend (procesare)
3. Database `energy_measurements`
4. Frontend chart (după refresh)
5. WebSocket real-time updates

