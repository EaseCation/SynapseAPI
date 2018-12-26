package org.itxtech.synapseapi.utils;

/**
 * @author CreeperFace
 */
public class ClassUtils {

    public static void requireInstance(Object obj, Class clazz) {
        requireInstance(obj, clazz, "Expected "+clazz.getName()+", got "+obj.getClass().getName());
    }

    public static void requireInstance(Object obj, Class clazz, String message) {
        if(!clazz.isInstance(obj)) throw new RuntimeException(message);
    }
}
