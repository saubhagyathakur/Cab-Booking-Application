package com.cabbooking.service;

import com.cabbooking.dto.DriverResponseDto;
import com.cabbooking.dto.RideResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastRideUpdate(RideResponseDto ride) {
        String destination = "/topic/rides/" + ride.getId();
        logger.info("Broadcasting ride update to {}: Status {}", destination, ride.getStatus());
        messagingTemplate.convertAndSend(destination, ride);

        // Also notify user/driver channels
        if (ride.getRiderId() != null) {
            messagingTemplate.convertAndSend("/topic/rider/" + ride.getRiderId() + "/rides", ride);
        }
        if (ride.getDriverId() != null) {
            messagingTemplate.convertAndSend("/topic/driver/" + ride.getDriverId() + "/rides", ride);
        }
    }

    public void notifyDriverNewRide(Long driverId, RideResponseDto ride) {
        String destination = "/topic/driver/" + driverId + "/requests";
        logger.info("Notifying driver {} with ride request: {}", driverId, destination);
        messagingTemplate.convertAndSend(destination, ride);
    }

    public void broadcastDriverLocation(DriverResponseDto driver) {
        messagingTemplate.convertAndSend("/topic/drivers/nearby", driver);
    }

    public void broadcastSosAlert(Map<String, Object> sosData) {
        logger.warn("SOS Alert triggered: {}", sosData);
        messagingTemplate.convertAndSend("/topic/sos", sosData);
        messagingTemplate.convertAndSend("/topic/admin/alerts", sosData);
    }
}
