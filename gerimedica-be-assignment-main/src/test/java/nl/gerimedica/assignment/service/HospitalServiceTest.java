package nl.gerimedica.assignment.service;

import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.entity.Appointment;
import nl.gerimedica.assignment.entity.Patient;
import nl.gerimedica.assignment.exception.InvalidDataException;
import nl.gerimedica.assignment.exception.PatientNotFoundException;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import nl.gerimedica.assignment.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalServiceTest {
    @Mock
    private PatientRepository patientRepo;
    @Mock
    private AppointmentRepository appointmentRepo;

    @InjectMocks
    private HospitalService hospitalService;

    @Test
    public void testBulkCreateAppointments_NewPatient() {
        String ssn = "123-45-6789";
        List<String> reasons = Arrays.asList("Checkup");
        List<String> dates = Arrays.asList("2025-02-01");

        Patient patient = new Patient("John Doe", ssn);
        when(patientRepo.findBySsn(ssn)).thenReturn(Optional.empty());
        when(patientRepo.save(any(Patient.class))).thenReturn(patient);
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        List<AppointmentResponseDTO> result = hospitalService.bulkCreateAppointments("John Doe", ssn, reasons, dates);

        assertEquals(1, result.size());
        assertEquals("Checkup", result.get(0).getReason());
        verify(patientRepo, times(1)).save(any(Patient.class));
        verify(appointmentRepo, times(1)).save(any(Appointment.class));
    }

    @Test
    public void testGetAppointmentsByReason() {
        Patient patient = new Patient("John Doe", "123-45-6789");
        Appointment appointment = new Appointment("Checkup", "2025-02-01", patient);

        when(appointmentRepo.findAll()).thenReturn(Collections.singletonList(appointment));

        List<AppointmentResponseDTO> result = hospitalService.getAppointmentsByReason("Checkup");

        assertEquals(1, result.size());
        assertEquals("Checkup", result.get(0).getReason());
    }

    @Test
    public void testDeleteAppointmentsBySSN_PatientExists() {
        String ssn = "123-45-6789";
        Patient patient = new Patient("John Doe", ssn);
        patient.setAppointments(Collections.singletonList(new Appointment("Checkup", "2025-02-01", patient)));

        when(patientRepo.findBySsn(ssn)).thenReturn(Optional.of(patient));
        doNothing().when(appointmentRepo).deleteAll(patient.getAppointments());

        hospitalService.deleteAppointmentsBySSN(ssn);

        verify(appointmentRepo, times(1)).deleteAll(patient.getAppointments());
    }

    @Test
    public void testFindLatestAppointmentBySSN() {
        String ssn = "123-45-6789";
        Patient patient = new Patient("John Doe", ssn);
        patient.setAppointments(Arrays.asList(new Appointment("Checkup", "2025-02-01", patient),
                new Appointment("X-Ray", "2025-02-15", patient)));

        when(patientRepo.findBySsn(ssn)).thenReturn(Optional.of(patient));

        AppointmentResponseDTO latest = hospitalService.findLatestAppointmentBySSN(ssn);

        assertEquals("X-Ray", latest.getReason());
    }

    @Test
    public void testFindLatestAppointmentBySSN_NoAppointments() {
        String ssn = "123-45-6789";
        Patient patient = new Patient("John Doe", ssn);
        patient.setAppointments(Collections.emptyList());

        when(patientRepo.findBySsn(ssn)).thenReturn(Optional.of(patient));

        assertThrows(InvalidDataException.class, () -> hospitalService.findLatestAppointmentBySSN(ssn));
    }

    @Test
    public void testDeleteAppointmentsBySSN_PatientNotFound() {
        String ssn = "123-45-6789";
        when(patientRepo.findBySsn(ssn)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> hospitalService.deleteAppointmentsBySSN(ssn));
    }
}