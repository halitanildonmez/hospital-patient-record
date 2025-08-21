package nl.gerimedica.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BulkAppointmentRequest {
    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotBlank(message = "SSN is required")
    private String ssn;

    @NotEmpty(message = "Reasons list cannot be empty")
    private List<String> reasons;

    @NotEmpty(message = "Dates list cannot be empty")
    private List<String> dates;
}
