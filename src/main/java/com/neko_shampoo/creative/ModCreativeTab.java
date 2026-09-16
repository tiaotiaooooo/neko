package com.neko_shampoo.creative;

import com.neko_shampoo.McKaifa;
import com.neko_shampoo.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式标签页注册器。
 * <p>
 * 创造模式物品栏里多出来的那一页,里面放我们这个模组的所有东西。
 */
public final class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, McKaifa.MODID);

    // 猫娘洗面奶标签页,图标先用洗面奶物品
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NEKO_TAB =
            CREATIVE_TABS.register("neko_tab", () -> CreativeModeTab.builder()
                    // 标签页标题,语言键在 lang 文件里定义
                    .title(Component.translatable("itemGroup.neko_shampoo"))
                    // 标签页图标
                    .icon(() -> new ItemStack(ModItems.NEKO_SHAMPOO.get()))
                    // 标签页里展示的物品
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.NEKO_SHAMPOO.get());
                        output.accept(ModItems.NEKO_SHAMPOO_STRONG.get());
                    })
                    .build());

    private ModCreativeTab() {
    }
}
