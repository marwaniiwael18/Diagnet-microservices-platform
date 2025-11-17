# 🚀 CI/CD Pipeline Guide - DiagNet Platform

## 📖 Table of Contents
- [Overview](#overview)
- [Pipeline Architecture](#pipeline-architecture)
- [GitHub Actions Workflows](#github-actions-workflows)
- [Setup Instructions](#setup-instructions)
- [How It Works](#how-it-works)
- [Container Registry](#container-registry)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

The DiagNet platform uses **GitHub Actions** for continuous integration and deployment. Every commit triggers automated builds, tests, and Docker image creation.

**Why CI/CD?**
- ✅ **Automated Testing**: Catches bugs before production
- ✅ **Consistent Builds**: Same process every time
- ✅ **Fast Feedback**: Know within minutes if code breaks
- ✅ **Docker Registry**: Pre-built images ready to deploy
- ✅ **Security Scanning**: Automatic vulnerability detection

---

## 🏗️ Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    GitHub Push/PR Created                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
        ┌────────────────────────────────────────┐
        │  Parallel Job Execution (Matrix Build) │
        └────────────────┬───────────────────────┘
                         │
         ┌───────────────┼───────────────┬───────────────┐
         ▼               ▼               ▼               ▼
    ┌────────┐     ┌────────┐     ┌────────┐     ┌──────────┐
    │Gateway │     │Collector│     │Analyzer│     │Frontend  │
    │Service │     │Service  │     │Service │     │Dashboard │
    └───┬────┘     └───┬────┘     └───┬────┘     └────┬─────┘
        │              │              │               │
        │   Maven      │   Maven      │   Maven       │  npm
        │   Test       │   Test       │   Test        │  build
        │              │              │               │
        └──────────────┴──────────────┴───────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │  Docker Build & Push  │
              │  (GitHub Container    │
              │   Registry - GHCR)    │
              └──────────┬────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │  Security Scanning    │
              │  (Trivy Vulnerability │
              │   Scanner)            │
              └──────────┬────────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │   Success!   │
                  │  ✅ All Tests│
                  │  ✅ Built    │
                  │  ✅ Pushed   │
                  └─────────────┘
```

---

## 📋 GitHub Actions Workflows

### 1. **Main Pipeline** (`build-deploy.yml`)
Runs on: Push to `main`/`develop` branches

**Jobs:**
1. **Backend Build** (Matrix: 3 services)
   - Checkout code
   - Setup Java 17
   - Run Maven tests
   - Build JAR files
   - Upload artifacts

2. **Frontend Build**
   - Setup Node.js 20
   - Install dependencies (`npm ci`)
   - Run linter
   - Build production bundle

3. **MQTT Simulator Build**
   - Validate Node.js syntax
   - Check dependencies

4. **Docker Build & Push**
   - Login to GitHub Container Registry
   - Build Docker images
   - Push to `ghcr.io/YOUR_USERNAME/diagnet-*`
   - Tag with: `latest`, `main-SHA`, version

5. **Security Scan**
   - Run Trivy scanner
   - Upload results to GitHub Security tab

### 2. **Pull Request Checks** (`pull-request.yml`)
Runs on: Every PR to `main`/`develop`

**Features:**
- ✅ PR title format validation (must start with `feat:`, `fix:`, etc.)
- ✅ Secret detection (TruffleHog)
- ✅ Large file detection
- ✅ Smart testing (only changed services)

### 3. **Dependabot** (`dependabot.yml`)
Automatic dependency updates:
- **Monday**: Maven dependencies
- **Tuesday**: npm dependencies
- **Wednesday**: Docker base images
- **Thursday**: GitHub Actions versions

---

## 🔧 Setup Instructions

### Step 1: Enable GitHub Container Registry

1. Go to your GitHub repository settings
2. Navigate to **Actions** → **General**
3. Under "Workflow permissions", select:
   - ✅ **Read and write permissions**
4. Save changes

### Step 2: Push Your Code

```bash
# Initialize git if not already done
cd /Users/macbook/Desktop/DiagNet
git init
git add .
git commit -m "feat: add CI/CD pipeline"

# Add remote (replace with your repo URL)
git remote add origin https://github.com/marwaniiwael18/Diagnet-microservices-platform.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### Step 3: Monitor Pipeline

1. Go to **Actions** tab in GitHub
2. Watch the pipeline run in real-time
3. Click on any job to see logs

### Step 4: View Built Images

```bash
# Images will be available at:
ghcr.io/marwaniiwael18/diagnet-gateway-service:latest
ghcr.io/marwaniiwael18/diagnet-collector-service:latest
ghcr.io/marwaniiwael18/diagnet-analyzer-service:latest
ghcr.io/marwaniiwael18/diagnet-mqtt-simulator:latest
```

---

## 🔍 How It Works

### Matrix Build Strategy
Instead of writing separate jobs for each service, we use **matrix builds**:

```yaml
strategy:
  matrix:
    service: [gateway-service, collector-service, analyzer-service]
```

This runs **3 jobs in parallel**, making builds 3x faster!

### Caching
Speeds up builds by caching:
- **Maven dependencies** (~2-3 minutes saved)
- **npm packages** (~1-2 minutes saved)
- **Docker layers** (~5-10 minutes saved)

```yaml
- uses: actions/setup-java@v4
  with:
    cache: 'maven'  # Automatic caching
```

### Artifact Sharing
Jobs share built files using artifacts:

```yaml
# Job 1: Build JAR
- uses: actions/upload-artifact@v4
  with:
    name: jar-gateway-service
    path: ./target/*.jar

# Job 2: Build Docker (uses JAR from Job 1)
- uses: actions/download-artifact@v4
  with:
    name: jar-gateway-service
```

---

## 📦 Container Registry (GHCR)

### Pulling Images

```bash
# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull an image
docker pull ghcr.io/marwaniiwael18/diagnet-gateway-service:latest

# Run it
docker run -p 8080:8080 ghcr.io/marwaniiwael18/diagnet-gateway-service:latest
```

### Image Tags
Each push creates multiple tags:
- `latest` - Most recent main branch build
- `main-abc1234` - Specific commit SHA
- `v1.0.0` - Semantic version (if tagged)

### Making Images Public

1. Go to package page: `https://github.com/users/USERNAME/packages`
2. Click on package (e.g., `diagnet-gateway-service`)
3. **Package settings** → **Change visibility** → **Public**

---

## 🐛 Troubleshooting

### Problem: Pipeline Fails on Maven Test

**Symptom:**
```
[ERROR] Tests run: 1, Failures: 1
```

**Solution:**
Check if tests require database connection. Either:
1. Add test database to workflow
2. Skip tests in Docker build: `mvn package -DskipTests`

### Problem: Docker Build Fails

**Symptom:**
```
ERROR: failed to solve: failed to copy files
```

**Solution:**
Ensure JAR file exists in `target/` directory:
```yaml
- uses: actions/download-artifact@v4
  with:
    name: jar-${{ matrix.service }}
    path: ./backend/microservices/${{ matrix.service }}/target/
```

### Problem: Permission Denied (GHCR)

**Symptom:**
```
Error: denied: permission_denied
```

**Solution:**
1. Go to repo **Settings** → **Actions** → **General**
2. Enable "Read and write permissions"
3. Re-run workflow

### Problem: Workflow Doesn't Trigger

**Checklist:**
- ✅ Workflow file in `.github/workflows/`
- ✅ File extension is `.yml` or `.yaml`
- ✅ Valid YAML syntax (use YAML validator)
- ✅ Pushed to correct branch (`main` or `develop`)

---

## 📊 Pipeline Metrics

**Typical Build Times:**
- Backend services (parallel): ~3-5 minutes each
- Frontend build: ~2-3 minutes
- Docker build & push: ~5-7 minutes per service
- Security scan: ~1-2 minutes

**Total Pipeline Duration:** ~8-12 minutes

---

## 🎓 Key Concepts for Your Internship

### 1. **Matrix Builds**
Run same job with different parameters in parallel.

**Example:**
```yaml
strategy:
  matrix:
    service: [service1, service2, service3]
steps:
  - run: mvn test
    working-directory: ./${{ matrix.service }}
```

### 2. **Artifacts**
Pass files between jobs (JAR → Docker build).

### 3. **Caching**
Store dependencies to speed up future builds.

### 4. **Container Registry**
Store Docker images for deployment (like Docker Hub but integrated with GitHub).

### 5. **GitHub Actions**
YAML-based CI/CD (alternative to Jenkins, GitLab CI, CircleCI).

---

## 🚀 Next Steps

1. ✅ **Push code to GitHub** to trigger first build
2. ⏳ **Watch pipeline run** in Actions tab
3. ⏳ **Fix any test failures** if they occur
4. ⏳ **Make images public** for easy deployment
5. ⏳ **Add status badge** to README.md

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Trivy Security Scanner](https://github.com/aquasecurity/trivy)

---

**Need Help?** Check the **Actions** tab in GitHub for detailed logs of each step.
