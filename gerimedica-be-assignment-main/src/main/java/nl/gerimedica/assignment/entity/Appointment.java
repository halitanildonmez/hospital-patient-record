package nl.gerimedica.assignment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String reason;

    /*
    I ran out of time but this should have been made a LocalDate
    * **/
    @Column(nullable = false)
    public String date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    public Patient patient;

    public Appointment(String reason, String date, Patient patient) {
        this.reason = reason;
        this.date = date;
        this.patient = patient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id) && Objects.equals(reason, that.reason) && Objects.equals(date, that.date) && Objects.equals(patient, that.patient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, date, patient != null ? patient.getSsn() : null);
    }
}
