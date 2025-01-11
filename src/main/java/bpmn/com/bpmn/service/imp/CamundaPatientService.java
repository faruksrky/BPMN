package bpmn.com.bpmn.service.imp;

import bpmn.com.bpmn.response.PatientResponse;
import bpmn.com.bpmn.service.PatientFeignClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CamundaPatientService {

    private final PatientFeignClient patientFeignClient;

    public CamundaPatientService(PatientFeignClient patientFeignClient) {
        this.patientFeignClient = patientFeignClient;
    }

    public PatientResponse registerPatient(Map<String, Object> patientData) {
        try {
            return patientFeignClient.addPatient(patientData);
        } catch (Exception e) {
            throw new RuntimeException("Patient registration failed");
        }
    }
}
