package com.entropire.simplefactions.testharness.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class FakePlayer implements InvocationHandler {

    private final UUID uuid;
    private final String name;
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final Player player;
    private Object spigot;

    public FakePlayer(String name) {
        this(UUID.randomUUID(), name);
    }

    private FakePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(), new Class<?>[] {Player.class}, this);
    }

    public FakePlayer withName(String newName) {
        return new FakePlayer(uuid, newName);
    }

    public Player player() {
        return player;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public List<String> messages() {
        return new ArrayList<>(messages);
    }

    public String allText() {
        return String.join("\n", messages());
    }

    public void clearMessages() {
        messages.clear();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String called = method.getName();
        int params = method.getParameterCount();

        if (called.startsWith("send") && called.contains("Message")) {
            if (args != null) for (Object arg : args) record(arg);
            return defaultValue(method.getReturnType());
        }
        if (called.equals("spigot") && params == 0) return spigot();
        if (called.equals("getUniqueId") && params == 0) return uuid;
        if ((called.equals("getName") || called.equals("getDisplayName") || called.equals("getPlayerListName")) && params == 0) return name;
        if (called.equals("hasPermission") || called.equals("isPermissionSet") || called.equals("isOp") || called.equals("isOnline")) return true;
        if (called.equals("getServer")) return Bukkit.getServer();
        if (called.equals("equals") && params == 1) return proxy == args[0];
        if (called.equals("hashCode") && params == 0) return System.identityHashCode(proxy);
        if (called.equals("toString") && params == 0) return "FakePlayer[" + name + "]";

        return defaultValue(method.getReturnType());
    }

    private synchronized Object spigot() {
        if (spigot == null) spigot = new PlayerSpigotRecorder(messages::add);
        return spigot;
    }

    private void record(Object arg) {
        if (arg instanceof String text) {
            messages.add(text);
        } else if (arg instanceof String[] texts) {
            Collections.addAll(messages, texts);
        } else if (arg != null && arg.getClass().getName().startsWith("net.kyori.adventure.text.")) {
            messages.add(AdventureText.plain(arg));
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        return 0d;
    }
}
