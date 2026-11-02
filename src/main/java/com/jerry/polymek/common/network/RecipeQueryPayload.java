package com.jerry.polymek.common.network;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests current choices without naming or accessing any world target outside the open menu. */
@NothingNullByDefault
public record RecipeQueryPayload(int containerId, RecipeTargetKey target) implements CustomPacketPayload {

    public static final Type<RecipeQueryPayload> TYPE = new Type<>(PolymorphicMekanism.rl("recipe_query"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeQueryPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeVarInt(value.containerId);
                buffer.writeResourceLocation(value.target.kind());
                buffer.writeVarInt(value.target.index());
            }, buffer -> new RecipeQueryPayload(buffer.readVarInt(), new RecipeTargetKey(buffer.readResourceLocation(), buffer.readVarInt())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
