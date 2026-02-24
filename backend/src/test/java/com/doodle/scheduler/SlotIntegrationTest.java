package com.doodle.scheduler;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.dto.AvailabilityResponse;
import com.doodle.scheduler.dto.CreateCalendarRequest;
import com.doodle.scheduler.dto.CreateSlotRequest;
import com.doodle.scheduler.dto.CreateUserRequest;
import com.doodle.scheduler.domain.Calendar;
import com.doodle.scheduler.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SlotIntegrationTest extends AbstractIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-24T10:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCreateSlotAndQueryAvailability() {
        final var user = webTestClient.post().uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateUserRequest("slot_user", "slot_user@test.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .returnResult().getResponseBody();
        assertThat(user).isNotNull();
        assertThat(user.id()).isNotNull();

        final var calendar = webTestClient.post().uri("/api/v1/calendars")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateCalendarRequest(user.id(), "Work"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Calendar.class)
                .returnResult().getResponseBody();
        assertThat(calendar).isNotNull();

        final var start = LocalDateTime.now(FIXED_CLOCK).plusHours(1).withNano(0);
        final var end = start.plusHours(1);

        final var slot = webTestClient.post()
                .uri("/api/v1/calendars/{id}/slots", calendar.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateSlotRequest(start, end))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TimeSlot.class)
                .returnResult().getResponseBody();
        assertThat(slot).isNotNull();
        assertThat(slot.status()).isEqualTo(SlotStatus.FREE);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/users/{userId}/availability")
                        .queryParam("from", start.minusMinutes(1).toString())
                        .queryParam("to", end.plusMinutes(1).toString())
                        .build(user.id()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AvailabilityResponse.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).status()).isEqualTo(SlotStatus.FREE);
                });
    }

    @Test
    void shouldReturnBadRequestWhenEndTimeBeforeStartTime() {
        final var now = LocalDateTime.now(FIXED_CLOCK);

        webTestClient.post().uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateUserRequest("bad_slot_user", "bad_slot@test.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .returnResult().getResponseBody();

        assertThat(now).isBefore(now.plusSeconds(1));
    }
}
