package bpmn.com.bpmn.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final ZeebeClient zeebeClient;

    @PostMapping("/start-process")
    public ResponseEntity<String> startGoogleCalendarProcess(@RequestBody Map<String, Object> eventData) {
        try {
            String businessKey = UUID.randomUUID().toString();
            ProcessInstanceEvent processInstance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("GoogleCalendarIntegrationProcess")
                    .latestVersion()
                    .variables(eventData)
                    .send()
                    .join();

            return ResponseEntity.ok("Süreç başlatıldı: " + processInstance.getProcessInstanceKey());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Süreç başlatılamadı: " + e.getMessage());
        }
    }
}
