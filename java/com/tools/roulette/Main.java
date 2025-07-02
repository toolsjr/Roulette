package com.tools.roulette;

import com.tools.roulette.utils.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.tools.roulette.game.GameManager;
import com.tools.roulette.listeners.GameListener;
import com.tools.roulette.listeners.LobbyListener;

public class Main extends JavaPlugin {

    private GameManager gameManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.gameManager = new GameManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getLogger().info("Buckshot Roulette plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Buckshot Roulette plugin disabled!");
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}