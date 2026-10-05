package com.biharlearning.web;

import com.biharlearning.config.SecurityConfig;
import com.biharlearning.dto.Dtos.ClassDto;
import com.biharlearning.security.JwtAuthFilter;
import com.biharlearning.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer slice test for the public catalogue endpoints. Verifies the response envelope
 * shape and that services are wired correctly, without a database or the security stack.
 */
@WebMvcTest(controllers = CatalogController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class CatalogControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ContentService content;

    @Test
    void listsClassesInEnvelope() throws Exception {
        when(content.listClasses()).thenReturn(List.of(
                new ClassDto("id-9", "9", "Class IX", true, 4)));

        mvc.perform(get("/api/v1/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("9"))
                .andExpect(jsonPath("$.data[0].name").value("Class IX"))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }
}
