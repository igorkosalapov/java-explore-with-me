package ru.practicum.ewm.server.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DbFunctionsInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate.execute(
                "CREATE OR REPLACE FUNCTION distance_m(" +
                        "    lat1 double precision," +
                        "    lon1 double precision," +
                        "    lat2 double precision," +
                        "    lon2 double precision" +
                        ") " +
                        "RETURNS double precision AS $$ " +
                        "SELECT 6371000 * acos(" +
                        "    cos(radians(lat1)) * cos(radians(lat2)) * " +
                        "    cos(radians(lon2) - radians(lon1)) + " +
                        "    sin(radians(lat1)) * sin(radians(lat2))" +
                        "); " +
                        "$$ LANGUAGE sql IMMUTABLE;"
        );
    }
}
