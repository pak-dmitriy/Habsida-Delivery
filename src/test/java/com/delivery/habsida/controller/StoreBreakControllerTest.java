package com.delivery.habsida.controller;

import com.delivery.habsida.dto.StoreBreakRequest;
import com.delivery.habsida.dto.StoreBreakResponse;
import com.delivery.habsida.security.JwtService;
import com.delivery.habsida.security.WebSecurityConfig;
import com.delivery.habsida.service.StoreBreakService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreBreakController.class)
@Import(WebSecurityConfig.class)
@WithMockUser
public class StoreBreakControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private StoreBreakService storeBreakService;

    @MockitoBean
    private JwtService jwtService;

    private StoreBreakResponse defaultResponse() {
        return new StoreBreakResponse(
                2L,
                3L,
                LocalTime.of(12, 0),
                LocalTime.of(13, 0)
        );
    }

    private StoreBreakRequest defaultRequest() {
        return new StoreBreakRequest(
                LocalTime.of(12, 0),
                LocalTime.of(13, 0)
        );
    }

    @Test
    void createStoreBreak_success() throws Exception {
        when(storeBreakService.createStoreBreak(anyLong(), anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/stores/{storeId}/hours/{hourId}/breaks", 1L, 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.storeHourId").value(3L));

        verify(storeBreakService).createStoreBreak(eq(1L), eq(3L), any());
    }

    @Test
    void getStoreBreaks_success() throws Exception {
        when(storeBreakService.getStoreBreaks(anyLong(), anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(defaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/hours/{hourId}/breaks", 1L, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[0].storeHourId").value(3L))
                .andExpect(jsonPath("$.content[0].startTime").value("12:00:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("13:00:00"));

        verify(storeBreakService).getStoreBreaks(eq(1L), eq(3L), any(Pageable.class));
    }

    @Test
    void getStoreBreak_success() throws Exception {
        when(storeBreakService.getStoreBreak(anyLong(), anyLong(), anyLong())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}/hours/{hourId}/breaks/{breakId}", 1L, 3L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeHourId").value(3L))
                .andExpect(jsonPath("$.startTime").value("12:00:00"))
                .andExpect(jsonPath("$.endTime").value("13:00:00"));

        verify(storeBreakService).getStoreBreak(eq(1L), eq(3L), eq(2L));
    }

    @Test
    void updateStoreBreak_success() throws Exception {
        when(storeBreakService.updateStoreBreak(anyLong(), anyLong(), anyLong(), any())).thenReturn(defaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}/hours/{hourId}/breaks/{breakId}", 1L, 3L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.storeHourId").value(3L));

        verify(storeBreakService).updateStoreBreak(eq(1L), eq(3L), eq(2L), any());
    }

    @Test
    void deleteStoreBreak_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}/hours/{hourId}/breaks/{breakId}", 1L, 3L, 2L))
                .andExpect(status().isNoContent());

        verify(storeBreakService).deleteStoreBreak(1L, 3L, 2L);
    }
}
