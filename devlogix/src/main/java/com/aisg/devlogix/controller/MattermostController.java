package com.aisg.devlogix.controller;

import com.aisg.devlogix.common.SSEClientManager;
import com.aisg.devlogix.service.MattermostService;
import com.aisg.devlogix.service.RateLimiterService;
import com.aisg.devlogix.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Map;
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
    private RateLimiterService rateLimiterService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SSEClientManager sseClientManager;

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleJsonWebhook(@RequestBody Map<String, Object> payload) {
        logger.info("Incoming Mattermost Webhook: " + payload);

        String token = (String) payload.get("token");
        String text = (String) payload.get("text");
        String userName = (String) payload.get("user_name");
        String channelName = (String) payload.get("channel_name");

        if (!validToken.equals(token)) {
            logger.warning("Invalid token received in webhook: " + token);
            return ResponseEntity.status(403).body("Invalid token");
        }

        mattermostService.saveParsedData(channelName, text);

        logger.info("Broadcasting SSE event to Mattermost clients...");
        sseClientManager.broadcastEvent("updateEvent", "New data received from Mattermost Webhook", "mattermost-" + channelName, ":mattermost");

        logger.info(String.format("Processed Mattermost Webhook: message='%s', user='%s', channel='%s'", text, userName, channelName));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("X-Accel-Buffering", "no");
        headers.add("Connection", "keep-alive");

        return ResponseEntity.ok()
                .headers(headers)
                .body("Webhook processed successfully");
    }

    @GetMapping(value = "/events", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> streamEvents(
            @RequestParam("token") String token,
            @RequestParam("username") String username,
            @RequestParam("serviceType") String serviceType) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(token, userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String clientKey = username + ":" + serviceType;

        if (sseClientManager.clientExists(clientKey)) {
            logger.info("Existing connection found for client: " + clientKey + ". Closing the old connection.");
            sseClientManager.removeClient(clientKey);
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseClientManager.addClient(clientKey, emitter);

        logger.info("New connection established for client: " + clientKey);

        emitter.onCompletion(() -> {
            logger.info("Client disconnected: " + clientKey);
            sseClientManager.removeClient(clientKey);
        });

        emitter.onTimeout(() -> {
            logger.warning("Client connection timed out: " + clientKey);
            sseClientManager.removeClient(clientKey);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to " + serviceType));
            logger.info("Sent connection confirmation to client: " + clientKey);
        } catch (IOException e) {
            logger.warning("Failed to send connection confirmation to client: " + clientKey);
            sseClientManager.removeClient(clientKey);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("X-Accel-Buffering", "no");
        headers.add("Connection", "keep-alive");
        headers.add("Content-Type", "text/event-stream");

        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }
}