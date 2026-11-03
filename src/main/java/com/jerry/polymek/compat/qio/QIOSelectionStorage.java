package com.jerry.polymek.compat.qio;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Persists the independently selected recipes of a block or portable QIO dashboard. */
public interface QIOSelectionStorage {

    /** Saves a changed workbench choice on the owning dashboard, marking only an actual edit dirty. */
    void polymek$storeRecipe(int index, @Nullable ResourceLocation recipeId);
}