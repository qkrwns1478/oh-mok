package com.example.ohmok.controller;

import com.example.ohmok.dto.GameStateResponse;
import com.example.ohmok.dto.MoveRequest;
import com.example.ohmok.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping("/games")
    public GameStateResponse createGame() {
        return service.createGame();
    }

    @GetMapping("/games/{id}")
    public GameStateResponse getGame(@PathVariable String id) {
        return service.getGame(id);
    }

    @PostMapping("/games/{id}/moves")
    public GameStateResponse move(@PathVariable String id, @RequestBody MoveRequest req) {
        return service.makeMove(id, req.x(), req.y());
    }
}