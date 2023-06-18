package dev.noeul.fabricmod.clientdatacommand.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientCommandSource.class)
public class ClientCommandSourceMixin {
	@Inject(method = "hasPermissionLevel", at = @At("HEAD"), cancellable = true)
	private void inject$hasPermissionLevel(int level, CallbackInfoReturnable<Boolean> callbackInfo) {
		callbackInfo.setReturnValue(true);
	}
}
