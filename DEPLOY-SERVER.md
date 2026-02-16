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

### 2. Docker Compose ile tümünü başlat (Keycloak gibi)

```bash
docker compose up -d --build
```

BPMN uygulaması da Docker'da build edilip çalışır – host'ta Java gerekmez.

Servisler: postgres (5433), elasticsearch (9200), zeebe (26500), operate (8081), tasklist (8084), **bpmn-app (8082)**

### 3. Alternatif: Sadece Maven ile (host'ta Java gerekir)

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

Backend repo'daki hazır config ile:

```bash
cd ~/PsikoHekimBackend/nginx
sudo cp bpmn.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/bpmn.iyihislerapp.com
sudo ln -sf /etc/nginx/sites-available/bpmn.iyihislerapp.com /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

Veya tüm domain'leri kurmak için: `sudo bash setup-cloudflare.sh`

Cloudflare kullanıyorsan: DNS’e `bpmn.iyihislerapp.com` → sunucu IP ekle, proxy aç.

### 5. Frontend & Backend ayarları

- **Frontend** `VITE_BPMN_BASE_URL`: `https://bpmn.iyihislerapp.com`
- **Backend** `BPMN_SERVICE_URL`: `https://bpmn.iyihislerapp.com` (aynı sunucudaysa `http://localhost:8082`)

## Sorun Giderme

**postgresPsikoHekim sürekli Restarting ise:**

1. Logları kontrol et: `docker logs postgresPsikoHekim`
2. Volume bozuksa sıfırla (tüm BPMN verisi silinir):
   ```bash
   cd ~/BPMN
   docker compose down
   docker volume rm bpmn_postgres_data 2>/dev/null || true
   docker compose up -d --build
   ```

## Port Özeti

| Servis | Port | Açıklama |
|--------|------|----------|
| BPMN API | 8082 | REST API |
| Zeebe | 26500 | gRPC |
| Operate | 8081 | Camunda Operate |
| Tasklist | 8084 | Camunda Tasklist |
| Elasticsearch | 9200 | Zeebe export |
| Postgres | 5433 | Camunda DB |
