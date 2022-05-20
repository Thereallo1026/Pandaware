package dev.africa.pandaware.utils.client;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

@UtilityClass
public class SoundUtils implements MinecraftInstance {

    public void playSound(CustomSound sound) {
        playSound(sound.getSound());
    }

    public void playSound(String sound) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(sound), 0.5f));
    }

    @Getter
    @AllArgsConstructor
    public enum CustomSound {
        ENABLE("sound.pandaware.module.enable"),
        DISABLE("sound.pandaware.module.disable");

        private final String sound;
    }
}
