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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PublicEventIntegrationTest extends EwmIntegrationTestBase {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    long catId;
    long initiatorId;
    long requesterId;

    @BeforeEach
    void setUp() throws Exception {
        catId = createCategory("Концерты");
        initiatorId = createUser("init@practicummail.ru", "Инициатор");
        requesterId = createUser("req@practicummail.ru", "Запросчик");
        fakeStats.reset();
    }

    @Test
    void publicEndpointsReturnOnlyPublishedEventsAndWriteStats() throws Exception {
        long publishedEventId = createEvent(initiatorId, catId, false);
        publishEvent(publishedEventId);

        createEvent(initiatorId, catId, false);

        mvc.perform(post("/users/{userId}/requests", requesterId)
                        .param("eventId", String.valueOf(publishedEventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        mvc.perform(get("/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) publishedEventId)))
                .andExpect(jsonPath("$[0].confirmedRequests", is(1)))
                .andExpect(jsonPath("$[0].views", is(0)));

        assertEquals(1L, fakeStats.getHitsByUri().getOrDefault("/events", 0L));

        mvc.perform(get("/events/{id}", publishedEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) publishedEventId)))
                .andExpect(jsonPath("$.confirmedRequests", is(1)))
                .andExpect(jsonPath("$.views", is(1)));

        assertEquals(1L, fakeStats.getHitsByUri().getOrDefault("/events/" + publishedEventId, 0L));

        mvc.perform(get("/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].views", is(1)));

        assertEquals(2L, fakeStats.getHitsByUri().getOrDefault("/events", 0L));

        mvc.perform(get("/events/{id}", publishedEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) publishedEventId)))
                .andExpect(jsonPath("$.views", is(2)));

        assertEquals(2L, fakeStats.getHitsByUri().getOrDefault("/events/" + publishedEventId, 0L));
    }

    @Test
    void initiatorCannotCreateParticipationRequestForOwnEvent() throws Exception {
        long eventId = createEvent(initiatorId, catId, false);
        publishEvent(eventId);

        mvc.perform(post("/users/{userId}/requests", initiatorId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isConflict());
    }

    private long createCategory(String name) throws Exception {
        String created = mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(created).get("id").asLong();
    }

    private long createUser(String email, String name) throws Exception {
        String created = mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(created).get("id").asLong();
    }

    private long createEvent(long userId, long categoryId, boolean paid) throws Exception {
        String date = LocalDateTime.now().plusHours(3).format(FMT);
        String body = "{" +
                "\"title\":\"Событие\"," +
                "\"annotation\":\"Очень интересное событие для теста\"," +
                "\"description\":\"Подробное описание события для интеграционного теста\"," +
                "\"category\":" + categoryId + "," +
                "\"location\":{\"lat\":55.75,\"lon\":37.62}," +
                "\"eventDate\":\"" + date + "\"," +
                "\"paid\":" + paid + "," +
                "\"participantLimit\":10," +
                "\"requestModeration\":false" +
                "}";

        String created = mvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(created).get("id").asLong();
    }

    private void publishEvent(long eventId) throws Exception {
        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stateAction\":\"PUBLISH_EVENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("PUBLISHED")));
    }
}
