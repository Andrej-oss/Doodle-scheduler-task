package com.doodle.scheduler;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.domain.Calendar;
import com.doodle.scheduler.domain.User;
import com.doodle.scheduler.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingIntegrationTest extends AbstractIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-24T10:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldScheduleMeetingAndMarkSlotBusy() {
        final var organizer = createUser("organizer", "organizer@test.com");
        final var participant = createUser("participant", "participant@test.com");

        final var calendar = webTestClient.post().uri("/api/v1/calendars")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateCalendarRequest(organizer.id(), "My Calendar"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Calendar.class)
                .returnResult().getResponseBody();
        assertThat(calendar).isNotNull();

        final var start = LocalDateTime.now(FIXED_CLOCK).plusDays(1).withNano(0);
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

        final var meeting = webTestClient.post().uri("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateMeetingRequest(
                        slot.id(),
                        organizer.id(),
                        "Team Sync",
                        "Weekly sync meeting",
                        List.of(participant.id())
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult().getResponseBody();
        assertThat(meeting).isNotNull();
        assertThat(meeting.title()).isEqualTo("Team Sync");
        assertThat(meeting.participantIds()).contains(participant.id());

        webTestClient.get()
                .uri("/api/v1/calendars/{id}/slots?status=BUSY", calendar.id())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TimeSlot.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).status()).isEqualTo(SlotStatus.BUSY);
                    assertThat(list.get(0).meetingId()).isEqualTo(meeting.id());
                });

        webTestClient.get()
                .uri("/api/v1/users/{userId}/meetings", organizer.id())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MeetingResponse.class)
                .value(list -> assertThat(list).hasSize(1));
    }

    @Test
    void shouldReturn409WhenSchedulingOnBusySlot() {
        final var user = createUser("busy_user", "busy_user@test.com");
        final var calendar = webTestClient.post().uri("/api/v1/calendars")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateCalendarRequest(user.id(), "Cal"))
                .exchange().expectStatus().isCreated()
                .expectBody(Calendar.class).returnResult().getResponseBody();
        assertThat(calendar).isNotNull();

        final var start = LocalDateTime.now(FIXED_CLOCK).plusDays(2).withNano(0);
        final var slot = webTestClient.post()
                .uri("/api/v1/calendars/{id}/slots", calendar.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateSlotRequest(start, start.plusHours(1)))
                .exchange().expectStatus().isCreated()
                .expectBody(TimeSlot.class).returnResult().getResponseBody();
        assertThat(slot).isNotNull();

        final var meetingRequest = new CreateMeetingRequest(
                slot.id(), user.id(), "First", null, List.of());

        webTestClient.post().uri("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(meetingRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post().uri("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(meetingRequest)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    private User createUser(final String username, final String email) {
        final var user = webTestClient.post().uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateUserRequest(username, email))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .returnResult().getResponseBody();
        assertThat(user).isNotNull();
        return user;
    }
}
