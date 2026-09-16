package com.neko_shampoo.event;

import com.neko_shampoo.entity.ai.FragrantFollowGoal;
import com.neko_shampoo.entity.ai.FragrantTemptGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局事件处理。
 * <p>
 * 这里用 NeoForge 的运行时事件总线(NeoForge.EVENT_BUS)监听游戏事件。
 * <p>
 * 核心功能:
 * - 野生豹猫(Ocelot)进入世界:给它挂上"香香软软"诱惑 Goal,
 *   带 buff 的玩家靠近时豹猫不害怕、还能直接喂鱼亲近。
 * - 普通猫(Cat)进入世界:给它挂上"香香软软"亲近 Goal,
 *   带 buff 的玩家靠近时猫会主动走近玩家。
 */
public final class ModEvents {

    private static final Set<UUID> OCELOTS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<UUID> CATS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Ocelot ocelot) {
            if (OCELOTS.add(ocelot.getUUID())) {
                // 高优先级:让"香香软软"的诱惑压过豹猫的躲避玩家行为
                ocelot.goalSelector.addGoal(2, new FragrantTemptGoal(ocelot));
            }
        } else if (event.getEntity() instanceof Cat cat) {
            if (CATS.add(cat.getUUID())) {
                // 家猫:优先级高于它的"躲避玩家"(4),带 buff 时猫走近而不逃跑
                cat.goalSelector.addGoal(2, new FragrantFollowGoal(cat));
            }
        }
    }

    private ModEvents() {
    }
}
