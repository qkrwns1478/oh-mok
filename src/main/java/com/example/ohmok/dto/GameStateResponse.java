package com.example.ohmok.dto;

public record GameStateResponse(
        String gameId,
        int currentPlayer,   // 1=흑, 2=백
        boolean finished,
        int winner,          // 0=없음/무승부, 1=흑, 2=백
        int moveCount,
        int[][] board
) { }