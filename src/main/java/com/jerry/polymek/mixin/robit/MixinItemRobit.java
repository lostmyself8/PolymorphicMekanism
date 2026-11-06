package com.jerry.polymek.mixin.robit;

import com.jerry.polymek.common.registries.PolymekDataComponents;
import com.jerry.polymek.compat.robit.RobitSmeltingTarget;
import com.jerry.polymek.compat.robit.RobitSmeltingChoice;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.common.item.ItemRobit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Restores the furnace's item component before the placed Robit begins ticking.
 */
@Mixin(value = ItemRobit.class, remap = false)
public abstract class MixinItemRobit {

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean polymek$restorePlacedSmeltingChoice(Level level, Entity entity, Operation<Boolean> original, UseOnContext context) {
        RobitSmeltingChoice choice = context.getItemInHand().get(PolymekDataComponents.ROBIT_SMELTING_CHOICE.get());
        if (choice != null) {
            ((RobitSmeltingTarget) entity).polymek$restoreSmeltingChoice(choice);
        }
        return original.call(level, entity);
    }
}