package io.themegax.chronos.sound;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static io.themegax.chronos.ChronosMain.modID;

public class ChronosSoundEvents {
    public static SoundEvent CLICK_1;
    public static SoundEvent CLICK_2;
    public static SoundEvent DEACTIVATE;
    public static SoundEvent RESONATE;

    public static void init() {
        CLICK_1 = registerSoundEvent("click_1");
        CLICK_2 = registerSoundEvent("click_2");
        DEACTIVATE = registerSoundEvent("ui.deactivate");
        RESONATE = registerSoundEvent("ui.resonate");
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(modID, name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }
}
