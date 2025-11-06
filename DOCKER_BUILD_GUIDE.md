# Docker Build & Latest Code Guarantee Guide

## Problem Statement

**Issue**: When running the application from Docker, old code was being executed instead of the latest changes.

**Root Cause**: Docker's build cache system was reusing previously built layers, which meant:
- Old compiled frontend JavaScript bundles were served
- Old compiled Java JAR files were executed
- Code changes weren't reflected in running containers

## Solution Implemented

### 1. Universal Python Launcher (`run_app.py`)

The `run_app.py` script ensures latest code is always used by:

```python
# Always build with --no-cache flag
cmd = ['docker-compose', 'build', '--no-cache', '--parallel']
```

**What `--no-cache` does:**
- Forces Docker to rebuild every layer from scratch
- Ignores all cached layers
- Re-downloads dependencies
- Re-compiles all source code
- Guarantees that the latest code is in the image

### 2. Multi-Stage Dockerfiles

All services use multi-stage builds to ensure clean compilation:

#### Frontend Dockerfile
```dockerfile
# Build stage - Always recompiles from source
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps  # Fresh dependency install
COPY . .                        # Copy latest source
RUN npm run build              # Fresh build

# Production stage - Fresh nginx with latest build
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
```

**Guarantees:**
- `COPY . .` always copies your latest code
- `npm run build` always recompiles from scratch with --no-cache
- Production image gets freshly built assets

#### Backend Dockerfile (All Microservices)
```dockerfile
# Build stage - Always recompiles from source
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B  # Download dependencies
COPY src ./src                     # Copy latest source
RUN mvn clean package -DskipTests  # Fresh compilation

# Runtime stage - Fresh JRE with latest JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
```

**Guarantees:**
- `COPY src ./src` always copies your latest source code
- `mvn clean package` always compiles from scratch with --no-cache
- Runtime image gets freshly compiled JAR

## How to Ensure Latest Code

### Recommended Method: Use the Python Launcher

```bash
# This ALWAYS uses latest code
python run_app.py start
```

**What happens:**
1. Stops all running containers
2. Kills processes on required ports
3. Builds ALL images with `--no-cache`
4. Starts services with fresh images
5. Your latest code is guaranteed to run

### Alternative: Manual Docker Compose with --no-cache

```bash
cd docker
docker-compose build --no-cache
docker-compose up -d
```

**Note**: This only rebuilds images, you must also ensure containers are recreated.

### Quick Restart (For Testing Only)

```bash
# Faster but uses existing images
python run_app.py start --skip-build
```

**Warning**: This does NOT guarantee latest code. Use only when:
- You haven't made code changes
- You want to restart services quickly
- You're testing configuration changes only

## Build Cache Behavior

### What Gets Cached (Without --no-cache)

| Build Step | Cached? | Impact |
|------------|---------|--------|
| Base image pull | Yes | OK - base images rarely change |
| `npm ci` / `mvn dependency` | Yes | ⚠️ May miss dependency updates |
| `COPY src` | Yes | ❌ OLD CODE USED |
| `npm build` / `mvn package` | Yes | ❌ OLD BUILD USED |

### What Happens With --no-cache

| Build Step | Cached? | Impact |
|------------|---------|--------|
| Base image pull | No | ✅ Latest base image |
| `npm ci` / `mvn dependency` | No | ✅ Latest dependencies |
| `COPY src` | No | ✅ Latest source code |
| `npm build` / `mvn package` | No | ✅ Fresh compilation |

## Best Practices

### 1. Always Use Python Launcher for Code Changes

```bash
# After making code changes
python run_app.py restart
```

This ensures:
- ✅ Latest code is compiled
- ✅ New images are built
- ✅ Containers are recreated
- ✅ No cache issues

### 2. Periodic Clean Builds

```bash
# Weekly or after major changes
python run_app.py clean
```

This ensures:
- ✅ Old volumes removed
- ✅ Dangling images cleaned
- ✅ Fresh database state (careful!)
- ✅ Complete rebuild

### 3. Verify Running Code

After starting services, verify the build timestamp:

```bash
# Check frontend build
docker exec evfleet-frontend ls -la /usr/share/nginx/html/static/js/

# Check backend build (JAR timestamp)
docker exec evfleet-auth ls -la /app/app.jar
```

The timestamps should match when you ran `build`.

### 4. Development Workflow

```bash
# 1. Make code changes
vim frontend/src/components/SomeComponent.tsx

# 2. Restart with latest code
python run_app.py restart

# 3. Test changes
open http://localhost:3000

# 4. Verify logs
docker-compose logs -f frontend
```

## Troubleshooting

### Still Seeing Old Code?

**Browser Cache**
```bash
# Hard refresh in browser
Ctrl+Shift+R  # Windows/Linux
Cmd+Shift+R   # Mac
```

**Force Clean Rebuild**
```bash
python run_app.py clean
python run_app.py start
```

**Manual Verification**
```bash
# Stop everything
docker-compose down -v  # WARNING: Removes volumes!

# Remove all images
docker-compose down --rmi all

# Rebuild from scratch
python run_app.py start
```

### Build Failures

**Out of Disk Space**
```bash
# Clean Docker system
docker system prune -a --volumes

# Then rebuild
python run_app.py clean
```

**Dependency Download Fails**
```bash
# Network issue - retry
python run_app.py clean

# Or build with higher timeout
cd docker
docker-compose build --no-cache --parallel
```

## Performance Considerations

### Build Times

| Method | Time | Guarantees Latest Code? |
|--------|------|-------------------------|
| With `--no-cache` | 10-15 min | ✅ Yes |
| With cache | 2-3 min | ❌ No |
| With `--skip-build` | < 1 min | ❌ No |

**Recommendation**: Accept the 10-15 min build time for code changes to ensure correctness.

### Optimization Tips

1. **Use `--parallel` flag** (already in script)
   - Builds multiple services simultaneously
   - Reduces total build time

2. **Keep dependencies stable**
   - Don't change `package.json` / `pom.xml` frequently
   - Consider separate dependency layer caching (advanced)

3. **Build only changed services** (manual)
   ```bash
   cd docker
   docker-compose build --no-cache frontend
   docker-compose up -d --force-recreate frontend
   ```

## Docker Compose Build Flags Reference

| Flag | Purpose | When to Use |
|------|---------|-------------|
| `--no-cache` | Don't use cache | Code changes (ALWAYS) |
| `--pull` | Pull latest base images | Base image updates |
| `--parallel` | Build in parallel | Faster builds (default) |
| `--force-rm` | Remove intermediate containers | Clean builds |

## Summary

### ✅ Guarantees Latest Code

1. **Always use Python launcher**: `python run_app.py start`
2. **Script uses `--no-cache`**: Forces fresh builds
3. **Multi-stage Dockerfiles**: Separate build and runtime
4. **Container recreation**: New containers from new images

### ❌ Does NOT Guarantee Latest Code

1. Manual `docker-compose up` without rebuild
2. Using `--skip-build` flag
3. Relying on Docker cache
4. Only restarting containers without rebuilding

### The Golden Rule

**For any code change, always run:**
```bash
python run_app.py restart
```

This single command:
- Stops old services ✅
- Cleans up ports ✅
- Rebuilds with latest code ✅
- Starts fresh services ✅
- Verifies health ✅

---

**Document Version**: 1.0.0  
**Last Updated**: November 6, 2025  
**Purpose**: Ensure developers always run latest code in Docker
