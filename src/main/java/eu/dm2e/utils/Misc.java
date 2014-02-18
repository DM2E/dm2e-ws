package eu.dm2e.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Miscellaneous methods for stringifying {@link java.util.Map} and {@link java.util.Collection}.
 * 
 * @author Kai Eckert
 */
public class Misc {
    public static String output(Map input){
        StringBuilder sb = new StringBuilder();
        sb.append("Size: ").append(input.size()).append("; Content: ");
        for (Object key:input.keySet()) {
            sb.append(key.toString()).append("(").append(input.get(key)).append("),");

        }
        return sb.toString();

    }
    public static String output(Collection input){
        StringBuilder sb = new StringBuilder();
        sb.append("Size: ").append(input.size()).append("; Content: ");
        for (Object key:input) {
            sb.append(key.toString()).append(",");

        }
        return sb.toString();

    }
}
