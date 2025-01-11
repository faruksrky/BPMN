package bpmn.com.bpmn.controller;

import bpmn.com.bpmn.response.TherapistResponse;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/bpmn/patient")
@RequiredArgsConstructor
public class BpmnController {


private final RuntimeService runtimeService;

@PostMapping("/start-process")
public ResponseEntity<List<TherapistResponse>> startProcess(@RequestBody Map<String, Object> patientData) {
    try {
        // Benzersiz bir iş anahtarı oluşturulur
        String businessKey = UUID.randomUUID().toString();

        // Süreç başlatılır
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_Patient", businessKey, patientData);

        // Süreç değişkenlerinden therapist listesini al
        Object availableTherapists = runtimeService.getVariable(processInstance.getId(), "availableTherapists");

        // Terapist listesi var mı kontrol et
        if (availableTherapists instanceof List) {
            // Listeyi döndür
            @SuppressWarnings("unchecked")
            List<TherapistResponse> therapistList = (List<TherapistResponse>) availableTherapists;
            return ResponseEntity.ok(therapistList);
        } else {
            // Eğer süreç değişkeni beklenen türde değilse
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    } catch (Exception e) {
        // Hata durumunda loglama ve kullanıcıya hata mesajı döndürme
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }
}

    }



