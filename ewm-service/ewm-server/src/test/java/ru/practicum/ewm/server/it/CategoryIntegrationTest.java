package ru.practicum.ewm.server.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CategoryIntegrationTest extends EwmIntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void adminCanCreateUpdateDeleteCategory_andPublicCanReadIt() throws Exception {
        String createBody = "{\"name\":\"Концерты\"}";
        String created = mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Концерты")))
                .andReturn().getResponse().getContentAsString();

        long catId = objectMapper.readTree(created).get("id").asLong();

        mvc.perform(get("/categories/{catId}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) catId)))
                .andExpect(jsonPath("$.name", is("Концерты")));

        String updateBody = "{\"id\":" + catId + ",\"name\":\"Фестивали\"}";
        mvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) catId)))
                .andExpect(jsonPath("$.name", is("Фестивали")));

        mvc.perform(get("/categories").param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", notNullValue()));

        mvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/categories/{catId}", catId))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCreateCategoryWithDuplicateName_returns409() throws Exception {
        String body = "{\"name\":\"Дубликат\"}";
        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
