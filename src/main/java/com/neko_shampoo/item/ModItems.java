package com.neko_shampoo.item;

import com.neko_shampoo.McKaifa;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册器。
 * <p>
 * 一个物品 = 可以在背包/物品栏里出现的东西。
 * DeferredRegister 是 NeoForge 提供的"延迟注册"工具,
 * 我们在这里"申报"物品,游戏启动时再真正注册进去。
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(McKaifa.MODID);

    // 猫娘洗面奶:右键使用后清除负面效果并获得"香香软软" buff。
    // 30 次耐久(每次使用消耗 1 点),耐久耗尽后物品损坏消失。
    // 蹲下 + 右键可投掷(见 ShampooItem)
    // amplifier=0:普通版,给"香香软软 I"
    public static final DeferredItem<ShampooItem> NEKO_SHAMPOO = ITEMS.register("neko_shampoo",
            () -> new ShampooItem(new Item.Properties().stacksTo(1).durability(30), 0));

    // 猫娘洗面奶(强效版):由普通洗面奶 + 牛奶桶合成。
    // amplifier=1:给"香香软软 II",对猫的吸引等级最高。
    // 同样 30 次耐久,用法和普通版一样。
    public static final DeferredItem<ShampooItem> NEKO_SHAMPOO_STRONG = ITEMS.register("neko_shampoo_strong",
            () -> new ShampooItem(new Item.Properties().stacksTo(1).durability(30), 1));

    private ModItems() {
    }
}
