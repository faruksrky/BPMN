# Zeebe "io exception" Giderme

## Sorun
BPMN `Process başlatılamadı: io exception` döndürüyor → Zeebe'ye bağlanamıyor.

## Kontrol Adımları

### 1. Zeebe çalışıyor mu?
```bash
docker ps | grep zeebe
# zeebe-broker görünmeli
```

### 2. Tüm BPMN stack çalışıyor mu?
```bash
cd ~/BPMN  # veya BPMN proje dizini
docker compose ps

# Beklenen: zeebe, elasticsearch, bpmn-app, postgres (hepsi Up)
```

### 3. Zeebe çalışmıyorsa - tüm stack'i başlat
```bash
cd ~/BPMN
docker compose up -d

# Sıra önemli: elasticsearch → zeebe → bpmn-app
# Zeebe'nin ayağa kalkması 30-60 sn sürebilir
```

### 4. BPMN loglarını kontrol et
```bash
docker logs bpmn-app --tail 100

# Zeebe bağlantı hatalarını ara
docker logs bpmn-app 2>&1 | grep -i -E "zeebe|io exception|connection"
```

### 5. Zeebe portunu test et (sunucu içinden)
```bash
# BPMN container'dan Zeebe'ye erişim
docker exec bpmn-app sh -c "nc -zv zeebe 26500 2>&1 || echo 'nc yok, curl dene'"
```

### 6. BPMN process deploy edilmiş mi?
```bash
# Zeebe'ye process deploy edilmesi BPMN uygulama başlangıcında yapılır
# BPMN loglarında "Deployed process" veya benzeri mesaj ara
docker logs bpmn-app 2>&1 | grep -i deploy
```

## Hızlı Çözüm
Zeebe kapalıysa:
```bash
cd ~/BPMN
docker compose up -d
sleep 60   # Zeebe'nin hazır olmasını bekle
docker compose restart bpmn-app
```
