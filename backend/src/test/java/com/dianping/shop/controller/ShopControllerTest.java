package com.dianping.shop.controller;

import com.dianping.shop.entity.Shop;
import com.dianping.shop.service.ShopService;
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

@WebMvcTest(ShopController.class)
class ShopControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShopService shopService;

    @Test
    void createShopReturnsOk() throws Exception {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("Cafe A");
        shop.setCity("上海");

        when(shopService.create(any(Shop.class))).thenReturn(shop);

        mockMvc.perform(post("/api/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cafe A\",\"city\":\"上海\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listShopsReturnsOk() throws Exception {
        when(shopService.listByCity("上海")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/shops").param("city", "上海"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
