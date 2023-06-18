package dev.noeul.fabricmod.clientdatacommand;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface EntitySelectorHelper {
	static PlayerEntity getPlayer(World world, String name) {
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getGameProfile().getName().equalsIgnoreCase(name))
				return player;
		}
		return null;
	}

	static PlayerEntity getPlayer(World world, UUID uuid) {
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getGameProfile().getId().equals(uuid))
				return player;
		}
		return null;
	}

	static Entity getEntity(World world, UUID uuid) {
		return world.getEntityLookup().get(uuid);
	}

	static List<PlayerEntity> getPlayers(World world, Predicate<? super PlayerEntity> predicate, int limit) {
		List<PlayerEntity> list = Lists.newArrayList();
		Iterator<? extends PlayerEntity> var4 = world.getPlayers().iterator();

		while (var4.hasNext()) {
			PlayerEntity serverPlayerEntity = var4.next();
			if (predicate.test(serverPlayerEntity)) {
				list.add(serverPlayerEntity);
				if (list.size() >= limit) {
					return list;
				}
			}
		}

		return list;
	}

	static <T extends Entity> void collectEntitiesByType(World world, TypeFilter<Entity, T> filter, Predicate<? super T> predicate, List<? super T> result, int limit) {
		world.getEntityLookup().forEach(filter, (entity) -> {
			if (predicate.test(entity)) {
				result.add(entity);
				if (result.size() >= limit) {
					return LazyIterationConsumer.NextIteration.ABORT;
				}
			}

			return LazyIterationConsumer.NextIteration.CONTINUE;
		});
	}
}
