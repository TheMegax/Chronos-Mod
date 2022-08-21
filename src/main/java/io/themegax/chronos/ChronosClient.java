package io.themegax.chronos;

import io.themegax.chronos.config.ChronosConfig;
import io.themegax.chronos.mixin.mouse.HandledScreenAccessor;
import io.themegax.chronos.sound.ChronosSoundEvents;
import me.lortseam.completeconfig.gui.ConfigScreenBuilder;
import me.lortseam.completeconfig.gui.cloth.ClothConfigScreenBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

import static io.themegax.chronos.ChronosMain.*;

public class ChronosClient implements ClientModInitializer {
	boolean ticker = false;
	public static boolean isSneakKeyPressed = false;
	public static Text sneakKeyString = null;
	public static int scroll;
	private static int secondTicker = 0;
	private static float angle = 0;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
		ClientTickEvents.START_WORLD_TICK.register(this::onWorldTick);
		ModelPredicateProviderRegistry.register(CHRONOS_CLOCK, new Identifier("angle"), (stack, world, entity, seed) -> {
			if (isClockBroken(stack)) {
				return 1;
			}
			return angle/8;
		});

		if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
			ConfigScreenBuilder.setMain(modID, new ClothConfigScreenBuilder());
		}

		ClientPlayNetworking.registerGlobalReceiver(
				BELLSOUND_PACKET_ID, (client, handler, buf, responseSender) -> client.execute(this::playBellOnClient));
	}

	private void playBellOnClient() {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity clientPlayer = client.player;
		assert clientPlayer != null;
		clientPlayer.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1f);
		client.getSoundManager().stopSounds(ChronosSoundEvents.RESONATE.getId(), SoundCategory.PLAYERS);
	}

	private void onWorldTick(ClientWorld clientWorld) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player != null && !(client.currentScreen instanceof DownloadingTerrainScreen)) {
			secondTicker++;
			if (secondTicker >= 20) {
				secondTicker = 0;
				angle++;
				if (angle >= 8) angle = 0;
				ticker = !ticker;
				ItemStack mainStack = player.getMainHandStack();
				ItemStack offStack = player.getOffHandStack();

				if (mainStack.isOf(CHRONOS_CLOCK) || offStack.isOf(CHRONOS_CLOCK)) {
					if (!(isClockBroken(mainStack) && isClockBroken(offStack))) {
						if (ticker) {
							player.playSound(ChronosSoundEvents.CLICK_1, SoundCategory.PLAYERS, 1f, 1f);
						}
						else {
							player.playSound(ChronosSoundEvents.CLICK_2, SoundCategory.PLAYERS, 1f, 1f);
						}
					}
				}
			}
		}
	}
	public static boolean isClockBroken(ItemStack stack) {
		if (ChronosConfig.getMaxDurability() == 0) return false;
		if (!stack.isOf(ChronosMain.CHRONOS_CLOCK)) return true;
		return (stack.getMaxDamage() - stack.getDamage() == 1);
	}
	public static void scrollMouse(int scroll) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		Slot itemSlot = getHoverSlot();
		if (itemSlot != null) {
			ItemStack clockStack = getHoverSlot().getStack();
			if (player != null && isSneakKeyPressed && clockStack.isOf(CHRONOS_CLOCK)) {
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
				// Detect if using sneak key
				String boundedSneakTranslation = client.options.sneakKey.getBoundKeyTranslationKey();
				Text boundedSneakText = client.options.sneakKey.getBoundKeyLocalizedText();

				if (boundedSneakText.getString().equals("")) {
					sneakKeyString = Text.translatable(boundedSneakTranslation);
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
					scroll = 0;
				}
			}
			else {
				isSneakKeyPressed = false;
			}
		}
	}
}
