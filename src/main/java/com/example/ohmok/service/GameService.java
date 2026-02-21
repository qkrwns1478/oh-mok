package com.example.ohmok.service;

import com.example.ohmok.dto.GameStateResponse;
import com.example.ohmok.exception.ApiException;
import com.example.ohmok.model.Game;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public GameStateResponse createGame() {
        String id = UUID.randomUUID().toString();
        Game g = new Game(id);
        games.put(id, g);
        return toResponse(g);
    }

    public GameStateResponse getGame(String id) {
        Game g = games.get(id);
        if (g == null) throw new ApiException(HttpStatus.NOT_FOUND, "게임을 찾을 수 없습니다.");
        return toResponse(g);
    }

    public GameStateResponse makeMove(String id, int x, int y) {
        Game g = games.get(id);
        if (g == null) throw new ApiException(HttpStatus.NOT_FOUND, "게임을 찾을 수 없습니다.");

        synchronized (g) {
            if (g.isFinished()) throw new ApiException(HttpStatus.BAD_REQUEST, "이미 종료된 게임입니다.");

            if (x < 0 || x >= Game.N || y < 0 || y >= Game.N) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "좌표 범위가 올바르지 않습니다. (0~18)");
            }
            int[][] board = g.getBoard();
            if (board[x][y] != 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "이미 돌이 놓인 자리입니다.");
            }

            int player = g.getCurrentPlayer();
            board[x][y] = player;

            // 렌주룰: 흑(1)만 금수 체크
            if (player == 1) {
                RenjuRules.ForbiddenResult fr = RenjuRules.isForbiddenBlackMove(board, x, y);
                if (fr.forbidden()) {
                    board[x][y] = 0; // 되돌림
                    throw new ApiException(HttpStatus.BAD_REQUEST, "금수입니다: " + fr.reason());
                }
            }

            // 커밋
            g.incMoveCount();
            g.decEmpties();

            // 승리 판정(빠른 최적화는 가능하지만 19x19라 큰 의미는 없어서 단순화)
            int winner = RenjuRules.checkWinner(board, x, y);
            if (winner != 0) {
                g.setWinner(winner);
                g.setFinished(true);
                return toResponse(g);
            }

            if (g.getEmpties() == 0) {
                g.setFinished(true); // 무승부
                return toResponse(g);
            }

            // 턴 교대
            g.setCurrentPlayer(player == 1 ? 2 : 1);
            return toResponse(g);
        }
    }

    private GameStateResponse toResponse(Game g) {
        return new GameStateResponse(
                g.getId(),
                g.getCurrentPlayer(),
                g.isFinished(),
                g.getWinner(),
                g.getMoveCount(),
                deepCopy(g.getBoard())
        );
    }

    private static int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}