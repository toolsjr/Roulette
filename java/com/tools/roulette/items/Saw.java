package com.tools.roulette.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Saw extends GameItem {
    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Складная ножовка");
        meta.setLore(Arrays.asList("Увеличивает урон до двух", "Используется во время хода"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getName() {
        return "Складная ножовка";
    }

    @Override
    public void use(Player player) {
    }
}