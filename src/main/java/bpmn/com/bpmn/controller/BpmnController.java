package bpmn.com.bpmn.controller;

import bpmn.com.bpmn.dto.PublishMessageRequest;
import bpmn.com.bpmn.service.imp.BpmnProcessService;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bpmn")
@RequiredArgsConstructor
public class BpmnController {

    private final BpmnProcessService bpmnProcessService;

    @PostMapping("/patient/start-process")
    public ResponseEntity<Map<String, Object>> startTherapistAssignProcess(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> result = bpmnProcessService.startTherapistAssignmentProcess(payload);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Süreç başlatılamadı", "error", e.getMessage()));
        }
    }

    @PostMapping("/message")
    public ResponseEntity<Void> publishMessage(@RequestBody PublishMessageRequest request) {
        bpmnProcessService.publishMessage(request);
        return ResponseEntity.ok().build();
    }

}