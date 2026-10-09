package com.delivery.habsida;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrations_shouldBeApplied() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history", Integer.class);

        assertTrue(count > 0);
    }
}