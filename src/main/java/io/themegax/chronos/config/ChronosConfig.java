package io.themegax.chronos.config;

import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.data.Config;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("all")
@ConfigEntries(includeAll = true)
public final class ChronosConfig extends Config implements ConfigContainer {
    @ConfigEntry.BoundedInteger(min = 0)
    private static int xpCost = 0;
    @ConfigEntry.BoundedInteger(min = 0)
    private static float healthCost = 8f;
    @ConfigEntry.BoundedInteger(min = 1)
    private static int repairCostLevels = 3;
    @ConfigEntry.BoundedInteger(min = 0)
    private static int maxDurability = 10;
    @ConfigEntry.BoundedInteger(min = 0)
    private static int cooldownSeconds = 30;
    @ConfigEntry.BoundedInteger(min = 0)
    private static int resetTimerSeconds = 30;
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

    public static int getResetTimer() {
        return resetTimerSeconds*1000;
    }
    public static int getCooldown() {
        return cooldownSeconds*20;
    }
    public static int getRepairCost() {
        return repairCostLevels -1;
    }
    public static Item getRepairItem() {
        return Registry.ITEM.get(new Identifier(repairItem));
    }

    public ChronosConfig(String modId) {
        super(modId);
    }
}
