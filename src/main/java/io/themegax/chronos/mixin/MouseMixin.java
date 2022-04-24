package io.themegax.chronos.mixin;

import io.themegax.chronos.ChronosClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    double eventDeltaWheel;
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z"))
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        double d = (client.options.discreteMouseScroll ? Math.signum(vertical) : vertical) * client.options.mouseWheelSensitivity;
        this.eventDeltaWheel += d;
        int i = (int)this.eventDeltaWheel;
        if (i == 0) {
            return;
        }
        this.eventDeltaWheel -= (double)i;
        ChronosClient.scrollMouse(i);
    }
}
