package com.cabbooking.controller;

import com.cabbooking.dto.DriverLocationDto;
import com.cabbooking.dto.RideResponseDto;
import com.cabbooking.service.DriverService;
import com.cabbooking.service.RideService;
import com.cabbooking.service.WebSocketService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class RideWebSocketController {

    private final RideService rideService;
    private final DriverService driverService;
    private final WebSocketService webSocketService;

    public RideWebSocketController(RideService rideService,
                                   DriverService driverService,
                                   WebSocketService webSocketService) {
        this.rideService = rideService;
        this.driverService = driverService;
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/driver/{driverId}/location")
    public void handleDriverLocationUpdate(@DestinationVariable Long driverId, @Payload DriverLocationDto locationDto) {
        driverService.updateDriverLocation(driverId, locationDto.getLatitude(), locationDto.getLongitude());
    }

    @MessageMapping("/rides/{rideId}/sos")
    public void handleSosAlert(@DestinationVariable Long rideId, @Payload Map<String, Object> payload) {
        payload.put("rideId", rideId);
        payload.put("timestamp", System.currentTimeMillis());
        webSocketService.broadcastSosAlert(payload);
    }
}
