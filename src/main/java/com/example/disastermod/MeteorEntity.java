package com.example.disastermod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos; // 追加
import net.minecraft.block.Blocks; // 追加
import net.minecraft.block.BlockState; // 追加

public class MeteorEntity extends ThrownItemEntity {

    private int disasterLevel = 1;

    public MeteorEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public MeteorEntity(World world, LivingEntity owner, int level) {
        super(DisasterMod.METEOR_ENTITY_TYPE, owner, world);
        this.disasterLevel = level;
    }

    public MeteorEntity(World world, double x, double y, double z, int level) {
        super(DisasterMod.METEOR_ENTITY_TYPE, x, y, z, world);
        this.disasterLevel = level;
    }

    public void setLevel(int level) {
        this.disasterLevel = level;
    }

    // NBTデータ保存・読み込み処理（前回修正済み）
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DisasterLevel", this.disasterLevel);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("DisasterLevel")) {
            this.disasterLevel = nbt.getInt("DisasterLevel");
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_BLOCK;
    }

    // 衝突時の処理 (メインスレッドで一括同期破壊を実行)
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient) {

            // 1. 直撃ダメージ処理
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                float directDamage = this.disasterLevel * 50.0f;
                entityHit.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), directDamage);
            }

            // 2. 爆発規模の設定
            int radius;
            switch (this.disasterLevel) {
                case 1: radius = 20; break;
                case 2: radius = 40; break;
                case 3: radius = 60; break; // 直径120ブロック
                case 4: radius = 90; break;
                case 5: radius = 128; break; // 直径256ブロック (16チャンク規模)
                default: radius = 10;
            }

            // 3. メインスレッドでの一括同期破壊を実行
            performInstantSphereDestruction(radius);

            // 4. 隕石を消滅
            this.discard();
        }
    }

    // 【最重要】メインスレッドで球体破壊を「一括」で実行するメソッド
    private void performInstantSphereDestruction(int radius) {
        BlockPos centerPos = this.getBlockPos();
        World world = this.getWorld();
        long radiusSq = (long) radius * radius;

        // X, Y, Z の全ての範囲をチェック (計算量多)
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    // 球体判定 (三平方の定理: x^2 + y^2 + z^2 <= radius^2)
                    if ((long) x * x + (long) y * y + (long) z * z <= radiusSq) {

                        BlockPos targetPos = centerPos.add(x, y, z);
                        BlockState blockState = world.getBlockState(targetPos);

                        // 破壊判定ロジック: Airや壊せないブロックを除外
                        if (!blockState.isAir() && !blockState.isOf(Blocks.WATER) && blockState.getHardness(world, targetPos) >= 0) {
                            // setBlockStateで一括破壊（第3引数2はクライアントへの通知フラグ）
                            world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }
}