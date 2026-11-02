package com.jerry.polymek.compat.qio;

import com.jerry.polymek.common.recipe.RecipeTarget;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Keeps one QIO workbench's recipe choice attached to its native inventory rather than to a player. */
public interface QioCraftingTarget extends RecipeTarget {

    /** Restores a saved choice before native recipe recalculation, without writing the inventory again. */
    void polymek$restoreRecipe(@Nullable ResourceLocation recipeId);
}