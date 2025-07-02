package com.tools.roulette.listeners;

import com.tools.roulette.Main;
import com.tools.roulette.game.GameManager;
import com.tools.roulette.game.GameState;
import com.tools.roulette.utils.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LobbyListener implements Listener {
    private final Main plugin;

    public LobbyListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();
        ScoreboardManager scoreboardManager = plugin.getScoreboardManager();

        // Телепортация в лобби
        Location lobby = new Location(
                Bukkit.getWorld(plugin.getConfig().getString("locations.lobby.world")),
                plugin.getConfig().getDouble("locations.lobby.x"),
                plugin.getConfig().getDouble("locations.lobby.y"),
                plugin.getConfig().getDouble("locations.lobby.z"),
                (float) plugin.getConfig().getDouble("locations.lobby.yaw"),
                (float) plugin.getConfig().getDouble("locations.lobby.pitch")
        );

        if (plugin.getGameManager().getGameState() == GameState.WAITING) {
            gameManager.addPlayer(player);
            player.playSound(player.getLocation(), "theme", 1.0f, 1.0f);
            player.setGameMode(GameMode.ADVENTURE);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("Игра уже началась.");
        }

        player.teleport(lobby);
        player.getInventory().clear();
        event.setJoinMessage(null);
        scoreboardManager.run(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GameManager gameManager = plugin.getGameManager();

        gameManager.removePlayer(event.getPlayer());
        event.setQuitMessage(null);
    }
}