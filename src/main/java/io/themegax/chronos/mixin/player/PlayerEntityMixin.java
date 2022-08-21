package io.themegax.chronos.mixin.player;

import io.themegax.chronos.ext.PlayerEntityExt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerEntityExt {
    private float resetTimerMilliseconds = 0f;

    public float getResetTimer() {
        return resetTimerMilliseconds;
    }
    public void setResetTimer(float resetTimer) {
        this.resetTimerMilliseconds = resetTimer;
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("resetTimerMilliseconds", this.resetTimerMilliseconds);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("resetTimerMilliseconds")) {
            this.resetTimerMilliseconds = nbt.getFloat("resetTimerMilliseconds");
        }
    }
}