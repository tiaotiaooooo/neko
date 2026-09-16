package com.neko_shampoo.entity.ai;

import com.neko_shampoo.effect.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * "香香软软"亲近目标(通用版本)。
 * <p>
 * 适用于普通猫(Cat)等原版本来就不怕玩家的生物:
 * 当附近有带着"香香软软" buff 的玩家时,主动走向这位玩家,
 * 表现出"小猫亲近香香的人"的效果。
 */
public class FragrantFollowGoal extends Goal {

    private static final double RANGE = 8.0D;
    private static final double STOP_RANGE = 2.25D;
    private static final double SPEED = 0.7D;
    private static final int MAX_TICKS = 200;

    private final PathfinderMob mob;
    private LivingEntity targetSource;
    private int ticks;

    public FragrantFollowGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 已驯服/有主人/正在坐下的动物都不被吸引,避免影响它坐下或跟随主人
        if (this.mob instanceof TamableAnimal tame) {
            if (tame.isTame() || tame.getOwnerUUID() != null || tame.isOrderedToSit()) {
                return false;
            }
        }
        this.targetSource = findFragrantSource();
        return this.targetSource != null;
    }

    @Override
    public boolean canContinueToUse() {
        // 中途被驯服/坐下也要及时停止
        if (this.mob instanceof TamableAnimal tame) {
            if (tame.isTame() || tame.getOwnerUUID() != null || tame.isOrderedToSit()) {
                return false;
            }
        }
        if (this.targetSource == null || !this.targetSource.isAlive()) return false;
        if (!this.targetSource.hasEffect(ModEffects.FRAGRANT)) return false;
        if (this.mob.distanceToSqr(this.targetSource) > RANGE * RANGE * 4.0D) return false;
        return this.ticks < MAX_TICKS;
    }

    @Override
    public void start() { this.ticks = 0; }

    @Override
    public void stop() {
        this.targetSource = null;
        this.ticks = 0;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticks++;
        this.mob.getLookControl().setLookAt(this.targetSource, 30.0F, 30.0F);
        if (this.mob.distanceToSqr(this.targetSource) > STOP_RANGE) {
            this.mob.getNavigation().moveTo(this.targetSource, SPEED);
        }
    }

    /**
     * 找附近"带香香软软 buff"的生灵(不限于玩家)。
     * <p>
     * 优先级规则:香香软软 II(amplifier=1,强效版洗面奶给的) > 香香软软 I(amplifier=0)。
     * 也就是说,只要附近有强效 buff 的生物,猫就优先走向它;
     * 没有强效 buff 时,才走向带普通 buff 的最近目标。
     * 这样猫不会被其他"普通香香"的生物抢走,会优先找最强的那个。
     */
    private LivingEntity findFragrantSource() {
        if (this.mob.level() instanceof ServerLevel serverLevel) {
            // 记录两个档位里各自最近的候选
            LivingEntity bestAmplifier1 = null; // 强效(香香软软 II)
            double bestDist1 = RANGE * RANGE;
            LivingEntity bestAmplifier0 = null; // 普通(香香软软 I)
            double bestDist0 = RANGE * RANGE;

            java.util.List<LivingEntity> candidates = new java.util.ArrayList<>();
            // 先加所有玩家
            candidates.addAll(serverLevel.players());
            // 再加范围内其他带 buff 的动物(投掷溅射到的实体)
            candidates.addAll(serverLevel.getEntitiesOfClass(LivingEntity.class,
                    this.mob.getBoundingBox().inflate(RANGE), e -> e != this.mob));

            for (LivingEntity e : candidates) {
                net.minecraft.world.effect.MobEffectInstance inst = e.getEffect(ModEffects.FRAGRANT);
                if (inst == null) continue; // 没带香香软软就跳过
                double d = this.mob.distanceToSqr(e);
                if (inst.getAmplifier() >= 1) {
                    // 强效档
                    if (d < bestDist1) { bestDist1 = d; bestAmplifier1 = e; }
                } else {
                    // 普通档
                    if (d < bestDist0) { bestDist0 = d; bestAmplifier0 = e; }
                }
            }
            // 强效优先;没有强效就退回普通
            return bestAmplifier1 != null ? bestAmplifier1 : bestAmplifier0;
        }
        return null;
    }
}
