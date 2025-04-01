package bpmn.com.bpmn.service.imp;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CamundaTherapistService {

    private final ZeebeClient zeebeClient;

    /**
     * ✅ Camunda 8 Zeebe ile Hasta Kayıt Sürecini Başlatır
     */
    public String startProcess(Map<String, Object> patientData) {
        Map<String, Object> variables = new HashMap<>(patientData);
        String businessKey = UUID.randomUUID().toString();
        variables.put("businessKey", businessKey);

        try {
            // 📌 Zeebe Sürecini Başlat
            ProcessInstanceEvent processInstance = zeebeClient
                    .newCreateInstanceCommand()
                    .bpmnProcessId("Process_Patient")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            // ✅ Primitive long değerini String'e çevir
            return String.valueOf(processInstance.getProcessInstanceKey());
        } catch (Exception e) {
            throw new RuntimeException("Zeebe process start failed", e);
        }
    }
}
