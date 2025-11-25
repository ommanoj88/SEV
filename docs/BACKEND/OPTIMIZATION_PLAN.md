# Complete Backend Optimization Plan

**Date**: 2025-11-15
**Status**: In Progress - Comprehensive Optimization

---

## CRITICAL ISSUE: Infinite User Fetch Loop

### Problem Analysis

**Symptoms**:
- Profile page loading infinitely
- Settings page loading infinitely
- Chrome console shows "user fetched" repeatedly

**Possible Root Causes**:
1. ✅ **Circular JSON serialization** (User → Roles → User)
2. ✅ **CORS misconfiguration** causing failed requests that retry
3. ✅ **Missing @JsonIgnore** on bidirectional relationships
4. ✅ **Frontend expecting different response format**
5. ✅ **No request caching** causing repeated identical requests

### Immediate Fixes Required

#### Fix #1: Add @JsonManagedReference/@JsonBackReference
**Issue**: Role and User have ManyToMany relationship that could cause circular serialization
**Location**: `User.java`, `Role.java`
**Fix**:
```java
// In User.java:
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "user_roles")
@JsonManagedReference  // ADD THIS
private Set<Role> roles = new HashSet<>();

// In Role.java (if it has users field):
@ManyToMany(mappedBy = "roles")
@JsonBackReference  // ADD THIS
private Set<User> users = new HashSet<>();
```

#### Fix #2: Use DTO Pattern (Already Implemented!)
**Status**: ✅ We already use `UserResponse` DTO
**Verify**: Make sure User entity is NEVER directly serialized to JSON

#### Fix #3: Optimize CORS Configuration
**Current**: May not allow all required headers
**Fix**: Update SecurityConfig CORS settings

#### Fix #4: Add Response Caching
**Issue**: Same /me request called multiple times
**Fix**: Add HTTP caching headers for /me endpoint

---

## OPTIMIZATION #1: Logging Optimization

### Current Issues:
- Too many DEBUG logs in production
- Missing structured logging
- No correlation IDs
- Excessive string concatenation in logs

### Fixes:

#### 1.1: Add Logback Configuration
**File**: `src/main/resources/logback-spring.xml`
```xml
<configuration>
    <!-- Console appender with pattern -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- File appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/evfleet.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/evfleet.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Production profile - INFO level -->
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>

    <!-- Development profile - DEBUG level -->
    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

#### 1.2: Fix String Concatenation in Logs
**Bad**:
```java
log.info("User logged in: " + user.getEmail());  // String concatenation always happens
```

**Good**:
```java
log.info("User logged in: {}", user.getEmail());  // Only happens if INFO enabled
```

**Status**: ✅ Already using parameterized logging everywhere!

#### 1.3: Reduce Verbose Logs
**Change**: Convert many DEBUG logs to TRACE
**Benefit**: Cleaner production logs

---

## OPTIMIZATION #2: Database Query Optimization

### Issue #1: N+1 Query Problem
**Where**: User fetching with roles (EAGER fetch)
**Impact**: Loads roles separately for each user

**Current**:
```java
@ManyToMany(fetch = FetchType.EAGER)  // Causes N+1
```

**Fix**: Use @EntityGraph or JOIN FETCH
```java
// In UserRepository:
@EntityGraph(attributePaths = {"roles"})
@Query("SELECT u FROM User u WHERE u.firebaseUid = :firebaseUid")
Optional<User> findByFirebaseUidWithRoles(@Param("firebaseUid") String firebaseUid);
```

### Issue #2: Missing Indexes
**Add indexes on frequently queried columns**

**Files to update**:
- Vehicle.java - Add index on status, fuel_type
- Trip.java - Add index on status, start_time
- ChargingSession.java - Add index on status, vehicle_id
- Driver.java - Add index on status, phone

---

## OPTIMIZATION #3: Caching

### Level 1: Application Cache (Spring @Cacheable)
**For**: Reference data that rarely changes

```java
// PricingPlanRepository
@Cacheable("pricingPlans")
List<PricingPlan> findByIsActiveTrue();

// RoleRepository
@Cacheable("roles")
Optional<Role> findByName(String name);
```

### Level 2: HTTP Caching
**For**: User profile (/me endpoint)

```java
// In AuthController /me endpoint:
return ResponseEntity.ok()
    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
    .body(user);
```

### Level 3: Redis Caching (Future)
**For**: Session data, rate limiting

---

## OPTIMIZATION #4: API Response Optimization

### Issue: Large Response Payloads
**Fix**: Implement field filtering

```java
// Add support for ?fields=id,name,email
public UserResponse filterFields(UserResponse user, Set<String> fields) {
    // Return only requested fields
}
```

### Issue: No Compression
**Fix**: Enable GZIP compression

```yaml
# application.yml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 1024
```

---

## OPTIMIZATION #5: Connection Pooling

### Current: Default HikariCP settings
**Optimize**:

```yaml
spring:
  datasource:
    auth:
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
```

---

## OPTIMIZATION #6: Async Processing

### Candidates for @Async:
1. ✅ Event publishing (already async)
2. Email notifications (future)
3. Report generation (future)
4. Analytics aggregation (should be scheduled)

---

## OPTIMIZATION #7: Request/Response Logging

### Add Logging Interceptor
**Benefits**:
- Log all incoming requests
- Log response time
- Log errors with context

```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        long duration = System.currentTimeMillis() -
                       (Long) request.getAttribute("startTime");
        log.info("Completed request: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), duration);
    }
}
```

---

## OPTIMIZATION #8: Security Enhancements

### Add Rate Limiting
**Library**: Bucket4j
**Limit**: 100 requests/minute per IP

### Add Request Validation
- Sanitize all inputs
- Validate all DTOs with @Valid
- Add custom validators

---

## OPTIMIZATION #9: Performance Monitoring

### Add Actuator Metrics
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Add Custom Metrics
```java
@Autowired
private MeterRegistry meterRegistry;

// Track user registrations
meterRegistry.counter("user.registrations").increment();

// Track request duration
Timer.Sample sample = Timer.start(meterRegistry);
// ... do work ...
sample.stop(meterRegistry.timer("api.request.duration", "endpoint", "/api/v1/auth/me"));
```

---

## OPTIMIZATION #10: Code Quality

### Add Method-Level Optimization
1. ✅ Use `@Transactional(readOnly = true)` for read operations
2. ✅ Use specific exception types (DuplicateResourceException, etc.)
3. Add input validation before database access
4. Use Optional properly (avoid .get(), use .orElseThrow())

---

## PRIORITY ORDER

### IMMEDIATE (Do Now):
1. **Fix infinite loop** - Check Role entity for circular reference
2. **Optimize CORS** - Ensure proper headers
3. **Add caching headers** - For /me endpoint
4. **Add logback config** - Clean up production logs

### HIGH (Do Next):
5. Add database indexes
6. Add @EntityGraph for N+1 fix
7. Enable GZIP compression
8. Add request logging interceptor

### MEDIUM (Nice to Have):
9. Add @Cacheable for reference data
10. Add rate limiting
11. Add custom metrics

### LOW (Future):
12. Field filtering
13. Async improvements
14. Report generation optimization

---

## TRACKING

I will create individual optimization files and mark each as complete.

**Status**: Starting with IMMEDIATE priorities...
