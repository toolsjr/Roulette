package com.tools.roulette.game;

public enum RoundType {
    ROUND_1(4),
    ROUND_2(5),
    ROUND_3(6);

    private final int lives;

    RoundType(int lives) {
        this.lives = lives;
    }

    public int getLives() {
        return lives;
    }

    public static RoundType fromRoundNumber(int round) {
        switch (round) {
            case 1: return ROUND_1;
            case 2: return ROUND_2;
            case 3: return ROUND_3;
            default: throw new IllegalArgumentException("Invalid round number");
        }
    }
}