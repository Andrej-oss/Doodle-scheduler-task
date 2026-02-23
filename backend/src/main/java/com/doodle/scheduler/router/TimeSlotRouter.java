package com.doodle.scheduler.router;

import com.doodle.scheduler.dto.CreateSlotRequest;
import com.doodle.scheduler.dto.UpdateSlotRequest;
import com.doodle.scheduler.handler.TimeSlotHandler;
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
public class TimeSlotRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/v1/calendars/{calendarId}/slots", method = RequestMethod.POST,
                    beanClass = TimeSlotHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createSlot", tags = "Slots",
                            summary = "Create a time slot in a calendar",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateSlotRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "Slot created"))),
            @RouterOperation(path = "/api/v1/slots/{slotId}", method = RequestMethod.PUT,
                    beanClass = TimeSlotHandler.class, beanMethod = "update",
                    operation = @Operation(operationId = "updateSlot", tags = "Slots",
                            summary = "Update slot time or status",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateSlotRequest.class))),
                            responses = @ApiResponse(responseCode = "200", description = "Slot updated"))),
            @RouterOperation(path = "/api/v1/slots/{slotId}", method = RequestMethod.DELETE,
                    beanClass = TimeSlotHandler.class, beanMethod = "delete",
                    operation = @Operation(operationId = "deleteSlot", tags = "Slots",
                            summary = "Delete a free slot",
                            responses = @ApiResponse(responseCode = "204", description = "Slot deleted"))),
            @RouterOperation(path = "/api/v1/calendars/{calendarId}/slots", method = RequestMethod.GET,
                    beanClass = TimeSlotHandler.class, beanMethod = "findByCalendar",
                    operation = @Operation(operationId = "listSlots", tags = "Slots",
                            summary = "List slots with optional filters: status, from, to",
                            responses = @ApiResponse(responseCode = "200", description = "Slots list"))),
            @RouterOperation(path = "/api/v1/users/{userId}/availability", method = RequestMethod.GET,
                    beanClass = TimeSlotHandler.class, beanMethod = "getAvailability",
                    operation = @Operation(operationId = "getAvailability", tags = "Slots",
                            summary = "Get aggregated free/busy availability for a user",
                            responses = @ApiResponse(responseCode = "200", description = "Availability list")))
    })
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
