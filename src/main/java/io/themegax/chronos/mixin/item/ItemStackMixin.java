package io.themegax.chronos.mixin.item;

import io.themegax.chronos.ChronosClockItem;
import io.themegax.chronos.ChronosMain;
import io.themegax.chronos.config.ChronosConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getRepairCost", at = @At("HEAD"))
    public void getRepairCost(CallbackInfoReturnable<Integer> cir) {
        ItemStack itemStack = ((ItemStack)(Object)this);
        if (itemStack.getItem() == ChronosMain.CHRONOS_CLOCK) {
            itemStack.setRepairCost(ChronosConfig.getRepairCost());
        }
    }
}
