package com.neko_shampoo.effect;

import com.neko_shampoo.McKaifa;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 状态效果注册器。
 * <p>
 * 本模组自定义状态效果:"香香软软"。
 * 效果 = 用洗面奶洗脸后获得的一种 buff。
 * <p>
 * 作用:
 * 1. 自带"移除玩家当前负面效果"的逻辑(实际移除放在使用物品时做,这里只做效果本体)
 * 2. 带此效果的玩家靠近野生豹猫时,豹猫不会被吓跑,
 *    反而可以去喂生鱼直接驯服。
 */
public final class ModEffects {

    // 状态效果的注册表名字是 MOB_EFFECT
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, McKaifa.MODID);

    // 香香软软:始终不产生负面(是 buff),颜色用粉白色
    public static final DeferredHolder<MobEffect, MobEffect> FRAGRANT = EFFECTS.register("fragrant",
            () -> new FragrantEffect(MobEffectCategory.BENEFICIAL, 0xFFF0F4));

    private ModEffects() {
    }

    /**
     * "香香软软"效果的具体行为。
     * 这里暂时只做一个最朴素的 buff(颜色、图标、可存储状态)。
     * 关于"清除负面""猫不害怕"的具体逻辑,见:
     * - 清除负面:由使用洗面奶时的逻辑执行(见 ShampooItem.use)
     * - 猫不害怕:由 FragrantEffect 的 applyEffectTick 或实体 AI 事件处理
     */
    static class FragrantEffect extends MobEffect {

        protected FragrantEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
