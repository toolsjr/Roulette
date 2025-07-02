package com.tools.roulette.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class GameItem {
    public abstract ItemStack getItem();
    public abstract String getName();
    public abstract void use(Player player);

}