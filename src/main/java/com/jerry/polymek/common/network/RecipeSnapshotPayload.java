package com.jerry.polymek.common.network;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Synchronizes complete choices and selection after authoritative validation and native cache refresh. */
@NothingNullByDefault
public record RecipeSnapshotPayload(RecipeSnapshot snapshot) implements CustomPacketPayload {

    public static final Type<RecipeSnapshotPayload> TYPE = new Type<>(PolymorphicMekanism.rl("recipe_snapshot"));
    private static final int MAX_INPUTS = 10;
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSnapshotPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                RecipeSnapshot value = packet.snapshot;
                buffer.writeVarInt(value.containerId());
                buffer.writeResourceLocation(value.target().kind());
                buffer.writeVarInt(value.target().index());
                List<ItemStack> inputs = value.inputs();
                buffer.writeVarInt(inputs.size());
                for (ItemStack input : inputs) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, input);
                }
                buffer.writeVarInt(value.options().size());
                for (RecipeOption option : value.options()) {
                    buffer.writeResourceLocation(option.id());
                    ItemStack.STREAM_CODEC.encode(buffer, option.output());
                }
                buffer.writeOptional(value.selected(), FriendlyByteBuf::writeResourceLocation);
                buffer.writeBoolean(value.editable());
            }, buffer -> {
                int containerId = buffer.readVarInt();
                RecipeTargetKey key = new RecipeTargetKey(buffer.readResourceLocation(), buffer.readVarInt());
                int inputCount = buffer.readVarInt();
                if (inputCount < 0 || inputCount > MAX_INPUTS) {
                    throw new IllegalArgumentException("Invalid recipe snapshot input count: " + inputCount);
                }
                List<ItemStack> inputs = new ArrayList<>(inputCount);
                for (int i = 0; i < inputCount; i++) {
                    inputs.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                }
                int size = buffer.readVarInt();
                if (size < 0 || size > 65536) {
                    throw new IllegalArgumentException("Invalid recipe snapshot size: " + size);
                }
                List<RecipeOption> options = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    options.add(new RecipeOption(buffer.readResourceLocation(), ItemStack.STREAM_CODEC.decode(buffer)));
                }
                Optional<ResourceLocation> selected = buffer.readOptional(FriendlyByteBuf::readResourceLocation);
                return new RecipeSnapshotPayload(new RecipeSnapshot(containerId, key, inputs, options, selected, buffer.readBoolean()));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}