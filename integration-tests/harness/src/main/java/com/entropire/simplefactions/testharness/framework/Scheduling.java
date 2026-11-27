package com.entropire.simplefactions.testharness.framework;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Folia has no Bukkit scheduler ({@code Bukkit.getScheduler()} throws there), so on Folia
 * the task goes to the global region scheduler. That API only exists on Paper/Folia, and the
 * harness is compiled against the Spigot 1.17 API, so it's called through reflection.
 */
public final class Scheduling {

    private Scheduling() {}

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        if (!isFolia()) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
            return;
        }

        try {
            Object scheduler = Server.class.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
            Class<?> schedulerType = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Consumer<Object> consumer = scheduledTask -> task.run();
            schedulerType.getMethod("runDelayed", Plugin.class, Consumer.class, long.class)
                    .invoke(scheduler, plugin, consumer, delayTicks);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not schedule the tests on Folia's global region scheduler", e);
        }
    }
}
