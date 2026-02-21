package com.example.ohmok.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Game {
    public static final int N = 19;

    private final String id;
    private final int[][] board = new int[N][N];

    @Setter
    private int currentPlayer = 1; // 1=흑
    @Setter
    private boolean finished = false;
    @Setter
    private int winner = 0;        // 0=없음/무승부
    private int moveCount = 0;
    private int empties = N * N;

    public Game(String id) {
        this.id = id;
    }

    public void incMoveCount() { this.moveCount++; }

    public void decEmpties() { this.empties--; }
}