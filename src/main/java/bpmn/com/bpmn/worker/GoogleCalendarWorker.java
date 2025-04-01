package bpmn.com.bpmn.worker;

import bpmn.com.bpmn.service.TherapistFeignClient;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleCalendarWorker {

    private final TherapistFeignClient feignClient;
    private final ZeebeClient zeebeClient;

    @JobWorker(type = "redirect-to-google-oauth")
    public void redirectToGoogleOAuth(final ActivatedJob job) {
        try {
            Map<String, String> response = feignClient.getGoogleOAuthUrl();
            zeebeClient.newCompleteCommand(job.getKey()).variables(response).send().join();
        } catch (Exception e) {
            zeebeClient.newFailCommand(job.getKey()).retries(0).errorMessage("Google OAuth yönlendirme hatası: " + e.getMessage()).send().join();
        }
    }

    @JobWorker(type = "fetch-google-access-token")
    public void fetchGoogleAccessToken(final ActivatedJob job) {
        try {
            Map<String, Object> jobVars = job.getVariablesAsMap();
            String authCode = (String) jobVars.get("authCode");

            Map<String, String> response = feignClient.fetchGoogleAccessToken(Map.of("code", authCode));
            zeebeClient.newCompleteCommand(job.getKey()).variables(response).send().join();
        } catch (Exception e) {
            zeebeClient.newFailCommand(job.getKey()).retries(0).errorMessage("Erişim tokeni alma hatası: " + e.getMessage()).send().join();
        }
    }

    @JobWorker(type = "fetch-google-calendar-events")
    public void fetchGoogleCalendarEvents(final ActivatedJob job) {
        try {
            String accessToken = (String) job.getVariablesAsMap().get("accessToken");
            Map<String, Object> events = feignClient.fetchGoogleCalendarEvents(accessToken);

            zeebeClient.newCompleteCommand(job.getKey()).variables(events).send().join();
        } catch (Exception e) {
            zeebeClient.newFailCommand(job.getKey()).retries(0).errorMessage("Google Takvim etkinlikleri çekilemedi: " + e.getMessage()).send().join();
        }
    }
}
