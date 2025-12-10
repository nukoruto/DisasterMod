package com.example.disastermod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DisasterManager {

    // 予約されている災害リスト
    private static final List<ScheduledDisaster> pendingDisasters = new ArrayList<>();

    // 経過時間計測用
    private static int globalTickCounter = 0;

    public static void tick(MinecraftServer server) {
        globalTickCounter++;

        // 1. 予約済み災害のカウントダウン処理
        handlePendingDisasters(server);

        // 2. 新規災害の自然発生判定 (20tick = 1秒ごとにチェック)
        if (globalTickCounter % 20 == 0) {
            checkRandomSpawning(server);
        }
    }

    private static void handlePendingDisasters(MinecraftServer server) {
        Iterator<ScheduledDisaster> iterator = pendingDisasters.iterator();

        while (iterator.hasNext()) {
            ScheduledDisaster disaster = iterator.next();
            disaster.ticksUntilStart--;

            if (disaster.ticksUntilStart <= 0) {
                executeDisaster(disaster, server);
                iterator.remove();
            }
        }
    }

    private static void executeDisaster(ScheduledDisaster disaster, MinecraftServer server) {
        String msg = String.format("【災害発生】タイプ:%s レベル:%d 座標:X%d, Y%d, Z%d",
                disaster.type, disaster.level,
                disaster.targetPos.getX(), disaster.targetPos.getY(), disaster.targetPos.getZ());

        server.getPlayerManager().broadcast(Text.literal(msg).formatted(Formatting.RED, Formatting.BOLD), false);

        // ▼ 隕石の場合の処理呼び出し
        if (disaster.type == DisasterType.METEOR) {
            spawnMeteor(disaster);
        }
    }

    // 隕石スポーン処理
    private static void spawnMeteor(ScheduledDisaster disaster) {
        // 1. スポーン位置（ターゲットの真上 100ブロック）
        double x = disaster.targetPos.getX() + 0.5;
        double y = disaster.targetPos.getY() + 100;
        double z = disaster.targetPos.getZ() + 0.5;

        // 2. 警告音（ウィザーのスポーン音）
        disaster.targetPlayer.getWorld().playSound(
                null,
                disaster.targetPos,
                net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN,
                net.minecraft.sound.SoundCategory.HOSTILE,
                10.0f,
                0.5f
        );

        // 3. 隕石生成
        MeteorEntity meteor = new MeteorEntity(disaster.targetPlayer.getWorld(), x, y, z, disaster.level);

        // 真下に加速
        meteor.setVelocity(0, -3.0, 0);

        disaster.targetPlayer.getWorld().spawnEntity(meteor);
    }

    private static void checkRandomSpawning(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            float localDiff = player.getWorld().getLocalDifficulty(player.getBlockPos()).getLocalDifficulty();
            // TODO: ここに確率計算を入れる
            if (localDiff > 100) { }
        }
    }

    public static void scheduleDisaster(DisasterType type, int level, ServerPlayerEntity player) {
        ScheduledDisaster disaster = new ScheduledDisaster(type, level, player);
        pendingDisasters.add(disaster);

        String warningMsg = String.format("⚠ 警告: %s (Lv%d) が30秒後に発生します！",
                type.name(), level);

        player.getServer().getPlayerManager().broadcast(
                Text.literal(warningMsg).formatted(Formatting.YELLOW, Formatting.BOLD), false);
    }
}