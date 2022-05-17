package io.themegax.chronos.config;

import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.data.Config;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("all")
@ConfigEntries
public final class ChronosConfig extends Config implements ConfigContainer {

    private static int xpCost = 50;
    private static float healthCost = 0f;
    private static int maxDurability = 10;

    private static int cooldownSeconds = 10;

    private static String repairItem = "minecraft:copper_ingot";

    public static int getXpCost() {
        return xpCost;
    }

    public static float getHealthCost() {
        return healthCost;
    }

    public static int getMaxDurability() {
        return maxDurability;
    }

    public static int getCooldown() {
        return cooldownSeconds*20;
    }

    public static Item getRepairItem() {
        return Registry.ITEM.get(new Identifier(repairItem));
    }

    public ChronosConfig(String modId) {
        super(modId);
    }
}
