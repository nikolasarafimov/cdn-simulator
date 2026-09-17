# CDN Simulator
[![CI](https://github.com/nikolasarafimov/cdn-simulator/actions/workflows/ci.yml/badge.svg)](https://github.com/nikolasarafimov/cdn-simulator/actions/workflows/ci.yml)

A Spring Boot application that simulates the core behavior of a **Content Delivery Network (CDN)**, including multi-layer caching, request routing, load balancing, cache eviction strategies, request tracing, and origin-server fallback.

The simulator provides an interactive interface that visualizes how resource requests travel through edge servers, replica servers, and the origin server while tracking cache hits, cache misses, request counts, and overall cache performance.

---

## Features

### Multi-Layer CDN Topology

The simulated CDN contains the following logical layers:

```text
Client
  ↓
Edge Server
  ↓
Replica Server
  ↓
Origin Server
```

The default topology contains:

- 2 edge servers
- 2 replica servers
- 1 origin server
- Cache capacity of 2 resources per caching server

The configured servers use different caching strategies:

| Server | Layer | Cache Strategy |
|---|---|---|
| `edge-a` | Edge | FIFO |
| `edge-b` | Edge | LRU |
| `replica-us-east` | Replica | LRU |
| `replica-eu-west` | Replica | LFU |

This allows different cache eviction strategies to operate within the same simulated CDN topology.

---

## Caching Algorithms

The project implements three cache replacement strategies.

### FIFO

**First In, First Out** removes the resource that entered the cache first when the cache reaches its configured capacity.

### LRU

**Least Recently Used** removes the resource that has not been accessed for the longest period of time.

Accessing a cached resource refreshes its position in the access order.

### LFU

**Least Frequently Used** removes the resource with the lowest access frequency.

Each successful cache access increases the resource's frequency counter.

---

## Request Routing

Requests are routed to the **least-loaded edge server** based on the number of requests handled by each edge server.

When a resource is requested:

1. The selected edge server checks its local cache.
2. If the resource is found, it is returned immediately.
3. If the edge cache misses, the request is forwarded to the replica server assigned to that edge.
4. The replica server checks its own cache.
5. If the replica cache also misses, the resource is retrieved from the origin server.
6. The retrieved resource is cached on the request path.
7. The resource is returned to the client.

Edge request counters are used to distribute requests between available edge servers.

---

## Request Tracing

Each simulated client request generates a trace describing the path taken through the CDN.

Every trace hop contains:

- Server identifier
- Server layer
- Cache hit or miss status
- Cache contents at that point in the request

Example:

```text
Client
  ↓
edge-a              CACHE MISS
  ↓
replica-us-east     CACHE MISS
  ↓
Origin Server
```

A later request may be served directly from an edge cache:

```text
Client
  ↓
edge-a              CACHE HIT
```

The frontend uses the backend-generated trace to visualize the request and update the request log.

---

## Simulation Metrics

The interface tracks:

- Total client requests
- Requests served from cache
- Requests requiring origin retrieval
- Cache hit ratio
- Requests handled by each edge server
- Requests handled by each replica server

These statistics make it possible to observe how repeated requests affect CDN cache efficiency.

---

## Resource Catalogue

The origin server contains a predefined collection of resources.

| Resource ID | Resource |
|---|---|
| `img1` | Bali Beach |
| `img2` | Mountain Bike Photo |
| `img3` | Dolphins Picture |
| `img4` | Bird Picture |
| `img5` | Tigers Photo |
| `img6` | Sydney, Australia |

The corresponding image files are served as static application resources.

---

## Cache Management

All edge and replica caches can be cleared from the simulator interface.

The backend endpoint is:

```http
DELETE /cache/clear
```

Clearing the caches removes currently cached resources while leaving the CDN topology and server configuration intact.

---

## REST API

The interactive frontend communicates with the Spring Boot backend through REST endpoints.

### List Resources

```http
GET /api/cdn/resources
```

Returns the resources available from the origin server.

### Fetch a Resource

```http
GET /api/cdn/fetchResource/{resourceId}
```

Fetches the requested resource through the simulated CDN.

### Simulate a Client Request

```http
POST /api/cdn/clientRequest
```

Example request:

```json
{
  "clientId": "client-1",
  "resourceId": "img1",
  "url": "/img/bali.jpg"
}
```

Example response:

```json
{
  "resourceId": "img1",
  "url": "/img/bali.jpg",
  "hitOnEdge": false,
  "trace": [
    {
      "serverId": "edge-a",
      "level": "EDGE",
      "hit": false,
      "cache": []
    },
    {
      "serverId": "replica-us-east",
      "level": "REPLICA",
      "hit": false,
      "cache": []
    }
  ],
  "resourcePath": "/img/bali.jpg"
}
```

---

## Authentication

The application uses **Spring Security** with form-based authentication.

The current demonstration account is:

```text
Username: user
Password: user
```

The credentials are intended only for local demonstration and educational use.

The simulator interface can be loaded directly, while protected backend functionality requires authentication.

---

## User Interface

The simulator includes an interactive frontend for observing CDN behavior.

The interface provides:

- Resource selection
- Visual CDN topology
- Client, edge, replica, and origin server visualization
- Edge and replica cache contents
- Cache hit and miss highlighting
- Animated request routing
- Delivered resource preview
- Request trace log
- Per-server request counters
- Cache clearing controls
- Simulation statistics
- Responsive layouts

The frontend uses the Spring Boot REST API as the source of truth for request routing and caching behavior instead of maintaining an independent CDN simulation.

---

## Architecture

The project separates CDN behavior into caching, model, service, API, and presentation layers.

```text
src/
├── main/
│   ├── java/
│   │   └── mk/ukim/finki/cdn_simulatorproject/
│   │       ├── cache/
│   │       │   ├── cachingAlgorithms/
│   │       │   │   ├── FIFOAlgorithm.java
│   │       │   │   ├── LFUAlgorithm.java
│   │       │   │   └── LRUAlgorithm.java
│   │       │   ├── CacheStrategy.java
│   │       │   └── CachingAlgorithmType.java
│   │       │
│   │       ├── config/
│   │       │   └── SecurityConfig.java
│   │       │
│   │       ├── dto/
│   │       │   ├── ClientRequestDTO.java
│   │       │   ├── HopDTO.java
│   │       │   ├── RequestTraceDTO.java
│   │       │   └── ResourceDTO.java
│   │       │
│   │       ├── exceptions/
│   │       │   ├── EdgeServerException.java
│   │       │   └── ReplicaServerException.java
│   │       │
│   │       ├── model/
│   │       │   ├── ClientRequest.java
│   │       │   ├── EdgeServer.java
│   │       │   ├── EdgeServerManager.java
│   │       │   ├── OriginServer.java
│   │       │   ├── ReplicaServer.java
│   │       │   ├── ReplicaServerManager.java
│   │       │   └── Resource.java
│   │       │
│   │       ├── service/
│   │       │   ├── impl/
│   │       │   │   ├── CacheServiceImpl.java
│   │       │   │   └── CDNServiceImpl.java
│   │       │   ├── CacheService.java
│   │       │   ├── CDNService.java
│   │       │   └── SimulationService.java
│   │       │
│   │       ├── web/
│   │       │   ├── CacheController.java
│   │       │   └── CdnController.java
│   │       │
│   │       └── CdnSimulatorProjectApplication.java
│   │
│   └── resources/
│       ├── static/
│       │   ├── images/
│       │   ├── img/
│       │   ├── app.js
│       │   ├── index.html
│       │   └── style.css
│       │
│       └── application.properties
│
└── test/
    └── java/
        └── mk/ukim/finki/cdn_simulatorproject/
            ├── CdnSimulatorProjectApplicationTests.java
            ├── EdgeServerManagerTest.java
            ├── EdgeServerTest.java
            ├── FIFOAlgorithmTest.java
            ├── LFUAlgorithmTest.java
            ├── LRUAlgorithmTest.java
            └── OriginServerTest.java
```

---

## Technologies

| Technology | Purpose |
|---|---|
| **Java 21** | Application language and runtime |
| **Spring Boot 3** | Application framework |
| **Spring Web** | REST API |
| **Spring Security** | Authentication and authorization |
| **Spring Data JPA** | Entity and persistence infrastructure |
| **H2 Database** | In-memory development database |
| **HTML5** | Frontend structure |
| **CSS3** | Responsive interface |
| **JavaScript** | API communication and request visualization |
| **JUnit 5** | Automated testing |
| **Maven** | Dependency management and build automation |
| **Docker** | Containerized application builds |

---

## Automated Tests

The project includes automated tests for the main CDN components.

Current coverage includes:

- Spring application context
- FIFO eviction behavior
- LRU eviction behavior
- LRU access-order updates
- LFU eviction behavior
- Edge server cache hit and miss flow
- Least-loaded edge server selection
- Origin resource retrieval
- Unknown origin resources

Run the test suite with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

---

## Getting Started

### Prerequisites

Install:

- Java 21
- Git

The Maven Wrapper is included in the repository, so a separate Maven installation is not required.

### 1. Clone the Repository

```bash
git clone https://github.com/nikolasarafimov/cdn-simulator.git
cd cdn-simulator
```

### 2. Run the Tests

Linux/macOS:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

### 3. Start the Application

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

Spring Security login is available at:

```text
http://localhost:8080/login
```

Demo credentials:

```text
Username: user
Password: user
```

---

## Building the Application

Create the executable JAR with:

```bash
./mvnw clean package
```

The generated artifact is placed inside:

```text
target/
```

Run the application with:

```bash
java -jar target/*.jar
```

---

## Docker

The repository contains a multi-stage Docker build.

### Build the Image

```bash
docker build -t cdn-simulator .
```

### Run the Container

```bash
docker run --rm -p 8080:8080 cdn-simulator
```

Then open:

```text
http://localhost:8080
```

The Docker build runs the Maven build and automated tests before producing the runtime image.

---

## Configuration

The application uses an in-memory H2 database during development.

```properties
spring.application.name=cdn-simulator

spring.datasource.url=jdbc:h2:mem:cdn_simulator
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
```

Because the database is stored in memory, its contents are recreated whenever the application restarts.

The CDN topology and cache contents are also initialized in memory when the application starts.

---

## Repository Notes

Generated and IDE-specific files are excluded from source control, including:

```text
target/
.idea/
.vscode/
*.iml
*.log
```

The Maven Wrapper remains tracked so the project can be built without requiring Maven to be installed globally.

---

## Future Improvements

Potential future extensions include:

- Geographic request routing
- Latency simulation
- Configurable server topology
- Runtime cache algorithm selection
- Persistent request metrics
- Cache TTL and expiration
- Distributed cache simulation
- Additional load-balancing strategies
- Server failure simulation
- Rate limiting
- Configurable authentication
- Additional integration tests
- Performance benchmarking

---

## Contributors

**Klaudija Stamenova**  
**Nikola Sarafimov**

---

## License

This project was developed for educational and portfolio purposes.
