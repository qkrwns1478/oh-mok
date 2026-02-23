package com.example.ohmok.controller;

import com.example.ohmok.dto.GameStateResponse;
import com.example.ohmok.dto.MoveRequest;
import com.example.ohmok.exception.ApiException;
import com.example.ohmok.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketGameController {

    private final GameService gameService;

    public WebSocketGameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/{id}/move")
    @SendTo("/topic/game/{id}")
    public GameStateResponse makeMove(@DestinationVariable String id, MoveRequest req) {
        return gameService.makeMove(id, req.x(), req.y());
    }

    @MessageExceptionHandler(ApiException.class)
    @SendToUser("/topic/errors")
    public Map<String, String> handleException(ApiException e) {
        return Map.of("error", e.getMessage());
    }
}