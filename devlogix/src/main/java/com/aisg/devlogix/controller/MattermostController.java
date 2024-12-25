package com.aisg.devlogix.controller;

import com.aisg.devlogix.service.MattermostService;
import com.aisg.devlogix.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/mattermost")
public class MattermostController {

    private static final Logger logger = Logger.getLogger(MattermostController.class.getName());

    @Value("${mattermost.valid-token}")
    private String validToken;

    @Autowired
    private MattermostService mattermostService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleJsonWebhook(@RequestBody Map<String, Object> payload) {
        logger.info("Incoming Webhook: " + payload);

        String token = (String) payload.get("token");
        String text = (String) payload.get("text");
        String userName = (String) payload.get("user_name");
        String channelName = (String) payload.get("channel_name");

        if (!validToken.equals(token)) {
            logger.warning("Invalid token received in webhook: " + token);
            return ResponseEntity.status(403).body("Invalid token");
        }

        mattermostService.saveParsedData(channelName, text);

        logger.info("Broadcasting SSE event to clients...");
        clients.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("updateEvent")
                        .data("New data received from Mattermost Webhook")
                        .id("12345")
                        .reconnectTime(1000));
                logger.info("Event sent to client: " + key);
            } catch (IOException e) {
                logger.warning("Failed to send event to client: " + key + ". Removing emitter.");
                emitter.complete();
                clients.remove(key);
            }
        });

        logger.info(String.format("Processed Webhook: message='%s', user='%s', channel='%s'", text, userName, channelName));

        return ResponseEntity.ok("Webhook processed successfully");
    }

    @GetMapping("/events")
    public SseEmitter streamEvents(@RequestParam("token") String token, @RequestParam("username") String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(token, userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        if (clients.containsKey(username)) {
            logger.info("Existing connection found for username: " + username + ". Closing the old connection.");
            try {
                clients.get(username).complete();
            } catch (Exception e) {
                logger.warning("Error while closing the old connection for username: " + username);
            }
            clients.remove(username);
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        clients.put(username, emitter);
        logger.info("New connection established for username: " + username);

        emitter.onCompletion(() -> {
            logger.info("Client disconnected: " + username);
            clients.remove(username);
        });

        emitter.onTimeout(() -> {
            logger.warning("Client connection timed out: " + username);
            clients.remove(username);
        });

        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("Connected to server"));
            logger.info("Sent connection confirmation to client: " + username);
        } catch (IOException e) {
            logger.warning("Failed to send connection confirmation to client: " + username);
            emitter.complete();
            clients.remove(username);
        }

        return emitter;
    }
}