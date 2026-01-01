package com.safevision.recognitionservice.controller;

import com.safevision.recognitionservice.facade.TrackingWorkflowFacade;
import com.safevision.recognitionservice.producer.AlertProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecognitionController.class)
class RecognitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrackingWorkflowFacade workflowFacade;

    @MockBean
    private AlertProducer alertProducer; // 👈 O Spring reclamou da falta deste cara

    @Test
    void controllerLoads() throws Exception {
        // Apenas valida que o contexto web do controller sobe
    }
    
}