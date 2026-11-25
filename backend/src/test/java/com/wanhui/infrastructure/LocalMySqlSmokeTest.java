package com.wanhui.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LocalMySqlSmokeTest {

    @Test
    void connectsToDockerMySqlAndReadsBaselineMetadata() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv("WANHUI_LOCAL_DB_TEST")),
                "Set WANHUI_LOCAL_DB_TEST=true to verify the Docker MySQL instance");

        String host = envOrDefault("DB_HOST", "127.0.0.1");
        String port = envOrDefault("DB_PORT", "3308");
        String database = envOrDefault("DB_NAME", "wanhui_dev");
        String username = envOrDefault("DB_USERNAME", "wanhui_app");
        String password = envOrDefault("DB_PASSWORD", "local_only_password");
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
                + "&allowPublicKeyRetrieval=true&useSSL=false";

        Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(jdbcUrl, username, password);
             var statement = connection.createStatement();
             var result = statement.executeQuery(
                     "SELECT metadata_value FROM platform_metadata WHERE metadata_key = 'schema_version'"
             )) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString(1)).isEqualTo("1");
        }
    }

    private String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
