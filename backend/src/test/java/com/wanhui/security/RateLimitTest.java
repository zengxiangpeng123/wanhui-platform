package com.wanhui.security;

import com.wanhui.security.rate.InMemoryRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "wanhui.security.rate-limit.enabled=true",
        "wanhui.security.rate-limit.login-capacity=2",
        "wanhui.security.rate-limit.register-capacity=3",
        "wanhui.security.rate-limit.comment-capacity=3",
        "wanhui.security.rate-limit.window-millis=60000"
})
@AutoConfigureMockMvc
class RateLimitTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void tokenBucketAllowsCapacityThenRefills() {
        MutableClock clock = new MutableClock(0);
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(3, 1000, clock);

        assertThat(limiter.tryAcquire("client")).isTrue();
        assertThat(limiter.tryAcquire("client")).isTrue();
        assertThat(limiter.tryAcquire("client")).isTrue();
        assertThat(limiter.tryAcquire("client")).isFalse();
        assertThat(limiter.tryAcquire("other-client")).isTrue();

        clock.advance(1000);
        assertThat(limiter.tryAcquire("client")).isTrue();
    }

    @Test
    void loginIsRateLimitedAndRecoversAfterWindow() throws Exception {
        String body = "{\"account\":\"nobody\",\"password\":\"WrongPass1\"}";

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    private static final class MutableClock extends Clock {
        private long millis;

        private MutableClock(long millis) {
            this.millis = millis;
        }

        private void advance(long delta) {
            this.millis += delta;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public long millis() {
            return millis;
        }
    }
}

