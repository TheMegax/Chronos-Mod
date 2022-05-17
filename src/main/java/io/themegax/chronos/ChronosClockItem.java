package io.themegax.chronos;

import io.themegax.chronos.config.ChronosConfig;
import io.themegax.chronos.sound.ChronosSoundEvents;
import io.themegax.slowmo.api.TickrateApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    public float storedTickrate = 0;
    private MinecraftServer server;

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!isUsable(user.getStackInHand(hand))) return TypedActionResult.fail(user.getStackInHand(hand));

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
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (server == null) {
            server = entity.getServer();
        }
        if (stack.getDamage() >= stack.getMaxDamage()) stack.setDamage(stack.getMaxDamage()-1);

        if (!world.isClient && storedTickrate == 0) {
            stack.getOrCreateNbt();
            assert stack.getNbt() != null;
            this.storedTickrate = stack.getNbt().getFloat("storedTickrate");
        }
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(ChronosConfig.getRepairItem());
    }

    public static boolean isUsable(ItemStack stack) {
        if (ChronosConfig.getMaxDurability() == 0) return true;
        return stack.getDamage() < stack.getMaxDamage() - 1;
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
                boolean hasEnoughHealth = clientPlayer.isCreative() || clientPlayer.getHealth() > ChronosConfig.getHealthCost();
                boolean hasEnoughXp = clientPlayer.isCreative() || clientPlayer.totalExperience >= ChronosConfig.getXpCost();


                if (shouldSpawnParticles() && hasEnoughHealth && hasEnoughXp) {
                    double r = clientPlayer.getX();
                    double s = clientPlayer.getY() + 0.5;
                    double d = clientPlayer.getZ();

                    for (double e = 0.0; e < Math.PI * 2; e += 0.15707963267948966) {
                        world.addParticle(ParticleTypes.EFFECT, r, s, d, Math.cos(e) * -0.5, 0.0, Math.sin(e) * -0.5);
                        world.addParticle(ParticleTypes.EFFECT, r, s, d, Math.cos(e) * -0.7, 0.0, Math.sin(e) * -0.7);
                    }
                    clientPlayer.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1f);
                    return stack;
                }

                if (!hasEnoughHealth) {
                    clientPlayer.sendMessage(new TranslatableText("actionbar.chronos.fail.not_enough_hp").formatted(Formatting.RED), true);
                }
                else if (!hasEnoughXp) {
                    clientPlayer.sendMessage(new TranslatableText("actionbar.chronos.fail.not_enough_xp").formatted(Formatting.RED), true);
                }
                else {
                    clientPlayer.sendMessage(new TranslatableText("actionbar.chronos.fail.not_configured").formatted(Formatting.RED), true);
                }
                clientPlayer.playSound(ChronosSoundEvents.DEACTIVATE, SoundCategory.PLAYERS, 1f, 1f);
                client.getSoundManager().stopSounds(ChronosSoundEvents.RESONATE.getId(), SoundCategory.PLAYERS);
            }
            return stack;
        }
        if (user instanceof PlayerEntity player && player.getServer() != null) {
            boolean hasEnoughHealth = player.isCreative() || player.getHealth() > ChronosConfig.getHealthCost();
            boolean hasEnoughXp = player.isCreative() || player.totalExperience >= ChronosConfig.getXpCost();

            if (hasEnoughHealth && hasEnoughXp) {
                MinecraftServer server = player.getServer();
                float oldServerTickrate = TickrateApi.getServerTickrate(server);
                TickrateApi.setServerTickrate(oldServerTickrate != storedTickrate ? storedTickrate : DEFAULT_TICKRATE, player.getServer());
                float newServerTickrate = TickrateApi.getServerTickrate(server);

                if (player.isCreative() || oldServerTickrate == newServerTickrate) {
                    player.getItemCooldownManager().set(this, 40);
                }
                else {
                    player.getItemCooldownManager().set(this, ChronosConfig.getCooldown());
                    if (!player.isCreative()) {
                        player.damage(DamageSource.MAGIC, ChronosConfig.getHealthCost());
                        player.addExperience(-ChronosConfig.getXpCost());
                    }
                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                    if (stack.getMaxDamage() - stack.getDamage() > 0) {
                        stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
                    }
                }
                stack.getOrCreateNbt();
                assert stack.getNbt() != null;
                stack.getOrCreateNbt().putFloat("storedTickrate", storedTickrate);
            }
            else {
                player.getItemCooldownManager().set(this, 40);
            }
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

        tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_speed", String.format("%.2f", tps_speed)));

        if (isUsable(itemStack)) {
            if (isSneaking) {
                tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_scroll").formatted(Formatting.GOLD));
                tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_usage").formatted(Formatting.GOLD));
            }
            else {
                if (ChronosClient.sneakKeyString != null) {
                    tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_sneak", ChronosClient.sneakKeyString).formatted(Formatting.LIGHT_PURPLE));
                }
            }
        }
        else {
            tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_broken_1").formatted(Formatting.RED));
            tooltip.add(new TranslatableText("item.chronos.chronos_clock_tooltip_broken_2", ChronosConfig.getRepairItem().getName()).formatted(Formatting.LIGHT_PURPLE));
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
