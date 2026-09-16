package com.neko_shampoo.event;

import com.neko_shampoo.entity.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端事件(渲染相关)。
 * <p>
 * 给投掷的洗面奶实体注册渲染器(用物品模型渲染,像雪球一样)。
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = "neko_shampoo")
public final class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHAMPOO_PROJECTILE.get(), ctx -> new ThrownItemRenderer<>(ctx));
    }

    private ClientEvents() {
    }
}
