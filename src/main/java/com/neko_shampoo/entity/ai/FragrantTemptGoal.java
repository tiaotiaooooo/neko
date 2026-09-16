package com.neko_shampoo.entity.ai;

import com.neko_shampoo.effect.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * "香香软软"诱惑目标。
 * <p>
 * 给野生豹猫(Ocelot)挂上这个 Goal:
 * - 当附近有身上带着"香香软软" buff 的玩家时,豹猫不再被吓跑,
 *   而是主动靠近这位玩家(就像被猫咪喜欢的气味吸引)。
 * - 若该玩家手上正好拿着生鱼(生鳕鱼/生鲑鱼),豹猫会被更强烈地吸引,
 *   靠近后把豹猫设置为"信任"状态,相当于可以被直接喂食亲近/驯服。
 * <p>
 * 用高优先级挂在豹猫 goalSelector 上,使它盖过原版的"躲避玩家"行为。
 */
public class FragrantTemptGoal extends Goal {

    // 生效半径(格)
    private static final double RANGE = 10.0D;
    // 豹猫靠近玩家的速度
    private static final double SPEED = 0.6D;
    // 多少 tick 没追上玩家就放弃一次(防卡死)
    private static final int MAX_TICKS = 200;

    private final Ocelot ocelot;
    private ServerPlayer targetPlayer;
    private int ticks;

    public FragrantTemptGoal(Ocelot ocelot) {
        this.ocelot = ocelot;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** 附近有带"香香软软"的玩家就激活(这时豹猫不逃,反而想靠近) */
    @Override
    public boolean canUse() {
        this.targetPlayer = findFragrantPlayer();
        return this.targetPlayer != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }
        // buff 消失或玩家走太远则结束
        if (!this.targetPlayer.hasEffect(ModEffects.FRAGRANT)) {
            return false;
        }
        if (this.ocelot.distanceToSqr(this.targetPlayer) > RANGE * RANGE * 4.0D) {
            return false;
        }
        return this.ticks < MAX_TICKS;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.ticks = 0;
        this.ocelot.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticks++;
        this.ocelot.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

        // 靠近一点
        if (this.ocelot.distanceToSqr(this.targetPlayer) > 2.0D) {
            this.ocelot.getNavigation().moveTo(this.targetPlayer, SPEED);
        }

        // 如果玩家拿着能喂豹猫的生鱼,豹猫走到跟前时对视/亲近并提升信任
        if (this.ocelot.distanceToSqr(this.targetPlayer) <= 2.5D && isTemptItem(this.targetPlayer.getMainHandItem())) {
            // 豹猫被吸引后:设置为信任状态,不再逃跑(近似"可直接喂食亲近")
            this.ocelot.setTrusting(true);
        }
    }

    /** 玩家手持的吸引物:生鳕鱼 / 生鲑鱼 之一 */
    private boolean isTemptItem(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.COD) || stack.is(Items.SALMON));
    }

    /** 找 10 格内最近的、带着"香香软软"的效果的玩家 */
    private ServerPlayer findFragrantPlayer() {
        if (!(this.ocelot.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        ServerPlayer best = null;
        double bestDist = RANGE * RANGE;
        for (ServerPlayer sp : serverLevel.players()) {
            if (!sp.hasEffect(ModEffects.FRAGRANT)) {
                continue;
            }
            double d = this.ocelot.distanceToSqr(sp);
            if (d < bestDist) {
                bestDist = d;
                best = sp;
            }
        }
        return best;
    }
}
