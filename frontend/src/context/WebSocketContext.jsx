import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const subscriptionsRef = useRef(new Map());

  useEffect(() => {
    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('[WebSocket] Connected successfully to Spring Boot STOMP broker');
        setConnected(true);
      },
      onDisconnect: () => {
        console.log('[WebSocket] Disconnected from STOMP broker');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers['message'], frame.body);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, []);

  const subscribe = (topic, callback) => {
    if (!clientRef.current || !clientRef.current.connected) {
      // Retry subscription once connected
      const timer = setTimeout(() => subscribe(topic, callback), 800);
      return () => clearTimeout(timer);
    }

    try {
      const sub = clientRef.current.subscribe(topic, (message) => {
        try {
          const parsed = JSON.parse(message.body);
          callback(parsed);
        } catch (e) {
          callback(message.body);
        }
      });
      return () => sub.unsubscribe();
    } catch (e) {
      console.warn('Subscription error for topic:', topic, e);
      return () => {};
    }
  };

  const publish = (destination, body) => {
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({
        destination,
        body: JSON.stringify(body),
      });
    }
  };

  const sendDriverLocation = (driverId, lat, lng) => {
    publish(`/app/driver/${driverId}/location`, { latitude: lat, longitude: lng });
  };

  const triggerSos = (rideId, payload = {}) => {
    publish(`/app/rides/${rideId}/sos`, payload);
  };

  return (
    <WebSocketContext.Provider
      value={{
        connected,
        subscribe,
        publish,
        sendDriverLocation,
        triggerSos,
      }}
    >
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
