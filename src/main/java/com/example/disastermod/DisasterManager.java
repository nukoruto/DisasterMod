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

    // 予約リストの処理
    private static void handlePendingDisasters(MinecraftServer server) {
        Iterator<ScheduledDisaster> iterator = pendingDisasters.iterator();

        while (iterator.hasNext()) {
            ScheduledDisaster disaster = iterator.next();

            // カウントダウン
            disaster.ticksUntilStart--;

            // 残り時間が0になったら発生！
            if (disaster.ticksUntilStart <= 0) {
                executeDisaster(disaster, server);
                iterator.remove(); // リストから削除
            }
        }
    }

    // 実際に災害を起こすメソッド
    private static void executeDisaster(ScheduledDisaster disaster, MinecraftServer server) {
        String msg = String.format("【災害発生】タイプ:%s レベル:%d 座標:X%d, Y%d, Z%d",
                disaster.type, disaster.level,
                disaster.targetPos.getX(), disaster.targetPos.getY(), disaster.targetPos.getZ());

        server.getPlayerManager().broadcast(Text.literal(msg).formatted(Formatting.RED, Formatting.BOLD), false);

        // ▼ 災害ごとの分岐処理
        if (disaster.type == DisasterType.METEOR) {
            spawnMeteor(disaster);
        }
    }

    // 隕石スポーン処理
    private static void spawnMeteor(ScheduledDisaster disaster) {
        // 1. スポーン位置の計算（ターゲットの真上 100ブロック）
        double x = disaster.targetPos.getX() + 0.5; // ブロックの中心
        double y = disaster.targetPos.getY() + 100; // 上空
        double z = disaster.targetPos.getZ() + 0.5;

        // 2. 音を鳴らす（中心座標から発する）
        // SoundCategory.HOSTILE, 音量10.0(広範囲), ピッチ0.5(低音)
        // 仮の音として「ウィザーのスポーン音」を使用
        disaster.targetPlayer.getWorld().playSound(
                null,
                disaster.targetPos,
                net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN, // 降る直前の警告音
                net.minecraft.sound.SoundCategory.HOSTILE,
                10.0f,
                0.5f
        );

        // 3. 隕石エンティティを生成してワールドに追加
        MeteorEntity meteor = new MeteorEntity(disaster.targetPlayer.getWorld(), x, y, z, disaster.level);

        // 真下に加速させる (Velocity)
        meteor.setVelocity(0, -3.0, 0); // かなり速く落とす

        disaster.targetPlayer.getWorld().spawnEntity(meteor);
    }

    // ランダム発生のチェック（1秒に1回）
    private static void checkRandomSpawning(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // 地域難易度を取得
            float localDiff = player.getWorld().getLocalDifficulty(player.getBlockPos()).getLocalDifficulty();

            if (localDiff > 100) { }
        }
    }

    // コマンド等から災害を予約するメソッド
    public static void scheduleDisaster(DisasterType type, int level, ServerPlayerEntity player) {
        ScheduledDisaster disaster = new ScheduledDisaster(type, level, player);
        pendingDisasters.add(disaster);

        String warningMsg = String.format("⚠ 警告: %s (Lv%d) が30秒後に発生します！",
                type.name(), level);

        player.getServer().getPlayerManager().broadcast(
                Text.literal(warningMsg).formatted(Formatting.YELLOW, Formatting.BOLD), false);
    }
}