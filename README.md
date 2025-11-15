# JVM ORM Benchmark

A benchmarking project comparing performance between JPA/Hibernate and Exposed ORM frameworks on the JVM using Kotlin.

## Overview

This project provides performance benchmarks for bulk insert operations using two popular ORM frameworks:

- **JPA/Hibernate** - Java Persistence API with Hibernate implementation
- **Exposed** - Kotlin SQL framework by JetBrains

The benchmarks use [kotlinx-benchmark](https://github.com/Kotlin/kotlinx-benchmark) (backed by JMH) to measure and
compare performance metrics.

## Stack

- **Language**: Kotlin 2.2.21
- **JDK**: 21
- **Build Tool**: Gradle with Kotlin DSL
- **Database**: PostgreSQL
- **Connection Pool**: HikariCP 7.0.2
- **ORM Frameworks**:
  - Hibernate 7.1.7.Final
  - Exposed 1.0.0-rc-3
- **Benchmarking**: kotlinx-benchmark 0.4.14 (JMH 1.37)
- **Code Quality**: ktlint

## Requirements

- JDK 21 or higher
- Docker and Docker Compose (for PostgreSQL)
- Gradle (wrapper included)

## Project Structure

```
jvm-orm-benchmark/
├── benchmark-app/       # Benchmark implementations and execution
├── common/              # Shared database configuration
├── jpa-app/             # JPA/Hibernate implementation
├── exposed-app/         # Exposed framework implementation
├── docs/                # Documentation and benchmark results
├── compose.yaml         # Docker Compose for PostgreSQL
└── gradle/              # Gradle wrapper and dependencies
```

### Module Descriptions

- **benchmark-app**: Contains JMH benchmark classes (`JpaBenchmarks`, `ExposedBenchmarks`) and generates benchmark
  reports
- **common**: Shared `DatabaseConfig` with HikariCP setup and environment variable handling
- **jpa-app**: JPA/Hibernate API implementation for database operations
- **exposed-app**: Exposed framework API implementation for database operations

## Setup

### 1. Start PostgreSQL Database

```bash
docker compose up -d
```

This starts a PostgreSQL container with:

- Database: `orm_bench`
- User: `postgres`
- Password: `postgres`
- Port: `5432`
- Max connections: 200
- Shared buffers: 512MB

### 2. Verify Database is Running

```bash
docker compose ps
```

Wait for the health check to pass.

## Running Benchmarks

### Run All Benchmarks

```bash
./gradlew benchmark
```

This command:

1. Executes all benchmark tests
2. Generates JSON reports in `benchmark-reports/main/`
3. Automatically updates `docs/benchmark.md` with results
4. Cleans up the benchmark-reports directory

### Benchmark Configuration

Default benchmark parameters (defined in `Constants.kt`):

- **Rows**: 100,000
- **Batch Size**: 10,000

Current benchmark settings (in `benchmark-app/build.gradle.kts`):

- Warmup iterations: 1
- Iterations: 1
- Iteration time: 3 ns
- Mode: AverageTime
- Report format: JSON

## Environment Variables

The application supports the following environment variables for database configuration:

| Environment Variable | System Property | Default Value                                                           |
|----------------------|-----------------|-------------------------------------------------------------------------|
| `PG_URL`             | `pg.url`        | `jdbc:postgresql://localhost:5432/orm_bench?reWriteBatchedInserts=true` |
| `PG_USER`            | `pg.user`       | `postgres`                                                              |
| `PG_PASSWORD`        | `pg.password`   | `postgres`                                                              |

### Setting Environment Variables

```bash
# Using environment variables
export PG_URL="jdbc:postgresql://localhost:5432/orm_bench?reWriteBatchedInserts=true"
export PG_USER="postgres"
export PG_PASSWORD="postgres"

# Or using system properties
./gradlew benchmark -Dpg.url="jdbc:postgresql://localhost:5432/orm_bench?reWriteBatchedInserts=true"
```

## Available Gradle Tasks

| Task                           | Description                                    |
|--------------------------------|------------------------------------------------|
| `./gradlew benchmark`          | Run all benchmarks and update documentation    |
| `./gradlew updateBenchmarkDoc` | Update `docs/benchmark.md` with latest results |
| `./gradlew build`              | Build all modules                              |
| `./gradlew test`               | Run unit tests                                 |
| `./gradlew ktlintCheck`        | Check code style                               |
| `./gradlew ktlintFormat`       | Format code according to ktlint rules          |

## Benchmark Results

📊 [View Detailed Benchmark Results](https://bossm0n5t3r.github.io/jvm-orm-benchmark/docs/benchmark)

Latest benchmark results are maintained in [`docs/benchmark.md`](docs/benchmark.md).

The results table includes:

- Timestamp
- Benchmark name
- Benchmark class
- Batch size
- Number of rows
- Score (seconds per operation)
- Threads, forks, JDK version
- Warmup configuration

## Tests

<!-- TODO: Add information about unit tests when implemented -->

To run tests:

```bash
./gradlew test
```

## Development

### Code Style

This project uses ktlint with the official Kotlin code style.

```bash
# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

### Adding New Benchmarks

1. Create a new benchmark class in `benchmark-app/src/main/kotlin/me/bossm0n5t3r/benchmark/`
2. Annotate with `@State(Scope.Benchmark)`
3. Use `@Benchmark` annotation for benchmark methods
4. Use `@Setup` and `@TearDown` for initialization and cleanup
5. Run `./gradlew benchmark` to execute

## Database Configuration

The project uses HikariCP for connection pooling with the following default settings:

- Maximum pool size: 8
- Minimum idle connections: 2
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes
- Auto-commit: false

PostgreSQL JDBC URL includes `reWriteBatchedInserts=true` for optimized batch insert performance.

## License

<!-- TODO: Add license information -->

## Contributing

<!-- TODO: Add contribution guidelines -->
