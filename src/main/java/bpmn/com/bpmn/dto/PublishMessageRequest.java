package bpmn.com.bpmn.dto;

import lombok.*;

import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PublishMessageRequest {
    private String messageName; // Örn: "therapist_decision"
    private String correlationKey; // Örn: patientId
    private Map<String, Object> variables; // Örn: { "TherapistDecision": "accepted" }
}
