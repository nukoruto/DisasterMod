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

    public MeteorEntity(World world, double x, double y, double z, int level) {
        super(DisasterMod.METEOR_ENTITY_TYPE, x, y, z, world);
        this.disasterLevel = level;
    }

    // レベルを設定するメソッド
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

        // サーバー側でのみ処理（爆発などはサーバー主導）
        if (!this.getWorld().isClient) {
            // 1. 直撃ダメージ処理
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                // 仕様: レベル × 15 の固定ダメージ
                float directDamage = this.disasterLevel * 15.0f;
                // ダメージを与える (ダメージソース: thrown)
                entityHit.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), directDamage);
            }

            // 2. 爆発規模の計算 (べき乗関数)
            // 仕様: レベルの2乗で強くなる
            // Lv1 = 4.0 (TNT1個分)
            // Lv2 = 4.0 * 4 = 16.0 (TNT数個分)
            // Lv3 = 4.0 * 9 = 36.0 (超巨大)
            // Lv5 = 4.0 * 25 = 100.0 (核兵器級)
            float explosionPower = 4.0f * (float) Math.pow(this.disasterLevel, 2);

            // 3. 爆発を起こす
            // ExplosionSourceType.TNT にすることで、ブロック破壊が発生し、かつ黒曜石などは壊れない(爆発耐性参照)
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), explosionPower, World.ExplosionSourceType.TNT);

            // 役目を終えたら消滅
            this.discard();
        }
    }
}