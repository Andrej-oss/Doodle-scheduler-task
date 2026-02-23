package com.doodle.scheduler.router;

import com.doodle.scheduler.handler.TimeSlotHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class TimeSlotRouter {

    @Bean
    public RouterFunction<ServerResponse> slotRoutes(final TimeSlotHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/calendars/{calendarId}/slots", handler::create)
                .PUT("/api/v1/slots/{slotId}", handler::update)
                .DELETE("/api/v1/slots/{slotId}", handler::delete)
                .GET("/api/v1/calendars/{calendarId}/slots", handler::findByCalendar)
                .GET("/api/v1/users/{userId}/availability", handler::getAvailability)
                .build();
    }
}
