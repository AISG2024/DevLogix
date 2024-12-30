package com.aisg.devlogix.controller;

import com.aisg.devlogix.common.SSEClientManager;
import com.aisg.devlogix.service.NotionService;
import com.aisg.devlogix.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/notion")
public class NotionController {

    private static final Logger logger = Logger.getLogger(NotionController.class.getName());

    @Autowired
    private NotionService notionService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SSEClientManager sseClientManager;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        logger.info("Incoming Notion Webhook: " + payload);

        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            
            if (data == null || !data.containsKey("properties") || !data.containsKey("id") || !data.containsKey("last_edited_time")) {
                return ResponseEntity.badRequest().body("Invalid payload: missing required fields.");
            }

            String id = (String) data.get("id");
            String lastEditedTime = (String) data.get("last_edited_time");

            if (!notionService.isLastEditedToday(lastEditedTime)) {
                return ResponseEntity.ok("Record not saved: last_edited_time is not today.");
            }

            if (notionService.recordExists(id, lastEditedTime)) {
                return ResponseEntity.ok("Duplicate record: id and last_edited_time already exist.");
            }

            Map<String, Object> properties = (Map<String, Object>) data.get("properties");
            if (properties == null || !properties.containsKey("Name") || !properties.containsKey("Person")) {
                return ResponseEntity.badRequest().body("Invalid payload: missing required properties.");
            }

            Map<String, Object> nameProperty = (Map<String, Object>) properties.get("Name");
            List<Map<String, Object>> titleList = (List<Map<String, Object>>) nameProperty.get("title");
            if (titleList == null || titleList.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid payload: Name property is empty.");
            }
            String name = (String) titleList.get(0).get("plain_text");

            Map<String, Object> personProperty = (Map<String, Object>) properties.get("Person");
            List<Map<String, Object>> peopleList = (List<Map<String, Object>>) personProperty.get("people");
            if (peopleList == null || peopleList.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid payload: Person property is empty.");
            }

            String personNames = peopleList.stream()
                    .map(person -> (String) person.get("name"))
                    .filter(personName -> personName != null)
                    .reduce((name1, name2) -> name1 + ", " + name2)
                    .orElse("");

            notionService.saveRecord(id, lastEditedTime, name, personNames);

            logger.info("Broadcasting SSE event to Notion clients...");
            sseClientManager.getClients().forEach((key, emitter) -> {
                if (key.endsWith(":notion")) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("updateEvent")
                                .data("New data received from Notion Webhook")
                                .id("notion-" + id)
                                .reconnectTime(1000));
                        logger.info("Event sent to client: " + key);
                    } catch (IOException e) {
                        logger.warning("Failed to send event to client: " + key + ". Removing emitter.");
                        sseClientManager.removeClient(key);
                    }
                }
            });

            logger.info(String.format("Processed Notion Webhook: id='%s', lastEditedTime='%s', name='%s', people='%s'", 
                    id, lastEditedTime, name, personNames));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache");
            headers.add("X-Accel-Buffering", "no");
            headers.add("Connection", "keep-alive");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body("Webhook processed successfully");
        } catch (Exception e) {
            logger.severe("Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing webhook");
        }
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
            sseClientManager.removeClient(clientKey);
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseClientManager.addClient(clientKey, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to " + serviceType));
        } catch (IOException e) {
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