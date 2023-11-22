package dev.bluevista.lunamod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.bluevista.lunamod.client.LunaEntityRenderer;
import dev.bluevista.lunamod.entity.LunaEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.*;

public class LunaMod implements ModInitializer, ClientModInitializer {

	public static final String MODID = "lunamod";
	public static final Logger LOGGER = LogManager.getLogger("Luna Mod");

	public static final EntityType<LunaEntity> LUNA = Registry.register(Registries.ENTITY_TYPE, new Identifier(MODID, "luna"), FabricEntityTypeBuilder
		.createMob()
		.entityFactory(LunaEntity::new)
		.spawnGroup(SpawnGroup.CREATURE)
		.trackRangeChunks(10)
		.defaultAttributes(LunaEntity::createAttributes)
		.dimensions(EntityDimensions.fixed(0.9F, 0.9F))
		.build());

	public static final Item LUNA_SPAWN_EGG = Registry.register(
		Registries.ITEM,
		new Identifier(MODID, "luna_spawn_egg"),
		new SpawnEggItem(LUNA, ColorHelper.Argb.getArgb(255, 210, 209, 183), ColorHelper.Argb.getArgb(255, 80, 64, 50), new FabricItemSettings())
	);

	public static final Identifier LUNA_WOOF_SOUND = new Identifier(MODID, "woof");
	public static final SoundEvent LUNA_WOOF_SOUND_EVENT = Registry.register(Registries.SOUND_EVENT, LUNA_WOOF_SOUND, SoundEvent.of(LUNA_WOOF_SOUND));
	public static final Identifier LUNA_HURT_SOUND = new Identifier(MODID, "hurt");
	public static final SoundEvent LUNA_HURT_SOUND_EVENT = Registry.register(Registries.SOUND_EVENT, LUNA_HURT_SOUND, SoundEvent.of(LUNA_HURT_SOUND));
	public static final Identifier LUNA_DEATH_SOUND = new Identifier(MODID, "death");
	public static final SoundEvent LUNA_DEATH_SOUND_EVENT = Registry.register(Registries.SOUND_EVENT, LUNA_DEATH_SOUND, SoundEvent.of(LUNA_DEATH_SOUND));

	@Override
	public void onInitialize() {
		LOGGER.info("woof.");
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(this::onModifyGroupEntries);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("lunasplosion")
				.then(argument("amount", IntegerArgumentType.integer(1))
				.executes(ctx -> {
					int amount = IntegerArgumentType.getInteger(ctx, "amount");
					var pos = ctx.getSource().getPosition();
					var world = ctx.getSource().getWorld();
					for (int i = 0; i < amount; i++) {
						var luna = LUNA.create(world);

						// minimal amount of spacing
						luna.updatePosition(
							pos.x + (world.random.nextDouble() - 0.5) * amount * 0.02,
							pos.y,
							pos.z + (world.random.nextDouble() - 0.5) * amount * 0.02
						);
						world.spawnEntity(luna);
					}
					return 1;
				}))
			);
		});
	}

	private void onModifyGroupEntries(FabricItemGroupEntries entries) {
		entries.add(LUNA_SPAWN_EGG);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onInitializeClient() {
		EntityRendererRegistry.register(LUNA, LunaEntityRenderer::new);
	}

}
