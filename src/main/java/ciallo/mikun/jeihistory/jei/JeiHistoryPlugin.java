package ciallo.mikun.jeihistory.jei;

import ciallo.mikun.jeihistory.JeiHistory;
import ciallo.mikun.jeihistory.gui.history.HistoryIngredientListGrid;
import ciallo.mikun.jeihistory.gui.input.handler.ExtendedFocusInputHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JeiPlugin
public class JeiHistoryPlugin implements IModPlugin {

    public static final Logger LOGGER = LoggerFactory.getLogger(JeiHistoryPlugin.class);
    public static HistoryIngredientListGrid historyIngredientListGrid;
    public static ExtendedFocusInputHandler extendedFocusInputHandler;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(JeiHistory.MODID, "jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }

}
