package com.jerry.polymek.common.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

/** Registers explicit menu-to-target adapters and refuses unknown recipe contexts. */
public final class RecipeTargets {

    private static final Map<ResourceLocation, RecipeTargetFactory> FACTORIES = new HashMap<>();

    private RecipeTargets() {
    }

    /** Registers one context kind during common initialization; duplicate registrations are a programming error. */
    public static void register(ResourceLocation kind, RecipeTargetFactory factory) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(factory, "factory");
        if (FACTORIES.putIfAbsent(kind, factory) != null) {
            throw new IllegalStateException("Duplicate recipe target registration: " + kind);
        }
    }

    /** Uses the caller's actual menu and player rather than trusting a client-supplied world target. */
    public static @Nullable RecipeTarget resolve(ServerPlayer player, AbstractContainerMenu menu, RecipeTargetKey key) {
        RecipeTargetFactory factory = FACTORIES.get(key.kind());
        return factory == null ? null : factory.resolve(player, menu, key.index());
    }
}