package dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;

public interface ClientEntityArgumentType {
	static Entity getEntity(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		return ((ClientEntitySelector) context.getArgument(name, EntitySelector.class)).getEntity(context.getSource());
	}
}
