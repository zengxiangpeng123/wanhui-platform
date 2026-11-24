package com.wanhui.infrastructure;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DatabaseMigrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("wanhui_dev")
            .withUsername("wanhui_test")
            .withPassword("test_only_password");

    @Test
    void appliesBaselineAndRecordsSuccessfulFlywayHistory() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .load();

        MigrateResult result = flyway.migrate();

        assertThat(result.migrationsExecuted).isEqualTo(1);
        try (var connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(),
                MYSQL.getUsername(),
                MYSQL.getPassword()
        )) {
            try (var history = connection.createStatement().executeQuery(
                    "SELECT version, success FROM flyway_schema_history WHERE version = '1'"
            )) {
                assertThat(history.next()).isTrue();
                assertThat(history.getString("version")).isEqualTo("1");
                assertThat(history.getBoolean("success")).isTrue();
            }
            try (var table = connection.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = DATABASE() AND table_name = 'platform_metadata'"
            )) {
                assertThat(table.next()).isTrue();
                assertThat(table.getInt(1)).isEqualTo(1);
            }
        }

        MigrateResult secondRun = flyway.migrate();
        assertThat(secondRun.migrationsExecuted).isZero();
    }
}
