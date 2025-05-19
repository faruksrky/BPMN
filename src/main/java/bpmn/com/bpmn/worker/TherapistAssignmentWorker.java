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
        startSendAssignmentRequestWorker();
        startAssignTherapistWorker();
        startRejectAssignmentWorker();
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

    private void handleSendAssignmentRequest(JobClient client, ActivatedJob job) {
        try {
            Map<String, Object> request = createRequestFromJob(job, "patientId", "therapistId");
            log.info("Processing send-assignment-request for patientId: {} and therapistId: {}",
                    request.get("patientId"), request.get("therapistId"));

            therapistFeignClient.sendAssignmentRequest(request);
            completeJob(client, job);

            log.info("Successfully completed send-assignment-request");
        } catch (Exception e) {
            handleError(client, job, e, "send-assignment-request");
        }
    }

    private void handleAssignTherapist(JobClient client, ActivatedJob job) {
        try {
            Map<String, Object> request = createRequestFromJob(job, "patientId", "therapistId");
            request.put("status", 1);

            log.info("Processing assign-therapist for patientId: {} and therapistId: {}",
                    request.get("patientId"), request.get("therapistId"));

            therapistFeignClient.assignTherapistToPatient(request);
            completeJob(client, job);

            log.info("Successfully completed assign-therapist");
        } catch (Exception e) {
            handleError(client, job, e, "assign-therapist");
        }
    }

    private void handleRejectAssignment(JobClient client, ActivatedJob job) {
        try {
            Map<String, Object> request = createRequestFromJob(job, "patientId", "therapistId");
            request.put("status", 0);

            log.info("Processing reject-assignment for patientId: {} and therapistId: {}",
                    request.get("patientId"), request.get("therapistId"));

            therapistFeignClient.assignTherapistToPatient(request);
            completeJob(client, job);

            log.info("Successfully completed reject-assignment");
        } catch (Exception e) {
            handleError(client, job, e, "reject-assignment");
        }
    }

    private Map<String, Object> createRequestFromJob(ActivatedJob job, String... requiredVars) {
        Map<String, Object> vars = job.getVariablesAsMap();
        validateRequiredVariables(vars, requiredVars);

        Map<String, Object> request = new HashMap<>();
        for (String var : requiredVars) {
            request.put(var, String.valueOf(vars.get(var)));
        }

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