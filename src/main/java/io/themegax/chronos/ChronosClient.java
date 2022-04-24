package io.themegax.chronos;

import io.themegax.chronos.mixin.HandledScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

import static io.themegax.chronos.ChronosMain.*;

public class ChronosClient implements ClientModInitializer {
	boolean ticker = false;
	public static boolean isSneakKeyPressed = false;
	public static Text sneakKeyString = null;
	public static int scroll;
	private static float prevTickrate = 20f;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
		ClientPlayNetworking.registerGlobalReceiver(
				SERVER_TICK_PACKET, (client, handler, buf, responseSender) -> client.execute(this::onServerTick));
	}

	public static void scrollMouse(int scroll) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		Slot itemSlot = getHoverSlot();
		if (itemSlot != null) {
			Item clockItem = getHoverSlot().getStack().getItem();
			if (player != null && isSneakKeyPressed && clockItem instanceof ChronosClockItem) {
				ChronosClient.scroll += scroll;
			}
		}
	}

	@Nullable
	private static Slot getHoverSlot() {
		MinecraftClient client = MinecraftClient.getInstance();
		double mouseX = client.mouse.getX();
		double mouseY = client.mouse.getY();
		mouseX = mouseX * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
		mouseY = mouseY * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();

		Slot hoverSlot = null;
		if (client.currentScreen instanceof HandledScreen<?>) { // Inventory screens use this
			HandledScreenAccessor handledScreenAccessor = ((HandledScreenAccessor) client.currentScreen);
			hoverSlot = handledScreenAccessor.invokeGetSlotAt(mouseX, mouseY);
		}
		return hoverSlot;
	}

	private void onClientTick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		int sneakKey;

		if (player != null) {
			ItemStack chronosStack = null;
			ChronosClockItem chronosItem = null;

			// Detect if mouse cursor is over an item
			Slot hoverSlot = getHoverSlot();

			if (hoverSlot != null) {
				chronosStack = hoverSlot.getStack();
				if (chronosStack.getItem() instanceof ChronosClockItem)
					chronosItem = (ChronosClockItem) chronosStack.getItem();
			}

			if (chronosStack != null && chronosItem != null) {
				// Check for nbt
				if (chronosStack.hasNbt()) {
					assert chronosStack.getNbt() != null;
					float newStoredTickrate = chronosStack.getNbt().getFloat("storedTickrate");
					if (prevTickrate != newStoredTickrate) {
						chronosItem.storedTickrate = newStoredTickrate;
						prevTickrate = newStoredTickrate;
					}
				}

				// Detect if using sneak key
				String boundedSneakTranslation = client.options.sneakKey.getBoundKeyTranslationKey();
				Text boundedSneakText = client.options.sneakKey.getBoundKeyLocalizedText();

				if (boundedSneakText.asString().equals("")) {
					sneakKeyString = new TranslatableText(boundedSneakTranslation);
				}
				else {
					sneakKeyString = boundedSneakText;
				}


				InputUtil.Key boundedSneakKey = InputUtil.fromTranslationKey(boundedSneakTranslation);
				sneakKey = boundedSneakKey.getCode();

				isSneakKeyPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), sneakKey);

				// Scroll
				if (isSneakKeyPressed && scroll != 0) {
					chronosItem.storedTickrate += scroll/2f;
					chronosItem.storedTickrate = MathHelper.clamp(chronosItem.storedTickrate, 1, 100);

					PacketByteBuf buf = PacketByteBufs.create();

					chronosStack.getOrCreateNbt().putFloat("storedTickrate", chronosItem.storedTickrate);

					buf.writeItemStack(chronosStack);
					buf.writeFloat(chronosItem.storedTickrate);
					ClientPlayNetworking.send(CLOCK_SPEED_PACKET, buf);
					scroll = 0;
				}
			}
			else {
				isSneakKeyPressed = false;
			}
		}
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
