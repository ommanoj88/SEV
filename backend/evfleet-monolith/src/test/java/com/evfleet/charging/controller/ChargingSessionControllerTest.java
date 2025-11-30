package com.evfleet.charging.controller;

import com.evfleet.charging.dto.ChargingSessionResponse;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.service.ChargingSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChargingSessionController
 * PR #41: Backend Controller Unit Tests
 *
 * Tests cover:
 * - GET /api/v1/charging/sessions (get all sessions)
 * - POST /api/v1/charging/sessions/start (start session)
 * - POST /api/v1/charging/sessions/{id}/complete (complete session)
 * - GET /api/v1/charging/sessions/{id} (get by ID)
 * - GET /api/v1/charging/sessions/vehicle/{vehicleId} (get by vehicle)
 * - GET /api/v1/charging/sessions/company/{companyId} (get by company)
 * - Error cases (404, 400)
 */
@WebMvcTest(ChargingSessionController.class)
@DisplayName("ChargingSessionController Tests")
class ChargingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChargingSessionService sessionService;

    private ChargingSession testSession;

    @BeforeEach
    void setUp() {
        testSession = ChargingSession.builder()
                .id(1L)
                .vehicleId(10L)
                .stationId(5L)
                .companyId(100L)
                .status(ChargingSession.SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .initialSoc(20.0)
                .currentSoc(50.0)
                .energyConsumed(BigDecimal.valueOf(15.5))
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/charging/sessions")
    class GetAllSessions {

        @Test
        @DisplayName("Should return empty list when no companyId provided")
        void shouldReturnEmptyListWhenNoCompanyId() throws Exception {
            mockMvc.perform(get("/api/v1/charging/sessions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return sessions when companyId provided")
        void shouldReturnSessionsForCompany() throws Exception {
            List<ChargingSession> sessions = Arrays.asList(testSession);
            given(sessionService.getSessionsByCompany(100L)).willReturn(sessions);

            mockMvc.perform(get("/api/v1/charging/sessions")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/charging/sessions/start")
    class StartSession {

        @Test
        @DisplayName("Should start charging session successfully")
        void shouldStartSessionSuccessfully() throws Exception {
            given(sessionService.startSession(anyLong(), anyLong(), anyLong(), any()))
                    .willReturn(testSession);

            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", "10")
                    .param("stationId", "5")
                    .param("companyId", "100")
                    .param("initialSoc", "20.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.vehicleId").value(10))
                    .andExpect(jsonPath("$.stationId").value(5));
        }

        @Test
        @DisplayName("Should start session without initial SOC")
        void shouldStartSessionWithoutInitialSoc() throws Exception {
            given(sessionService.startSession(anyLong(), anyLong(), anyLong(), any()))
                    .willReturn(testSession);

            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", "10")
                    .param("stationId", "5")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 400 when required params missing")
        void shouldReturn400WhenParamsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", "10")
                    // Missing stationId and companyId
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/charging/sessions/{id}/complete")
    class CompleteSession {

        @Test
        @DisplayName("Should complete charging session successfully")
        void shouldCompleteSessionSuccessfully() throws Exception {
            ChargingSession completedSession = ChargingSession.builder()
                    .id(1L)
                    .vehicleId(10L)
                    .stationId(5L)
                    .companyId(100L)
                    .status(ChargingSession.SessionStatus.COMPLETED)
                    .startTime(LocalDateTime.now().minusHours(1))
                    .endTime(LocalDateTime.now())
                    .initialSoc(20.0)
                    .finalSoc(80.0)
                    .energyConsumed(BigDecimal.valueOf(45.0))
                    .build();

            given(sessionService.completeSession(anyLong(), any(BigDecimal.class), any()))
                    .willReturn(completedSession);

            mockMvc.perform(post("/api/v1/charging/sessions/1/complete")
                    .param("energyConsumed", "45.0")
                    .param("finalSoc", "80.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.finalSoc").value(80.0));
        }

        @Test
        @DisplayName("Should complete session without final SOC")
        void shouldCompleteSessionWithoutFinalSoc() throws Exception {
            ChargingSession completedSession = ChargingSession.builder()
                    .id(1L)
                    .status(ChargingSession.SessionStatus.COMPLETED)
                    .energyConsumed(BigDecimal.valueOf(30.0))
                    .build();

            given(sessionService.completeSession(anyLong(), any(BigDecimal.class), any()))
                    .willReturn(completedSession);

            mockMvc.perform(post("/api/v1/charging/sessions/1/complete")
                    .param("energyConsumed", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/charging/sessions/{id}")
    class GetSessionById {

        @Test
        @DisplayName("Should return session when found")
        void shouldReturnSessionWhenFound() throws Exception {
            given(sessionService.getSessionById(1L)).willReturn(testSession);

            mockMvc.perform(get("/api/v1/charging/sessions/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.vehicleId").value(10))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("Should return 404 when session not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(sessionService.getSessionById(999L))
                    .willThrow(new jakarta.persistence.EntityNotFoundException("Charging session not found"));

            mockMvc.perform(get("/api/v1/charging/sessions/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/charging/sessions/vehicle/{vehicleId}")
    class GetSessionsByVehicle {

        @Test
        @DisplayName("Should return sessions for vehicle")
        void shouldReturnSessionsForVehicle() throws Exception {
            List<ChargingSession> sessions = Arrays.asList(testSession);
            given(sessionService.getSessionsByVehicle(10L)).willReturn(sessions);

            mockMvc.perform(get("/api/v1/charging/sessions/vehicle/10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].vehicleId").value(10));
        }

        @Test
        @DisplayName("Should return empty list for vehicle with no sessions")
        void shouldReturnEmptyListForVehicleWithNoSessions() throws Exception {
            given(sessionService.getSessionsByVehicle(999L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/charging/sessions/vehicle/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/charging/sessions/company/{companyId}")
    class GetSessionsByCompany {

        @Test
        @DisplayName("Should return sessions for company")
        void shouldReturnSessionsForCompany() throws Exception {
            List<ChargingSession> sessions = Arrays.asList(testSession);
            given(sessionService.getSessionsByCompany(100L)).willReturn(sessions);

            mockMvc.perform(get("/api/v1/charging/sessions/company/100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }
}
