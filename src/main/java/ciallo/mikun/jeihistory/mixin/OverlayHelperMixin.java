package ciallo.mikun.jeihistory.mixin;

import ciallo.mikun.jeihistory.gui.history.HistoryIngredientListGrid;
import ciallo.mikun.jeihistory.jei.JeiHistoryPlugin;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.startup.OverlayHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author mikun_12138
 * @date 2024/10/9 下午1:25
 */
@Mixin(value = OverlayHelper.class, remap = false)
public class OverlayHelperMixin {
    @Inject(method = "createIngredientGridWithNavigation", at = @At("RETURN"), cancellable = true)
    private static void IngredientGridWithNavigation(String debugName, IIngredientGridSource ingredientFilter, IIngredientManager ingredientManager, IIngredientGridConfig ingredientGridConfig, DrawableNineSliceTexture background, DrawableNineSliceTexture slotBackground, IInternalKeyMappings keyMappings, IIngredientFilterConfig ingredientFilterConfig, IClientConfig clientConfig, IClientToggleState toggleState, IConnectionToServer serverConnection, IColorHelper colorHelper, IScreenHelper screenHelper, boolean supportsEditMode, CallbackInfoReturnable<IngredientGridWithNavigation> cir) {
        JeiHistoryPlugin.historyIngredientListGrid = new HistoryIngredientListGrid(
                ingredientManager,
                ingredientGridConfig,
                ingredientFilterConfig,
                clientConfig,
                toggleState,
                serverConnection,
                keyMappings,
                colorHelper,
                supportsEditMode
        );

        cir.setReturnValue(
                new IngredientGridWithNavigation(
                        debugName,
                        ingredientFilter,
                        // here
                        JeiHistoryPlugin.historyIngredientListGrid,
                        toggleState,
                        clientConfig,
                        serverConnection,
                        ingredientGridConfig,
                        background,
                        slotBackground,
                        screenHelper,
                        ingredientManager
                )
        );
    }

}
