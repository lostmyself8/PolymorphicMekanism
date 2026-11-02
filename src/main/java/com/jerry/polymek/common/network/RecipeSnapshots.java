package com.jerry.polymek.common.network;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/** Keeps packet registration independent of client-only Minecraft classes on a dedicated server. */
public final class RecipeSnapshots {

    private static WeakReference<Consumer<RecipeSnapshot>> receiver = new WeakReference<>(null);

    private RecipeSnapshots() {
    }

    /** Installs a weak callback; the active widget owns its callback strongly until that widget closes. */
    public static void receiveWith(Consumer<RecipeSnapshot> receiverIn) {
        receiver = new WeakReference<>(receiverIn);
    }

    /** Delivers a client packet; packets received after a screen closes are ignored by its receiver. */
    public static void accept(RecipeSnapshot snapshot) {
        Consumer<RecipeSnapshot> activeReceiver = receiver.get();
        if (activeReceiver != null) {
            activeReceiver.accept(snapshot);
        }
    }
}