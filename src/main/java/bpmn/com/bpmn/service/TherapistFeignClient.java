package bpmn.com.bpmn.service;

import bpmn.com.bpmn.response.TherapistResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "therapist-service", url = "http://localhost:8083/therapist")
public interface TherapistFeignClient {

    @GetMapping("/all")
    TherapistResponse getAvailableTherapists();
}
