package com.tools.roulette.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Round {
    private final RoundType roundType;
    private final Player player1;
    private final Player player2;
    private Queue<Boolean> bullets;
    private int currentBulletPattern = 0;
    private final List<BulletPattern> patterns = new ArrayList<>();

    public Round(RoundType roundType, Player player1, Player player2) {
        this.roundType = roundType;
        this.player1 = player1;
        this.player2 = player2;
        initializePatterns();
        this.bullets = generateBullets();
        announceInitialBullets();
    }

    private void initializePatterns() {
        switch (roundType) {
            case ROUND_1:
                patterns.add(new BulletPattern(1, 1)); // 1 холостой, 1 заряженный
                patterns.add(new BulletPattern(1, 2)); // 1 холостой, 2 заряженных
                patterns.add(new BulletPattern(2, 3)); // 2 холостых, 3 заряженных (будет повторяться)
                break;
            case ROUND_2:
                patterns.add(new BulletPattern(2, 2)); // 2 холостых, 2 заряженных
                patterns.add(new BulletPattern(2, 3)); // 2 холостых, 3 заряженных
                patterns.add(new BulletPattern(3, 4)); // 3 холостых, 4 заряженных (будет повторяться)
                break;
            case ROUND_3:
                patterns.add(new BulletPattern(2, 2)); // 2 холостых, 2 заряженных
                patterns.add(new BulletPattern(3, 4)); // 3 холостых, 4 заряженных
                patterns.add(new BulletPattern(4, 5)); // 4 холостых, 5 заряженных (будет повторяться)
                break;
        }
    }

    private Queue<Boolean> generateBullets() {
        List<Boolean> bulletList = new ArrayList<>();
        BulletPattern pattern = patterns.get(currentBulletPattern);

        for (int i = 0; i < pattern.dummies; i++) {
            bulletList.add(false); // холостой
        }
        for (int i = 0; i < pattern.live; i++) {
            bulletList.add(true); // заряженный
        }

        Collections.shuffle(bulletList);
        return new LinkedList<>(bulletList);
    }

    public boolean shoot(boolean isSelfShot) {
        // Если патроны закончились, загружаем новый набор
        if (bullets.isEmpty()) {
            changeBulletPattern();
        }

        // Достаем следующий патрон
        boolean bullet = bullets.poll();

        // Если после выстрела патроны закончились, загружаем новый набор
        if (bullets.isEmpty()) {
            changeBulletPattern();
        }

        return bullet;
    }

    private void changeBulletPattern() {
        if (currentBulletPattern < patterns.size() - 1) {
            currentBulletPattern++;
        }
        bullets = generateBullets();
        announceNewBullets();
    }

    private void announceInitialBullets() {
        BulletPattern pattern = patterns.get(currentBulletPattern);
        String message = "§6Начальный набор патронов: §f" + pattern.dummies + " холостых, " + pattern.live + " заряженных";
        player1.sendMessage(message);
        player2.sendMessage(message);
    }

    private void announceNewBullets() {
        BulletPattern pattern = patterns.get(currentBulletPattern);
        String message = "§6Новый набор патронов: §f" + pattern.dummies + " холостых, " + pattern.live + " заряженных";
        player1.sendMessage(message);
        player2.sendMessage(message);
        Bukkit.getLogger().info("New bullet pattern: " + pattern.dummies + " dummies, " + pattern.live + " live");
    }

    public RoundType getRoundType() {
        return roundType;
    }

    private static class BulletPattern {
        final int dummies;
        final int live;

        BulletPattern(int dummies, int live) {
            this.dummies = dummies;
            this.live = live;
        }
    }
}