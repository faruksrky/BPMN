# 500 - start-process Hatası Kontrol Listesi

## 1. BPMN loglarını kontrol et
```bash
docker logs bpmn-app --tail 100
```

Aranacak hatalar:
- `BPMN Süreci Deploy Edilemedi` → Zeebe bağlantı/deploy sorunu
- `Process başlatılamadı` → Zeebe process yok veya değişken hatası
- `Eksik parametre` → Frontend'den gelen veri eksik

## 2. Zeebe deploy durumu
BPMN başlarken logda şunlar görünmeli:
```
✅ Zeebe Broker çalışıyor!
✅ BPMN dosyası bulundu
✅ Process_Patient_Registration deploy edildi!
```

## 3. Backend logları (Feign hata detayı)
```bash
docker logs psikohekim-backend --tail 50 2>&1 | grep -i bpmn
```

## 4. Tarayıcı Network sekmesi
500 yanıtının Response body'sinde `bpmnResponse` veya `error` alanı gerçek hata mesajını içerir.
