package io.themegax.chronos;

import io.themegax.chronos.config.ChronosConfig;
import io.themegax.chronos.ext.PlayerEntityExt;
import io.themegax.chronos.sound.ChronosSoundEvents;
import io.themegax.slowmo.api.TickrateApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
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
	public static final String modID = "chronos";
	public static final Logger LOGGER = LoggerFactory.getLogger("Chronos");
	public static Item CHRONOS_CLOCK = null;
	public static final Identifier BELLSOUND_PACKET_ID = new Identifier(modID, "bellsound_packet");

	private static boolean shouldRun = true;
	private static MinecraftServer minecraftServer = null;

	private static TimerThread timerThread = null;

	@Override
	public void onInitialize() {
		new ChronosConfig(modID).load();
		CHRONOS_CLOCK = new ChronosClockItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).rarity(Rarity.UNCOMMON).maxDamage(ChronosConfig.getMaxDurability()).fireproof());
		Registry.register(Registry.ITEM, new Identifier(modID, "chronos_clock"), CHRONOS_CLOCK);
		ChronosSoundEvents.init();

		timerThread = new TimerThread();
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStop);
	}

	private void onServerStart(MinecraftServer server) {
		minecraftServer = server;
		shouldRun = true;
		timerThread.start();
	}

	private void onServerStop(MinecraftServer server) {
		minecraftServer = null;
		shouldRun = false;
	}

	public static class TimerThread extends Thread {
		public void run() {
			final int sleepMillis = 50;
			while (shouldRun) {
				try {
					Thread.sleep(sleepMillis);
					MinecraftServer server = minecraftServer;
					if (server != null) {
						if (server.isSingleplayer() && MinecraftClient.getInstance().isPaused()) continue;
						for (ServerPlayerEntity player : PlayerLookup.all(server)) {
							float resetTimer = ((PlayerEntityExt)player).getResetTimer();
							if (resetTimer != 0) {
								resetTimer -= sleepMillis;
								if (resetTimer <= 0) {
									resetTimer = 0;
									TickrateApi.setServerTickrate(TickrateApi.getDefaultTickrate(), server);

									PacketByteBuf buf = PacketByteBufs.create();
									ServerPlayNetworking.send(player, BELLSOUND_PACKET_ID, buf);
								}
								((PlayerEntityExt)player).setResetTimer(resetTimer);
							}
						}
					}

				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
