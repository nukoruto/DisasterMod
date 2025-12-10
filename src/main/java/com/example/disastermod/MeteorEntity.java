package com.example.disastermod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class MeteorEntity extends ThrownItemEntity {

    private int disasterLevel = 1; // デフォルトレベル

    public MeteorEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public MeteorEntity(World world, LivingEntity owner, int level) {
        super(DisasterMod.METEOR_ENTITY_TYPE, owner, world);
        this.disasterLevel = level;
    }

    // スポーン用のコンストラクタ
    public MeteorEntity(World world, double x, double y, double z, int level) {
        super(DisasterMod.METEOR_ENTITY_TYPE, x, y, z, world);
        this.disasterLevel = level;
    }

    public void setLevel(int level) {
        this.disasterLevel = level;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_BLOCK; // 見た目はマグマブロック
    }

    // 何か（ブロックやエンティティ）に当たった時の処理
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        // サーバー側でのみ処理
        if (!this.getWorld().isClient) {
            // 1. 直撃ダメージ処理
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                // ダメージ: レベル × 15
                float directDamage = this.disasterLevel * 15.0f;
                entityHit.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), directDamage);
            }

            // 2. 爆発規模の計算 (べき乗関数: レベルの2乗 * 4.0)
            // Lv1=4.0, Lv2=16.0, Lv3=36.0...
            float explosionPower = 4.0f * (float) Math.pow(this.disasterLevel, 2);

            // 3. 爆発を起こす (TNTタイプなのでブロック破壊あり)
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), explosionPower, World.ExplosionSourceType.TNT);

            // 消滅
            this.discard();
        }
    }
}