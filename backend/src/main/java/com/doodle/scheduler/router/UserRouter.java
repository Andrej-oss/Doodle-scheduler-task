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

    static final String USERS = "/api/v1/users";
    static final String USERS_SEARCH = "/api/v1/users/search";
    static final String USER_BY_ID = "/api/v1/users/{userId}";
    static final String USER_CALENDARS = "/api/v1/users/{userId}/calendars";
    static final String CALENDARS = "/api/v1/calendars";
    static final String CALENDAR_BY_ID = "/api/v1/calendars/{calendarId}";

    @Bean
    @RouterOperations({
            @RouterOperation(path = USERS, method = RequestMethod.POST,
                    beanClass = UserHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createUser", tags = "Users",
                            summary = "Create a new user",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateUserRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "User created"))),
            @RouterOperation(path = USERS_SEARCH, method = RequestMethod.GET,
                    beanClass = UserHandler.class, beanMethod = "search",
                    operation = @Operation(operationId = "searchUsers", tags = "Users",
                            summary = "Search users by username or email (min 2 chars)",
                            responses = @ApiResponse(responseCode = "200", description = "Matching users"))),
            @RouterOperation(path = USER_BY_ID, method = RequestMethod.GET,
                    beanClass = UserHandler.class, beanMethod = "findById",
                    operation = @Operation(operationId = "getUserById", tags = "Users",
                            summary = "Get user by ID",
                            responses = @ApiResponse(responseCode = "200", description = "User found"))),
            @RouterOperation(path = USER_CALENDARS, method = RequestMethod.GET,
                    beanClass = CalendarHandler.class, beanMethod = "findByUserId",
                    operation = @Operation(operationId = "getUserCalendars", tags = "Calendars",
                            summary = "List calendars for a user",
                            responses = @ApiResponse(responseCode = "200", description = "Calendars list"))),
            @RouterOperation(path = CALENDARS, method = RequestMethod.POST,
                    beanClass = CalendarHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createCalendar", tags = "Calendars",
                            summary = "Create a calendar",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateCalendarRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "Calendar created"))),
            @RouterOperation(path = CALENDAR_BY_ID, method = RequestMethod.GET,
                    beanClass = CalendarHandler.class, beanMethod = "findById",
                    operation = @Operation(operationId = "getCalendarById", tags = "Calendars",
                            summary = "Get calendar by ID",
                            responses = @ApiResponse(responseCode = "200", description = "Calendar found")))
    })
    public RouterFunction<ServerResponse> userRoutes(final UserHandler userHandler,
                                                     final CalendarHandler calendarHandler) {
        return RouterFunctions.route()
                .POST(USERS, userHandler::create)
                .GET(USERS_SEARCH, userHandler::search)
                .GET(USER_BY_ID, userHandler::findById)
                .GET(USER_CALENDARS, calendarHandler::findByUserId)
                .POST(CALENDARS, calendarHandler::create)
                .GET(CALENDAR_BY_ID, calendarHandler::findById)
                .build();
    }
}
