package com.jerry.polymek.mixin.robit;

import com.illusivesoulworks.polymorph.common.PolymorphRecipeManager;
import com.jerry.polymek.compat.robit.RobitCraftingTarget;

import java.util.List;
import java.util.Optional;

import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes Polymorph's inherited vanilla workbench hook to the current Robit menu's own selection.
 */
@Mixin(value = PolymorphRecipeManager.class, remap = false)
public abstract class MixinPolymorphRobitCrafting {

    @Inject(method = "getPlayerRecipe", at = @At("HEAD"), cancellable = true)
    private void polymek$resolveRobitWorkbench(AbstractContainerMenu containerMenu, RecipeType<?> type,
                                               RecipeInput inventory, Level level, Player player, List<?> recipes,
                                               CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> callback) {
        if (containerMenu instanceof CraftingRobitContainer && type == RecipeType.CRAFTING && inventory instanceof CraftingInput crafting) {
            callback.setReturnValue(((RobitCraftingTarget) containerMenu).polymek$craftingRecipe(crafting, level));
        }
    }
}