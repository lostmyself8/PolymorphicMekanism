package com.jerry.polymek.client.recipe;

import com.illusivesoulworks.polymorph.api.client.base.AbstractRecipesWidget;
import com.illusivesoulworks.polymorph.api.client.base.ITickingRecipesWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.util.RecipePair;
import com.illusivesoulworks.polymorph.platform.Services;
import com.jerry.polymek.common.PolymekLang;
import com.jerry.polymek.common.network.RecipeQueryPayload;
import com.jerry.polymek.common.network.RecipeSelectPayload;
import com.jerry.polymek.common.network.RecipeSnapshot;
import com.jerry.polymek.common.network.RecipeSnapshots;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/** Reuses Polymorph's picker while scoping every request and response to one real native recipe context. */
public abstract class ContextualRecipesWidget extends AbstractRecipesWidget implements ITickingRecipesWidget {

    private final Consumer<RecipeSnapshot> snapshotReceiver = this::acceptSnapshot;
    private @Nullable RecipeTargetKey lastTarget;
    private List<ItemStack> lastInput = List.of();
    private boolean initialized;
    private boolean dirty = true;
    private boolean editable;
    private int ticksSinceQuery;

    protected ContextualRecipesWidget(AbstractContainerScreen<?> screen) {
        super(screen);
    }

    /** Returns null while this screen has no active recipe context, such as a closed QIO crafting window. */
    protected abstract @Nullable RecipeTargetKey targetKey();

    /** Supplies current input stacks and relevant item components; copies are kept to detect actual changes. */
    protected abstract List<ItemStack> inputStacks();

    /** Allows hosts to suppress presentation from live inputs without discarding their remembered recipe context. */
    protected boolean mayPresentPicker() {
        return true;
    }

    /** Closes the popup as well as hiding its button so suppressed views cannot keep or restore stale hit boxes. */
    protected final boolean enforcePickerPresentation() {
        if (!initialized) {
            return false;
        }
        if (mayPresentPicker()) {
            return true;
        }
        openButton.visible = false;
        selectionWidget.setActive(false);
        return false;
    }

    /** Prevents native views from presenting the previous window's snapshot before the new context is initialized. */
    protected final boolean hasPickerContext(RecipeTargetKey key) {
        return initialized && key.equals(lastTarget) && key.equals(targetKey());
    }

    /** Synchronizes both offsets and real hit boxes immediately; Polymorph's offset setters alone wait until render. */
    protected final void synchronizePickerPosition(int relativeX, int relativeY) {
        int left = Services.CLIENT_PLATFORM.getScreenLeft(containerScreen);
        int top = Services.CLIENT_PLATFORM.getScreenTop(containerScreen);
        openButton.setOffsets(relativeX, relativeY);
        openButton.setPosition(left + relativeX, top + relativeY);
        selectionWidget.setOffsets(relativeX + xOffset, relativeY + yOffset);
        selectionWidget.setPosition(left + relativeX + xOffset, top + relativeY + yOffset);
    }

    /** Draws only the picker contents at the caller's existing pose and depth, for native window ownership. */
    protected final void renderPickerContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Handles one picker click after its native owner has established position, focus, and hit testing. */
    protected final void clickPickerContents(double mouseX, double mouseY, int button) {
        if (editable) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void initChildWidgets() {
        super.initChildWidgets();
        initialized = true;
        RecipeSnapshots.receiveWith(snapshotReceiver);
        tick();
    }

    @Override
    public void tick() {
        if (!initialized || Minecraft.getInstance().screen != containerScreen) {
            return;
        }
        RecipeTargetKey key = targetKey();
        if (key == null) {
            openButton.visible = false;
            selectionWidget.setActive(false);
            lastTarget = null;
            return;
        }
        List<ItemStack> input = inputStacks();
        boolean contextChanged = !key.equals(lastTarget);
        if (contextChanged || !ItemStack.listMatches(lastInput, input)) {
            dirty = true;
            lastInput = input.stream().map(ItemStack::copy).toList();
        }
        if (contextChanged) {
            lastTarget = key;
            selectionWidget.setActive(false);
            super.setRecipesList(Set.of(), null);
        }
        resetWidgetOffsets();
        enforcePickerPresentation();
        ticksSinceQuery++;
        if (dirty || ticksSinceQuery >= 20) {
            PacketDistributor.sendToServer(new RecipeQueryPayload(containerScreen.getMenu().containerId, key));
            ticksSinceQuery = 0;
            dirty = false;
        }
    }

    /** The framework's unscoped packets may invalidate a view but cannot overwrite a contextual snapshot. */
    @Override
    public void setRecipesList(Set<IRecipePair> ignored, ResourceLocation selected) {
        dirty = true;
    }

    private void acceptSnapshot(RecipeSnapshot snapshot) {
        if (!initialized || Minecraft.getInstance().screen != containerScreen
                || snapshot.containerId() != containerScreen.getMenu().containerId
                || !Objects.equals(snapshot.target(), targetKey())) {
            return;
        }
        Set<IRecipePair> choices = new TreeSet<>();
        snapshot.options().forEach(option -> choices.add(new RecipePair(option.id(), option.output())));
        super.setRecipesList(choices, snapshot.selected().orElse(null));
        boolean missingSelection = snapshot.selected().isPresent() && snapshot.options().stream()
                .noneMatch(option -> option.id().equals(snapshot.selected().get()));
        openButton.visible = enforcePickerPresentation() && (choices.size() > 1 || missingSelection);
        ILangEntry tooltip = missingSelection
                ? (choices.isEmpty() ? PolymekLang.RECIPE_MISSING_NO_MATCH : PolymekLang.RECIPE_MISSING_CHOOSE)
                : (snapshot.editable() ? PolymekLang.RECIPE_CHOOSE : PolymekLang.RECIPE_LOCKED);
        openButton.setTooltip(Tooltip.create(tooltip.translate()));
        editable = snapshot.editable();
        openButton.active = editable;
        if (!editable) {
            selectionWidget.setActive(false);
        }
    }

    @Override
    public void selectRecipe(ResourceLocation recipe) {
        RecipeTargetKey key = targetKey();
        if (key != null) {
            // JEI may choose a recipe before the initial snapshot arrives; the server remains authoritative.
            PacketDistributor.sendToServer(new RecipeSelectPayload(containerScreen.getMenu().containerId, key, recipe));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (targetKey() != null && enforcePickerPresentation()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, containerScreen instanceof GuiMekanism<?> ? GuiMekanism.maxZOffset + 200 : 200);
            super.render(graphics, mouseX, mouseY, partialTick);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return targetKey() != null && enforcePickerPresentation() && editable && super.mouseClicked(mouseX, mouseY, button);
    }
}
