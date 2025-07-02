package com.tools.roulette.utils;

import com.tools.roulette.Main;
import com.tools.roulette.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Objects;

public class ScoreboardManager {
    private final Main plugin;

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
    }

    public void run(Player player) {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (plugin.getGameManager().getGameState() == GameState.IN_GAME) {
            game(player); } else if (plugin.getGameManager().getGameState() == GameState.STARTING) {
                starting(player); } else lobby(player);
        }, 0L, 20L);
    }

    public void lobby(Player player) {

        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = board.registerNewObjective("roulette", "dummy", ChatColor.BOLD + "ROULETTE");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(ChatColor.GRAY + "Ожидание игроков...").setScore(0);
        obj.getScore(ChatColor.GRAY + "Игроки: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "/2").setScore(-1);

        player.setScoreboard(board);

    }

    public void starting(Player player) {

        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = board.registerNewObjective("roulette", "dummy", ChatColor.BOLD + "ROULETTE");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(ChatColor.GRAY + "Подготовка к игре").setScore(0);
        obj.getScore(ChatColor.GRAY + "Игроки: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "/2").setScore(-1);

        player.setScoreboard(board);

    }

    public void game(Player player) {

            Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
            Objective obj = board.registerNewObjective("roulette", "dummy", ChatColor.BOLD + "ROULETTE");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            Player opponent = plugin.getGameManager().getOpponent(player);

            obj.getScore(ChatColor.GRAY + "Игра").setScore(0);
            obj.getScore(ChatColor.GOLD + "Раунд: " + ChatColor.WHITE + plugin.getGameManager().getRoundNumber()).setScore(-2);
            obj.getScore(ChatColor.GREEN + player.getDisplayName() + " " + getHearts(plugin.getGameManager().getPlayerData().get(player).getLives())).setScore(-2);
            obj.getScore(ChatColor.RED + opponent.getDisplayName() + " " + getHearts(plugin.getGameManager().getPlayerData().get(opponent).getLives())).setScore(-3);

            player.setScoreboard(board);

    }

    private String getHearts(int lives) {
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < lives; i++) {
            hearts.append("❤");
        }
        return hearts.toString().trim();
    }
}
