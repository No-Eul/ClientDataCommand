package dev.noeul.fabricmod.clientdatacommand.mixin;

import dev.noeul.fabricmod.clientdatacommand.ClientPosArgument;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.argument.PosArgument;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(PosArgument.class)
public interface PosArgumentMixin extends ClientPosArgument {
}
