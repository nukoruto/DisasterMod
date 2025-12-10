package com.example.disastermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisasterMod implements ModInitializer {

    // ロガー（コンソール出力用）
    public static final Logger LOGGER = LoggerFactory.getLogger("disastermod");

    @Override
    public void onInitialize() {
        LOGGER.info("DisasterMod Initializing...");

        // 1. 毎tickイベントの登録 (DisasterManagerを動かす)
        ServerTickEvents.END_SERVER_TICK.register(DisasterManager::tick);

        // 2. コマンドの登録 /disaster <type> <level>
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("disaster")
                    .then(CommandManager.argument("type", StringArgumentType.word()) // 第1引数:タイプ
                            .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 5)) // 第2引数:レベル
                                    .executes(context -> {
                                        // 入力値の取得
                                        String typeStr = StringArgumentType.getString(context, "type");
                                        int level = IntegerArgumentType.getInteger(context, "level");

                                        // 文字列をEnumに変換
                                        DisasterType type = DisasterType.fromString(typeStr);

                                        // エラーチェック
                                        if (type == null) {
                                            context.getSource().sendError(Text.literal("不正なタイプです。(meteor, earthquake, tsunami)"));
                                            return 0;
                                        }

                                        // 実行者がプレイヤーか確認
                                        if (context.getSource().getEntity() instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                                            // 災害をスケジュール登録
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