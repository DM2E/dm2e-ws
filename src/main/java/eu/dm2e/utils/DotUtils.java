package eu.dm2e.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
public class DotUtils {
    public static String getColumn(List<String> labels, List<String> ports){
        StringBuilder sb = new StringBuilder();
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
        for (int i=0;i<labels.size();i++) {
            sb.append("<TR>");
            sb.append("<TD");
            if (ports!=null && ports.get(i)!=null) sb.append(" PORT=\"").append(ports.get(i)).append("\"");
            sb.append(">").append(labels.get(i)).append("</TD>");
            sb.append("</TR>");
        }
        sb.append("</TABLE>");
        return sb.toString();
    }
    public static String getColumn(String... labels){
        List<String> labelList = new ArrayList<>();
        for (String l:labels) {
            labelList.add(l);
        }
        return getColumn(labelList, null);
    }
    public static String getRow(List<String> labels, List<String> ports, String color){
        StringBuilder sb = new StringBuilder();
        sb.append("<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"0\"");
        if (color!=null) sb.append(" BGCOLOR=\"").append(color).append("\"");
        sb.append("><TR>");
        for (int i=0;i<labels.size();i++) {
            sb.append("<TD");
            if (ports!=null && ports.get(i)!=null) sb.append(" PORT=\"").append(ports.get(i)).append("\"");
            sb.append(">").append(labels.get(i)).append("</TD>");
        }
        sb.append("</TR></TABLE>");
        return sb.toString();
    }
}
