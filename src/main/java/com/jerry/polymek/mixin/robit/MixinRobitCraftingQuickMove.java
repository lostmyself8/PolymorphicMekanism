package com.jerry.polymek.mixin.robit;

import com.illusivesoulworks.polymorph.mixin.core.AccessorCraftingMenu;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the same current-recipe validation to native Robit Shift crafting and direct menu calls.
 */
@Mixin(CraftingMenu.class)
public abstract class MixinRobitCraftingQuickMove {

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void polymek$refreshRobitResultBeforeQuickMove(Player player, int slot,
                                                           CallbackInfoReturnable<ItemStack> callback) {
        if (slot == 0 && (Object) this instanceof CraftingRobitContainer crafting) {
            crafting.slotsChanged(((AccessorCraftingMenu) crafting).getCraftSlots());
        }
    }
}