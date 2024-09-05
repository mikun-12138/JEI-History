package ciallo.mikun.jeihistory.gui.history;


import ciallo.mikun.jeihistory.mixin.IngredientGridAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.handlers.DeleteItemInputHandler;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientListRenderer;
import mezz.jei.gui.overlay.IngredientListSlot;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author mikun_12138
 * @date 2024/9/4 下午11:07
 */

public class HistoryIngredientListGrid extends IngredientGrid {

    public static final int INGREDIENT_PADDING = 1;
    private int MIN_ROWS = 5;
    private final IngredientGridAccessor accessor = (IngredientGridAccessor) this;
    public final IngredientListRenderer historyIngredientSlotRenderer;
    public final List<IElement<?>> historyIngredientsList;
    private boolean showHistory = false;
    private int historyMaxSize;
    private int historyHeight;


    public HistoryIngredientListGrid(IIngredientManager ingredientManager, IIngredientGridConfig gridConfig, IIngredientFilterConfig ingredientFilterConfig, IClientConfig clientConfig, IClientToggleState toggleState, IConnectionToServer serverConnection, IInternalKeyMappings keyBindings, IColorHelper colorHelper, boolean searchable) {
        super(ingredientManager, gridConfig, ingredientFilterConfig, clientConfig, toggleState, serverConnection, keyBindings, colorHelper, searchable);

        this.historyIngredientSlotRenderer = new IngredientListRenderer(ingredientManager, searchable);
        this.historyIngredientsList = new LinkedList<>();
    }

    @Override
    public void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
        // Updates ingredient list
        accessor.getIngredientListRenderer().clear();
        this.historyIngredientSlotRenderer.clear();

        // Draws searchbar and ingredient grid pages
        accessor.setArea(calculateBounds_History(accessor.getGridConfig(), availableArea));
        ImmutableRect2i area = this.getArea();
        if (area.isEmpty()) {
            return;
        }

        historyHeight = showHistory ? 2 * INGREDIENT_HEIGHT : 0;

        for (int y = area.getY(); y < area.getY() + area.getHeight() - historyHeight; y += INGREDIENT_HEIGHT) {
            for (int x = area.getX(); x < area.getX() + area.getWidth(); x += INGREDIENT_WIDTH) {
                IngredientListSlot ingredientListSlot = new IngredientListSlot(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT, INGREDIENT_PADDING);
                ImmutableRect2i stackArea = ingredientListSlot.getArea();
                final boolean blocked = MathUtil.intersects(guiExclusionAreas, stackArea);
                ingredientListSlot.setBlocked(blocked);
                accessor.getIngredientListRenderer().add(ingredientListSlot);
            }
        }

        if (showHistory) {
            int startY = area.getY() + area.getHeight() - historyHeight;
            for (int y = startY; y < area.getY() + area.getHeight(); y += INGREDIENT_HEIGHT) {
                for (int x = area.getX(); x < area.getX() + area.getWidth(); x += INGREDIENT_WIDTH) {
                    IngredientListSlot ingredientListSlot = new IngredientListSlot(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT, INGREDIENT_PADDING);
                    ImmutableRect2i stackArea = ingredientListSlot.getArea();
                    final boolean blocked = MathUtil.intersects(guiExclusionAreas, stackArea);
                    ingredientListSlot.setBlocked(blocked);
                    historyIngredientSlotRenderer.add(ingredientListSlot);
                }
            }
            this.historyIngredientSlotRenderer.set(0, this.historyIngredientsList);
        }

        // Makes sure the recipe history renders items when gui screen size changes
        this.historyIngredientSlotRenderer.set(0, this.historyIngredientsList);
    }

    private ImmutableRect2i calculateBounds_History(@NotNull IIngredientGridConfig config, @NotNull ImmutableRect2i availableArea) {
        final int columns = Math.min(availableArea.getWidth() / IngredientGrid.INGREDIENT_WIDTH, config.getMaxColumns());
        final int rows = Math.min(availableArea.getHeight() / IngredientGrid.INGREDIENT_HEIGHT, config.getMaxRows());
        this.showHistory = rows - 2 >= MIN_ROWS;

        if (rows < config.getMinRows() || columns < config.getMinColumns()) {
            return ImmutableRect2i.EMPTY;
        }
        this.historyMaxSize = 2 * columns;
        final int width = columns * IngredientGrid.INGREDIENT_WIDTH;
        final int height = rows * IngredientGrid.INGREDIENT_HEIGHT;

        final int x = switch (config.getHorizontalAlignment()) {
            case LEFT -> availableArea.getX();
            case CENTER -> availableArea.getX() + ((availableArea.getWidth() - width) / 2);
            case RIGHT -> availableArea.getX() + (availableArea.getWidth() - width);
        };

        final int y = switch (config.getVerticalAlignment()) {
            case TOP -> availableArea.getY();
            case CENTER -> availableArea.getY() + ((availableArea.getHeight() - height) / 2);
            case BOTTOM -> availableArea.getY() + (availableArea.getHeight() - height);
        };

        return new ImmutableRect2i(x, y, width, height);
    }

    @Override
    public void draw(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.draw(minecraft, guiGraphics, mouseX, mouseY);

        if (showHistory) {
            this.historyIngredientSlotRenderer.render(guiGraphics);
            if (isMouseOver(mouseX, mouseY)) {
                DeleteItemInputHandler deleteItemInputHandler = (DeleteItemInputHandler) this.getInputHandler();
                if (!deleteItemInputHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
                    this.historyIngredientSlotRenderer.getSlots()
                            .filter((s) -> s.getArea().contains(mouseX, mouseY))
                            .filter((s) -> s.getOptionalElement().isPresent())
                            .findFirst()
                            .ifPresent((s) -> drawHighlight(guiGraphics, s.getArea()));
                }
                ImmutableRect2i area = this.getArea();
                int endX = area.getX() + area.getWidth();
                int startY = area.getY() + area.getHeight() - historyHeight;
                int endY = area.getY() + area.getHeight();
                int colour = 0xee555555;

                drawHorizontalDashedLine(guiGraphics.pose(), area.getX(), endX, startY, colour, false);
                drawHorizontalDashedLine(guiGraphics.pose(), area.getX(), endX, endY, colour, true);

                drawVerticalDashedLine(guiGraphics.pose(), area.getX(), startY, endY, colour, false);
                drawVerticalDashedLine(guiGraphics.pose(), endX - 1, startY, endY, colour, true);
            }
        }
    }

    @Override
    public void drawTooltips(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
        if (showHistory) {
            if (isMouseOver(mouseX, mouseY)) {
                DeleteItemInputHandler deleteItemInputHandler = (DeleteItemInputHandler) this.getInputHandler();
                if (deleteItemInputHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
                    deleteItemInputHandler.drawTooltips(guiGraphics, mouseX, mouseY);
                } else {
                    this.historyIngredientSlotRenderer.getSlots()
                            .filter((s) -> s.isMouseOver(mouseX, mouseY))
                            .map(IngredientListSlot::getOptionalElement)
                            .flatMap(Optional::stream)
                            .findFirst()
                            .ifPresent((element) -> drawTooltip(guiGraphics, mouseX, mouseY, element));
                }
            }
        }
    }

    private <T> void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IElement<T> element) {
        ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
        IIngredientType<T> ingredientType = typedIngredient.getType();
        IIngredientRenderer<T> ingredientRenderer = this.accessor.getIngredientManager().getIngredientRenderer(ingredientType);
        IIngredientHelper<T> ingredientHelper = this.accessor.getIngredientManager().getIngredientHelper(ingredientType);
        JeiTooltip tooltip = new JeiTooltip();
        element.getTooltip(tooltip, this.accessor.getTooltipHelper(), ingredientRenderer, ingredientHelper);
        if (this.accessor.getSearchable()) {
            this.addCreativeTabs(tooltip, typedIngredient);
        }

        tooltip.draw(guiGraphics, mouseX, mouseY, typedIngredient, ingredientRenderer, this.accessor.getIngredientManager());
    }

    private <T> void addCreativeTabs(ITooltipBuilder tooltipBuilder, ITypedIngredient<T> typedIngredient) {
        IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
        if (clientConfig.isShowCreativeTabNamesEnabled()) {
            ItemStack itemStack = typedIngredient.getItemStack().orElse(ItemStack.EMPTY);
            if (!itemStack.isEmpty()) {
                Iterator var5 = CreativeModeTabs.allTabs().iterator();

                while (var5.hasNext()) {
                    CreativeModeTab itemGroup = (CreativeModeTab) var5.next();
                    if (itemGroup.shouldDisplay() && itemGroup.getType() == CreativeModeTab.Type.CATEGORY && itemGroup.contains(itemStack)) {
                        Component displayName = itemGroup.getDisplayName();
                        tooltipBuilder.add(displayName.copy().withStyle(ChatFormatting.BLUE));
                    }
                }

            }
        }
    }

    @Override
    public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
        return Stream.concat(
                super.getIngredientUnderMouse(mouseX, mouseY),
                historyIngredientSlotRenderer.getSlots()
                        .filter(slot -> slot.isMouseOver(mouseX, mouseY))
                        .map(IngredientListSlot::getClickableIngredient)
                        .flatMap(Optional::stream)
        );
    }

    @Override
    public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
        return Stream.concat(
                super.getDraggableIngredientUnderMouse(mouseX, mouseY),
                this.historyIngredientSlotRenderer.getSlots()
                        .filter(s -> s.isMouseOver(mouseX, mouseY))
                        .map(IngredientListSlot::getDraggableIngredient)
                        .flatMap(Optional::stream)
        );
    }

    @Override
    public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
        return Stream.concat(
                super.getVisibleIngredients(ingredientType),
                this.historyIngredientSlotRenderer.getSlots()
                        .map(IngredientListSlot::getOptionalElement)
                        .flatMap(Optional::stream)
                        .map(IElement::getTypedIngredient)
                        .map(i -> i.getIngredient(ingredientType))
                        .flatMap(Optional::stream)
        );
    }

    private void drawHorizontalDashedLine(PoseStack poseStack, int x1, int x2, int y, int color, boolean reverse) {
        float offset = (System.currentTimeMillis() % 600) / 100.0F;
        if (!reverse) offset = 6 - offset;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Matrix4f pose = poseStack.last().pose();

        for (float x = x1 - offset; x < x2; x += 7) {
            builder.vertex(pose, Mth.clamp(x + 4, x1, x2), y, 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, Mth.clamp(x, x1, x2), y, 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, Mth.clamp(x, x1, x2), y + 1, 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, Mth.clamp(x + 4, x1, x2), y + 1, 0).color(r, g, b, a).endVertex();
        }

        tesselator.end();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a vertical dashed line on the screen.
     *
     * @param poseStack The PoseStack for rendering.
     * @param x         The X position of the line.
     * @param y1        The starting Y position of the line.
     * @param y2        The ending Y position of the line.
     * @param color     The color of the line.
     * @param reverse   Specifies whether to reverse the offset.
     */
    // Copied from <a href="https://github.com/shedaniel/RoughlyEnoughItems/blob/8.x-1.18.2/runtime/src/main/java/me/shedaniel/rei/impl/client/gui/widget/favorites/history/DisplayHistoryWidget.java">...</a>
    private void drawVerticalDashedLine(PoseStack poseStack, int x, int y1, int y2, int color, boolean reverse) {
        float offset = (System.currentTimeMillis() % 600) / 100.0F;
        if (!reverse) offset = 6 - offset;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Matrix4f pose = poseStack.last().pose();

        for (float y = y1 - offset; y < y2; y += 7) {
            builder.vertex(pose, x + 1, Mth.clamp(y, y1, y2), 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, x, Mth.clamp(y, y1, y2), 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, x, Mth.clamp(y + 4, y1, y2), 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, x + 1, Mth.clamp(y + 4, y1, y2), 0).color(r, g, b, a).endVertex();
        }

        tesselator.end();
        RenderSystem.disableBlend();
    }

    public <T> void addHistory(@NotNull ITypedIngredient<T> ingredient) {
        if (ingredient != null) {
            ITypedIngredient<T> normalized = TypedIngredient.normalize(ingredient, this.accessor.getIngredientManager().getIngredientHelper(ingredient.getType()));
            if (normalized != null) {
                ingredient = normalized;
                IIngredientHelper<T> ingredientHelper = this.accessor.getIngredientManager().getIngredientHelper(ingredient.getType());
                String uniqueId = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Ingredient);
                @NotNull ITypedIngredient<T> value = ingredient;
                historyIngredientsList.removeIf(element -> equal(ingredientHelper, value, uniqueId, element.getTypedIngredient()));
                IngredientElement<T> ingredientElement = new IngredientElement<>(ingredient);
                historyIngredientsList.add(0, ingredientElement);
                if (historyIngredientsList.size() > historyMaxSize) {
                    historyIngredientsList.remove(historyMaxSize);
                }
                historyIngredientSlotRenderer.set(0, historyIngredientsList);
            }
        }
    }

    private static <T> boolean equal(IIngredientHelper<T> ingredientHelper, @NotNull ITypedIngredient<T> a, String uidA, @NotNull ITypedIngredient<?> b) {
        if (a.getIngredient() == b.getIngredient()) {
            return true;
        }

        if (a.getIngredient() instanceof ItemStack itemStackA && b.getIngredient() instanceof ItemStack itemStackB) {
            if (true) {
                return itemStackA.equals(itemStackB, true);
            } else {
                return ItemStack.isSameItem(itemStackA, itemStackB);
            }
        }

        Optional<T> filteredB = b.getIngredient(a.getType());
        if (filteredB.isPresent()) {
            T ingredientB = filteredB.get();
            String uidB = ingredientHelper.getUniqueId(ingredientB, UidContext.Ingredient);
            return uidA.equals(uidB);
        }

        return false;
    }

}
