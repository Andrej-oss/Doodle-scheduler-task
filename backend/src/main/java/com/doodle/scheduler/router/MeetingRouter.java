package com.doodle.scheduler.router;

import com.doodle.scheduler.handler.MeetingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MeetingRouter {

    @Bean
    public RouterFunction<ServerResponse> meetingRoutes(final MeetingHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/meetings", handler::schedule)
                .GET("/api/v1/meetings/{meetingId}", handler::findById)
                .GET("/api/v1/users/{userId}/meetings", handler::findByUser)
                .build();
    }
}
