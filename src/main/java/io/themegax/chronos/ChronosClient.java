package io.themegax.chronos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvents;

import static io.themegax.chronos.ChronosMain.CHRONOS_CLOCK;
import static io.themegax.chronos.ChronosMain.SERVER_TICK_PACKET;

public class ChronosClient implements ClientModInitializer {
	boolean ticker = false;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
		ClientPlayNetworking.registerGlobalReceiver(
				SERVER_TICK_PACKET, (client, handler, buf, responseSender) -> client.execute(this::onServerTick));
	}

	private void onClientTick(MinecraftClient minecraftClient) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
	}

	private void onServerTick() {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player != null && !(client.currentScreen instanceof DownloadingTerrainScreen)) {
			Item mainHand = player.getMainHandStack().getItem();
			Item offHand = player.getOffHandStack().getItem();
			if (mainHand == CHRONOS_CLOCK || offHand == CHRONOS_CLOCK) {
				if (ticker) {
					player.playSound(SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, 0.3F, 0.6F);
				}
				else {
					player.playSound(SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, 0.3F, 0.5F);
				}
				ticker = !ticker;
			}

		}
	}
}
