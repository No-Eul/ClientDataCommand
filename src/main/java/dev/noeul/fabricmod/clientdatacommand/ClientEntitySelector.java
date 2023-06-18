package dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public interface ClientEntitySelector {
	default Entity getEntity(FabricClientCommandSource source) throws CommandSyntaxException {
		this.checkSourcePermission(source);
		List<? extends Entity> list = this.getEntities(source);
		if (list.isEmpty()) {
			throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
		} else if (list.size() > 1) {
			throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
		} else {
			return list.get(0);
		}
	}

	private void checkSourcePermission(FabricClientCommandSource source) throws CommandSyntaxException {
		if (this.thiz().usesAt && !source.hasPermissionLevel(2)) {
			throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
		}
	}

	EntitySelector thiz();

	default List<? extends Entity> getEntities(FabricClientCommandSource source) throws CommandSyntaxException {
		this.checkSourcePermission(source);
		if (!this.thiz().includesNonPlayers) {
			return this.getPlayers(source);
		} else if (this.thiz().playerName != null) {
//			ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.thiz().playerName);
			PlayerEntity serverPlayerEntity = EntitySelectorHelper.getPlayer(source.getWorld(), this.thiz().playerName);
			return serverPlayerEntity == null ? List.of() : List.of(serverPlayerEntity);
		} else if (this.thiz().uuid != null) {
			/*for (ServerWorld serverWorld : source.getServer().getWorlds()) {
				Entity entity = serverWorld.getEntity(this.thiz().uuid);
				if (entity != null) {
					if (entity.getType().isEnabled(source.getEnabledFeatures())) {
						return List.of(entity);
					}
					break;
				}
			}*/
			ClientWorld serverWorld = source.getWorld();
//			Entity entity = serverWorld.getEntity(this.thiz().uuid);
			Entity entity = EntitySelectorHelper.getEntity(serverWorld, this.thiz().uuid);
			if (entity != null) {
				if (entity.getType().isEnabled(source.getEnabledFeatures())) {
					return List.of(entity);
				}
			}

			return List.of();
		} else {
			Vec3d vec3d = this.thiz().positionOffset.apply(source.getPosition());
			Box box = this.thiz().getOffsetBox(vec3d);
			if (this.thiz().senderOnly) {
				Predicate<Entity> predicate = this.thiz().getPositionPredicate(vec3d, box, null);
				return source.getEntity() != null && predicate.test(source.getEntity()) ? List.of(source.getEntity()) : List.of();
			} else {
				Predicate<Entity> predicate = this.thiz().getPositionPredicate(vec3d, box, source.getEnabledFeatures());
				List<Entity> list = new ObjectArrayList<>();
				/*if (this.thiz().isLocalWorldOnly()) {
					this.appendEntitiesFromWorld(list, source.getWorld(), box, predicate);
				} else {
					for (ServerWorld serverWorld2 : source.getServer().getWorlds()) {
						this.thiz().appendEntitiesFromWorld(list, serverWorld2, box, predicate);
					}
				}*/
				this.appendEntitiesFromWorld(list, source.getWorld(), box, predicate);

				return this.thiz().getEntities(vec3d, list);
			}
		}
	}

	default List<PlayerEntity> getPlayers(FabricClientCommandSource source) throws CommandSyntaxException {
		this.checkSourcePermission(source);
		if (this.thiz().playerName != null) {
//			ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.thiz().playerName);
			PlayerEntity serverPlayerEntity = EntitySelectorHelper.getPlayer(source.getWorld(), this.thiz().playerName);
			return serverPlayerEntity == null ? List.of() : List.of(serverPlayerEntity);
		} else if (this.thiz().uuid != null) {
//			ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.thiz().uuid);
			PlayerEntity serverPlayerEntity = EntitySelectorHelper.getPlayer(source.getWorld(), this.thiz().uuid);
			return serverPlayerEntity == null ? List.of() : List.of(serverPlayerEntity);
		} else {
			Vec3d vec3d = this.thiz().positionOffset.apply(source.getPosition());
			Box box = this.thiz().getOffsetBox(vec3d);
			Predicate<Entity> predicate = this.thiz().getPositionPredicate(vec3d, box, null);
			if (this.thiz().senderOnly) {
//				if (source.getEntity() instanceof ServerPlayerEntity serverPlayerEntity2 && predicate.test(serverPlayerEntity2)) {
				if (source.getEntity() instanceof PlayerEntity serverPlayerEntity2 && predicate.test(serverPlayerEntity2)) {
					return List.of(serverPlayerEntity2);
				}

				return List.of();
			} else {
				int i = this.thiz().getAppendLimit();
				List<PlayerEntity> list;
				if (this.thiz().isLocalWorldOnly()) {
//					list = source.getWorld().getPlayers(predicate, i);
					list = EntitySelectorHelper.getPlayers(source.getWorld(), predicate, i);
				} else {
					list = new ObjectArrayList<>();

//					for (ServerPlayerEntity serverPlayerEntity3 : source.getServer().getPlayerManager().getPlayerList()) {
					for (PlayerEntity serverPlayerEntity3 : source.getWorld().getPlayers()) {
						if (predicate.test(serverPlayerEntity3)) {
							list.add(serverPlayerEntity3);
							if (list.size() >= i) {
								return list;
							}
						}
					}
				}

				return this.thiz().getEntities(vec3d, list);
			}
		}
	}

	default void appendEntitiesFromWorld(List<Entity> entities, World world, @Nullable Box box, Predicate<Entity> predicate) {
		int i = this.thiz().getAppendLimit();
		if (entities.size() < i) {
			if (box != null) {
				world.collectEntitiesByType(this.thiz().entityFilter, box, predicate, entities, i);
			} else {
//				world.collectEntitiesByType(this.thiz().entityFilter, predicate, entities, i);
				EntitySelectorHelper.collectEntitiesByType(world, this.thiz().entityFilter, predicate, entities, i);
			}

		}
	}
}
