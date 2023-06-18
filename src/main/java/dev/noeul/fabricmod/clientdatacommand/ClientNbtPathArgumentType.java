package dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.NbtPathArgumentType;

public interface ClientNbtPathArgumentType {
	static NbtPathArgumentType.NbtPath getNbtPath(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, NbtPathArgumentType.NbtPath.class);
	}
}
