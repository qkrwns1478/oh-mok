package com.example.ohmok.service;

import com.example.ohmok.model.Game;

public final class RenjuRules {
    private RenjuRules() {}

    private static final int N = Game.N;
    private static final int[][] DIRS = {
            {0, 1}, {1, 0}, {1, 1}, {-1, 1}
    };

    public static int checkWinner(int[][] board, int x, int y) {
        int p = board[x][y];
        for (int[] d : DIRS) {
            int cnt = countConsecutive(board, x, y, d[0], d[1], p);
            if (p == 1) {
                if (cnt == 5) return 1;     // 흑: 정확히 5
            } else if (p == 2) {
                if (cnt >= 5) return 2;     // 백: 5 이상
            }
        }
        return 0;
    }

    public static ForbiddenResult isForbiddenBlackMove(int[][] board, int x, int y) {
        // board[x][y] == 1(흑) 가정

        // 1) 장목(6+) 금지
        for (int[] d : DIRS) {
            int cnt = countConsecutive(board, x, y, d[0], d[1], 1);
            if (cnt >= 6) return new ForbiddenResult(true, "장목(6목 이상)");
        }

        // 2) 정확히 5목이면 허용(금수 아님)
        for (int[] d : DIRS) {
            int cnt = countConsecutive(board, x, y, d[0], d[1], 1);
            if (cnt == 5) return new ForbiddenResult(false, null);
        }

        // 3) 4-4 / 3-3 금지(흑만)
        int fours = 0;
        int threes = 0;

        for (int[] d : DIRS) {
            if (hasFourInDirection(board, x, y, d[0], d[1], 1)) {
                fours++;
            } else if (hasOpenThreeInDirection(board, x, y, d[0], d[1], 1)) {
                threes++;
            }
        }

        if (fours >= 2) return new ForbiddenResult(true, "4-4 금수");
        if (threes >= 2) return new ForbiddenResult(true, "3-3 금수");
        return new ForbiddenResult(false, null);
    }

    private static int countConsecutive(int[][] board, int x, int y, int dx, int dy, int p) {
        int cnt = 1;

        int nx = x + dx, ny = y + dy;
        while (in(nx, ny) && board[nx][ny] == p) {
            cnt++;
            nx += dx; ny += dy;
        }

        nx = x - dx; ny = y - dy;
        while (in(nx, ny) && board[nx][ny] == p) {
            cnt++;
            nx -= dx; ny -= dy;
        }

        return cnt;
    }

    private static boolean in(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    // line: 0=빈칸, 1=내 돌, 2=막힘(상대/경계)
    private static int[] getLine(int[][] board, int x, int y, int dx, int dy, int p, int span) {
        int len = span * 2 + 1;
        int[] line = new int[len];
        for (int k = -span; k <= span; k++) {
            int nx = x + k * dx;
            int ny = y + k * dy;
            int idx = k + span;

            if (!in(nx, ny)) {
                line[idx] = 2;
                continue;
            }
            int v = board[nx][ny];
            if (v == p) line[idx] = 1;
            else if (v == 0) line[idx] = 0;
            else line[idx] = 2;
        }
        return line;
    }

    private static boolean makesOpenFour(int[] line, int center, int newpos) {
        // 0 1 1 1 1 0
        for (int s = 0; s + 5 < line.length; s++) {
            if (line[s] == 0 && line[s + 5] == 0
                    && line[s + 1] == 1 && line[s + 2] == 1 && line[s + 3] == 1 && line[s + 4] == 1) {

                boolean centerIn = (center >= s + 1 && center <= s + 4);
                boolean newIn = (newpos >= s + 1 && newpos <= s + 4);
                if (centerIn && newIn) return true;
            }
        }
        return false;
    }

    private static boolean hasFourInDirection(int[][] board, int x, int y, int dx, int dy, int p) {
        int span = 5;
        int[] line = getLine(board, x, y, dx, dy, p, span);
        int center = span;

        for (int s = 0; s + 4 < line.length; s++) {
            if (center < s || center > s + 4) continue;

            int ones = 0, zeros = 0;
            for (int i = s; i < s + 5; i++) {
                if (line[i] == 1) ones++;
                else if (line[i] == 0) zeros++;
            }
            if (ones == 4 && zeros == 1) return true;
        }
        return false;
    }

    private static boolean hasOpenThreeInDirection(int[][] board, int x, int y, int dx, int dy, int p) {
        int span = 5;
        int[] line = getLine(board, x, y, dx, dy, p, span);
        int center = span;

        for (int t = 0; t < line.length; t++) {
            if (line[t] != 0) continue;

            line[t] = 1; // 다음 수 가정
            boolean ok = makesOpenFour(line, center, t);
            line[t] = 0;

            if (ok) return true;
        }
        return false;
    }

    public record ForbiddenResult(boolean forbidden, String reason) {}
}