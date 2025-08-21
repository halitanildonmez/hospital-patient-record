package nl.gerimedica.assignment.dto;

import lombok.*;
import nl.gerimedica.assignment.entity.Patient;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class AppointmentResponseDTO {
    private String reason;
    private String date;
    private PatientDTO patient;
}
