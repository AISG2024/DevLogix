package com.aisg.devlogix.controller;

import com.aisg.devlogix.service.NotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;
import java.util.List; 

@RestController
@RequestMapping("/api/notion")
public class NotionController {

    private static final Logger logger = Logger.getLogger(NotionController.class.getName());

    @Autowired
    private NotionService notionService;

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

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.severe("Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
}