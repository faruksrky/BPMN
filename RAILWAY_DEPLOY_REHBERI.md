# 🚀 BPMN Service - Railway Deploy Rehberi

## 📋 Proje Bilgileri
- **Proje Adı:** BPMN (Camunda Process Engine)
- **Port:** 8082
- **Tip:** Spring Boot (Java 17)
- **Build:** Maven
- **Dizin:** `/Users/fs648/Desktop/BPMN`

---

## 🚀 Deploy Adımları

### 1️⃣ GitHub Repo Hazırlığı

```bash
cd /Users/fs648/Desktop/BPMN

# Git repo oluştur (eğer yoksa)
git init

# .gitignore kontrol et
cat > .gitignore << EOF
target/
*.log
.idea/
*.iml
.mvn/
mvnw
mvnw.cmd
EOF

# GitHub'da repo oluşturun: https://github.com/new
# Repo adı: bpmn-service

# Remote ekleyin
git remote add origin https://github.com/YOUR_USERNAME/bpmn-service.git

# Commit ve push
git add .
git commit -m "Initial commit - Railway deploy ready"
git push -u origin main
```

### 2️⃣ Railway'a Deploy

1. Railway Dashboard → **"New Project"** (veya mevcut projeye **"New"** → **"GitHub Repo"**)
2. `bpmn-service` repo'sunu seçin

### 3️⃣ Environment Variables

Railway → Variables:

```bash
# Port (Railway otomatik set eder)
PORT=8082

# Database (Railway PostgreSQL ekleyebilirsiniz)
# New → Database → Add PostgreSQL
# Railway otomatik olarak DATABASE_URL set eder

# VEYA manuel:
DATABASE_URL=postgresql://user:pass@host:5432/camunda

# Camunda Zeebe
CAMUNDA_CLIENT_ZEBBE_BASE_URL=http://localhost:26500
# Eğer Zeebe ayrı deploy edecekseniz:
# CAMUNDA_CLIENT_ZEBBE_BASE_URL=https://zeebe.railway.app

# Camunda Operate
CAMUNDA_OPERATE_URL=http://localhost:8081
CAMUNDA_OPERATE_USERNAME=demo
CAMUNDA_OPERATE_PASSWORD=demo

# Camunda Tasklist
CAMUNDA_TASKLIST_URL=http://localhost:8084
CAMUNDA_TASKLIST_USERNAME=demo
CAMUNDA_TASKLIST_PASSWORD=demo
```

### 4️⃣ Port Ayarı

`application.yml`'i güncelleyin:

```yaml
server:
  port: ${PORT:8082}  # Railway PORT env var'ını kullan
```

### 5️⃣ Database Ayarı

Railway'da PostgreSQL ekleyin:
1. Railway → Your Project → **"New"** → **"Database"** → **"Add PostgreSQL"**
2. Railway otomatik olarak `DATABASE_URL` environment variable'ını set eder
3. `application.yml`'de:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5433/camunda}
    # Railway DATABASE_URL otomatik olarak kullanılacak
```

### 6️⃣ Deploy URL

Railway → Settings → Networking → **"Generate Domain"**
- URL: `https://bpmn-service.up.railway.app`

### 7️⃣ Custom Domain (Opsiyonel)

- Domain: `bpmn.iyihislerapp.com`

---

## 🔄 Deployment Sonrası

### Cloudflare Pages Environment Variables

```bash
VITE_BPMN_BASE_URL=https://bpmn-service.up.railway.app
# VEYA
VITE_BPMN_BASE_URL=https://bpmn.iyihislerapp.com
```

---

## ✅ Checklist

- [ ] GitHub repo oluşturuldu
- [ ] Railway'da proje oluşturuldu
- [ ] PostgreSQL database eklendi
- [ ] Environment variables eklendi
- [ ] Port ayarı güncellendi
- [ ] Database URL ayarı güncellendi
- [ ] Deploy URL alındı
- [ ] Cloudflare Pages environment variables güncellendi
- [ ] Test edildi ✅

