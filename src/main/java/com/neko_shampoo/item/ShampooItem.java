package com.neko_shampoo.item;

import com.neko_shampoo.effect.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 猫娘洗面奶物品。
 * <p>
 * "进食式"使用:按住右键约 1 秒(会显示喝/用道具的进度条动画),
 * 时间走完后才会起效:
 * 1. 播放"喵~"的猫叫声音效
 * 2. 清除身上所有负面效果(中毒/虚弱/缓慢等)
 * 3. 获得"香香软软" buff(持续 180 秒)
 * <p>
 * amplifier = 0 是普通版;= 1 是强效版(香香软软 II,吸引等级更高)。
 * <p>
 * 每次使用不消耗洗面奶(消耗耐久,30 次后损坏消失)。
 */
public class ShampooItem extends Item {

    // 使用时间(ticks):1 秒 = 20 ticks,按 1 秒用完
    private static final int USE_DURATION = 20;
    // "香香软软"持续时长(ticks),60 秒 = 20 * 60
    private static final int EFFECT_DURATION = 20 * 180;
    // 效果等级:0 = 香香软软 I(普通),1 = 香香软软 II(强效)
    private final int amplifier;

    public ShampooItem(Properties properties, int amplifier) {
        super(properties);
        this.amplifier = amplifier;
    }

    /** 返回这个物品会带来的香香软软等级(0 或 1) */
    public int getShampooAmplifier() {
        return this.amplifier;
    }

    /**
     * 鼠标悬停提示文字。
     * 在物品名称下方显示一段说明。
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // 非快捷键查看时正常显示说明
        if (!tooltipFlag.hasShiftDown()) {
            if (this.amplifier >= 1) {
                // 强效版
                tooltipComponents.add(Component.literal("哇，还有升级版！用完好像会更香香软软喵~"));
            } else {
                tooltipComponents.add(Component.literal("会让你变得香香软软的喵~"));
            }
        }
    }

    /**
     * 右键互动。
     * - 普通洗面奶:蹲下 + 右键 = 投掷;普通右键 = 洗脸
     * - 强效洗面奶:不能投掷,蹲下 + 右键 和 普通右键 都 = 洗脸
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 只有普通版(amplifier=0)允许投掷;强效版(amplifier>=1)不能扔
        boolean canThrow = player.isShiftKeyDown() && this.amplifier == 0;
        if (canThrow) {
            // 蹲下+右键 = 投掷
            if (!level.isClientSide()) {
                com.neko_shampoo.entity.ShampooProjectile projectile =
                        new com.neko_shampoo.entity.ShampooProjectile(level, player);
                projectile.setItem(stack.copy());
                // 告诉投掷物:这个洗面奶是哪一级(决定落地溅射给谁的香香软软等级)
                projectile.setShampooAmplifier(this.amplifier);
                // 力度:速度 0.8F(原来是 1.5F 扔得太远),0 偏移,误差 1.0F
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.8F, 1.0F);
                level.addFreshEntity(projectile);
                // 非创造模式:投掷后消耗 1 个;创造模式不消耗
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        // 普通右键:开始"洗脸"使用动画
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /** 使用动画走完时(1 秒后)真正生效 */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            // 1. 播放"喵~"猫叫
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.CAT_AMBIENT,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            // 2. 清除负面效果
            clearNegativeEffects(serverPlayer);
            // 3. 给予"香香软软"(等级由 amplifier 决定:0=普通,1=强效)
            serverPlayer.addEffect(new MobEffectInstance(ModEffects.FRAGRANT, EFFECT_DURATION, this.amplifier));
            // 4. 消耗 1 次耐久(30 次用完后物品损坏消失)
            stack.hurtAndBreak(1, serverPlayer, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        return stack;
    }

    /** 使用所需时间(进食式, 1 秒) */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    /** 使用时的动画:用"喝/用"的动画,让玩家看到进度条 */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    /**
     * 清除玩家身上所有"伤害类"(负面)效果。
     * "香香软软"洗脸只洗掉坏效果:
     * - HARMFUL(如中毒/虚弱/缓慢/凋零)会被清除
     * - BENEFICIAL(增益)与 NEUTRAL(中性,如夜视/隐身)保留
     */
    private void clearNegativeEffects(ServerPlayer player) {
        List<MobEffectInstance> negative = player.getActiveEffects().stream()
                .filter(ei -> ei.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
                .toList();

        for (MobEffectInstance ei : negative) {
            player.removeEffect(ei.getEffect());
        }
    }
}
