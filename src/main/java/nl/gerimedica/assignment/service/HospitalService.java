package nl.gerimedica.assignment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.dto.PatientDTO;
import nl.gerimedica.assignment.exception.InvalidDataException;
import nl.gerimedica.assignment.exception.PatientNotFoundException;
import nl.gerimedica.assignment.utils.HospitalUtils;
import nl.gerimedica.assignment.entity.Patient;
import nl.gerimedica.assignment.repository.PatientRepository;
import nl.gerimedica.assignment.entity.Appointment;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HospitalService {

    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;

    public HospitalService(PatientRepository patientRepo, AppointmentRepository appointmentRepo) {
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @Transactional
    public List<AppointmentResponseDTO> bulkCreateAppointments(String patientName, String ssn, List<String> reasons,
                                                    List<String> dates) {
        if (ssn == null || ssn.isBlank()) {
            throw new InvalidDataException("SSN cannot be null or blank");
        }
        if (HospitalUtils.isPayloadInvalid(reasons, dates)) {
            throw new InvalidDataException("Reasons and dates must not be empty");
        }

        try {
            Patient patient = patientRepo.findBySsn(ssn).orElseGet(() -> {
                log.info("Creating new patient with name: {}", patientName);
                return patientRepo.save(new Patient(patientName, ssn));
            });

            List<AppointmentResponseDTO> responseDTOS = new ArrayList<>();

            for (int i = 0; i < Math.min(reasons.size(), dates.size()); i++) {
                String reason = reasons.get(i);
                // TODO: I ran out of time but this should have been made a local date
                String date = dates.get(i);

                if (HospitalUtils.isPayloadInvalid(reasons, dates)) {
                    throw new InvalidDataException("Appointment reason/date cannot be empty");
                }

                Appointment appointment = new Appointment(reason, date, patient);
                Appointment saved = appointmentRepo.save(appointment);

                responseDTOS.add(new AppointmentResponseDTO(saved.getReason(), saved.getDate(),
                        new PatientDTO(saved.getPatient().getName(), saved.getPatient().getSsn())));
                log.info("Created appointment reason='{}', date={}", saved.getReason(), saved.getDate());
            }

            return responseDTOS;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create bulk appointments");
        }
    }

    public List<AppointmentResponseDTO> getAppointmentsByReason(String reasonKeyword) {
        if (reasonKeyword == null || reasonKeyword.isBlank()) {
            throw new InvalidDataException("No reason given");
        }

        List<AppointmentResponseDTO> allAppointments = appointmentRepo.findAll().stream()
                .map(a -> new AppointmentResponseDTO(a.getReason(), a.getDate(),
                        new PatientDTO(a.getPatient().getName(), a.getPatient().getSsn())))
                .filter(dto -> dto.getReason().equalsIgnoreCase(reasonKeyword))
                .collect(Collectors.toList());

        HospitalUtils.recordUsage("Get appointments by reason");

        return allAppointments;
    }

    @Transactional
    public void deleteAppointmentsBySSN(String ssn) {
        Patient patient = patientRepo.findBySsn(ssn).orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        List<Appointment> appointments = patient.getAppointments();
        appointmentRepo.deleteAll(appointments);
    }

    public AppointmentResponseDTO findLatestAppointmentBySSN(String ssn) {
        Patient patient = patientRepo.findBySsn(ssn).orElseThrow(() -> new PatientNotFoundException(ssn));
        Appointment appointment = Optional.ofNullable(patient.getAppointments())
                .flatMap(a -> a.stream().max(Comparator.comparing(Appointment::getDate)))
                .orElseThrow(() -> new InvalidDataException("No appointments found for patient"));
        return new AppointmentResponseDTO(appointment.getReason(), appointment.getDate(),
                new PatientDTO(appointment.getPatient().getName(), appointment.getPatient().getSsn()));
    }
}
