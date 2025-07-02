package com.tools.roulette.items;

import com.tools.roulette.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class Cigarettes extends GameItem {
    private final Main plugin;

    public Cigarettes(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Сигареты");
        meta.setLore(Arrays.asList("текст 1", "текст 2"));
        NamespacedKey key = new NamespacedKey(plugin, "cigarettes");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, UUID.randomUUID().toString());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getName() {
        return "Сигареты";
    }

    @Override
    public void use(Player player) {
    }
}