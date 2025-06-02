package bpmn.com.bpmn.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "therapist-service", url = "http://localhost:8083")
public interface TherapistFeignClient {

    @PostMapping("/process/send-assignment-request")
    Map<String, Object> sendAssignmentRequest(@RequestBody Map<String, Object> request);

    @PostMapping("/therapist/assign")
    Map<String, Object> assignTherapistToPatient(@RequestBody Map<String, Object> assignRequest);

    @GetMapping("/api/google-calendar/redirect-to-google-oauth")
    Map<String, String> getGoogleOAuthUrl();

    @PostMapping("/api/google-calendar/callback")
    Map<String, String> fetchGoogleAccessToken(@RequestBody Map<String, String> authCode);

    @GetMapping("/api/google-calendar/fetch-google-calendar-events")
    Map<String, Object> fetchGoogleCalendarEvents(@RequestParam("accessToken") String accessToken);
}