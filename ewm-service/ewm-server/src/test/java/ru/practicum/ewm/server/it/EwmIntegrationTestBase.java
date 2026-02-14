package ru.practicum.ewm.server.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.practicum.ewm.server.it.support.FakeStatsServer;

import java.io.IOException;

@ActiveProfiles("test")
public abstract class EwmIntegrationTestBase {

    protected static final FakeStatsServer FAKE_STATS_SERVER = new FakeStatsServer();
    protected static final FakeStatsServer fakeStats = FAKE_STATS_SERVER;

    @BeforeAll
    static void startStatsServer() throws IOException {
        FAKE_STATS_SERVER.start();
    }

    @AfterAll
    static void stopStatsServer() {
        FAKE_STATS_SERVER.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("stats.server.url", FAKE_STATS_SERVER::getBaseUrl);
    }
}
