package nl.gerimedica.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.gerimedica.assignment.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.dto.BulkAppointmentRequest;
import nl.gerimedica.assignment.dto.PatientDTO;
import nl.gerimedica.assignment.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AppointmentControllerTest {
    private static final String SSN = "123456789";
    @Mock
    private HospitalService hospitalService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
        objectMapper = new ObjectMapper();
    }
    @Test
    public void testCreateBulkAppointments() throws Exception {
        BulkAppointmentRequest request = new BulkAppointmentRequest();
        request.setPatientName("John Doe");
        request.setSsn(SSN);
        request.setReasons(Arrays.asList("Checkup"));
        request.setDates(Arrays.asList("2025-02-01"));

        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO(
                "Checkup",
                "2025-02-01",
                new PatientDTO("John Doe", SSN)
        );

        when(hospitalService.bulkCreateAppointments(anyString(), anyString(), anyList(), anyList()))
                .thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(post("/api/bulk-appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Checkup"))
                .andExpect(jsonPath("$[0].patient.name").value("John Doe"));

        verify(hospitalService, times(1))
                .bulkCreateAppointments(anyString(), anyString(), anyList(), anyList());
    }

    @Test
    public void testGetAppointmentsByReason() throws Exception {
        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO(
                "Checkup",
                "2025-02-01",
                new PatientDTO("John Doe", SSN)
        );

        when(hospitalService.getAppointmentsByReason("Checkup"))
                .thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/api/appointments-by-reason")
                        .param("keyword", "Checkup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Checkup"));

        verify(hospitalService, times(1)).getAppointmentsByReason("Checkup");
    }

    @Test
    public void testDeleteAppointmentsBySSN() throws Exception {
        doNothing().when(hospitalService).deleteAppointmentsBySSN(SSN);

        mockMvc.perform(post("/api/delete-appointments")
                        .param("ssn", SSN))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted all appointments for SSN: " + SSN));

        verify(hospitalService, times(1)).deleteAppointmentsBySSN(SSN);
    }

    @Test
    public void testGetLatestAppointment() throws Exception {
        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO(
                "X-Ray",
                "2025-02-15",
                new PatientDTO("John Doe", SSN)
        );

        when(hospitalService.findLatestAppointmentBySSN(SSN)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/appointments/latest")
                        .param("ssn", SSN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("X-Ray"))
                .andExpect(jsonPath("$.patient.name").value("John Doe"));

        verify(hospitalService, times(1)).findLatestAppointmentBySSN(SSN);
    }
}