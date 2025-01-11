package bpmn.com.bpmn.delegateClass;

import bpmn.com.bpmn.response.PatientResponse;
import bpmn.com.bpmn.service.PatientFeignClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.delegate.BpmnError;

import java.util.Map;

@Component("registerPatientDelegate")
public class RegisterPatientDelegate implements JavaDelegate {

    private final PatientFeignClient patientFeignClient;

    public RegisterPatientDelegate(PatientFeignClient patientFeignClient) {
        this.patientFeignClient = patientFeignClient;
    }

    @Override
    public void execute(DelegateExecution execution) {
        try {
            // Süreç değişkenlerini alın
            Map<String, Object> patientData = execution.getVariables();

            // Hasta kayıt işlemini gerçekleştir
            PatientResponse patientResponse = patientFeignClient.addPatient(patientData);

            // Kayıt başarılı, süreç değişkenlerini ayarla
            execution.setVariable("patientId", patientResponse.getPatientId());
            execution.setVariable("registrationStatus", "success");

        } catch (Exception e) {
            // Hata durumunda PATIENT_REGISTRATION_FAILED hata koduyla BpmnError fırlat
            throw new BpmnError("PATIENT_REGISTRATION_FAILED", "Patient registration failed: " + e.getMessage());
        }
    }
}

