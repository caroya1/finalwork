package com.dianping.recommendation.controller;

import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.recommendation.service.RecommendationService;
import com.dianping.shop.entity.Shop;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void recommendReturnsOk() throws Exception {
        when(recommendationService.recommend(any(RecommendationRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"city\":\"上海\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
