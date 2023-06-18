package dev.noeul.fabricmod.clientdatacommand;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.DataCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientDataCommand {
	public static final Function<String, ObjectType> ENTITY_TYPE_FACTORY = argumentName -> new ObjectType() {
		@Override
		public DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
			return new EntityDataObject(ClientEntityArgumentType.getEntity(context, argumentName));
		}

		@Override
		public ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				ArgumentBuilder<FabricClientCommandSource, ?> argument,
				UnaryOperator<ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		) {
			return argument.then(literal("entity")
					.then(argumentAdder.apply(argument(argumentName, EntityArgumentType.entity())))
			);
		}
	};
	public static final Function<String, ObjectType> BLOCK_TYPE_FACTORY = argumentName -> new ObjectType() {
		@Override
		public DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
			BlockPos blockPos = ClientBlockPosArgumentType.getLoadedBlockPos(context, argumentName + "Pos");
			BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(blockPos);
			if (blockEntity == null) {
				throw BlockDataObject.INVALID_BLOCK_EXCEPTION.create();
			} else {
				return new BlockDataObject(blockEntity, blockPos);
			}
		}

		@Override
		public ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				ArgumentBuilder<FabricClientCommandSource, ?> argument,
				UnaryOperator<ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		) {
			return argument.then(literal("block")
					.then(argumentAdder.apply(argument(argumentName + "Pos", BlockPosArgumentType.blockPos())))
			);
		}
	};
	/*public static final Function<String, ObjectType> STORAGE_TYPE_FACTORY = argumentName -> new ObjectType() {
		@Override
		public DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) {
			return new StorageDataObject(ClientStorageDataObject.of(context), ClientIdentifierArgumentType.getIdentifier(context, argumentName));
		}

		@Override
		public ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				ArgumentBuilder<FabricClientCommandSource, ?> argument,
				UnaryOperator<ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		) {
			return argument.then(ClientCommandManager.literal("storage")
					.then(argumentAdder.apply(ClientCommandManager.argument(argumentName, IdentifierArgumentType.identifier())
							.suggests(ClientStorageDataObject.SUGGESTION_PROVIDER)
					))
			);
		}
	};*/
	public static final List<Function<String, ObjectType>> OBJECT_TYPE_FACTORIES = ImmutableList.of(
			ENTITY_TYPE_FACTORY, BLOCK_TYPE_FACTORY/*, STORAGE_TYPE_FACTORY*/
	);
	public static final List<ObjectType> TARGET_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream()
			.map(factory -> factory.apply("target"))
			.collect(ImmutableList.toImmutableList());
	public static final List<ObjectType> SOURCE_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream()
			.map(factory -> factory.apply("source"))
			.collect(ImmutableList.toImmutableList());

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		LiteralArgumentBuilder<FabricClientCommandSource> literalArgumentBuilder = literal("client-data");
//				.requires(source -> source.hasPermissionLevel(2));

		for (ObjectType objectType : TARGET_OBJECT_TYPES) {
			literalArgumentBuilder
					.then(objectType.addArgumentsToBuilder(literal("merge"), builder1 -> builder1
							.then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
									.executes(context1 -> executeMerge(
											context1.getSource(),
											objectType.getObject(context1),
											NbtCompoundArgumentType.getNbtCompound(context1, "nbt")
									))
							)
					))
					.then(objectType.addArgumentsToBuilder(literal("get"), builder2 -> builder2
							.executes(context2 -> executeGet(
									context2.getSource(),
									objectType.getObject(context2)
							))
							.then(argument("path", NbtPathArgumentType.nbtPath())
									.executes(context2 -> executeGet(
											context2.getSource(),
											objectType.getObject(context2),
											ClientNbtPathArgumentType.getNbtPath(context2, "path")
									))
									.then(argument("scale", DoubleArgumentType.doubleArg())
											.executes(context2 -> executeGet(
													context2.getSource(),
													objectType.getObject(context2),
													ClientNbtPathArgumentType.getNbtPath(context2, "path"),
													DoubleArgumentType.getDouble(context2, "scale")
											))
									)
							)
					))
					.then(objectType.addArgumentsToBuilder(literal("remove"), builder3 -> builder3
							.then(argument("path", NbtPathArgumentType.nbtPath())
									.executes(context3 -> executeRemove(
											context3.getSource(),
											objectType.getObject(context3),
											ClientNbtPathArgumentType.getNbtPath(context3, "path")
									))
							)
					))
					.then(addModifyArgument((builder, modifier) -> builder
							.then(literal("insert")
									.then(argument("index", IntegerArgumentType.integer())
											.then(modifier.create((context, sourceNbt, path, elements) -> path.insert(
															IntegerArgumentType.getInteger(context, "index"),
															sourceNbt, elements
													))
											)
									)
							)
							.then(literal("prepend")
									.then(modifier.create((context, sourceNbt, path, elements) ->
											path.insert(0, sourceNbt, elements)
									))
							)
							.then(literal("append")
									.then(modifier.create((context, sourceNbt, path, elements) ->
											path.insert(-1, sourceNbt, elements)
									))
							)
							.then(literal("set")
									.then(modifier.create((context, sourceNbt, path, elements) ->
											path.put(sourceNbt, Iterables.getLast(elements))
									))
							)
							.then(literal("merge")
									.then(modifier.create((context, element, path, elements) -> {
										NbtCompound nbtCompound = new NbtCompound();

										for (NbtElement nbtElement : elements) {
											if (NbtPathArgumentType.NbtPath.isTooDeep(nbtElement, 0)) {
												throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
											}

											if (!(nbtElement instanceof NbtCompound nbtCompound2)) {
												throw DataCommand.MODIFY_EXPECTED_OBJECT_EXCEPTION.create(nbtElement);
											}

											nbtCompound.copyFrom(nbtCompound2);
										}

										Collection<NbtElement> collection = path.getOrInit(element, NbtCompound::new);
										int i = 0;

										for (NbtElement nbtElement2 : collection) {
											if (!(nbtElement2 instanceof NbtCompound nbtCompound3)) {
												throw DataCommand.MODIFY_EXPECTED_OBJECT_EXCEPTION.create(nbtElement2);
											}

											NbtCompound nbtCompound4 = nbtCompound3.copy();
											nbtCompound3.copyFrom(nbtCompound);
											i += nbtCompound4.equals(nbtCompound3) ? 0 : 1;
										}

										return i;
									}))
							)
					));
		}

		dispatcher.register(literal("datac")
				.redirect(dispatcher.register(literalArgumentBuilder))
		);
	}

	private static ArgumentBuilder<FabricClientCommandSource, ?> addModifyArgument(
			BiConsumer<ArgumentBuilder<FabricClientCommandSource, ?>, ModifyArgumentCreator> subArgumentAdder
	) {
		LiteralArgumentBuilder<FabricClientCommandSource> literalArgumentBuilder = literal("modify");

		for (ObjectType objectType : TARGET_OBJECT_TYPES) {
			objectType.addArgumentsToBuilder(
					literalArgumentBuilder,
					builder -> {
						ArgumentBuilder<FabricClientCommandSource, ?> argumentBuilder = argument("targetPath", NbtPathArgumentType.nbtPath());

						for (ObjectType objectType2 : SOURCE_OBJECT_TYPES) {
							subArgumentAdder.accept(
									argumentBuilder,
									operation -> objectType2.addArgumentsToBuilder(literal("from"), builderx -> builderx
											.executes(context -> executeModify(
													context, objectType, operation,
													getValues(context, objectType2)
											))
											.then(argument("sourcePath", NbtPathArgumentType.nbtPath())
													.executes(context -> executeModify(
															context, objectType, operation,
															getValuesByPath(context, objectType2)
													))
											)
									)
							);
							subArgumentAdder.accept(
									argumentBuilder,
									operation -> objectType2.addArgumentsToBuilder(literal("string"), builderx -> builderx
											.executes(context -> executeModify(
													context, objectType, operation,
													DataCommand.mapValues(getValues(context, objectType2), value -> value)
											))
											.then(argument("sourcePath", NbtPathArgumentType.nbtPath())
													.executes(context -> executeModify(
															context, objectType, operation,
															DataCommand.mapValues(getValuesByPath(context, objectType2), value -> value)
													))
													.then(argument("start", IntegerArgumentType.integer())
															.executes(context -> executeModify(
																	context, objectType, operation,
																	DataCommand.mapValues(
																			getValuesByPath(context, objectType2),
																			value -> DataCommand.substring(value, IntegerArgumentType.getInteger(context, "start"))
																	)
															))
															.then(argument("end", IntegerArgumentType.integer())
																	.executes(context -> executeModify(
																			context, objectType, operation,
																			DataCommand.mapValues(
																					getValuesByPath(context, objectType2),
																					value -> DataCommand.substring(
																							value,
																							IntegerArgumentType.getInteger(context, "start"),
																							IntegerArgumentType.getInteger(context, "end")
																					)
																			)
																	))
															)
													)
											)
									)
							);
						}

						subArgumentAdder.accept(argumentBuilder, modifier -> literal("value")
								.then(argument("value", NbtElementArgumentType.nbtElement())
										.executes(context -> {
											List<NbtElement> list = Collections.singletonList(
													NbtElementArgumentType.getNbtElement(context, "value")
											);
											return executeModify(context, objectType, modifier, list);
										})
								)
						);
						return builder.then(argumentBuilder);
					}
			);
		}

		return literalArgumentBuilder;
	}

	private static int executeModify(
			CommandContext<FabricClientCommandSource> context,
			ObjectType objectType,
			ModifyOperation modifier,
			List<NbtElement> elements
	) throws CommandSyntaxException {
		DataCommandObject dataCommandObject = objectType.getObject(context);
		NbtPathArgumentType.NbtPath nbtPath = ClientNbtPathArgumentType.getNbtPath(context, "targetPath");
		NbtCompound nbtCompound = dataCommandObject.getNbt();
		int i = modifier.modify(context, nbtCompound, nbtPath, elements);
		if (i == 0) {
			throw DataCommand.MERGE_FAILED_EXCEPTION.create();
		} else {
			dataCommandObject.setNbt(nbtCompound);
			context.getSource().sendFeedback(dataCommandObject.feedbackModify());
			return i;
		}
	}

	private static List<NbtElement> getValues(CommandContext<FabricClientCommandSource> context, ObjectType objectType) throws CommandSyntaxException {
		DataCommandObject dataCommandObject = objectType.getObject(context);
		return Collections.singletonList(dataCommandObject.getNbt());
	}

	private static List<NbtElement> getValuesByPath(CommandContext<FabricClientCommandSource> context, ObjectType objectType) throws CommandSyntaxException {
		DataCommandObject dataCommandObject = objectType.getObject(context);
		NbtPathArgumentType.NbtPath nbtPath = ClientNbtPathArgumentType.getNbtPath(context, "sourcePath");
		return nbtPath.get(dataCommandObject.getNbt());
	}

	private static int executeMerge(FabricClientCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
		NbtCompound nbtCompound = object.getNbt();
		if (NbtPathArgumentType.NbtPath.isTooDeep(nbt, 0)) {
			throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
		} else {
			NbtCompound nbtCompound2 = nbtCompound.copy().copyFrom(nbt);
			if (nbtCompound.equals(nbtCompound2)) {
				throw DataCommand.MERGE_FAILED_EXCEPTION.create();
			} else {
				object.setNbt(nbtCompound2);
				source.sendFeedback(object.feedbackModify());
				return 1;
			}
		}
	}

	private static int executeGet(FabricClientCommandSource source, DataCommandObject object) throws CommandSyntaxException {
		NbtCompound nbtCompound = object.getNbt();
		source.sendFeedback(object.feedbackQuery(nbtCompound));
		return 1;
	}

	private static int executeGet(FabricClientCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
		NbtElement nbtElement = DataCommand.getNbt(path, object);
		if (!(nbtElement instanceof AbstractNbtNumber)) {
			throw DataCommand.GET_INVALID_EXCEPTION.create(path.toString());
		} else {
			int i = MathHelper.floor(((AbstractNbtNumber) nbtElement).doubleValue() * scale);
			source.sendFeedback(object.feedbackGet(path, scale, i));
			return i;
		}
	}

	private static int executeGet(FabricClientCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
		NbtElement nbtElement = DataCommand.getNbt(path, object);
		int i;
		if (nbtElement instanceof AbstractNbtNumber) {
			i = MathHelper.floor(((AbstractNbtNumber) nbtElement).doubleValue());
		} else if (nbtElement instanceof AbstractNbtList) {
			i = ((AbstractNbtList) nbtElement).size();
		} else if (nbtElement instanceof NbtCompound) {
			i = ((NbtCompound) nbtElement).getSize();
		} else {
			if (!(nbtElement instanceof NbtString)) {
				throw DataCommand.GET_UNKNOWN_EXCEPTION.create(path.toString());
			}

			i = nbtElement.asString().length();
		}

		source.sendFeedback(object.feedbackQuery(nbtElement));
		return i;
	}

	private static int executeRemove(FabricClientCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
		NbtCompound nbtCompound = object.getNbt();
		int i = path.remove(nbtCompound);
		if (i == 0) {
			throw DataCommand.MERGE_FAILED_EXCEPTION.create();
		} else {
			object.setNbt(nbtCompound);
			source.sendFeedback(object.feedbackModify());
			return i;
		}
	}

	public interface ObjectType {
		DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException;

		ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				ArgumentBuilder<FabricClientCommandSource, ?> argument,
				UnaryOperator<ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		);
	}

	@FunctionalInterface
	interface ModifyArgumentCreator {
		ArgumentBuilder<FabricClientCommandSource, ?> create(ModifyOperation modifier);
	}

	@FunctionalInterface
	interface ModifyOperation {
		int modify(
				CommandContext<FabricClientCommandSource> context,
				NbtCompound sourceNbt,
				NbtPathArgumentType.NbtPath path,
				List<NbtElement> elements
		) throws CommandSyntaxException;
	}
}
