# 🔧 CI/CD Troubleshooting Guide

## Common Issues & Solutions

### Issue 1: Dependabot Label Errors ❌

**Error Message:**
```
The following labels could not be found: analyzer-service, dependencies, java. 
Please create them before Dependabot can add them to a pull request.
```

**Cause:** Dependabot tried to add labels that don't exist in your repository.

**Solution:**
```yaml
# Before (fails):
labels:
  - "dependencies"
  - "java"
  - "analyzer-service"

# After (works):
labels:
  - "dependencies"
```

**Why:** GitHub repositories don't have labels by default. Either:
1. Simplify to use only basic labels
2. Or create labels manually in GitHub: Settings → Labels → New label

---

### Issue 2: `npm ci` Fails Without package-lock.json ❌

**Error Message:**
```
npm error `npm ci` can only install packages when your package.json 
and package-lock.json or npm-shrinkwrap.json are in sync.
```

**Cause:** `npm ci` requires `package-lock.json` for reproducible builds, but it was in `.gitignore`.

**Solution:**
1. Remove `package-lock.json` from `.gitignore`
2. Generate lock files:
   ```bash
   cd frontend/react-dashboard && npm install
   cd mqtt-simulator && npm install
   ```
3. Commit lock files to repository

**Why `package-lock.json` is Important:**
- ✅ **Reproducible builds** - Same dependencies every time
- ✅ **Faster installs** - `npm ci` is 2-3x faster than `npm install`
- ✅ **Security** - Lock specific versions to prevent supply chain attacks
- ✅ **CI/CD best practice** - Industry standard

---

### Issue 3: GitHub Actions Cache Path Errors ❌

**Error Message:**
```
Error: Path does not exist: ./frontend/react-dashboard/package-lock.json
```

**Cause:** GitHub Actions tried to cache npm dependencies before the file existed.

**Solution:**
```yaml
# Option 1: Remove cache initially
- uses: actions/setup-node@v4
  with:
    node-version: '20'
    # cache: 'npm'  # Commented until package-lock.json exists
    # cache-dependency-path: './frontend/react-dashboard/package-lock.json'

# Option 2: Add cache after package-lock.json exists
- uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'npm'
    cache-dependency-path: './frontend/react-dashboard/package-lock.json'
```

---

### Issue 4: Maven Test Failures ❌

**Error Message:**
```
[ERROR] Tests run: 1, Failures: 1
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin
```

**Cause:** Tests require database connection, but CI environment has no database.

**Solutions:**

**Option A: Skip Tests in Docker Builds**
```dockerfile
# In Dockerfile
RUN mvn clean package -DskipTests
```

**Option B: Add Test Database to Workflow**
```yaml
services:
  postgres:
    image: postgres:14
    env:
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
      POSTGRES_DB: testdb
    ports:
      - 5432:5432
```

**Option C: Use H2 In-Memory Database for Tests**
```xml
<!-- In pom.xml -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

### Issue 5: Docker Build Context Not Found ❌

**Error Message:**
```
ERROR: failed to solve: failed to read dockerfile: open /path/Dockerfile: no such file or directory
```

**Cause:** Incorrect `context` path in GitHub Actions workflow.

**Solution:**
```yaml
# Correct path for backend services
- uses: docker/build-push-action@v5
  with:
    context: ./backend/microservices/gateway-service
    file: ./backend/microservices/gateway-service/Dockerfile

# Correct path for mqtt-simulator
- uses: docker/build-push-action@v5
  with:
    context: ./mqtt-simulator
    file: ./mqtt-simulator/Dockerfile
```

---

### Issue 6: Permission Denied - GitHub Container Registry ❌

**Error Message:**
```
Error: denied: permission_denied: write_package
```

**Cause:** GitHub Actions doesn't have permission to push to GHCR.

**Solution:**
1. Go to repository **Settings**
2. Navigate to **Actions** → **General**
3. Scroll to "Workflow permissions"
4. Select **"Read and write permissions"** ✅
5. Click **Save**

**Alternative:** Use Personal Access Token (PAT) with `packages:write` scope.

---

## 📊 Verifying Fixes

### Check Workflow Status:
```bash
# View latest workflow runs
curl -s "https://api.github.com/repos/USERNAME/REPO/actions/runs?per_page=5" \
  | grep -E '"name"|"conclusion"'
```

### View Workflow Logs:
1. Go to **Actions** tab in GitHub
2. Click on the failed workflow run
3. Click on failed job
4. Expand failed step to see detailed logs

### Re-run Failed Workflows:
1. Go to **Actions** tab
2. Click on failed workflow
3. Click **"Re-run all jobs"** button
4. Or click **"Re-run failed jobs"** to save time

---

## 🎓 Key Lessons

### 1. **Start Simple, Add Complexity Later**
- Begin with basic workflow
- Add caching after confirming builds work
- Add advanced features incrementally

### 2. **Lock Your Dependencies**
- Always commit `package-lock.json` and `package.json`
- Use `npm ci` in CI/CD (not `npm install`)
- Update lock files regularly

### 3. **Test Locally First**
```bash
# Test backend build
cd backend/microservices/gateway-service
mvn clean test

# Test frontend build
cd frontend/react-dashboard
npm install
npm run build

# Test Docker build
docker build -t test-image ./backend/microservices/gateway-service
```

### 4. **Read Error Messages Carefully**
GitHub Actions provides detailed logs. Common patterns:
- `No such file or directory` → Check paths
- `Permission denied` → Check workflow permissions
- `npm ci requires lock file` → Add package-lock.json
- `Tests failed` → Add test database or skip tests

---

## 🔍 Debugging Workflow

### Step 1: Identify the Failing Step
Look for ❌ in the workflow run.

### Step 2: Read the Full Error Log
Scroll to the bottom of the failed step for the actual error.

### Step 3: Reproduce Locally
Run the same command on your machine:
```bash
# If workflow runs: mvn test
cd backend/microservices/gateway-service
mvn test

# If workflow runs: npm run build
cd frontend/react-dashboard
npm run build
```

### Step 4: Fix and Test
Make changes, test locally, then push.

### Step 5: Monitor Next Run
Watch the Actions tab to confirm fix works.

---

## 📚 Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Dependabot Configuration](https://docs.github.com/en/code-security/dependabot/dependabot-version-updates/configuration-options-for-the-dependabot.yml-file)
- [npm ci vs npm install](https://docs.npmjs.com/cli/v8/commands/npm-ci)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Docker Build Push Action](https://github.com/docker/build-push-action)

---

**Remember:** Every failed build is a learning opportunity! Read the logs, understand the error, and document the solution. 🚀
