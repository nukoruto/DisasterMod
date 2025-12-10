package com.example.disastermod;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ScheduledDisaster {
    public final DisasterType type;
    public final int level;
    public final ServerPlayerEntity targetPlayer;
    public final BlockPos targetPos;
    public int ticksUntilStart; // カウントダウン用タイマー

    public ScheduledDisaster(DisasterType type, int level, ServerPlayerEntity player) {
        this.type = type;
        this.level = level;
        this.targetPlayer = player;
        this.targetPos = player.getBlockPos(); // 発生地点を記録

        // 30秒 * 20tick = 600tick
        this.ticksUntilStart = 600;
    }
}