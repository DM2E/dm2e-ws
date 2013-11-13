package eu.dm2e.utils;

import java.util.Collection;
import java.util.Map;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
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
