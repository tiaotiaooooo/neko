package com.neko_shampoo.entity;

import com.neko_shampoo.McKaifa;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册器。
 * <p>
 * 注册投掷用的洗面奶实体(ShampooProjectile)。
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, McKaifa.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShampooProjectile>> SHAMPOO_PROJECTILE =
            ENTITY_TYPES.register("shampoo_projectile",
                    () -> EntityType.Builder.<ShampooProjectile>of((type, level) -> new ShampooProjectile(type, level),
                                    MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .build(McKaifa.MODID + ":shampoo_projectile"));

    private ModEntities() {
    }
}
