package com.jerry.polymek.common.network;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.recipe.RecipeTarget;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import com.jerry.polymek.common.recipe.RecipeTargets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import mekanism.common.inventory.container.ISecurityContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.Nullable;

/** Validates access and current inputs in one server task before permitting any native recipe mutation. */
public final class RecipeNetwork {

    private static final Map<ServerPlayer, QueryStamp> LAST_QUERY = new WeakHashMap<>();

    private RecipeNetwork() {
    }

    /** Registers common codecs and handlers without loading any client-side screen classes. */
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PolymorphicMekanism.MOD_ID).versioned("2");
        registrar.playToServer(RecipeQueryPayload.TYPE, RecipeQueryPayload.STREAM_CODEC, (packet, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                query(player, packet);
            }
        }));
        registrar.playToServer(RecipeSelectPayload.TYPE, RecipeSelectPayload.STREAM_CODEC, (packet, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                select(player, packet);
            }
        }));
        registrar.playToClient(RecipeSnapshotPayload.TYPE, RecipeSnapshotPayload.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> RecipeSnapshots.accept(packet.snapshot())));
    }

    /** Retrieves a target only through the sender's still-open, authorized native menu. */
    public static @Nullable RecipeTarget resolve(ServerPlayer player, int containerId, RecipeTargetKey key) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != containerId || !menu.stillValid(player)
                || menu instanceof ISecurityContainer secured && !secured.canPlayerAccess(player)) {
            PolymorphicMekanism.LOGGER.warn("Rejected recipe context {} in menu {} for {}", key, containerId, player.getUUID());
            return null;
        }
        RecipeTarget target = RecipeTargets.resolve(player, menu, key);
        if (target == null) {
            PolymorphicMekanism.LOGGER.warn("Rejected unknown or unauthorized recipe target {} in menu {} for {}", key, containerId, player.getUUID());
        }
        return target;
    }

    private static void query(ServerPlayer player, RecipeQueryPayload packet) {
        RecipeTarget target = resolve(player, packet.containerId(), packet.target());
        if (target == null) {
            return;
        }
        List<ItemStack> inputs = target.polymek$inputStacks();
        QueryStamp stamp = LAST_QUERY.get(player);
        long tick = player.serverLevel().getGameTime();
        if (stamp != null && stamp.containerId == packet.containerId() && stamp.target.equals(packet.target())
                && ItemStack.listMatches(stamp.inputs, inputs) && tick - stamp.tick < 2) {
            return;
        }
        LAST_QUERY.put(player, new QueryStamp(packet.containerId(), packet.target(), inputs, tick));
        sendSnapshot(player, packet.containerId(), packet.target(), target);
    }

    /** Applies only an option matching the real inputs now, never a previously sent client preview. */
    public static boolean select(ServerPlayer player, RecipeSelectPayload packet) {
        RecipeTarget target = resolve(player, packet.containerId(), packet.target());
        if (target == null) {
            return false;
        }
        List<RecipeOption> options = target.polymek$options();
        boolean accepted = target.polymek$canSelect() && options.stream().anyMatch(option -> option.id().equals(packet.recipe()));
        if (accepted) {
            target.polymek$selectRecipe(packet.recipe());
            player.containerMenu.broadcastChanges();
        } else {
            PolymorphicMekanism.LOGGER.warn("Rejected non-matching or locked recipe {} for target {} in menu {} for {}", packet.recipe(), packet.target(), packet.containerId(), player.getUUID());
        }
        sendSnapshot(player, packet.containerId(), packet.target(), target);
        return accepted;
    }

    private static void sendSnapshot(ServerPlayer player, int containerId, RecipeTargetKey key, RecipeTarget target) {
        RecipeSnapshot snapshot = new RecipeSnapshot(containerId, key, target.polymek$inputStacks(), target.polymek$options(),
                Optional.ofNullable(target.polymek$selectedRecipe()), target.polymek$canSelect());
        PacketDistributor.sendToPlayer(player, new RecipeSnapshotPayload(snapshot));
    }

    private record QueryStamp(int containerId, RecipeTargetKey target, List<ItemStack> inputs, long tick) {

        private QueryStamp {
            inputs = inputs.stream().map(ItemStack::copy).toList();
        }
    }
}
