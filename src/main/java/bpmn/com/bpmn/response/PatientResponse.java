package bpmn.com.bpmn.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class PatientResponse {
    @JsonProperty("patientId")
    private Long patientId;

    @JsonProperty("patientFirstName")
    private String patientFirstName;

    @JsonProperty("patientLastName")
    private String patientLastName;

    @JsonProperty("patientEmail")
    private String patientEmail;

    @JsonProperty("patientPhoneNumber")
    private String patientPhoneNumber;

    @JsonProperty("patientAddress")
    private String patientAddress;

    @JsonProperty("patientAge")
    private Integer patientAge;

    @JsonProperty("patientCountry")
    private String patientCountry;

    @JsonProperty("patientCity")
    private String patientCity;

    @JsonProperty("patientGender")
    private String patientGender;
}