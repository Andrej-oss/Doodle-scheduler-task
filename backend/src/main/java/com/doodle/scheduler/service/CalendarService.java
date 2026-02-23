package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.Calendar;
import com.doodle.scheduler.dto.CreateCalendarRequest;
import com.doodle.scheduler.exception.NotFoundException;
import com.doodle.scheduler.repository.CalendarRepository;
import com.doodle.scheduler.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    public Mono<Calendar> create(@NonNull final CreateCalendarRequest request) {
        return userRepository.existsById(request.userId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new NotFoundException("User not found: " + request.userId()));
                    }
                    final Calendar calendar = Calendar.builder()
                            .userId(request.userId())
                            .name(request.name())
                            .build();
                    return calendarRepository.save(calendar);
                });
    }

    public Mono<Calendar> findById(@NonNull final UUID id) {
        return calendarRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Calendar not found: " + id)));
    }

    public Flux<Calendar> findByUserId(@NonNull final UUID userId) {
        return calendarRepository.findAllByUserId(userId);
    }
}
