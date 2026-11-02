package com.jerry.polymek.mixin.robit;

import com.illusivesoulworks.polymorph.mixin.core.AccessorCraftingMenu;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Revalidates a Robit result immediately before a player can take it after a recipe reload.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class RobitMenuClickMixin {

    @Inject(method = "clicked", at = @At("HEAD"))
    private void polymek$refreshRobitResultBeforeClick(int slot, int button, ClickType clickType,
                                                       Player player, CallbackInfo callback) {
        if (slot == 0 && (Object) this instanceof CraftingRobitContainer crafting) {
            crafting.slotsChanged(((AccessorCraftingMenu) crafting).getCraftSlots());
        }
    }
}