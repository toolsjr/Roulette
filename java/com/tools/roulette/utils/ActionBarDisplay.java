package com.tools.roulette.utils;

import com.tools.roulette.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class ActionBarDisplay {
    private final List<Player> players;
    private Player currentTurn;
    private BukkitTask updateTask;
    private final Main plugin;

    public ActionBarDisplay(Main plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.players.add(player1);
        this.players.add(player2);
    }

    public void start() {
        // Останавливаем предыдущий task если был
        stop();

        // Создаем новый task для обновления ActionBar
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateActionBars();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            clearActionBars();
        }
    }

    public void setCurrentTurn(Player player) {
        this.currentTurn = player;
        updateActionBars();
    }

    private void updateActionBars() {
        for (Player player : players) {
            if (player.equals(currentTurn)) {
                sendActionBar(player, ChatColor.GREEN + "Ваш ход");
            } else {
                Player opponent = players.get(0).equals(player) ? players.get(1) : players.get(0);
                sendActionBar(player, ChatColor.RED + "Ход противника: " + ChatColor.YELLOW + opponent.getName());
            }
        }
    }

    private void clearActionBars() {
        for (Player player : players) {
            sendActionBar(player, "");
        }
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}