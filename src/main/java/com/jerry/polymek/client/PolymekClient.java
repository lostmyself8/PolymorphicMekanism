package com.jerry.polymek.client;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.client.assemblicator.AssemblicatorClientIntegration;
import com.jerry.polymek.client.qio.QIOClientIntegration;
import com.jerry.polymek.client.robit.RobitClientIntegration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Isolates screen and picker registration from the dedicated-server class loader.
 */
@Mod(value = PolymorphicMekanism.MOD_ID, dist = Dist.CLIENT)
public final class PolymekClient {

    public PolymekClient(IEventBus bus) {
        bus.addListener(this::setup);
    }

    private void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            AssemblicatorClientIntegration.register();
            QIOClientIntegration.register();
            RobitClientIntegration.register();
        });
    }
}