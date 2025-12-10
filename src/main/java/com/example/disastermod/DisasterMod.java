package com.example.disastermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisasterMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("disastermod");

    // ▼ 隕石エンティティの登録用変数
    public static final EntityType<MeteorEntity> METEOR_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("disastermod", "meteor"),
            FabricEntityTypeBuilder.<MeteorEntity>create(SpawnGroup.MISC, MeteorEntity::new)
                    .dimensions(EntityDimensions.fixed(1.0f, 1.0f)) // サイズ 1x1
                    .trackRangeBlocks(128).trackedUpdateRate(10) // 遠くでも見えるように
                    .build()
    );

    @Override
    public void onInitialize() {
        LOGGER.info("DisasterMod Initializing...");

        // 1. 毎tickイベントの登録
        ServerTickEvents.END_SERVER_TICK.register(DisasterManager::tick);

        // 2. コマンドの登録 /disaster <type> <level>
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("disaster")
                    .then(CommandManager.argument("type", StringArgumentType.word())
                            .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 5))
                                    .executes(context -> {
                                        String typeStr = StringArgumentType.getString(context, "type");
                                        int level = IntegerArgumentType.getInteger(context, "level");

                                        DisasterType type = DisasterType.fromString(typeStr);

                                        if (type == null) {
                                            context.getSource().sendError(Text.literal("不正なタイプです。(meteor, earthquake, tsunami)"));
                                            return 0;
                                        }

                                        if (context.getSource().getEntity() instanceof ServerPlayerEntity player) {
                                            DisasterManager.scheduleDisaster(type, level, player);
                                            return 1;
                                        } else {
                                            context.getSource().sendError(Text.literal("このコマンドはプレイヤーのみ実行可能です。"));
                                            return 0;
                                        }
                                    })
                            )
                    )
            );
        });
    }
}