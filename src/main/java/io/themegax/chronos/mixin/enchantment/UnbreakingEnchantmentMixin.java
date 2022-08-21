package io.themegax.chronos.mixin.enchantment;

import io.themegax.chronos.ChronosMain;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UnbreakingEnchantment.class)
public abstract class UnbreakingEnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() == ChronosMain.CHRONOS_CLOCK) {
            cir.setReturnValue(false);
        }
    }
}
