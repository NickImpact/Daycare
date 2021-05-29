package com.nickimpact.daycare.sponge;

import com.google.gson.GsonBuilder;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.sponge.commands.DaycareCmd;
import com.nickimpact.daycare.common.json.PenAdapter;
import com.nickimpact.daycare.common.json.RanchAdapter;
import com.nickimpact.daycare.common.storage.StorageFactory;
import com.nickimpact.daycare.sponge.commands.DaycareCommandManager;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeDaycareService;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.listeners.JoinListener;
import com.nickimpact.daycare.sponge.provided.EconomicModule;
import com.nickimpact.daycare.sponge.tasks.DaycareRunningTasks;
import lombok.Getter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.sponge.configuration.SpongeConfig;
import net.impactdev.impactor.sponge.configuration.SpongeConfigAdapter;
import net.impactdev.impactor.sponge.logging.SpongeLogger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;

public class PluginBootstrap {

    private SpongeDaycarePlugin plugin;

    public PluginBootstrap(SpongeDaycarePlugin plugin) {
        this.plugin = plugin;
    }

    @Getter
    private GsonBuilder builder = new GsonBuilder().setPrettyPrinting();

    public void preInit() {
        this.plugin.setLogger(new SpongeLogger(this.plugin, this.plugin.getFallbackLogger()));
        this.plugin.getPluginLogger().info("Initializing Daycare...");

        SpongeDaycareService service = new SpongeDaycareService();
        Sponge.getServiceManager().setProvider(this.plugin, DaycareService.class, service);
        this.plugin.setService(service);
        new GsonUtils(this.plugin);

        this.plugin.getPluginLogger().info("Loading configuration...");
        this.plugin.setConfig(new SpongeConfig(new SpongeConfigAdapter(this.plugin, new File(this.plugin.getConfigDir().toFile(), "daycare.conf")), new ConfigKeys(), true));
        this.plugin.setMsgConfig(new SpongeConfig(new SpongeConfigAdapter(this.plugin, new File(this.plugin.getConfigDir().toFile(), "lang/en_us.conf")), new MsgConfigKeys(), true));

        RanchAdapter r = new RanchAdapter(this.plugin);
        PenAdapter p = new PenAdapter(this.plugin);

        builder.registerTypeAdapter(Ranch.class, r)
                .registerTypeAdapter(Pen.class, p);

        try {
            r.getRegistry().register(SpongeRanch.class);
            p.getRegistry().register(SpongePen.class);
        } catch (Exception e) {
            this.plugin.getPluginLogger().error("Unable to register class typings into API Service...");
        }

        this.plugin.getService().registerUnlocker("economic", new EconomicModule());
        Impactor.getInstance().getRegistry().registerBuilderSupplier(Ranch.RanchBuilder.class, SpongeRanch.SpongeRanchBuilder::new);
    }

    public void init() {
        this.plugin.setGson(builder.create());
        this.plugin.getService().setActiveModule(this.plugin.getConfig().get(ConfigKeys.RANCH_UNLOCK_MODULE));

        this.plugin.getPluginLogger().info("Initializing additional dependencies...");

        new DaycareCommandManager(this.plugin.getPluginContainer()).register();
        Sponge.getEventManager().registerListeners(this.plugin, new JoinListener());

        this.plugin.getLogger().info("Initializing and reading storage...");
        ((SpongeDaycareService) this.plugin.getService()).setStorage(new StorageFactory(this.plugin).getInstance(StorageType.JSON));

        this.plugin.getService().getStorage().getAllNPCs().thenAccept(npcs -> npcs.forEach(npc -> {
            this.plugin.getService().getNPCManager().addNPC(npc);
        }));
    }

    public void started(boolean async) {
        DaycareRunningTasks.breedingTask();
        DaycareRunningTasks.levelTask(async);
    }

    public void disconnecting() {

    }

    public void serviceRegistry(ChangeServiceProviderEvent e) {
        if(e.getNewProvider() instanceof EconomyService) {
            this.plugin.setEconomy((EconomyService) e.getNewProvider());
        }
    }

}
