package com.tools.roulette.game;

public class PlayerData {
    private int lives;
    private int wins;

    public PlayerData() {
        this.lives = 4;
        this.wins = 0;
    }

    public void takeDamage(int amount) {
        lives -= amount;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int incrementWins() {
        return ++wins;
    }

    public int getWins() {
        return wins;
    }
}