package com.tools.roulette.game;

import com.tools.roulette.Main;
import com.tools.roulette.items.*;
import com.tools.roulette.utils.ActionBarDisplay;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final Main plugin;
    private ActionBarDisplay actionBarDisplay;
    private GameState gameState = GameState.WAITING;
    private final List<Player> players = new ArrayList<>();
    private final Map<Player, PlayerData> playerData = new HashMap<>();
    private Round currentRound;
    private int roundNumber = 0;
    private Player currentTurn;

    public GameManager(Main plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        if (players.size() >= 2 || gameState != GameState.WAITING) {
            player.sendMessage("Игра уже началась или заполнена!");
        } else {

            players.add(player);
            playerData.put(player, new PlayerData());
            Bukkit.broadcastMessage(player.getName() + " зашёл в игру " + players.size() + "/2");

            if (players.size() == 2) {
                startCountdown();
            }
        }
    }

    public void removePlayer(Player player) {

        players.remove(player);
        playerData.remove(player);
        if (gameState == GameState.WAITING) {
            Bukkit.broadcastMessage(player.getName() + " вышел из игры " + players.size() + "/2");
        } else if (gameState == GameState.IN_GAME && players.size() == 1) {
            Player winner = getOpponent(player);
            endGame(winner);
        }

    }

    private void startCountdown() {
        gameState = GameState.STARTING;
        new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (countdown <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                Bukkit.broadcastMessage("Игра начнётся через " + countdown + " секунд!");
                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void startGame() {
        gameState = GameState.IN_GAME;
        roundNumber = 1;

        FileConfiguration config = plugin.getConfig();
        Location player1Loc = new Location(
                Bukkit.getWorld(config.getString("locations.player1.world")),
                config.getDouble("locations.player1.x"),
                config.getDouble("locations.player1.y"),
                config.getDouble("locations.player1.z"),
                (float) config.getDouble("locations.player1.yaw"),
                (float) config.getDouble("locations.player1.pitch")
        );

        Location player2Loc = new Location(
                Bukkit.getWorld(config.getString("locations.player2.world")),
                config.getDouble("locations.player2.x"),
                config.getDouble("locations.player2.y"),
                config.getDouble("locations.player2.z"),
                (float) config.getDouble("locations.player2.yaw"),
                (float) config.getDouble("locations.player2.pitch")
        );


            players.get(0).teleport(player1Loc);
            players.get(1).teleport(player2Loc);
            giveArrows();

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), "bootup", 1, 1);
            }

            currentTurn = players.get(new Random().nextInt(2));
            startRound();
            actionBarDisplay = new ActionBarDisplay(plugin, players.get(0), players.get(1));
            actionBarDisplay.start();
            actionBarDisplay.setCurrentTurn(currentTurn);

    }

    private void startRound() {         // Сообщаем о начале раунда
        RoundType roundType = RoundType.fromRoundNumber(roundNumber);

        String roundMessage = "§a§lНачало раунда " + roundNumber + "! §fЖизней: " + roundType.getLives();
        Bukkit.broadcastMessage(roundMessage);

        currentRound = new Round(roundType, players.get(0), players.get(1));

        giveCrossbow();
        giveItems();

        for (Player online : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                online.playSound(online.getLocation(), "reduce", 1, 1);
            }, 38L);
            online.stopSound("theme");
            if (getRoundNumber() == 1) {
                online.playSound(players.get(0).getLocation(), "round1", 1, 1);
            } else if (getRoundNumber() == 2) {
                online.stopSound("round1");
                online.playSound(players.get(0).getLocation(), "round2", 1, 1);
            } else {
                online.stopSound("round2");
                online.playSound(players.get(0).getLocation(), "round3", 1, 1);
            }
        }

    }

    private void giveItems() {
        int itemsCount = roundNumber == 1 ? 2 : roundNumber == 2 ? 3 : 4;
        List<GameItem> items = new ArrayList<>();
        items.add(new Saw());
        items.add(new Cigarettes(plugin));
        items.add(new Handcuffs(plugin));
        items.add(new MagnifyingGlass(plugin));
        Random random = new Random();

        for (Player player : players) {
            for (int i = 0; i < itemsCount; i++) {
                GameItem randomItem = items.get(random.nextInt(items.size()));
                ItemStack itemStack = randomItem.getItem();

                // Ищем свободный слот, начиная с 1 (пропускаем первый слот 0)
                int slot = findFirstEmptySlot(player.getInventory(), 1);

                if (slot != -1) {
                    player.getInventory().setItem(slot, itemStack);
                }
            }
        }
    }

    private int findFirstEmptySlot(Inventory inventory, int startFrom) {
        for (int i = startFrom; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                return i;
            }
        }
        return -1; // нет свободных слотов
    }

    private void giveCrossbow() {
        ItemStack crossbow = getCurrentCrossbow(currentTurn);
        currentTurn.getInventory().addItem(crossbow);
    }

    private void giveArrows() {
        ItemStack arrows = getArrows();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().setItem(9, arrows); // выдача в левый крайний слот инвентаря
        }
    }

    public void removeCrossbow() {
        ItemStack crossbow = getCurrentCrossbow(currentTurn);
        currentTurn.getInventory().remove(crossbow);
    }

    public void handleShot(Player shooter, boolean isSelfShot) {
        if (!players.contains(shooter) || shooter != currentTurn) return;

            boolean isLive = currentRound.shoot(isSelfShot);
            Player opponent = getOpponent(shooter);

            if (isSelfShot) {
                if (!isLive) {
                    shooter.sendMessage("Холостой патрон! Вы получаете ещё один ход.");
                    soundBlankShot(players.get(0));
                    soundBlankShot(players.get(1));
                    return;
                } else {
                    PlayerData data = playerData.get(shooter);
                    data.takeDamage(1);
                    shooter.sendMessage("Вы выстрелили в себя! -1 жизнь. Осталось: " + data.getLives());
                    soundWasShot(shooter);
                    soundLiveShot(opponent);
                    removeCrossbow();
                    currentTurn = opponent;
                    spawnShotParticles(shooter);
                }
            } else {
                if (isLive) {
                    PlayerData data = playerData.get(opponent);
                    data.takeDamage(1);
                    shooter.sendMessage("Выстрел по противнику! -1 жизнь у " + opponent.getName());
                    opponent.sendMessage("В вас выстрелили! -1 жизнь. Осталось: " + data.getLives());
                    soundLiveShot(shooter);
                    soundWasShot(opponent);
                    spawnShotParticles(opponent);
                } else {
                    shooter.sendMessage("Холостой выстрел по противнику!");
                    opponent.sendMessage("Противник выстрелил в вас холостым патроном!");
                    soundBlankShot(players.get(0));
                    soundBlankShot(players.get(1));
                }
                removeCrossbow();
                currentTurn = opponent;
            }
            actionBarDisplay.setCurrentTurn(currentTurn);
            checkRoundEnd();
        }

    private void spawnShotParticles(Player player) {
        Location particleLoc = player.getLocation();

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.spawnParticle(
                    Particle.BLOCK_CRACK,
                    particleLoc,
                    500,
                    0.5, 1, 0.5,
                    1,
                    Material.REDSTONE_BLOCK.createBlockData()
            );
        }
    }

    private void checkRoundEnd() {
        for (Player player : players) {
            if (playerData.get(player).getLives() <= 0) {
                endRound(player);
                return;
            }
        }

        currentTurn.getInventory().addItem(getCurrentCrossbow(currentTurn));
    }

    private void endRound(Player loser) {
        Player winner = getOpponent(loser);
        Location back = loser.getLocation();
        loser.setGameMode(GameMode.SPECTATOR);

        int wins = playerData.get(winner).incrementWins();
        if (wins >= 2 || roundNumber >= 3) {
            endGame(winner);
        } else {
            roundNumber++;
            Bukkit.broadcastMessage(loser.getName() + " проиграл раунд!");
            resetPlayers();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                startRound();
                loser.setGameMode(GameMode.ADVENTURE);
                loser.teleport(back);
            }, 60L);
        }
    }

    private void resetPlayers() {
        for (Player player : players) {
            PlayerData data = playerData.get(player);
            data.setLives(roundNumber == 2 ? 5 : 6);
        }
    }

    private void endGame(Player winner) {
        Bukkit.broadcastMessage("Игра окончена! Победитель: " + winner.getName());
        gameState = GameState.WAITING;
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.stopSound("round2");
            online.stopSound("round3");
            online.stopSound("defib");
            online.stopSound("defib_round2");
            online.stopSound("defib_round3");
        }
        winner.playSound(winner.getLocation(), "win", 1, 1);
        players.clear();
        playerData.clear();
        if (actionBarDisplay != null) {
            actionBarDisplay.stop();
            actionBarDisplay = null;
        }
    }

    public ItemStack getCurrentCrossbow(Player player) {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = crossbow.getItemMeta();
        meta.setDisplayName("§cДробовик");
        meta.setUnbreakable(true);
        crossbow.setItemMeta(meta);
        player.setCooldown(Material.CROSSBOW, 50);
        return crossbow;
    }

    public ItemStack getArrows() {
        ItemStack arrows = new ItemStack(Material.ARROW);
        arrows.setAmount(64);
        return arrows;
    }

    private void soundLiveShot(Player player) {
        player.playSound(player.getLocation(), "live", 1, 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), "crash", 1, 1), 8L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), "reload", 1, 1), 30L);

    }

    private void soundBlankShot(Player player) {
        player.playSound(player.getLocation(), "blank", 1, 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), "reload", 1, 1), 20L);

    }

    private void soundWasShot(Player player) {
        player.playSound(player.getLocation(), "live", 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 70, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 140, 3, false, false));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.playSound(player.getLocation(), "defib", 1, 1);
            player.playSound(player.getLocation(), "heartbeat", 1, 1);
        }, 10L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (gameState == GameState.IN_GAME) {
                if (getRoundNumber() == 1) {
                    player.playSound(player.getLocation(), "defib_round1", 1, 1);
                } else if (getRoundNumber() == 2) {
                    player.playSound(player.getLocation(), "defib_round2", 1, 1);
                } else player.playSound(player.getLocation(), "defib_round3", 1, 1);
            }
            }, 38L);

    }

    public Player getOpponent(Player player) {
        return players.get(0).equals(player) ? players.get(1) : players.get(0);
    }

    public boolean isInGame(Player player) {
        return players.contains(player);
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Map<Player, PlayerData> getPlayerData() {
        return playerData;
    }
}