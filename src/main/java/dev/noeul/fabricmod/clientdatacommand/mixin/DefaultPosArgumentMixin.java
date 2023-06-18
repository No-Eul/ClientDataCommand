package dev.noeul.fabricmod.clientdatacommand.mixin;

import dev.noeul.fabricmod.clientdatacommand.ClientPosArgument;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(DefaultPosArgument.class)
public abstract class DefaultPosArgumentMixin implements ClientPosArgument {
	@Shadow
	@Final
	private CoordinateArgument x;

	@Shadow
	@Final
	private CoordinateArgument y;

	@Shadow
	@Final
	private CoordinateArgument z;

	@Override
	public Vec3d toAbsolutePos(FabricClientCommandSource source) {
		Vec3d vec3d = source.getPosition();
		return new Vec3d(
				this.x.toAbsoluteCoordinate(vec3d.x),
				this.y.toAbsoluteCoordinate(vec3d.y),
				this.z.toAbsoluteCoordinate(vec3d.z)
		);
	}
}
