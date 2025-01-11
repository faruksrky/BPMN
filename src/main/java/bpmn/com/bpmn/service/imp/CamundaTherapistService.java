package bpmn.com.bpmn.service.imp;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CamundaTherapistService {

    private final RuntimeService runtimeService;

    public CamundaTherapistService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public String startProcess(Map<String, Object> patientData) {
        Map<String, Object> variables = new HashMap<>(patientData);
        variables.put("businessKey", UUID.randomUUID().toString());

        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_Patient", variables);
            return processInstance.getId();
        } catch (Exception e) {
            throw new RuntimeException("Process start failed", e);
        }
    }

}







