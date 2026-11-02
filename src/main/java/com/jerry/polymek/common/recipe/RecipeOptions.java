package com.jerry.polymek.common.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Produces authoritative previews by assembling recipes against actual inputs, including component-sensitive recipes. */
public final class RecipeOptions {

    private RecipeOptions() {
    }

    /** Enumerates the complete currently matching set; empty outputs cannot be selected or crafted. */
    public static <I extends RecipeInput, R extends Recipe<I>> List<RecipeOption> matching(Level level, RecipeType<R> type, I input) {
        return level.getRecipeManager().getRecipesFor(type, input, level).stream()
                .filter(holder -> holder.value().isSpecial() || !holder.value().isIncomplete()).map(holder -> {
            ItemStack output = holder.value().assemble(input, level.registryAccess());
            return output.isEmpty() ? null : new RecipeOption(holder.id(), output);
        }).filter(java.util.Objects::nonNull).toList();
    }
}