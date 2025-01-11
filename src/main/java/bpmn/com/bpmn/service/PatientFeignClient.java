package bpmn.com.bpmn.service;

import bpmn.com.bpmn.response.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;


@FeignClient(name = "patient-service", url = "http://localhost:8083/patient")
public interface PatientFeignClient {

    @PostMapping("/addPatient")
    PatientResponse addPatient(@RequestBody Map<String, Object> patientData);
}
