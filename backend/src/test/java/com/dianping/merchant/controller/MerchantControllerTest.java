package com.dianping.merchant.controller;

import com.dianping.merchant.entity.Merchant;
import com.dianping.merchant.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantService merchantService;

    @Test
    void createMerchantReturnsOk() throws Exception {
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setName("Test Merchant");

        when(merchantService.create(any(Merchant.class))).thenReturn(merchant);

        mockMvc.perform(post("/api/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Merchant\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listMerchantsReturnsOk() throws Exception {
        when(merchantService.list()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
