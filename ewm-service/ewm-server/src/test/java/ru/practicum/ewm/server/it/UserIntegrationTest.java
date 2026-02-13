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
class UserIntegrationTest extends EwmIntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void adminCanCreateListAndDeleteUsers() throws Exception {
        String body = "{\"email\":\"ivan.petrov@practicummail.ru\",\"name\":\"Иван Петров\"}";
        String created = mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("ivan.petrov@practicummail.ru")))
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(created).get("id").asLong();

        mvc.perform(get("/admin/users")
                        .param("ids", String.valueOf(userId))
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) userId)));

        mvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }
}
