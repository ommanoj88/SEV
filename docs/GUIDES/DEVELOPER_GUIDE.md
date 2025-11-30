# SEV Fleet Management - Developer Guide

**Version:** 2.0.0  
**Last Updated:** January 2024

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Development Environment Setup](#development-environment-setup)
3. [Project Architecture](#project-architecture)
4. [Code Style Guidelines](#code-style-guidelines)
5. [Development Workflow](#development-workflow)
6. [Testing Guidelines](#testing-guidelines)
7. [Debugging Tips](#debugging-tips)
8. [Common Issues & Solutions](#common-issues--solutions)
9. [Contribution Guidelines](#contribution-guidelines)
10. [Architecture Decision Records](#architecture-decision-records)

---

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| JDK | 17+ | Backend development |
| Node.js | 18.x LTS | Frontend development |
| Maven | 3.8+ | Build tool |
| PostgreSQL | 14+ | Database |
| Docker | 20+ | Containerization |
| Git | 2.30+ | Version control |

### Recommended Tools

- **IDE:** IntelliJ IDEA Ultimate or VS Code
- **API Testing:** Postman or Insomnia
- **Database Client:** DBeaver or DataGrip
- **Container Management:** Docker Desktop

---

## Development Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/ommanoj88/SEV.git
cd SEV
```

### 2. Start Database

```bash
# Using Docker
cd docker
docker-compose -f docker-compose-infrastructure.yml up -d

# Or install PostgreSQL locally and create database:
# CREATE DATABASE evfleet;
# CREATE DATABASE evfleet_test;
```

### 3. Backend Setup

```bash
cd backend/evfleet-monolith

# Install dependencies
mvn clean install -DskipTests

# Set environment variables (create .env file or set in IDE)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/evfleet
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# Run the application
mvn spring-boot:run
```

Backend will be available at: `http://localhost:8080`

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Set environment variables
cp .env.example .env
# Edit .env with your Firebase config

# Start development server
npm start
```

Frontend will be available at: `http://localhost:3000`

### 5. Verify Setup

- Backend health: `curl http://localhost:8080/actuator/health`
- Frontend: Open `http://localhost:3000` in browser
- API docs: `http://localhost:8080/swagger-ui.html`

---

## Project Architecture

### Backend Structure

```
backend/evfleet-monolith/src/main/java/com/evfleet/
├── analytics/          # Analytics module
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── model/
├── billing/            # Billing & payments
├── charging/           # Charging management
├── common/             # Shared components
│   ├── config/         # Spring configurations
│   ├── exception/      # Custom exceptions
│   ├── event/          # Event publishing
│   └── security/       # Security config
├── driver/             # Driver management
├── fleet/              # Fleet/Vehicle management
├── maintenance/        # Maintenance tracking
└── telematics/         # Real-time data
```

### Frontend Structure

```
frontend/src/
├── components/         # React components
│   ├── analytics/
│   ├── charging/
│   ├── common/         # Reusable components
│   ├── dashboard/
│   ├── drivers/
│   ├── fleet/
│   └── maintenance/
├── pages/              # Page components
├── services/           # API services
├── store/              # Redux store
├── utils/              # Utilities
└── types/              # TypeScript types
```

### Key Design Patterns

| Pattern | Usage |
|---------|-------|
| Repository Pattern | Data access layer |
| Service Layer | Business logic |
| DTO Pattern | API data transfer |
| Event-Driven | Async notifications |
| Factory Pattern | Object creation |

---

## Code Style Guidelines

### Java

```java
// Good
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    
    public VehicleResponse createVehicle(Long companyId, VehicleRequest request) {
        log.info("Creating vehicle for company: {}", companyId);
        
        // Validate before save
        validateVehicleRequest(request);
        
        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber(request.getVehicleNumber())
                .companyId(companyId)
                .build();
                
        return VehicleResponse.fromEntity(vehicleRepository.save(vehicle));
    }
}
```

**Java Conventions:**
- Use `@RequiredArgsConstructor` with final fields (constructor injection)
- Use Lombok annotations (`@Builder`, `@Data`, `@Slf4j`)
- Log at appropriate levels (DEBUG for details, INFO for operations)
- Use `Optional` instead of null returns
- Validate inputs at service layer
- Use meaningful variable names

### TypeScript/React

```typescript
// Good
interface StatCardProps {
  title: string;
  value: string | number;
  icon?: React.ReactNode;
  trend?: number;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  trend,
}) => {
  const trendColor = trend && trend >= 0 ? 'success' : 'error';
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">{title}</Typography>
        <Typography variant="h4">{value}</Typography>
        {trend !== undefined && (
          <TrendIndicator value={trend} color={trendColor} />
        )}
      </CardContent>
    </Card>
  );
};

export default StatCard;
```

**TypeScript Conventions:**
- Define interfaces for all props
- Use functional components with hooks
- Destructure props in function signature
- Handle optional props with defaults
- Use meaningful component names (PascalCase)
- Export as default at end of file

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `VehicleService` |
| Methods | camelCase | `createVehicle()` |
| Constants | UPPER_SNAKE | `MAX_RETRY_ATTEMPTS` |
| Database tables | snake_case | `charging_sessions` |
| REST endpoints | kebab-case | `/api/v1/charging-sessions` |
| React components | PascalCase | `ChargingStationMap.tsx` |
| CSS classes | kebab-case | `.stat-card-value` |

---

## Development Workflow

### Branch Strategy

```
main                 # Production-ready code
├── develop          # Integration branch
├── feature/XXX      # New features
├── bugfix/XXX       # Bug fixes
├── hotfix/XXX       # Production fixes
└── release/X.X.X    # Release preparation
```

### Creating a Feature Branch

```bash
# Update main
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/add-vehicle-filters

# Make changes, commit frequently
git add .
git commit -m "feat(fleet): add vehicle type filter

- Add VehicleType dropdown to filter panel
- Implement filter logic in VehicleService
- Add unit tests for filter functionality"
```

### Commit Message Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

### Pull Request Process

1. **Create PR** with descriptive title
2. **Fill template** with:
   - Summary of changes
   - Testing done
   - Screenshots (if UI)
3. **Request review** from team members
4. **Address feedback** promptly
5. **Squash and merge** when approved

---

## Testing Guidelines

### Backend Testing

```java
// Unit Test Example
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    
    @InjectMocks
    private VehicleService vehicleService;
    
    @Test
    @DisplayName("Should create EV vehicle with valid battery capacity")
    void shouldCreateEVVehicle() {
        // Given
        VehicleRequest request = VehicleRequest.builder()
                .fuelType(FuelType.EV)
                .batteryCapacity(75.0)
                .build();
                
        given(vehicleRepository.save(any())).willReturn(testVehicle);
        
        // When
        VehicleResponse response = vehicleService.createVehicle(1L, request);
        
        // Then
        assertThat(response.getFuelType()).isEqualTo(FuelType.EV);
        verify(vehicleRepository).save(any(Vehicle.class));
    }
}
```

**Test Categories:**
- `@UnitTest`: Fast, isolated tests
- `@IntegrationTest`: Database/API tests
- `@E2ETest`: Full stack tests

**Running Tests:**
```bash
# Unit tests only
mvn test

# All tests including integration
mvn verify

# With coverage
mvn verify -Pcoverage
```

### Frontend Testing

```typescript
// Component Test Example
import { render, screen, fireEvent } from '@testing-library/react';
import StatusBadge from './StatusBadge';

describe('StatusBadge', () => {
  it('renders ACTIVE status with success color', () => {
    render(<StatusBadge status="ACTIVE" />);
    
    const badge = screen.getByText('ACTIVE').closest('.MuiChip-root');
    expect(badge).toHaveClass('MuiChip-colorSuccess');
  });
});
```

**Running Tests:**
```bash
# Run all tests
npm test

# With coverage
npm test -- --coverage

# Specific file
npm test -- StatusBadge.test.tsx
```

### Coverage Requirements

| Type | Minimum Coverage |
|------|------------------|
| Services | 80% |
| Controllers | 70% |
| Utils | 90% |
| Components | 70% |

---

## Debugging Tips

### Backend Debugging

**Enable Debug Logging:**
```yaml
# application-dev.yml
logging:
  level:
    com.evfleet: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
```

**Common Debug Endpoints:**
- `/actuator/health` - Application health
- `/actuator/info` - Build info
- `/actuator/metrics` - Performance metrics
- `/actuator/loggers` - Runtime log level control

**SQL Logging:**
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Frontend Debugging

**React DevTools:**
- Install browser extension
- Inspect component props and state
- Profile rendering performance

**Redux DevTools:**
- Track state changes
- Time-travel debugging
- Export/import state

**Network Debugging:**
```typescript
// Add to api client
axios.interceptors.request.use(request => {
  console.log('Starting Request', request);
  return request;
});
```

---

## Common Issues & Solutions

### Backend Issues

**Issue:** `Flyway migration failed`
```bash
# Reset and re-run migrations
mvn flyway:clean flyway:migrate
```

**Issue:** `Port 8080 already in use`
```bash
# Find and kill process
lsof -i :8080
kill -9 <PID>
```

**Issue:** `Lombok not working in IDE`
```
IntelliJ: Enable annotation processing
Settings > Build > Compiler > Annotation Processors
```

### Frontend Issues

**Issue:** `Module not found`
```bash
# Clear cache and reinstall
rm -rf node_modules
rm package-lock.json
npm install
```

**Issue:** `TypeScript errors after update`
```bash
# Regenerate type definitions
npm run generate-types
```

**Issue:** `Firebase auth errors`
```
Check .env file has correct Firebase config
Verify Firebase project settings match
```

---

## Contribution Guidelines

### Before Contributing

1. Read this entire guide
2. Check existing issues/PRs for duplicates
3. Discuss major changes before starting

### Code Review Checklist

- [ ] Code follows style guidelines
- [ ] Tests included and passing
- [ ] Documentation updated
- [ ] No hardcoded values (use config)
- [ ] Error handling implemented
- [ ] Logging added for operations
- [ ] Security considered

### Getting Help

- **Slack:** #sev-dev channel
- **Email:** dev-team@sevfleet.com
- **Office Hours:** Wednesdays 3-4 PM IST

---

## Architecture Decision Records

### ADR-001: Monolith Over Microservices

**Context:** Initial architecture decision

**Decision:** Build as modular monolith with clear module boundaries

**Rationale:**
- Faster development velocity
- Easier deployment and debugging
- Lower operational complexity
- Can extract to microservices later if needed

### ADR-002: PostgreSQL for Primary Database

**Context:** Database selection

**Decision:** Use PostgreSQL with PostGIS extension

**Rationale:**
- Strong ACID compliance
- Native geospatial support (PostGIS)
- Rich JSON support
- Excellent performance at scale

### ADR-003: Firebase for Authentication

**Context:** Authentication system

**Decision:** Use Firebase Authentication

**Rationale:**
- Quick to implement
- Built-in MFA support
- Social login options
- No credential storage responsibility

### ADR-004: Event-Driven Notifications

**Context:** Notification system design

**Decision:** Use internal event bus with external delivery

**Rationale:**
- Decoupled notification logic
- Easy to add new notification types
- Can queue and retry failures
- Supports multiple channels

---

*PR #47: Developer Guide Documentation*
