
# Smart Energy Management System

Platformă microservicii distribuită pentru monitorizarea în timp real a consumului energetic, gestionarea utilizatorilor și dispozitivelor .



## Arhitectura

Sistemul este compus din 3 microservicii backend independente, un frontend React, și infrastructură de messaging și persistență:

### Flux de Date

1. **Autentificare & Gestionare Utilizatori**
   - Frontend → Person Backend (`/auth/login`, `/api/persons/`)
   - Person Backend → RabbitMQ (`sync.queue`) → Monitoring Backend (sync users)

2. **Gestionare Dispozitive**
   - Frontend → Device Backend (`/api/devices/`)
   - Device Backend → RabbitMQ (`sync.queue`) → Monitoring Backend (sync devices)

3. **Monitorizare Energie**
   - Dispozitiv  → RabbitMQ (`energy.data.queue`) → Monitoring Backend
   - Monitoring Backend → Database (energy_measurements)
   - Frontend → Monitoring Backend (`/api/energy/measurements/{id}/hourly`) → Chart Display
   - Monitoring Backend → WebSocket → Frontend (real-time updates)


## Notificări în timp real

Sistemul include un microserviciu dedicat pentru notificări (demonotification-backend), care trimite alerte către utilizatori atunci când un dispozitiv depășește limita de consum (`maxConsumption`).

**Flux notificare:**
1. Monitoring Backend detectează depășirea valorii `maxConsumption` pentru un device.
2. Trimite o notificare (prin RabbitMQ sau direct HTTP) către demonotification-backend.
3. demonotification-backend transmite notificarea către frontend prin WebSocket (`/ws/notifications`).
4. Utilizatorul vede instant mesajul de alertă în aplicație.

**Exemplu notificare:**
> Dispozitivul X a depășit consumul maxim admis!

**Utilizare:**
- Frontend-ul ascultă pe WebSocket și afișează notificările în UI.
- Notificările sunt generate automat, nu necesită acțiune manuală.

---

## Chatbot (Asistent conversațional)

Platforma include un microserviciu chatbot (chatbot-backend) care răspunde automat la întrebările utilizatorilor, oferind suport sau informații despre sistem.

**Flux chatbot:**
1. Utilizatorul trimite o întrebare din interfața de chat (frontend).
2. Frontend-ul face un request POST la `/api/chatbot/ask` cu întrebarea.
3. chatbot-backend procesează întrebarea și returnează un răspuns (bazat pe reguli simple sau AI).
4. Răspunsul este afișat în chat-ul aplicației.

---

## Tehnologii Utilizate

### Backend
- **Java 21** - Limbaj de programare
- **Spring Boot 3.4.0** - Framework pentru microservicii
  - Spring Web
  - Spring Data JPA
  - Spring AMQP (RabbitMQ)
  - Spring WebSocket
- **TypeScript** - Limbaj tipizat
- **Recharts** - Vizualizare date (charts)
- **Nginx** - Web server și reverse proxy

### Infrastructure
- **Docker Desktop** instalat și pornit
- **Porturi disponibile**: 80, 3307, 3309, 3310, 5672, 8080, 8081, 8082, 8083, ,8090,8091,15672
- **Sistem de operare**: Windows/Linux/MacOS cu minim 8GB RAM
- **(Opțional) Python 3.x** - Pentru rularea simulatorului de dispozitive
- **(Opțional) Node.js 20+** - Pentru development frontend


- `docker-compose.yml` - Orchestrare servicii
- `demodevice/Dockerfile` - Device backend
- `demoperson/Dockerfile` - Person backend
- `demomonitoring/Dockerfile` - Monitoring backend
- `react-demo/Dockerfile` - Frontend
- `traefik.yml` - Configurare Traefik

# Build toate imaginile și pornire servicii

# SAU pentru rulare în background
docker-compose up -d --build

- `energy-frontend` (port 80)
- `person-backend` (port 8081)
- `reverse-proxy` (port 8080)
- `person-db` (port 3307)
- `device-db` (port 3309)
2. **Login Implicit**:
   - **Client**: `anto` / `anto`

3. **Workflow de Bază**:
   - Login ca admin
   - Asociere dispozitive la utilizatori
   - Vizualizare consumuri (selectați dispozitiv → afișare grafic)

### Accesare Servicii Directe

- **Frontend**: http://localhost
- **Person API**: http://localhost:8081/api/people
- **Device API**: http://localhost:8082/api/devices
- **Monitoring API**: http://localhost:8083/api/energy
- **RabbitMQ Management**: http://localhost:15672 (user: `anna`, pass: `anna`)
# Stop și ștergere volume (resetare completă)
docker-compose down -v
## Simulator de Dispozitive

Pentru testare, sistemul include un simulator Python care generează date de consum energetic.

### Prerequisite Simulator

```bash
pip install pika

### Utilizare Simulator

```bash
cd simulator
python run_simulator.py <device-uuid> <interval-seconds>

# Exemplu: Trimite măsurători la fiecare 10 secunde pentru dispozitivul specificat
python run_simulator.py 82a4c8e1-68d8-404b-b115-c72a6232ae48 10
```

### Parametri
- `interval-seconds`: Interval între măsurători (recomandat: 10-60 secunde)
### Output Așteptat

Connecting to RabbitMQ...
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

>>>>>>> main
