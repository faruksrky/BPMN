package bpmn.com.bpmn.response;


import bpmn.com.bpmn.entity.Therapist;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TherapistResponse {

    @JsonProperty("therapists")
    private List<Therapist> therapists;

    // Getter ve Setter
    public List<Therapist> getTherapists() {
        return therapists;
    }

    public void setTherapists(List<Therapist> therapists) {
        this.therapists = therapists;
    }
}
