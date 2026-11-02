package com.jerry.polymek.common.recipe;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

/** Resolves a context only when the player's open native menu really owns and authorizes that target. */
@FunctionalInterface
public interface RecipeTargetFactory {

    /** Validates menu type, index, world, and target relationship; null explicitly rejects this target. */
    @Nullable RecipeTarget resolve(ServerPlayer player, AbstractContainerMenu menu, int index);
}
