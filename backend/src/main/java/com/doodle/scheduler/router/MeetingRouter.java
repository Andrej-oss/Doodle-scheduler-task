package com.doodle.scheduler.router;

import com.doodle.scheduler.dto.CreateMeetingRequest;
import com.doodle.scheduler.handler.MeetingHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MeetingRouter {

    static final String MEETINGS = "/api/v1/meetings";
    static final String MEETING_BY_ID = "/api/v1/meetings/{meetingId}";
    static final String USER_MEETINGS = "/api/v1/users/{userId}/meetings";

    @Bean
    @RouterOperations({
            @RouterOperation(path = MEETINGS, method = RequestMethod.POST,
                    beanClass = MeetingHandler.class, beanMethod = "schedule",
                    operation = @Operation(operationId = "scheduleMeeting", tags = "Meetings",
                            summary = "Convert a free slot into a meeting with participants",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "Meeting scheduled"))),
            @RouterOperation(path = MEETING_BY_ID, method = RequestMethod.GET,
                    beanClass = MeetingHandler.class, beanMethod = "findById",
                    operation = @Operation(operationId = "getMeetingById", tags = "Meetings",
                            summary = "Get meeting details with participants",
                            responses = @ApiResponse(responseCode = "200", description = "Meeting found"))),
            @RouterOperation(path = USER_MEETINGS, method = RequestMethod.GET,
                    beanClass = MeetingHandler.class, beanMethod = "findByUser",
                    operation = @Operation(operationId = "getUserMeetings", tags = "Meetings",
                            summary = "List all meetings organized by a user",
                            responses = @ApiResponse(responseCode = "200", description = "Meetings list")))
    })
    public RouterFunction<ServerResponse> meetingRoutes(final MeetingHandler handler) {
        return RouterFunctions.route()
                .POST(MEETINGS, handler::schedule)
                .GET(MEETING_BY_ID, handler::findById)
                .GET(USER_MEETINGS, handler::findByUser)
                .build();
    }
}
