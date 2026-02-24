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

    static final String CALENDAR_SLOTS = "/api/v1/calendars/{calendarId}/slots";
    static final String SLOT_BY_ID = "/api/v1/slots/{slotId}";
    static final String USER_AVAILABILITY = "/api/v1/users/{userId}/availability";

    @Bean
    @RouterOperations({
            @RouterOperation(path = CALENDAR_SLOTS, method = RequestMethod.POST,
                    beanClass = TimeSlotHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createSlot", tags = "Slots",
                            summary = "Create a time slot in a calendar",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateSlotRequest.class))),
                            responses = @ApiResponse(responseCode = "201", description = "Slot created"))),
            @RouterOperation(path = SLOT_BY_ID, method = RequestMethod.PUT,
                    beanClass = TimeSlotHandler.class, beanMethod = "update",
                    operation = @Operation(operationId = "updateSlot", tags = "Slots",
                            summary = "Update slot time or status",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateSlotRequest.class))),
                            responses = @ApiResponse(responseCode = "200", description = "Slot updated"))),
            @RouterOperation(path = SLOT_BY_ID, method = RequestMethod.DELETE,
                    beanClass = TimeSlotHandler.class, beanMethod = "delete",
                    operation = @Operation(operationId = "deleteSlot", tags = "Slots",
                            summary = "Delete a free slot",
                            responses = @ApiResponse(responseCode = "204", description = "Slot deleted"))),
            @RouterOperation(path = CALENDAR_SLOTS, method = RequestMethod.GET,
                    beanClass = TimeSlotHandler.class, beanMethod = "findByCalendar",
                    operation = @Operation(operationId = "listSlots", tags = "Slots",
                            summary = "List slots with optional filters: status, from, to",
                            responses = @ApiResponse(responseCode = "200", description = "Slots list"))),
            @RouterOperation(path = USER_AVAILABILITY, method = RequestMethod.GET,
                    beanClass = TimeSlotHandler.class, beanMethod = "getAvailability",
                    operation = @Operation(operationId = "getAvailability", tags = "Slots",
                            summary = "Get aggregated free/busy availability for a user",
                            responses = @ApiResponse(responseCode = "200", description = "Availability list")))
    })
    public RouterFunction<ServerResponse> slotRoutes(final TimeSlotHandler handler) {
        return RouterFunctions.route()
                .POST(CALENDAR_SLOTS, handler::create)
                .PUT(SLOT_BY_ID, handler::update)
                .DELETE(SLOT_BY_ID, handler::delete)
                .GET(CALENDAR_SLOTS, handler::findByCalendar)
                .GET(USER_AVAILABILITY, handler::getAvailability)
                .build();
    }
}
