package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.Calendar;
import com.doodle.scheduler.dto.CreateCalendarRequest;
import com.doodle.scheduler.exception.CalendarNotFoundException;
import com.doodle.scheduler.exception.UserNotFoundException;
import com.doodle.scheduler.repository.CalendarRepository;
import com.doodle.scheduler.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String ERR_USER_NOT_FOUND = "User not found: ";
    private static final String ERR_CALENDAR_NOT_FOUND = "Calendar not found: ";

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    @Transactional
    public Mono<Calendar> create(@NonNull final CreateCalendarRequest request) {
        log.info("Creating calendar: name='{}', userId={}", request.name(), request.userId());
        return userRepository.existsById(request.userId())
                .flatMap(exists -> {
                    if (!exists) {
                        log.warn("User not found: {}", request.userId());
                        return Mono.error(new UserNotFoundException(ERR_USER_NOT_FOUND + request.userId()));
                    }
                    final var calendar = Calendar.builder()
                            .userId(request.userId())
                            .name(request.name())
                            .build();
                    return calendarRepository.save(calendar);
                })
                .doOnSuccess(c -> log.info("Calendar created: id={}, name='{}'", c.id(), c.name()));
    }

    public Mono<Calendar> findById(@NonNull final UUID id) {
        log.debug("Finding calendar by id={}", id);
        return calendarRepository.findById(id)
                .switchIfEmpty(Mono.error(new CalendarNotFoundException(ERR_CALENDAR_NOT_FOUND + id)));
    }

    public Flux<Calendar> findByUserId(@NonNull final UUID userId) {
        log.debug("Finding calendars for userId={}", userId);
        return calendarRepository.findAllByUserId(userId);
    }
}
