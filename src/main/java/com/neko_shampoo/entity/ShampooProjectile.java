package com.neko_shampoo.entity;

import com.neko_shampoo.effect.ModEffects;
import com.neko_shampoo.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * 投掷出去的洗面奶。
 * <p>
 * 蹲下+右键时,把洗面奶像雪球一样扔出去。
 * 命中/落地后:在附近产生范围效果(给命中的生物加"香香软软"+清除负面),
 * 并播放碎裂声音,然后投掷物消失。
 */
public class ShampooProjectile extends ThrowableItemProjectile {

    // 效果作用半径(格)
    private static final double EFFECT_RADIUS = 3.0D;
    // "香香软软"持续时长(ticks);1 分半 = 90 秒 = 20 * 90
    private static final int EFFECT_DURATION = 20 * 90;

    // 这个投掷物扔出去后给命中的生物带来哪个等级的"香香软软":
    // 0 = 普通(I),1 = 强效(II)。由 ShampooItem.use 在投掷时设置。
    private int shampooAmplifier = 0;

    public ShampooProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public ShampooProjectile(Level level, LivingEntity owner) {
        super(ModEntities.SHAMPOO_PROJECTILE.get(), owner, level);
    }

    /** 设置落地溅射时给目标的香香软软等级(0=普通,1=强效) */
    public void setShampooAmplifier(int amplifier) {
        this.shampooAmplifier = amplifier;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.NEKO_SHAMPOO.get();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        // 服务端负责声音 + 触发原版"喷溅药水"粒子 + 范围效果。
        // 粒子用 levelEvent(2002) 广播给客户端播放原版药水喷洒特效(带颜色),
        // 跟原版喷溅药水(ThrownPotion)一模一样的做法,最可靠。
        if (this.level().isClientSide) {
            return;
        }
        // 碎裂声音
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        // 触发"喷溅药水"世界粒子事件:级别 2002 = 普通药水喷洒。
        // 第 3 个参数是粒子颜色(ARGB int):普通版鲜粉红,强效版玫红。
        int color = this.shampooAmplifier >= 1 ? 0xFF1493 : 0xFF69B4;
        this.level().levelEvent(2002, this.blockPosition(), color);
        // 范围效果:给半径内生物加香香软软 + 清负面
        AABB area = new AABB(this.getX() - EFFECT_RADIUS, this.getY() - EFFECT_RADIUS, this.getZ() - EFFECT_RADIUS,
                this.getX() + EFFECT_RADIUS, this.getY() + EFFECT_RADIUS, this.getZ() + EFFECT_RADIUS);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : targets) {
            // 清除负面
            clearNegative(target);
            // 加香香软软(等级由投掷时的洗面奶决定:0=普通,1=强效)
            target.addEffect(new MobEffectInstance(ModEffects.FRAGRANT, EFFECT_DURATION, this.shampooAmplifier));
        }
        // 投掷物消失
        this.discard();
    }

    private void clearNegative(LivingEntity entity) {
        entity.getActiveEffects().stream()
                .filter(ei -> ei.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
                .toList()
                .forEach(ei -> entity.removeEffect(ei.getEffect()));
    }
}