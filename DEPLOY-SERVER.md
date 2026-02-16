# BPMN Sunucu Deploy Rehberi

## camundaTherapistAssignment.bpmn Akışı

```
Start → CreateAppointmentDraft → SendAssignmentRequest → WaitTherapistDecision (therapist_decision) 
  → Gateway → AssignTherapist (accepted) / RejectAssignment (rejected) → End
```

- **correlationKey:** patientId
- **Message:** therapist_decision

## Sunucuda Adımlar

### 1. BPMN projesini klonla

```bash
cd ~
git clone https://github.com/faruksrky/BPMN.git
cd BPMN
```

### 2. Docker Compose ile altyapıyı başlat

```bash
docker compose up -d
```

Servisler: postgres (5433), elasticsearch (9200), zeebe (26500), operate (8081), tasklist (8084)

### 3. BPMN uygulamasını başlat

**Seçenek A - Maven ile:**
```bash
./mvnw package -DskipTests
# Prod: Backend api.iyihislerapp.com'da
java -jar target/BPMN-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Seçenek B - Docker ile:**
```bash
./mvnw package -DskipTests
docker build -t bpmn-app .
docker run -d --name bpmn --network host \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e BACKEND_URL=https://api.iyihislerapp.com \
  -e ZEEBE_GATEWAY=http://localhost:26500 \
  -e DATABASE_URL=jdbc:postgresql://localhost:5433/camunda \
  -e DATABASE_USER=camunda \
  -e DATABASE_PASSWORD=camunda \
  bpmn-app
```

**Prod:** `BACKEND_URL=https://api.iyihislerapp.com` (varsayılan)

### 4. Nginx / Proxy – bpmn.iyihislerapp.com

BPMN API için ayrı subdomain:

```nginx
# /etc/nginx/sites-available/bpmn.iyihislerapp.com
server {
    listen 80;
    server_name bpmn.iyihislerapp.com;
    location / {
        proxy_pass http://127.0.0.1:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Cloudflare kullanıyorsan: DNS’e `bpmn.iyihislerapp.com` → sunucu IP ekle, proxy aç.

### 5. Frontend & Backend ayarları

- **Frontend** `VITE_BPMN_BASE_URL`: `https://bpmn.iyihislerapp.com`
- **Backend** `BPMN_SERVICE_URL`: `https://bpmn.iyihislerapp.com` (aynı sunucudaysa `http://localhost:8082`)

## Port Özeti

| Servis | Port | Açıklama |
|--------|------|----------|
| BPMN API | 8082 | REST API |
| Zeebe | 26500 | gRPC |
| Operate | 8081 | Camunda Operate |
| Tasklist | 8084 | Camunda Tasklist |
| Elasticsearch | 9200 | Zeebe export |
| Postgres | 5433 | Camunda DB |
