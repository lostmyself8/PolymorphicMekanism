package com.jerry.polymek.compat.robit;

import com.jerry.polymek.common.recipe.RecipeTarget;
import java.util.Optional;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/** A menu-local workbench selection, deliberately isolated from another player's Robit menu. */
public interface RobitCraftingAccess extends RecipeTarget {

    /** Resolves both the crafting result and remainders against the same selected recipe. */
    Optional<RecipeHolder<CraftingRecipe>> polymek$craftingRecipe(CraftingInput input, Level level);
}