package dev.noeul.fabricmod.clientdatacommand;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class ClientDataCommandMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> ClientDataCommand.register(dispatcher)
		);
	}
}
