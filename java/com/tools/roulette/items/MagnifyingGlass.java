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

public class MagnifyingGlass extends GameItem {
    private final Main plugin;

    public MagnifyingGlass(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Лупа");
        meta.setLore(Arrays.asList("текст1", "текст2"));
        NamespacedKey key = new NamespacedKey(plugin, "magnifyingglass");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, UUID.randomUUID().toString());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getName() {
        return "Лупа";
    }

    @Override
    public void use(Player player) {
    }
}
