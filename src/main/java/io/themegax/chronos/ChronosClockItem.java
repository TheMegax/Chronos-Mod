package io.themegax.chronos;

import io.themegax.chronos.sound.ChronosSoundEvents;
import io.themegax.slowmo.api.TickrateApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static io.themegax.slowmo.SlowmoMain.DEFAULT_TICKRATE;

public class ChronosClockItem extends Item {

    public ChronosClockItem(Settings settings) {
        super(settings);
    }

    public float storedTickrate = DEFAULT_TICKRATE;
    private static float prevTickrate = DEFAULT_TICKRATE;
    private MinecraftServer server;

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) user.setCurrentHand(hand);
        else {
            playSoundOnce(ChronosSoundEvents.RESONATE.getId(), user, ChronosSoundEvents.RESONATE);
        }
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    private void playSoundOnce(Identifier id, PlayerEntity user, SoundEvent soundEvent) {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();
        soundManager.stopSounds(id, SoundCategory.PLAYERS);
        user.playSound(soundEvent, SoundCategory.PLAYERS, 1, 1);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (server != null) {
            return TickrateApi.getServerTickrate(server) != DEFAULT_TICKRATE;
        }
        return (storedTickrate != DEFAULT_TICKRATE);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (server == null) {
            server = entity.getServer();
        }
        if (world.isClient()) {
            stack.getOrCreateNbt().putFloat("storedTickrate", prevTickrate);
            float newStoredTickrate = stack.getNbt().getFloat("storedTickrate");
            if (prevTickrate != newStoredTickrate) {
                this.storedTickrate = newStoredTickrate;
            }
            prevTickrate = newStoredTickrate;
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        for (int i = 0; i < 6; i++) {
            Random random = user.getRandom();
            world.addParticle(ParticleTypes.PORTAL, user.getParticleX(0.5), user.getRandomBodyY() - 0.25, user.getParticleZ(1), (random.nextDouble() - 0.5) * 2.0, -random.nextDouble(), (random.nextDouble() - 0.5) * 2.0);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity usingLivingEntity, int remainingUseTicks) {
        if (world.isClient) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity clientPlayer = client.player;
            if (clientPlayer != null) {
                clientPlayer.playSound(ChronosSoundEvents.DEACTIVATE, SoundCategory.PLAYERS, 1f, 1f);
                client.getSoundManager().stopSounds(ChronosSoundEvents.RESONATE.getId(), SoundCategory.PLAYERS);
            }
        }
        else if (usingLivingEntity instanceof PlayerEntity player){
            player.getItemCooldownManager().set(this, 40);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world.isClient) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
            if (clientPlayer != null) {
                if (shouldSpawnParticles()) {
                    double r = clientPlayer.getX();
                    double s = clientPlayer.getY() + 0.5;
                    double d = clientPlayer.getZ();

                    for (double e = 0.0; e < Math.PI * 2; e += 0.15707963267948966) {
                        world.addParticle(ParticleTypes.EFFECT, r, s, d, Math.cos(e) * -0.5, 0.0, Math.sin(e) * -0.5);
                        world.addParticle(ParticleTypes.EFFECT, r, s, d, Math.cos(e) * -0.7, 0.0, Math.sin(e) * -0.7);
                    }
                    clientPlayer.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1f);
                }
                else {
                    clientPlayer.sendMessage(new TranslatableText("actionbar.chronos.fail.not_configured").formatted(Formatting.RED), true);
                    clientPlayer.playSound(ChronosSoundEvents.DEACTIVATE, SoundCategory.PLAYERS, 1f, 1f);
                    client.getSoundManager().stopSounds(ChronosSoundEvents.RESONATE.getId(), SoundCategory.PLAYERS);
                }
            }
            return stack;
        }
        if (user instanceof PlayerEntity player && player.getServer() != null) {
            MinecraftServer server = player.getServer();
            float oldServerTickrate = TickrateApi.getServerTickrate(server);
            TickrateApi.setServerTickrate(oldServerTickrate != storedTickrate ? storedTickrate : DEFAULT_TICKRATE, player.getServer());
            float newServerTickrate = TickrateApi.getServerTickrate(server);

            if (player.isCreative() || oldServerTickrate == newServerTickrate) {
                player.getItemCooldownManager().set(this, 40);
            }
            else player.getItemCooldownManager().set(this, 200);
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return stack;
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
        return 38;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    private boolean shouldSpawnParticles() {
        if (server != null && TickrateApi.getServerTickrate(server) != storedTickrate)
            return true;
        else return (storedTickrate != DEFAULT_TICKRATE);
    }
}
