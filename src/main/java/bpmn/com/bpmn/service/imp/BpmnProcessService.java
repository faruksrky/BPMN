package bpmn.com.bpmn.service.imp;

import bpmn.com.bpmn.dto.PublishMessageRequest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BpmnProcessService {

    private final ZeebeClient zeebeClient;
    public Map<String, Object> startTherapistAssignmentProcess(Map<String, Object> data) {
        Map<String, Object> variables = (Map<String, Object>) data.get("variables");

        // Gerekli alanları kontrol et
        String patientId = String.valueOf(variables.get("patientId"));
        String therapistId = String.valueOf(variables.get("therapistId"));
        String processName = String.valueOf(variables.get("processName"));
        String description = String.valueOf(variables.get("description"));
        String startedBy = String.valueOf(variables.get("startedBy"));

        // Zorunlu alanları kontrol et
        if (patientId == null || therapistId == null || processName == null || description == null || startedBy == null) {
            throw new RuntimeException("Eksik parametre: patientId, therapistId, processName, description ve startedBy alanları zorunludur");
        }

        try {
            // Tüm değişkenleri bir Map'e ekleyin
            Map<String, Object> allVariables = new HashMap<>();
            allVariables.put("patientId", patientId);
            allVariables.put("therapistId", therapistId);
            allVariables.put("processName", processName);
            allVariables.put("description", description);
            allVariables.put("startedBy", startedBy);
            allVariables.put("status", "PENDING");
            allVariables.put("createdAt", LocalDateTime.now().toString());
            allVariables.put("updatedAt", LocalDateTime.now().toString());
            allVariables.put("TherapistDecision", "PENDING");

            // BPMN sürecini başlat
            ProcessInstanceEvent instance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_Patient_Registration")
                    .latestVersion()
                    .variables(allVariables)
                    .send()
                    .join();

            // processInstanceKey'i al
            long processInstanceKey = instance.getProcessInstanceKey();

            // Değişkenleri güncelle
            try {
                // processInstanceKey'i variables'a ekle
                allVariables.put("processInstanceKey", String.valueOf(processInstanceKey)); // String'e çevir

                zeebeClient.newSetVariablesCommand(processInstanceKey)
                        .variables(allVariables)
                        .send()
                        .join();

            } catch (Exception e) {
                // Hata durumunda bile processInstanceKey'i döndür
                return Map.of(
                        "processInstanceKey", String.valueOf(processInstanceKey), // String'e çevir
                        "processName", processName,
                        "description", description,
                        "status", "PENDING",
                        "startedBy", startedBy,
                        "createdAt", LocalDateTime.now().toString(),
                        "patientId", patientId,
                        "therapistId", therapistId,
                        "error", "Variables could not be updated: " + e.getMessage()
                );
            }

            return Map.of(
                    "processInstanceKey", String.valueOf(processInstanceKey), // String'e çevir
                    "processName", processName,
                    "description", description,
                    "status", "PENDING",
                    "startedBy", startedBy,
                    "createdAt", LocalDateTime.now().toString(),
                    "patientId", patientId,
                    "therapistId", therapistId
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Process başlatılamadı: " + e.getMessage());
        }

}

    public void publishMessage(PublishMessageRequest request) {
        zeebeClient.newPublishMessageCommand()
                .messageName(request.getMessageName())         // "therapist_decision"
                .correlationKey(request.getCorrelationKey())   // patientId
                .variables(request.getVariables())             // {"TherapistDecision": "accepted"}
                .send()
                .join();
    }
}