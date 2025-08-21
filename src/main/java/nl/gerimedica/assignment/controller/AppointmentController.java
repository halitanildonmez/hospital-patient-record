package nl.gerimedica.assignment.controller;

import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.dto.BulkAppointmentRequest;
import nl.gerimedica.assignment.entity.Appointment;
import nl.gerimedica.assignment.service.HospitalService;
import nl.gerimedica.assignment.utils.HospitalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * I wanted to change the controller name and the endpoint names but decided to keep them to make sure the
 * functionality would be as is. I want to note that the naming can be better (for example mapping would be /api/appointment
 * then bulk-appointment -> bulk etc et
 * */
@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final HospitalService hospitalService;

    public AppointmentController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    /**
     * Example: {
     * "reasons": ["Checkup", "Follow-up", "X-Ray"],
     * "dates": ["2025-02-01", "2025-02-15", "2025-03-01"]
     * }
     */
    @PostMapping("/bulk-appointments")
    public ResponseEntity<List<AppointmentResponseDTO>> createBulkAppointments(@RequestBody BulkAppointmentRequest request) {
        HospitalUtils.recordUsage("Controller triggered bulk appointments creation");

        List<AppointmentResponseDTO> created = hospitalService.bulkCreateAppointments(request.getPatientName(), request.getSsn(),
                request.getReasons(), request.getDates());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/appointments-by-reason")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByReason(@RequestParam String keyword) {
        return ResponseEntity.ok(hospitalService.getAppointmentsByReason(keyword));
    }

    /**
     * This could be delete and with delete endpoint type but keeping as is for now
     * */
    @PostMapping("/delete-appointments")
    public ResponseEntity<String> deleteAppointmentsBySSN(@RequestParam String ssn) {
        hospitalService.deleteAppointmentsBySSN(ssn);
        return ResponseEntity.ok("Deleted all appointments for SSN: " + ssn);
    }

    @GetMapping("/appointments/latest")
    public ResponseEntity<AppointmentResponseDTO> getLatestAppointment(@RequestParam String ssn) {
        return ResponseEntity.ok(hospitalService.findLatestAppointmentBySSN(ssn));
    }
}
