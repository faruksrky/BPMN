package bpmn.com.bpmn.worker;

import bpmn.com.bpmn.service.TherapistFeignClient;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TherapistAssignmentWorker {

    private final ZeebeClient zeebeClient;
    private final TherapistFeignClient therapistFeignClient;

    @PostConstruct
    public void startWorkers() {
        startCreateAppointmentDraftWorker();
        startSendAssignmentRequestWorker();
        startAssignTherapistWorker();
        startRejectAssignmentWorker();
        startFinalizeAppointmentWorker();
        startCancelAppointmentWorker();
    }

    private void startCreateAppointmentDraftWorker() {
        zeebeClient.newWorker()
                .jobType("create-appointment-draft")
                .handler(this::handleCreateAppointmentDraft)
                .name("create-appointment-draft-worker")
                .open();
    }

    private void startSendAssignmentRequestWorker() {
        zeebeClient.newWorker()
                .jobType("send-assignment-request")
                .handler(this::handleSendAssignmentRequest)
                .name("send-assignment-request-worker")
                .open();
    }

    private void startAssignTherapistWorker() {
        zeebeClient.newWorker()
                .jobType("assign-therapist")
                .handler(this::handleAssignTherapist)
                .name("assign-therapist-worker")
                .open();
    }

    private void startRejectAssignmentWorker() {
        zeebeClient.newWorker()
                .jobType("reject-assignment")
                .handler(this::handleRejectAssignment)
                .name("reject-assignment-worker")
                .open();
    }

    private void startFinalizeAppointmentWorker() {
        zeebeClient.newWorker()
                .jobType("finalize-appointment")
                .handler(this::handleFinalizeAppointment)
                .name("finalize-appointment-worker")
                .open();
    }

    private void startCancelAppointmentWorker() {
        zeebeClient.newWorker()
                .jobType("cancel-appointment")
                .handler(this::handleCancelAppointment)
                .name("cancel-appointment-worker")
                .open();
    }

    private void handleCreateAppointmentDraft(JobClient client, ActivatedJob job) {
        try {
            Map<String, Object> vars = job.getVariablesAsMap();
            validateRequiredVariables(vars, "patientId", "therapistId", "scheduledDate", "sessionType");
            completeJob(client, job);
            log.info("Successfully completed create-appointment-draft");
        } catch (Exception e) {
            handleError(client, job, e, "create-appointment-draft");
        }
    }

    private void handleSendAssignmentRequest(JobClient client, ActivatedJob job) {
        try {
            Map<String, Object> request = createRequestFromJob(job);
            therapistFeignClient.sendAssignmentRequest(request);
            completeJob(client, job);

            log.info("Successfully completed send-assignment-request");
        } catch (Exception e) {
            handleError(client, job, e, "send-assignment-request");
        }
    }

    private void handleAssignTherapist(JobClient client, ActivatedJob job) {
        try {
            // Sadece job'ı tamamla, çünkü atama işlemi zaten ProcessServiceImpl'de yapıldı
            completeJob(client, job);
            log.info("Successfully completed assign-therapist job for processInstanceKey: {}",
                    job.getVariablesAsMap().get("processInstanceKey"));
        } catch (Exception e) {
            handleError(client, job, e, "assign-therapist");
        }
    }

    private void handleRejectAssignment(JobClient client, ActivatedJob job) {
        try {
            completeJob(client, job);
            log.info("Successfully completed reject-assignment");
        } catch (Exception e) {
            handleError(client, job, e, "reject-assignment");
        }
    }

    private void handleFinalizeAppointment(JobClient client, ActivatedJob job) {
        try {
            completeJob(client, job);
            log.info("Successfully completed finalize-appointment");
        } catch (Exception e) {
            handleError(client, job, e, "finalize-appointment");
        }
    }

    private void handleCancelAppointment(JobClient client, ActivatedJob job) {
        try {
            completeJob(client, job);
            log.info("Successfully completed cancel-appointment");
        } catch (Exception e) {
            handleError(client, job, e, "cancel-appointment");
        }
    }

    private Map<String, Object> createRequestFromJob(ActivatedJob job) {
        Map<String, Object> vars = job.getVariablesAsMap();
        Map<String, Object> request = new HashMap<>();

        String processInstanceKey = String.valueOf(job.getProcessInstanceKey());
        request.put("patientId", String.valueOf(vars.get("patientId")));
        request.put("processInstanceKey", processInstanceKey);
        request.put("therapistId", String.valueOf(vars.get("therapistId")));
        request.put("processName", String.valueOf(vars.get("processName")));
        request.put("description", String.valueOf(vars.get("description")));
        request.put("startedBy", String.valueOf(vars.get("startedBy")));
        request.put("createdAt", vars.get("createdAt"));
        request.put("updatedAt", vars.get("updatedAt"));

        // Backend zorunlu alanlar - null/boş ise varsayılan değer
        Object scheduledDate = vars.get("scheduledDate");
        if (scheduledDate == null || String.valueOf(scheduledDate).trim().isEmpty() || "null".equals(String.valueOf(scheduledDate))) {
            scheduledDate = java.time.LocalDateTime.now().plusDays(1).toString();
        }
        request.put("scheduledDate", scheduledDate);

        Object sessionType = vars.get("sessionType");
        if (sessionType == null || String.valueOf(sessionType).trim().isEmpty() || "null".equals(String.valueOf(sessionType))) {
            sessionType = "REGULAR";
        }
        request.put("sessionType", sessionType);

        Object sessionFormat = vars.get("sessionFormat");
        if (sessionFormat == null || String.valueOf(sessionFormat).trim().isEmpty() || "null".equals(String.valueOf(sessionFormat))) {
            sessionFormat = "IN_PERSON";
        }
        request.put("sessionFormat", sessionFormat);

        return request;
    }

    private void validateRequiredVariables(Map<String, Object> vars, String... requiredVars) {
        for (String var : requiredVars) {
            if (!vars.containsKey(var) || vars.get(var) == null) {
                throw new IllegalArgumentException("Required variable '" + var + "' is missing or null");
            }
        }
    }

    private void completeJob(JobClient client, ActivatedJob job) {
        client.newCompleteCommand(job.getKey())
                .send()
                .join();
    }

    private void handleError(JobClient client, ActivatedJob job, Exception e, String operation) {
        String errorMessage = String.format("%s failed: %s", operation, e.getMessage());
        log.error("Error in {} operation: {}", operation, errorMessage, e);

        int retries = job.getRetries() - 1;
        if (retries > 0) {
            log.info("Retrying {} operation. Remaining retries: {}", operation, retries);
            client.newFailCommand(job.getKey())
                    .retries(retries)
                    .errorMessage(errorMessage)
                    .send()
                    .join();
        } else {
            log.error("No more retries left for {} operation", operation);
            client.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage(errorMessage)
                    .send()
                    .join();
        }
    }
}