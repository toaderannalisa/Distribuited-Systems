# Energy Management System 

This is an **Energy Management System** that permits authenticated users to access, monitor, and manage smart energy metering devices. The system is constructed as a set of loosely coupled, containerized microservices, each deployed independently, and orchestrated through a reverse proxy and API gateway.

* **Architecture:** Microservices (Person and Device).
* **API Gateway:** Traefik acts as a smart reverse proxy for unified external access.
* **Authentication:** HTTP Basic Authentication is used for stateless API access.
* **Authorization:** Role-Based Access Control (RBAC) enforced using Spring Security (ADMIN and CLIENT roles).
* **Deployment:** Full containerization via Docker and Docker Compose.
* **Data Isolation:** Two separate MySQL databases ensure strict data isolation between domains.
* **API Documentation:** Two separate Swagger (OpenAPI 3) interfaces are exposed for clarity.

## Project structure
This repository contains the following primary services orchestrated by `docker-compose.yml`:

| Service Name | Internal Port | Technology | Primary Function |
| :--- | :--- | :--- | :--- |
| **reverse-proxy** | 80/8080 | Traefik v3 | Routing and Load Balancing. |
| **frontend** | 80 | React / Nginx | Web Client UI. |
| **person-backend** | 8081 | Spring Boot 4 / Java 21 | User Management (CRUD) and Authentication logic. |
| **device-backend** | 8082 | Spring Boot 4 / Java 21 | Device Management (CRUD) and allocation logic. |
| **person-db** | 3306 | MySQL 8.0 | Stores user and role data. |
| **device-db** | 3306 | MySQL 8.0 | Stores device and consumption data. |

## Prerequisites
- **Docker** Engine
- **Docker Compose**

## Database (MySQL) — (Dockerized)
The application uses two separate, isolated MySQL instances. Connection settings are managed via Environment Variables in `docker-compose.yml`.

> **Note:** The `ddl-auto=update` setting ensures tables are created/updated automatically on first run.

## Configuration
All application settings are externalized:

| Purpose | Property | Source | Default Value (for containers) |
|---|---|---|---|
| DB host | `SPRING_DATASOURCE_URL` | `docker-compose.yml` | `jdbc:mysql://[service-name]:3306/[db-name]` |
| DB user | `SPRING_DATASOURCE_USER` | `docker-compose.yml` | `root` |
| HTTP port | `SERVER_PORT` | `docker-compose.yml` | `8081` (Person), `8082` (Device) |

## How to run (local)
From the project root (where `docker-compose.yml` is located), follow these steps:

1.  **Build and Run the System:**
    ```bash
    docker-compose up -d --build
    ```
    *(The `--build` flag ensures your latest Java code is compiled into the images.)*

2.  **Access Logs (Troubleshooting):**
    ```bash
    docker-compose logs -f person-backend
    ```

3.  **Stop Services:**
    ```bash
    docker-compose down
    ```

## API quick peek
All API endpoints are accessed externally via the Traefik proxy on the base path `/api/`.

| Method | Resource | Service | Description | Required Role |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/auth/me` | Person | Retrieves the currently authenticated user's details. | Authenticated |
| `POST` | `/api/auth/register` | Person | Creates a new user in the system. | ADMIN (Enforced by Logic) |
| `GET` | `/api/people` | Person | Lists all user accounts. | ADMIN |
| `DELETE` | `/api/people/{id}` | Person | Deletes a user by ID. | ADMIN |
| `POST` | **`/api/devices`** | Device | **Creates a new device resource.** | ADMIN |
| `GET` | **`/api/devices`** | Device | **Lists all devices** (ADMIN only). | ADMIN |
| `GET` | **`/api/devices/{id}`** | Device | **Fetches a single device by ID.** | ADMIN/CLIENT |
| `PUT` | **`/api/devices/{id}`** | Device | **Updates or assigns a device.** | ADMIN |
| `DELETE` | **`/api/devices/{id}`** | Device | **Deletes a device** by ID. | ADMIN |
| `GET` | `/api/devices/person/{personId}` | Device | Lists all devices assigned to a specific person. | Authenticated (Client/Admin) |

## Test with Postman
1.  Access your API documentation at: `http://localhost/swagger-ui-person.html`
2.  **Authentication:** All requests must include an `Authorization` header with the Base64-encoded `username:password`.
* **Header Value:** `Basic [base64_encoded_user:password]`
3.  **Initial Setup:** You must manually create the first user with the **ADMIN** role in the `person-db` (using a tool like MySQL Workbench and a BCrypt hash for the password) to gain access to the secure endpoints.

## Where it runs
The system is accessible via the host machine's Port 80, managed by Traefik.

| Punct de Acces | URL | Service Routed To |
| :--- | :--- | :--- |
| **Frontend App** | `http://localhost/` | `frontend` |
| **Person API Docs** | `http://localhost/swagger-ui-person.html` | `person-backend` |
| **Device API Docs** | `http://localhost/swagger-ui-device.html` | `device-backend` |
| **Traefik Dashboard** | `http://localhost:8080/dashboard/` | `reverse-proxy` |