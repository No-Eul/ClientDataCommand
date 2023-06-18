package dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ClientBlockPosArgumentType {

	static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
//		ServerWorld serverWorld = context.getSource().getWorld();
		World serverWorld = context.getSource().getWorld();
		return getLoadedBlockPos(context, serverWorld, name);
	}

	static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, World world, String name) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(context, name);
		if (!world.isChunkLoaded(blockPos)) {
			throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
		} else if (!world.isInBuildLimit(blockPos)) {
			throw BlockPosArgumentType.OUT_OF_WORLD_EXCEPTION.create();
		} else {
			return blockPos;
		}
	}

	static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
		return ((ClientPosArgument) context.getArgument(name, PosArgument.class)).toAbsoluteBlockPos(context.getSource());
	}
}
