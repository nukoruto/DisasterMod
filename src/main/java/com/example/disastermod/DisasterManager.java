package com.example.disastermod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvents; // 追加
import net.minecraft.sound.SoundCategory; // 追加
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DisasterManager {

    private static final List<ScheduledDisaster> pendingDisasters = new ArrayList<>();
    private static int globalTickCounter = 0;

    public static void tick(MinecraftServer server) {
        globalTickCounter++;
        handlePendingDisasters(server);
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

        // ※音はここではなく、警告時に鳴らすように移動しました

        // 2. 隕石生成
        MeteorEntity meteor = new MeteorEntity(disaster.targetPlayer.getWorld(), x, y, z, disaster.level);
        meteor.setVelocity(0, -3.0, 0); // 真下に加速
        disaster.targetPlayer.getWorld().spawnEntity(meteor);
    }

    private static void checkRandomSpawning(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
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

        // ▼【変更点】サイレン（角笛）をここで鳴らす（30秒前）
        // 音量は極大(100.0f)にして、どこにいても聞こえるようにします
        disaster.targetPlayer.getWorld().playSound(
                null,
                disaster.targetPos,
                net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN,
                net.minecraft.sound.SoundCategory.HOSTILE,
                30.0f,
                0.5f
        );
    }
}