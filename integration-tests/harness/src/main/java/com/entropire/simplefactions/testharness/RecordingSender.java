package com.entropire.simplefactions.testharness;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * A fake {@link CommandSender} that records every message sent to it.
 *
 * <p>It's a dynamic proxy instead of a normal class, because {@code CommandSender} has
 * different methods on every server type and version (Paper adds Adventure's Audience
 * methods, for example). A proxy implements whatever the running server's interface has.
 * It's not a {@code Player}, so it behaves like a non-player sender such as the console.
 */
final class RecordingSender implements InvocationHandler {

    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final CommandSender sender = (CommandSender) Proxy.newProxyInstance(
            CommandSender.class.getClassLoader(), new Class<?>[] {CommandSender.class}, this);

    CommandSender sender() {
        return sender;
    }

    List<String> messages() {
        return new ArrayList<>(messages);
    }

    String allText() {
        return String.join("\n", messages());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String name = method.getName();

        if (name.startsWith("send") && name.contains("Message")) {
            if (args != null) for (Object arg : args) record(arg);
            return defaultValue(method.getReturnType());
        }
        if (name.equals("spigot") && method.getParameterCount() == 0) return spigot();
        if (name.equals("getName") && method.getParameterCount() == 0) return "SF-TestSender";
        if (name.equals("hasPermission") || name.equals("isPermissionSet") || name.equals("isOp")) return true;
        if (name.equals("getServer")) return Bukkit.getServer();
        if (name.equals("equals") && method.getParameterCount() == 1) return proxy == args[0];
        if (name.equals("hashCode") && method.getParameterCount() == 0) return System.identityHashCode(proxy);
        if (name.equals("toString") && method.getParameterCount() == 0) return "SF-TestSender";

        return defaultValue(method.getReturnType());
    }

    private Object spigot;

    private synchronized Object spigot() {
        if (spigot == null) spigot = new SpigotRecorder(messages::add);
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
