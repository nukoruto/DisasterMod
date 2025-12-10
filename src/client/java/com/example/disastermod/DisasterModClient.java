package com.example.disastermod;

import net.fabricmc.api.ClientModInitializer;
// ▼ 以下の2行のインポートを追加（これでエラーが消えます）
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class DisasterModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 隕石エンティティのレンダラーを登録
		// FlyingItemEntityRenderer は「アイテムが飛んでいる見た目」を自動でやってくれます
		EntityRendererRegistry.register(DisasterMod.METEOR_ENTITY_TYPE, FlyingItemEntityRenderer::new);
	}
}