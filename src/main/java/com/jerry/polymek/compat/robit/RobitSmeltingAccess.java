package com.jerry.polymek.compat.robit;

import com.jerry.polymek.common.recipe.RecipeTarget;
import org.jetbrains.annotations.Nullable;

/** Entity-owned selection access used to preserve a Robit's furnace when it becomes an item. */
public interface RobitSmeltingAccess extends RecipeTarget {

    /** Returns the explicit locked selection, or null when the furnace uses its ordinary recipe lookup. */
    @Nullable RobitSmeltingChoice polymek$smeltingChoice();

    /** Restores a saved item selection before the new Robit enters the world. */
    void polymek$restoreSmeltingChoice(RobitSmeltingChoice choice);
}
