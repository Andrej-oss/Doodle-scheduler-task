package com.doodle.scheduler.router;

import com.doodle.scheduler.handler.CalendarHandler;
import com.doodle.scheduler.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(final UserHandler userHandler,
                                                     final CalendarHandler calendarHandler) {
        return RouterFunctions.route()
                .POST("/api/v1/users", userHandler::create)
                .GET("/api/v1/users/{userId}", userHandler::findById)
                .GET("/api/v1/users/{userId}/calendars", calendarHandler::findByUserId)
                .POST("/api/v1/calendars", calendarHandler::create)
                .GET("/api/v1/calendars/{calendarId}", calendarHandler::findById)
                .build();
    }
}
