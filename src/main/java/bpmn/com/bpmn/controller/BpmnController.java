package bpmn.com.bpmn.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bpmn")
@RequiredArgsConstructor
public class BpmnController {

    private final ZeebeClient zeebeClient;

    @PostMapping("/patient/start-process")
    public ResponseEntity<String> startPatientProcess(@RequestBody Map<String, Object> patientData) {
        try {
            String businessKey = UUID.randomUUID().toString();

            ProcessInstanceEvent processInstance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_Patient_Therapist_ID") // BPMN Model'deki Process ID ile aynı olmalı
                    .latestVersion()
                    .variables(patientData)
                    .send()
                    .join();

            return ResponseEntity.ok("Süreç başlatıldı: " + processInstance.getProcessInstanceKey());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Süreç başlatılamadı: " + e.getMessage());
        }
    }
}
