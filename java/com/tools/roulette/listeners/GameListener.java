package com.tools.roulette.listeners;

import com.tools.roulette.Main;
import com.tools.roulette.game.GameManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class GameListener implements Listener {
    private final Main plugin;
    private final Map<Player, Long> chargingPlayers = new HashMap<>();

    public GameListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();

        if (!gameManager.isInGame(player)) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (chargingPlayers.containsKey(player)) {
            long chargeTime = System.currentTimeMillis() - chargingPlayers.get(player);
            int chargeDuration = 1500; // 1.5 секунды на зарядку

            if (chargeTime >= chargeDuration) {
                // Выстрел после завершения зарядки
                gameManager.handleShot(player, false);
                player.getInventory().remove(Material.CROSSBOW);
                chargingPlayers.remove(player);
            }
            return;
        }

        // Обработка выстрелов из арбалета
        if (item.getType() == Material.CROSSBOW) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                gameManager.handleShot(player, true); // Выстрел в себя
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                startCharging(player);
            }
        }
    }

    @EventHandler
    public void blockSwapCrossbow(PlayerItemHeldEvent event) {
        // Отменять при смене слота с заряженного арбалета
        if (chargingPlayers.containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockChangeCrossbow(InventoryClickEvent event) {
        // Чтобы не багать смену заряженного арбалета
        Player player = (Player) event.getWhoClicked();
        if (chargingPlayers.containsKey(player)) {
            // Проверяем, не пытается ли игрок изменить предмет в руке
            if (event.getSlot() == player.getInventory().getHeldItemSlot() || event.getHotbarButton() == player.getInventory().getHeldItemSlot()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockThrow(PlayerDropItemEvent event) {
        // Блок выкидывания предметов
        event.setCancelled(true);
    }

    private void startCharging(Player player) {
        chargingPlayers.put(player, System.currentTimeMillis());

        // Частицы зарядки
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!chargingPlayers.containsKey(player)) {
                    cancel();
                    return;
                }

                if (ticks++ >= 30) { // 1.5 секунды анимации
                    cancel();
                    return;
                }

            }
        }.runTaskTimer(plugin, 0, 1);
    }
}