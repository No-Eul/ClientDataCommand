package dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Identifier;

public interface ClientIdentifierArgumentType {
	static Identifier getIdentifier(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, Identifier.class);
	}
}
