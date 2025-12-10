package com.example.disastermod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DisasterManager {

    // 予約されている災害リスト
    private static final List<ScheduledDisaster> pendingDisasters = new ArrayList<>();

    // 経過時間計測用
    private static int globalTickCounter = 0;

    /**
     * サーバーの毎tick処理で呼ばれるメソッド
     */
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
        // 全員に通知メッセージを作成
        String msg = String.format("【災害発生】タイプ:%s レベル:%d 座標:X%d, Y%d, Z%d",
                disaster.type, disaster.level,
                disaster.targetPos.getX(), disaster.targetPos.getY(), disaster.targetPos.getZ());

        // 赤色でチャット欄に表示
        server.getPlayerManager().broadcast(Text.literal(msg).formatted(Formatting.RED, Formatting.BOLD), false);

        // TODO: ここに実際の隕石スポーン処理を書く
    }

    // ランダム発生のチェック（1秒に1回）
    private static void checkRandomSpawning(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // 地域難易度を取得
            float localDiff = player.getWorld().getLocalDifficulty(player.getBlockPos()).getLocalDifficulty();

            // エラー回避のため、一旦変数を使うふりをしておく（警告消し）
            if (localDiff > 100) {
                // ダミー処理
            }
        }
    }

    // コマンド等から災害を予約するメソッド
    public static void scheduleDisaster(DisasterType type, int level, ServerPlayerEntity player) {
        ScheduledDisaster disaster = new ScheduledDisaster(type, level, player);
        pendingDisasters.add(disaster);

        // 警告メッセージ
        String warningMsg = String.format("⚠ 警告: %s (Lv%d) が30秒後に発生します！",
                type.name(), level);

        // 黄色で強調表示
        player.getServer().getPlayerManager().broadcast(
                Text.literal(warningMsg).formatted(Formatting.YELLOW, Formatting.BOLD), false);
    }
}