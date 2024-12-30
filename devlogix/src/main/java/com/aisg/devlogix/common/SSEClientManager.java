package com.aisg.devlogix.common;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Logger;
import java.io.IOException;

@Component
public class SSEClientManager {

    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(SSEClientManager.class.getName());

    public Map<String, SseEmitter> getClients() {
        return clients;
    }

    public void addClient(String key, SseEmitter emitter) {
        clients.put(key, emitter);
    }

    public void removeClient(String key) {
        clients.remove(key);
    }

    public SseEmitter getClient(String key) {
        return clients.get(key);
    }

    public boolean clientExists(String clientKey) {
        return clients.containsKey(clientKey);
    }

    public void broadcastEvent(String eventName, String data, String id, String suffix) {
        clients.forEach((key, emitter) -> {
            if (key.endsWith(suffix)) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(data)
                            .id(id)
                            .reconnectTime(1000));
                    logger.info("Event sent to client: " + key);
                } catch (IOException e) {
                    logger.warning("Failed to send event to client: " + key + ". Reason: " + e.getMessage());
                    emitter.complete();
                    clients.remove(key);
                    logger.info("Client removed: " + key);
                }
            }
        });
    }
}