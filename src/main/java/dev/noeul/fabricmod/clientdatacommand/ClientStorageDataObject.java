package dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DataCommandStorage;

public interface ClientStorageDataObject {
	SuggestionProvider<FabricClientCommandSource> SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestIdentifiers(
			of(context).getIds(), builder
	);

	static DataCommandStorage of(CommandContext<FabricClientCommandSource> context) {
//		return context.getSource().getServer().getDataCommandStorage();
		return null;
	}
}
