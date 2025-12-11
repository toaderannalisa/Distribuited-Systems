<<<<<<< HEAD
# DS2025_30242_Toader_Annalisa_Assignment_2



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

* [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
* [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.com/toaderannalisa-group/ds2025_30242_toader_annalisa_assignment_2.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

* [Set up project integrations](https://gitlab.com/toaderannalisa-group/ds2025_30242_toader_annalisa_assignment_2/-/settings/integrations)

## Collaborate with your team

* [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
* [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
* [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
* [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
* [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

* [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/)
* [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
* [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
* [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
* [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
=======
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

>>>>>>> main
