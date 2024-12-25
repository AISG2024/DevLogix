package com.aisg.devlogix.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mattermost")
public class MattermostController {

    @Value("${mattermost.valid-token}")
    private String validToken;

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleJsonWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Incoming Webhook: " + payload);

        String token = (String) payload.get("token");
        String text = (String) payload.get("text");
        String userName = (String) payload.get("user_name");
        String channelName = (String) payload.get("channel_name");

        if (!validToken.equals(token)) {
            return ResponseEntity.status(403).body("Invalid token");
        }

        System.out.printf("Received message: '%s' from user: '%s' in channel: '%s'%n", text, userName, channelName);

        String responseMessage = String.format("Hello %s, you said: %s", userName, text);

        return ResponseEntity.ok(responseMessage);
    }
}