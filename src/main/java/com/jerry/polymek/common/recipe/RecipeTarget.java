package com.jerry.polymek.common.recipe;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Adapts one native machine, entity, or crafting window without replacing its crafting mechanics. */
public interface RecipeTarget {

    /**
     * Captures the raw inputs used to bind a response to the client's current inventory view, including counts,
     * components, and empty slots. Slot order must match the corresponding client's inputStacks contract: the Robit
     * furnace has one input, QIO has nine row-major inputs, and the assemblicator has its formula followed by nine
     * row-major crafting inputs. Returned stacks are copies so later native inventory changes cannot alter the capture.
     */
    List<ItemStack> polymek$inputStacks();

    /** Recomputes matching recipes and actual outputs from the target's current server-side input. */
    List<RecipeOption> polymek$options();

    /** Returns this target's own selected recipe id, independently of other targets and players. */
    @Nullable ResourceLocation polymek$selectedRecipe();

    /** Prevents changing selections while the native target is locked, for example in automatic mode. */
    boolean polymek$canSelect();

    /** Applies an id already validated against current options and refreshes native recipe/output caches. */
    void polymek$selectRecipe(ResourceLocation id);
}
