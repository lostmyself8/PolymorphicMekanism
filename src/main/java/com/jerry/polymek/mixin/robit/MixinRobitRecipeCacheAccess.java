package com.jerry.polymek.mixin.robit;

import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Clears a Robit's real Mekanism processing cache when its selected furnace recipe changes.
 */
@Mixin(value = RecipeCacheLookupMonitor.class, remap = false)
public interface MixinRobitRecipeCacheAccess {

    /**
     * Drops the previous processing recipe so an unchanged material can select another output.
     */
    @Accessor("cachedRecipe")
    void polymek$setCachedRecipe(CachedRecipe<?> recipe);

    /**
     * Allows lookup to resume after an earlier selected recipe stopped matching.
     */
    @Accessor("hasNoRecipe")
    void polymek$setHasNoRecipe(boolean hasNoRecipe);

    /**
     * Makes the new processing cache reevaluate its ordinary Mekanism errors.
     */
    @Accessor("shouldUnpause")
    void polymek$setShouldUnpause(boolean shouldUnpause);
}