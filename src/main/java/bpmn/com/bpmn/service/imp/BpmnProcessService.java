package bpmn.com.bpmn.service.imp;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BpmnProcessService {

    private final ZeebeClient zeebeClient;

    public Map<String, Object> startTherapistAssignmentProcess(Map<String, Object> data) {
        Map<String, Object> variables = (Map<String, Object>) data.get("variables");
        String patientId = String.valueOf(variables.get("patientId"));
        String therapistId = String.valueOf(variables.get("therapistId"));

        if (patientId == null || therapistId == null) {
            throw new RuntimeException("Danışan veya Danışman ID'si eksik");
        }

        ProcessInstanceEvent instance = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_Patient_Registration")
                .latestVersion()
                .variables(Map.of(
                        "patientId", patientId,
                        "therapistId", therapistId,
                        "TherapistDecision", "PENDING" // default value, will be updated by therapist
                ))
                .send()
                .join();

        return Map.of("processInstanceKey", instance.getProcessInstanceKey());
    }
}