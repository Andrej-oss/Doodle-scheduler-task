package com.doodle.scheduler.router;

import com.doodle.scheduler.dto.CreateCalendarRequest;
import com.doodle.scheduler.dto.CreateUserRequest;
import com.doodle.scheduler.handler.CalendarHandler;
import com.doodle.scheduler.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/v1/users", method = RequestMethod.POST,
                    beanClass = UserHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createUser", tags = "Users",
                            summary = "Create a new user",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateUserRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "User created"))),
            @RouterOperation(path = "/api/v1/users/{userId}", method = RequestMethod.GET,
                    beanClass = UserHandler.class, beanMethod = "findById",
                    operation = @Operation(operationId = "getUserById", tags = "Users",
                            summary = "Get user by ID",
                            responses = @ApiResponse(responseCode = "200", description = "User found"))),
            @RouterOperation(path = "/api/v1/users/{userId}/calendars", method = RequestMethod.GET,
                    beanClass = CalendarHandler.class, beanMethod = "findByUserId",
                    operation = @Operation(operationId = "getUserCalendars", tags = "Calendars",
                            summary = "List calendars for a user",
                            responses = @ApiResponse(responseCode = "200", description = "Calendars list"))),
            @RouterOperation(path = "/api/v1/calendars", method = RequestMethod.POST,
                    beanClass = CalendarHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createCalendar", tags = "Calendars",
                            summary = "Create a calendar",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateCalendarRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "Calendar created"))),
            @RouterOperation(path = "/api/v1/calendars/{calendarId}", method = RequestMethod.GET,
                    beanClass = CalendarHandler.class, beanMethod = "findById",
                    operation = @Operation(operationId = "getCalendarById", tags = "Calendars",
                            summary = "Get calendar by ID",
                            responses = @ApiResponse(responseCode = "200", description = "Calendar found")))
    })
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
