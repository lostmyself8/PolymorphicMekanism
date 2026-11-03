package com.jerry.polymek.mixin.qio;

import com.jerry.polymek.common.registries.PolymekDataComponents;
import com.jerry.polymek.compat.qio.QIOCraftingTarget;
import com.jerry.polymek.compat.qio.QIOSelectionStorage;
import mekanism.common.content.qio.PortableQIODashboardInventory;
import mekanism.common.content.qio.QIOCraftingWindow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps portable dashboard choices on the actual dashboard stack across menu recreation and world saves.
 */
@Mixin(value = PortableQIODashboardInventory.class, remap = false)
public abstract class MixinPortableQIODashboard implements QIOSelectionStorage {

    @Shadow
    @Final
    private ItemStack stack;
    @Shadow
    @Final
    private QIOCraftingWindow[] craftingWindows;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymek$restoreChoices(@Nullable Level level, ItemStack stack, CallbackInfo callback) {
        for (int index = 0; index < craftingWindows.length; index++) {
            ((QIOCraftingTarget) craftingWindows[index]).polymek$restoreRecipe(stack.get(PolymekDataComponents.qioRecipeComponent(index)));
            craftingWindows[index].invalidateRecipe();
        }
    }

    @Override
    public void polymek$storeRecipe(int index, @Nullable ResourceLocation recipeId) {
        if (recipeId == null) {
            stack.remove(PolymekDataComponents.qioRecipeComponent(index));
        } else {
            stack.set(PolymekDataComponents.qioRecipeComponent(index), recipeId);
        }
    }
}