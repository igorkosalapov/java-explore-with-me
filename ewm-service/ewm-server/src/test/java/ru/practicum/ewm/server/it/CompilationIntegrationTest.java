package ru.practicum.ewm.server.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CompilationIntegrationTest extends EwmIntegrationTestBase {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private long catId;
    private long initiatorId;
    private long eventId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        catId = objectMapper.readTree(
                mockMvc.perform(post("/admin/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("name", "Категория-" + suffix))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asLong();

        initiatorId = objectMapper.readTree(
                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "name", "Инициатор-" + suffix,
                                        "email", "initiator-" + suffix + "@example.com"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asLong();

        String eventDate = LocalDateTime.now().plusHours(4).format(DTF);
        String newEventJson = objectMapper.writeValueAsString(Map.of(
                "title", "Событие для подборки",
                "annotation", "Аннотация для тестового события (достаточно длинная)",
                "description", "Описание для тестового события (достаточно длинное)",
                "category", catId,
                "location", Map.of("lat", 55.75, "lon", 37.61),
                "eventDate", eventDate
        ));

        eventId = objectMapper.readTree(
                mockMvc.perform(post("/users/{userId}/events", initiatorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newEventJson))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asLong();

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stateAction", "PUBLISH_EVENT"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateReadAndUpdateCompilationViaAdminAndPublicApi() throws Exception {
        String createJson = objectMapper.writeValueAsString(Map.of(
                "title", "Летние концерты",
                "pinned", true,
                "events", new long[]{eventId}
        ));

        long compId = objectMapper.readTree(
                mockMvc.perform(post("/admin/compilations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.pinned", is(true)))
                        .andExpect(jsonPath("$.events", hasSize(1)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asLong();

        mockMvc.perform(get("/compilations").param("pinned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) compId)));

        mockMvc.perform(get("/compilations/{compId}", compId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) compId)))
                .andExpect(jsonPath("$.pinned", is(true)))
                .andExpect(jsonPath("$.events", hasSize(1)));

        String updateJson = objectMapper.writeValueAsString(Map.of(
                "title", "Обновленная подборка",
                "pinned", false,
                "events", new long[]{}
        ));
        mockMvc.perform(patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Обновленная подборка")))
                .andExpect(jsonPath("$.pinned", is(false)))
                .andExpect(jsonPath("$.events", hasSize(0)));

        mockMvc.perform(get("/compilations").param("pinned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
