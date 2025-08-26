package com.example.equipmentmanagement.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class EditNotificationController {
    @MessageMapping("/edit/start")
    @SendTo("/topic/editing")
    public String notifyEditing(String equipmentId) {
        return equipmentId;
    }
}