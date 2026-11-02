package com.jerry.polymek.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.client.assemblicator.AssemblicatorRecipesWidget;
import com.jerry.polymek.compat.qio.QioIntegration;
import com.jerry.polymek.compat.robit.RobitIntegration;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Isolates screen and picker registration from the dedicated-server class loader. */
@Mod(value = PolymorphicMekanism.MOD_ID, dist = Dist.CLIENT)
public final class PolymekClient {

    public PolymekClient(IEventBus bus) {
        bus.addListener(this::setup);
    }

    private void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PolymorphWidgets.getInstance().registerWidget(screen -> screen.getMenu() instanceof FormulaicAssemblicatorContainer menu
                    ? new AssemblicatorRecipesWidget(screen, menu) : null);
            QioIntegration.registerClient();
            RobitIntegration.registerClient();
        });
    }
}