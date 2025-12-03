# Courier Tracker Service

A RESTful web application for tracking courier locations and logging store entries for Migros Online.

##  Overview

This service provides real-time tracking of courier geolocations and automatically detects when couriers enter the proximity of Migros stores. It calculates total travel distances and maintains a log of all store entries.

##  Features

- **Real-time Location Tracking**: Accept streaming geolocation updates from couriers
- **Store Proximity Detection**: Automatically detect when a courier enters the 100-meter radius of a Migros store
- **Re-entry Prevention**: Entries to the same store within 1 minute are not counted (cooldown period)
- **Distance Calculation**: Track and query total travel distance for any courier
- **RESTful API**: Well-documented API with Swagger/OpenAPI support
- **Production Ready**: Global exception handling, validation, and proper transaction management

##  Design Patterns

### 1. Observer Pattern
Used for notifying multiple components when a courier enters a store's radius:
- **Subject Interface**: `StoreEntrySubject` - Defines observer management contract
- **Subject Implementation**: `StoreEntryNotificationService` - Manages observers and broadcasts events
- **Observer Interface**: `StoreEntryObserver` - Defines the observer contract
- **Observers**:
  - `LoggingStoreEntryObserver` - Logs store entry details to console
  - `PersistenceStoreEntryObserver` - Persists entry to database

### 2. Strategy Pattern
Used for distance calculation algorithms:
- **Strategy Interface**: `DistanceCalculationStrategy`
- **Implementations**:
  - `HaversineDistanceStrategy` - Great-circle distance calculation (default)
  - `EuclideanDistanceStrategy` - Fast straight-line distance for short ranges

##  Technology Stack

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **MapStruct** (DTO mapping)
- **Lombok** (boilerplate reduction)
- **SpringDoc OpenAPI** (Swagger documentation)
- **Maven** (build tool)
- **Docker** (containerization)

##  Getting Started

### Prerequisites

- **Java 21** (Amazon Corretto 21 recommended)
- Maven 3.8+
- Docker & Docker Compose (optional, for containerized deployment)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/OmerCeyhan/courier-tracker-service.git
   cd courier-tracker-service
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or use the provided script:
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

4. **Access the application**
   - API Base URL: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:courierdb`)
   - Health Check: http://localhost:8080/actuator/health

## Docker Support

### Build and Run with Docker

#### Using Docker directly

1. **Build the Docker image**
   ```bash
   docker build -t courier-tracker-service:latest .
   ```

2. **Run the container**
   ```bash
   docker run -d \
     --name courier-tracker-service \
     -p 8080:8080 \
     courier-tracker-service:latest
   ```

3. **View logs**
   ```bash
   docker logs -f courier-tracker-service
   ```

4. **Stop the container**
   ```bash
   docker stop courier-tracker-service
   docker rm courier-tracker-service
   ```

#### Using Docker Compose (Recommended)

1. **Build and start the service**
   ```bash
   docker-compose up -d --build
   ```

2. **View logs**
   ```bash
   docker-compose logs -f
   ```

3. **Stop the service**
   ```bash
   docker-compose down
   ```

4. **Rebuild after code changes**
   ```bash
   docker-compose up -d --build
   ```


### Docker Commands Reference

```bash
# Build image
docker build -t courier-tracker-service:latest .

# Run container
docker run -d -p 8080:8080 --name courier-tracker-service courier-tracker-service:latest

```

## 📡 API Endpoints

### Courier Location Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/courier/location` | Report courier location |
| GET | `/api/v1/courier/location/courier/{courierId}` | Get location history |
| GET | `/api/v1/courier/location/courier/{courierId}/latest` | Get latest location |
| GET | `/api/v1/courier/location/courier/{courierId}/total-distance` | Get total travel distance |

### Store Entry Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/store-entries` | Get all store entries |
| GET | `/api/v1/store-entries/courier/{courierId}` | Get entries by courier |
| GET | `/api/v1/store-entries/store/{storeId}` | Get entries by store |
| GET | `/api/v1/store-entries/time-range` | Get entries by time range |


## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Tests in Docker

```bash
docker run --rm courier-tracker-service:latest ./mvnw test
```


##  Business Rules

1. **Store Proximity**: A courier is considered to have "entered" a store when they are within **100 meters** of the store coordinates.

2. **Re-entry Cooldown**: If a courier re-enters the same store's radius within **1 minute** of a previous entry, it is not logged as a new entry.

3. **Distance Calculation**: Uses the **Haversine formula** by default for calculating distances between geographic coordinates.

## Pre-loaded Stores

The application initializes with the following Migros stores from `stores.json`:

| Store Name | Latitude | Longitude |
|------------|----------|-----------|
| Ataşehir MMM Migros | 40.9923307 | 29.1244229 |
| Novada MMM Migros | 40.986106 | 29.1161293 |
| Beylikdüzü 5M Migros | 41.0066851 | 28.6552262 |
| Ortaköy MMM Migros | 41.055783 | 29.0210292 |
| Caddebostan MMM Migros | 40.9632463 | 29.0630908 |

##  API Documentation

Interactive API documentation is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs


## Author

**Ömer Ceyhan**
- GitHub: [@OmerCeyhan](https://github.com/OmerCeyhan)
