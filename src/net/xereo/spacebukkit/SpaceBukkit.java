/*
 * This file is part of SpaceBukkit (http://spacebukkit.xereo.net/).
 *
 * SpaceBukkit is free software: you can redistribute it and/or modify it under the terms of the
 * Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA) license as published by the Creative Common organization,
 * either version 3.0 of the license, or (at your option) any later version.
 *
 * SpaceBukkit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Attribution-NonCommercial-ShareAlike
 * Unported (CC BY-NC-SA) license for more details.
 *
 * You should have received a copy of the Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA) license along with
 * this program. If not, see <http://creativecommons.org/licenses/by-nc-sa/3.0/>.
 */
package net.xereo.spacebukkit;

import java.io.File;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Logger;

import net.xereo.spacebukkit.actions.PlayerActions;
import net.xereo.spacebukkit.actions.ServerActions;
import net.xereo.spacebukkit.actions.SystemActions;
import net.xereo.spacebukkit.players.SBListener;
import net.xereo.spacebukkit.plugins.PluginsManager;
import net.xereo.spacebukkit.system.PerformanceMonitor;
import net.xereo.spacebukkit.utilities.PermissionsManager;
import net.xereo.spacemodule.api.ActionsManager;
import net.xereo.spacertk.SpaceRTK;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

@SuppressWarnings("deprecation")
public class SpaceBukkit extends JavaPlugin {
    public static SpaceRTK     spaceRTK = null;
    private static SpaceBukkit spacebukkit;

    public static SpaceBukkit getInstance() {
        return spacebukkit;
    }

    public int                 port;
    public int                 rPort;
    public String              salt;

    public PluginsManager      pluginsManager;
    public ActionsManager      actionsManager;
    public PanelListener       panelListener;
    public PerformanceMonitor  performanceMonitor;

    private Configuration      configuration;
    public Logger              logger = Logger.getLogger("Minecraft");
    public String              logTag = "[SpaceBukkit] ";

    private final Timer        timer  = new Timer();
    private PermissionsManager pManager;

    @Override
    public void onDisable() {
        performanceMonitor.infanticide();
        pManager = null;
        timer.cancel();
        try {
            if (panelListener != null)
                panelListener.stopServer();
        } catch (final Exception e) {
            logger.severe(logTag + e.getMessage());
        }
        logger.info("----------------------------------------------------------");
        logger.info("|             SpaceBukkit is now disabled!               |");
        logger.info("----------------------------------------------------------");
    }

    @Override
    public void onEnable() {
        spacebukkit = this;
        configuration = new Configuration(new File("SpaceModule", "configuration.yml"));
        configuration.load();
        salt = configuration.getString("General.Salt", "<default>");
        if (salt.equals("<default>")) {
            salt = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            configuration.setProperty("General.Salt", salt);
        }
        configuration.setProperty("General.WorldContainer", Bukkit.getWorldContainer().getPath());
        port = configuration.getInt("SpaceBukkit.Port", 2011);
        rPort = configuration.getInt("SpaceRTK.Port", 2012);
        configuration.save();
        new SBListener(this);
        pluginsManager = new PluginsManager();
        actionsManager = new ActionsManager();
        actionsManager.register(PlayerActions.class);
        actionsManager.register(ServerActions.class);
        actionsManager.register(SystemActions.class);
        panelListener = new PanelListener();
        performanceMonitor = new PerformanceMonitor();
        timer.scheduleAtFixedRate(performanceMonitor, 0L, 1000L);
        logger.info("----------------------------------------------------------");
        logger.info("|        SpaceBukkit version "
                + Bukkit.getPluginManager().getPlugin("SpaceBukkit").getDescription().getVersion()
                + " is now enabled!         |");
        logger.info("----------------------------------------------------------");
    }

    public PermissionsManager getPermissionsManager() {
        if(pManager == null) {
            pManager = new PermissionsManager(PermissionsManager.findConnector());
        }

        return pManager;
    }
}