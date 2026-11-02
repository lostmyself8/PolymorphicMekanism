package com.jerry.polymek.mixin.robit;

import com.jerry.polymek.compat.robit.RobitCraftingAccess;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Uses the selected recipe's remainders when vanilla ResultSlot consumes a Robit crafting grid.
 */
@Mixin(ResultSlot.class)
public abstract class RobitResultSlotMixin {

    @Shadow
    @Final
    private Player player;

    @Redirect(method = "onTake", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRemainingItemsFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;"))
    private NonNullList<ItemStack> polymek$selectedRobitRemainders(RecipeManager recipes,
                                                                   RecipeType<CraftingRecipe> type, RecipeInput input, Level level) {
        if (player.containerMenu instanceof CraftingRobitContainer && input instanceof CraftingInput crafting) {
            return ((RobitCraftingAccess) player.containerMenu).polymek$craftingRecipe(crafting, level)
                    .map(holder -> holder.value().getRemainingItems(crafting))
                    .orElseGet(() -> NonNullList.withSize(crafting.size(), ItemStack.EMPTY));
        }
        return recipes.getRemainingItemsFor(type, (CraftingInput) input, level);
    }
}