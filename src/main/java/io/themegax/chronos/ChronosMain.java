package io.themegax.chronos;

import io.themegax.chronos.config.ChronosConfig;
import io.themegax.chronos.sound.ChronosSoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class ChronosMain implements ModInitializer {
	public static final String modID = "chronos";
	public static final Logger LOGGER = LoggerFactory.getLogger("Chronos");
	public static Item CHRONOS_CLOCK = null;

	@Override
	public void onInitialize() {
		new ChronosConfig(modID).load();
		CHRONOS_CLOCK = new ChronosClockItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(ChronosConfig.getMaxDurability()).fireproof());
		Registry.register(Registry.ITEM, new Identifier(modID, "chronos_clock"), CHRONOS_CLOCK);
		ChronosSoundEvents.init();
	}
}
