package bpmn.com.bpmn.delegateClass;

import bpmn.com.bpmn.response.TherapistResponse;
import bpmn.com.bpmn.service.TherapistFeignClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import org.springframework.stereotype.Component;

@Component("therapistAvailabilityQueryDelegate")
public class TherapistAvailabilityQueryDelegate implements JavaDelegate {

    private final TherapistFeignClient feignClient;

    public TherapistAvailabilityQueryDelegate(TherapistFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public void execute(DelegateExecution execution) {


        try {
            TherapistResponse therapistResponse = feignClient.getAvailableTherapists();

            execution.setVariable("availableTherapists", therapistResponse.getTherapists());
        } catch (Exception e) {
            throw new RuntimeException("Therapist availability query failed");
        }
    }


}
