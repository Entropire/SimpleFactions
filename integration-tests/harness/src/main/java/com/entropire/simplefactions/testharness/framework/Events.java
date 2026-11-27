package com.entropire.simplefactions.testharness.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Events {

    private Events() {}

    public static void join(FakePlayer player) throws ReflectiveOperationException {
        Bukkit.getPluginManager().callEvent(create(PlayerJoinEvent.class, player.player()));
    }

    public static void quit(FakePlayer player) throws ReflectiveOperationException {
        Bukkit.getPluginManager().callEvent(create(PlayerQuitEvent.class, player.player()));
    }

    private static <E extends Event> E create(Class<E> type, Player player) throws ReflectiveOperationException {
        Constructor<?> constructor = Arrays.stream(type.getConstructors())
                .filter(c -> c.getParameterCount() > 0 && c.getParameterTypes()[0] == Player.class)
                .min(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new NoSuchMethodException(type.getName() + " has no constructor that takes a Player"));

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        args[0] = player;
        for (int i = 1; i < args.length; i++) args[i] = defaultArgument(parameterTypes[i]);
        return type.cast(constructor.newInstance(args));
    }

    private static Object defaultArgument(Class<?> type) throws ReflectiveOperationException {
        if (type == String.class) return "";
        if (type == boolean.class) return false;
        if (type.isEnum()) return type.getEnumConstants()[0];
        if (type.getName().equals("net.kyori.adventure.text.Component")) {
            Method empty = type.getMethod("empty");
            return empty.invoke(null);
        }
        throw new NoSuchMethodException("Don't know how to create a " + type.getName() + " for an event");
    }
}
