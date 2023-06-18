package dev.noeul.fabricmod.clientdatacommand.mixin;

import dev.noeul.fabricmod.clientdatacommand.ClientEntitySelector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(EntitySelector.class)
public class EntitySelectorMixin implements ClientEntitySelector {
	@Override
	public EntitySelector thiz() {
		return (EntitySelector) (Object) this;
	}
}
