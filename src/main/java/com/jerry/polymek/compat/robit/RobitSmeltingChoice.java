package com.jerry.polymek.compat.robit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Persists the exact Robit smelting recipe together with the material it was selected for. */
public record RobitSmeltingChoice(ResourceLocation recipe, ItemStack input) {

    public static final Codec<RobitSmeltingChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ResourceLocation.CODEC.fieldOf("recipe").forGetter(RobitSmeltingChoice::recipe),
          ItemStack.SINGLE_ITEM_CODEC.fieldOf("input").forGetter(RobitSmeltingChoice::input)
    ).apply(instance, RobitSmeltingChoice::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RobitSmeltingChoice> STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, RobitSmeltingChoice::recipe,
          ItemStack.STREAM_CODEC, RobitSmeltingChoice::input,
          RobitSmeltingChoice::new);

    public RobitSmeltingChoice {
        Objects.requireNonNull(recipe, "A Robit smelting choice requires a recipe ID");
        if (input.isEmpty()) {
            throw new IllegalArgumentException("A Robit smelting choice requires a material");
        }
        input = input.copyWithCount(1);
    }

    @Override
    public ItemStack input() {
        return input.copy();
    }

    /** Counts may change during processing; changing the material starts a different selection context. */
    public boolean matchesMaterial(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(input, stack);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RobitSmeltingChoice choice && recipe.equals(choice.recipe)
              && ItemStack.isSameItemSameComponents(input, choice.input);
    }

    @Override
    public int hashCode() {
        return 31 * recipe.hashCode() + ItemStack.hashItemAndComponents(input);
    }
}
