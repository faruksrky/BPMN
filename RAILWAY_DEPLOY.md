# 🚀 BPMN - Railway Deploy Rehberi

## 📋 Proje Bilgileri
- **Proje Adı:** BPMN (Camunda Process Engine)
- **Port:** 8082 (veya application.yml'deki port)
- **Tip:** Spring Boot (Java)
- **GitHub Repo:** (GitHub'da repo oluşturun)

---

## 🚀 Deploy Adımları

### 1️⃣ GitHub Repo Oluşturun (Eğer yoksa)

```bash
cd /Users/fs648/Desktop/BPMN
git init
git remote add origin https://github.com/YOUR_USERNAME/BPMN.git
git add .
git commit -m "Initial commit"
git push -u origin main
```

### 2️⃣ Railway'a Deploy

1. Railway Dashboard → **"New Project"**
2. **"Deploy from GitHub repo"**
3. BPMN repo'sunu seçin

### 3️⃣ Environment Variables

Railway → Variables:

```bash
PORT=8082
# VEYA application.yml'deki port'u kullanın

# Database (eğer kullanıyorsanız)
DATABASE_URL=${DATABASE_URL}
```

### 4️⃣ Port Ayarı

`application.yml`:
```yaml
server:
  port: ${PORT:8082}
```

### 5️⃣ Deploy URL

Railway → Settings → Networking → **"Generate Domain"**
- URL: `https://bpmn-service.up.railway.app`

### 6️⃣ Custom Domain (Opsiyonel)

- Domain: `bpmn.iyihislerapp.com`

---

## 🔄 Frontend Environment Variables Güncelleme

**Cloudflare Pages:**

```bash
VITE_BPMN_BASE_URL=https://bpmn-service.up.railway.app
# VEYA
VITE_BPMN_BASE_URL=https://bpmn.iyihislerapp.com
```

