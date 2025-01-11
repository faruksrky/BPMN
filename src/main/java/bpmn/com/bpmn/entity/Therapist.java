package bpmn.com.bpmn.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class Therapist implements Serializable {

    @JsonProperty("therapistId")
    private Long therapistId;

    @JsonProperty("therapistFirstName")
    private String therapistFirstName;

    @JsonProperty("therapistSurname")
    private String therapistSurname;

    @JsonProperty("therapistEmail")
    private String therapistEmail;

    @JsonProperty("therapistPhoneNumber")
    private String therapistPhoneNumber;

    @JsonProperty("therapistAddress")
    private String therapistAddress;

    @JsonProperty("therapistType")
    private String therapistType;

    @JsonProperty("specializationAreas")
    private List<String> specializationAreas;

    @JsonProperty("yearsOfExperience")
    private String yearsOfExperience;

    @JsonProperty("therapistEducation")
    private String therapistEducation;

    @JsonProperty("therapistCertifications")
    private String therapistCertifications;

    @JsonProperty("appointmentFee")
    private BigDecimal appointmentFee;

    @JsonProperty("therapistUniversity")
    private String therapistUniversity;

    @JsonProperty("therapistRating")
    private int therapistRating;
}
