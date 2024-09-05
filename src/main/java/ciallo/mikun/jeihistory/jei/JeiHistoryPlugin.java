package ciallo.mikun.jeihistory.jei;

import ciallo.mikun.jeihistory.Jeihistory;
import ciallo.mikun.jeihistory.gui.history.HistoryIngredientListGrid;
import ciallo.mikun.jeihistory.mixin.IngredientGridAccessor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.input.handlers.DeleteItemInputHandler;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue;

/**
 * @author mikun_12138
 * @date 2024/9/4 下午11:36
 */

@JeiPlugin
public class JeiHistoryPlugin implements IModPlugin {

    public static final Logger LOGGER = LoggerFactory.getLogger(JeiHistoryPlugin.class);
    public static HistoryIngredientListGrid grid;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Jeihistory.MODID, "jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

        IngredientGridWithNavigation ingredientGridWithNavigation = getPrivateValue(IngredientListOverlay.class, (IngredientListOverlay) jeiRuntime.getIngredientListOverlay(), "contents");
        IngredientGrid ingredientGrid = getPrivateValue(IngredientGridWithNavigation.class, ingredientGridWithNavigation, "ingredientGrid");
        IngredientGridAccessor accessor = (IngredientGridAccessor) ingredientGrid;
        IngredientGridTooltipHelper tooltipHelper = getPrivateValue(IngredientGrid.class, ingredientGrid, "tooltipHelper");
        DeleteItemInputHandler deleteItemHandler = getPrivateValue(IngredientGrid.class, ingredientGrid, "deleteItemHandler");

        params.ingredientManager = accessor.getIngredientManager();
        params.gridConfig = accessor.getGridConfig();
        params.ingredientFilterConfig = getPrivateValue(IngredientGridTooltipHelper.class, tooltipHelper, "ingredientFilterConfig");
        params.clientConfig = getPrivateValue(DeleteItemInputHandler.class, deleteItemHandler, "clientConfig");
        params.toggleState = getPrivateValue(IngredientGridTooltipHelper.class, tooltipHelper, "toggleState");
        params.serverConnection = getPrivateValue(DeleteItemInputHandler.class, deleteItemHandler, "serverConnection");
        params.keyBindings = getPrivateValue(IngredientGridTooltipHelper.class, tooltipHelper, "keyBindings");
        params.colorHelper = getPrivateValue(IngredientGridTooltipHelper.class, tooltipHelper, "colorHelper");
        params.searchable = accessor.getSearchable();

        grid = new HistoryIngredientListGrid(
                params.ingredientManager,
                params.gridConfig,
                params.ingredientFilterConfig,
                params.clientConfig,
                params.toggleState,
                params.serverConnection,
                params.keyBindings,
                params.colorHelper,
                params.searchable
        );

        ObfuscationReflectionHelper.setPrivateValue(
                IngredientGridWithNavigation.class,
                getPrivateValue(IngredientListOverlay.class, (IngredientListOverlay) jeiRuntime.getIngredientListOverlay(), "contents"),
                grid,
                "ingredientGrid"
        );
    }

    public static Params params = new Params();
    public static class Params {
        public IIngredientManager ingredientManager;
        public IIngredientGridConfig gridConfig;
        public IIngredientFilterConfig ingredientFilterConfig;
        public IClientConfig clientConfig;
        public IClientToggleState toggleState;
        public IConnectionToServer serverConnection;
        public IInternalKeyMappings keyBindings;
        public IColorHelper colorHelper;
        public boolean searchable;
    }
}
