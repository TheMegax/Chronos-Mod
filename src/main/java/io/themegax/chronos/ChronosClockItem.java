package io.themegax.chronos;

import io.themegax.slowmo.api.TickrateApi;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

import static io.themegax.slowmo.SlowmoMain.DEFAULT_TICKRATE;

public class ChronosClockItem extends Item {

    public ChronosClockItem(Settings settings) {
        super(settings);
    }

    public float storedTickrate = 20.0f;
    private MinecraftServer server;

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (server != null) {
            return TickrateApi.getServerTickrate(server) != DEFAULT_TICKRATE;
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (server == null) {
            server = entity.getServer();
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity usingLivingEntity, int remainingUseTicks) {
        if (world.isClient || getPullProgress(this.getMaxUseTime(stack) - remainingUseTicks) < 1.0F)
            return;
        if (usingLivingEntity instanceof PlayerEntity player && player.getServer() != null) {
            MinecraftServer server = player.getServer();
            float oldServerTickrate = TickrateApi.getServerTickrate(server);
            TickrateApi.setServerTickrate(oldServerTickrate != storedTickrate ? storedTickrate : DEFAULT_TICKRATE, player.getServer());
            float newServerTickrate = TickrateApi.getServerTickrate(server);

            if (oldServerTickrate != newServerTickrate) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 60.0F, 1.0F);
                player.getItemCooldownManager().set(this, 200);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        boolean isSneaking = false;

        float tps_speed = storedTickrate/DEFAULT_TICKRATE;

        if (world != null && world.isClient()) {
            isSneaking = ChronosClient.isSneakKeyPressed;
        }

        if (isSneaking) {
            tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_speed", String.format("%.2f", tps_speed)));
            tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_scroll").formatted(Formatting.GOLD));
            tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_usage").formatted(Formatting.GOLD));
        }
        else {
            tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_speed", String.format("%.2f", tps_speed)));
            if (ChronosClient.sneakKeyString != null) {
                tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_sneak", ChronosClient.sneakKeyString).formatted(Formatting.LIGHT_PURPLE));
            }
        }
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public static float getPullProgress(int useTicks) {
        float f = (float) useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }
}
