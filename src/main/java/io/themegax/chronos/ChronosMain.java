package io.themegax.chronos;

import io.themegax.chronos.sound.SoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class ChronosMain implements ModInitializer {
	public static String modID = "chronos";
	public static int secondTicker = 0;
	public static final Logger LOGGER = LoggerFactory.getLogger("Chronos");
	public static final Item CHRONOS_CLOCK = new ChronosClockItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).rarity(Rarity.UNCOMMON));
	public static final Identifier SERVER_TICK_PACKET = new Identifier(modID, "server_tick_packet");

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier(modID, "chronos_clock"), CHRONOS_CLOCK);
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
		SoundEvents.init();
	}

	private void onServerTick(MinecraftServer minecraftServer) {
		secondTicker++;
		if (secondTicker >= 20) {
			secondTicker = 0;
			for (ServerPlayerEntity player : PlayerLookup.all(minecraftServer)) {
				PacketByteBuf buf = PacketByteBufs.create();
				ServerPlayNetworking.send(player, SERVER_TICK_PACKET, buf);
			}
		}
	}
}
