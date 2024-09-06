package ciallo.mikun.jeihistory;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = JeiHistory.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JeiHistoryConfig {
    private static final ForgeConfigSpec.Builder BUILDER;

    private static final ForgeConfigSpec.BooleanValue OPEN_HISTORY;
    private static final ForgeConfigSpec.IntValue HISTORY_ROWS;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER = new ForgeConfigSpec.Builder();
        BUILDER.push("client-settings");
        OPEN_HISTORY = BUILDER.comment("是否打开历史记录").define("open_history", true);
        HISTORY_ROWS = BUILDER.comment("历史记录行数").defineInRange("history_rows", 2, 1, 10);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static boolean open_history;
    public static int history_rows;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        open_history = OPEN_HISTORY.get();
        history_rows = HISTORY_ROWS.get();
    }
}
