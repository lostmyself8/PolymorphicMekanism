package com.jerry.polymek.common.network;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Carries only a choice and its menu context; matching inputs are always resolved on the server. */
@NothingNullByDefault
public record RecipeSelectPayload(int containerId, RecipeTargetKey target, ResourceLocation recipe) implements CustomPacketPayload {

    public static final Type<RecipeSelectPayload> TYPE = new Type<>(PolymorphicMekanism.rl("recipe_select"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSelectPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeVarInt(value.containerId);
                buffer.writeResourceLocation(value.target.kind());
                buffer.writeVarInt(value.target.index());
                buffer.writeResourceLocation(value.recipe);
            }, buffer -> new RecipeSelectPayload(buffer.readVarInt(), new RecipeTargetKey(buffer.readResourceLocation(), buffer.readVarInt()), buffer.readResourceLocation()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
