package io.themegax.chronos;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ChronosClockItem extends Item {

    public ChronosClockItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity usingLivingEntity, int remainingUseTicks) {
        if (world.isClient || getPullProgress(this.getMaxUseTime(stack) - remainingUseTicks) < 1.0F)
            return;
        if (usingLivingEntity instanceof PlayerEntity player) {
            for (LivingEntity entity : world.getNonSpectatingEntities(LivingEntity.class, player.getBoundingBox().expand(10.0D))) {
                if (entity != usingLivingEntity) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200));
                }
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVENT_RAID_HORN, SoundCategory.PLAYERS, 60.0F, 1.0F);
            player.getItemCooldownManager().set(this, 200);
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode)
                stack.damage(1, player, e -> e.sendToolBreakStatus(player.getActiveHand()));
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
