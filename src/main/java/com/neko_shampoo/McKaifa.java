package com.neko_shampoo;

import com.neko_shampoo.creative.ModCreativeTab;
import com.neko_shampoo.effect.ModEffects;
import com.neko_shampoo.entity.ModEntities;
import com.neko_shampoo.event.ModEvents;
import com.neko_shampoo.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 模组主类。
 * <p>
 * 整个模组的"入口"。NeoForge 在加载模组时,
 * 会先找到被 @Mod 注解标记的类,然后调用它的构造函数。
 * <p>
 * 本模组:猫娘洗面奶(neko_shampoo)——一款 1.21.1 娱乐模组。
 */
@Mod(McKaifa.MODID)
public final class McKaifa {

    // 模组 ID,必须与 gradle.properties 里的 mod_id 完全一致
    // 且只能是小写英文字母、数字和下划线
    public static final String MODID = "neko_shampoo";

    /**
     * NeoForge 会自动把 modEventBus 注入到构造函数里。
     * modEventBus 负责"注册"阶段的事件;游戏运行时的事件则用 NeoForge.EVENT_BUS。
     */
    public McKaifa(IEventBus modEventBus) {
        // 把物品、状态效果、创造标签、实体的注册器挂到模组事件总线上
        ModItems.ITEMS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModCreativeTab.CREATIVE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        // 监听运行时事件(实体进入世界等)
        NeoForge.EVENT_BUS.register(ModEvents.class);
    }
}
