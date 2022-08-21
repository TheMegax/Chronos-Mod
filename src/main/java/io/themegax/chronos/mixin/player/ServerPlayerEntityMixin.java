package io.themegax.chronos.mixin.player;

import io.themegax.chronos.ext.PlayerEntityExt;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "copyFrom", at = @At("RETURN"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity serverPlayer = ((ServerPlayerEntity)(Object)this);

        float RESET_TIMER = ((PlayerEntityExt)oldPlayer).getResetTimer();
        ((PlayerEntityExt)serverPlayer).setResetTimer(RESET_TIMER);
    }
}
