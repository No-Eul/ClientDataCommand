package dev.noeul.fabricmod.clientdatacommand.mixin;

import dev.noeul.fabricmod.clientdatacommand.ClientPosArgument;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.LookingPosArgument;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(LookingPosArgument.class)
public abstract class LookingPosArgumentMixin implements ClientPosArgument {
	@Shadow
	@Final
	private double x;

	@Shadow
	@Final
	private double y;

	@Shadow
	@Final
	private double z;

	@Override
	public Vec3d toAbsolutePos(FabricClientCommandSource source) {
		Vec2f vec2f = source.getRotation();
//		Vec3d vec3d = source.getEntityAnchor().positionAt(source);
		Vec3d vec3d = source.getEntity().getEyePos();
		float f = MathHelper.cos((vec2f.y + 90.0F) * 0.017453292F);
		float g = MathHelper.sin((vec2f.y + 90.0F) * 0.017453292F);
		float h = MathHelper.cos(-vec2f.x * 0.017453292F);
		float i = MathHelper.sin(-vec2f.x * 0.017453292F);
		float j = MathHelper.cos((-vec2f.x + 90.0F) * 0.017453292F);
		float k = MathHelper.sin((-vec2f.x + 90.0F) * 0.017453292F);
		Vec3d vec3d2 = new Vec3d(f * h, i, g * h);
		Vec3d vec3d3 = new Vec3d(f * j, k, g * j);
		Vec3d vec3d4 = vec3d2.crossProduct(vec3d3).multiply(-1.0);
		double d = vec3d2.x * this.z + vec3d3.x * this.y + vec3d4.x * this.x;
		double e = vec3d2.y * this.z + vec3d3.y * this.y + vec3d4.y * this.x;
		double l = vec3d2.z * this.z + vec3d3.z * this.y + vec3d4.z * this.x;
		return new Vec3d(vec3d.x + d, vec3d.y + e, vec3d.z + l);
	}

}
