package bpmn.com.bpmn.controller;

import bpmn.com.bpmn.dto.PublishMessageRequest;
import bpmn.com.bpmn.service.imp.BpmnProcessService;
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
        System.out.println("=== BPMN MESSAGE ENDPOINT CALLED ===");
        System.out.println("Request: " + request);
        System.out.println("Message Name: " + request.getMessageName());
        System.out.println("Correlation Key: " + request.getCorrelationKey());
        System.out.println("Variables: " + request.getVariables());
        
        try {
            bpmnProcessService.publishMessage(request);
            System.out.println("Message published successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error publishing message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/process/{processInstanceKey}")
    public ResponseEntity<Map<String, Object>> getProcessInfo(@PathVariable String processInstanceKey) {
        System.out.println("=== BPMN PROCESS INFO ENDPOINT CALLED ===");
        System.out.println("Process Instance Key: " + processInstanceKey);
        
        try {
            Map<String, Object> processInfo = bpmnProcessService.getProcessInfo(processInstanceKey);
            System.out.println("Process info retrieved successfully");
            return ResponseEntity.ok(processInfo);
        } catch (Exception e) {
            System.out.println("Error getting process info: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Process bilgisi alınamadı", "message", e.getMessage()));
        }
    }

}