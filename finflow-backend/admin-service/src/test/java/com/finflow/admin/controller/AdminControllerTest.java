package com.finflow.admin.controller;


import com.finflow.admin.service.AdminWorkflowService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminWorkflowService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetApps() throws Exception {

        when(service.getAllApps()).thenReturn(List.of());

        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk());
    }
}
