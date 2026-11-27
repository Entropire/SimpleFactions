package com.entropire.simplefactions.testharness.framework;

import java.lang.reflect.Method;

/**
 * Turns an Adventure Component into plain text. Adventure only exists on Paper and its forks,
 * and the harness is compiled against the Spigot API, so this goes through reflection.
 */
public final class AdventureText {

    private AdventureText() {}

    public static String plain(Object component) {
        try {
            ClassLoader loader = component.getClass().getClassLoader();
            Class<?> serializerType = Class.forName(
                    "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer", true, loader);
            Object serializer = serializerType.getMethod("plainText").invoke(null);

            for (Method method : serializerType.getMethods()) {
                if (method.getName().equals("serialize")
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(component)) {
                    return String.valueOf(method.invoke(serializer, component));
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Fall through to toString(), which still contains the text content.
        }
        return component.toString();
    }
}
