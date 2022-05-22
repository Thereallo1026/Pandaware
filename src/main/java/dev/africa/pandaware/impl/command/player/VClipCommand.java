package dev.africa.pandaware.impl.command.player;


import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;
import dev.africa.pandaware.utils.client.Printer;
import net.minecraft.util.MathHelper;

@CommandInformation(name = "VClip", description = "Clips vertically")
public class VClipCommand extends Command {
    @Override
    public void process(String[] arguments) {
        try {
            double dist = MathHelper.clamp_double(Double.parseDouble(arguments[1]), -100, 100);
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY
                    + dist, mc.thePlayer.posZ);
            Printer.chat("Teleported " + dist + " blocks");
        } catch (Exception e) {
            this.sendInvalidArgumentsMessage("Height");
        }
    }
}
