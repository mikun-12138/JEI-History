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
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.startup.OverlayHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OverlayHelper.class, remap = false)
public class OverlayHelperMixin {
    @Inject(method = "createIngredientListOverlay", at = @At("HEAD"), cancellable = true)
    private static void IngredientGridWithNavigation(IIngredientManager ingredientManager, IScreenHelper screenHelper, IIngredientGridSource ingredientFilter, IFilterTextSource filterTextSource, IInternalKeyMappings keyMappings, IIngredientGridConfig ingredientGridConfig, IClientConfig clientConfig, IClientToggleState toggleState, IConnectionToServer serverConnection, IIngredientFilterConfig ingredientFilterConfig, Textures textures, IColorHelper colorHelper, CallbackInfoReturnable<IngredientListOverlay> cir) {
        JeiHistoryPlugin.historyIngredientListGrid = new HistoryIngredientListGrid(
                ingredientManager,
                ingredientGridConfig,
                ingredientFilterConfig,
                clientConfig,
                toggleState,
                serverConnection,
                keyMappings,
                colorHelper,
                true
        );

        IngredientGridWithNavigation ingredientGridWithNavigation1 = new IngredientGridWithNavigation(
                "IngredientListOverlay",
                ingredientFilter,
                // here
                JeiHistoryPlugin.historyIngredientListGrid,
                toggleState,
                clientConfig,
                serverConnection,
                ingredientGridConfig,
                textures.getIngredientListBackground(),
                textures.getIngredientListSlotBackground(),
                screenHelper,
                ingredientManager
        );

        cir.setReturnValue(
                new IngredientListOverlay(
                        ingredientFilter,
                        filterTextSource,
                        screenHelper,
                        ingredientGridWithNavigation1,
                        clientConfig,
                        toggleState,
                        keyMappings
                )
        );
    }

}
