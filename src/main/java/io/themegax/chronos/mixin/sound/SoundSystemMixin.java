package io.themegax.chronos.mixin.sound;

import com.google.common.collect.Multimap;
import io.themegax.chronos.ext.SoundSystemExt;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin implements SoundSystemExt {
    @Shadow @Final private Multimap<SoundCategory, SoundInstance> sounds;

    public boolean isPlayingById(Identifier id) {
        for (var soundInstance : sounds.get(SoundCategory.PLAYERS)) {
            if (soundInstance.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
