package RSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class RSON {
    private final String PATH;
    private final HashMap<String, String> data = new HashMap<>();

    public RSON(String PATH) {
        this.PATH = PATH;
        proces(read());
    }

    private String read() {
        Scanner scanner;
        String lines = "";
        try {
            scanner = new Scanner(new File(PATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains("#"))
                lines += rmSpace(line);//removes all unnecessary spaces (there are necessary ones in the names)
        }
        lines = lines.replaceAll("\"", ""); //replaces all " with nothing for the easier data proces
        return lines;
    }

    private void proces(String line) {
        String[] list = line.split(";");
        for (String string : list) {
            String[] values = string.split("=");
            data.put(values[0], values[1]);
        }
    }

    public void setConf(String key, String value){
        data.put(key,value);
    }

    public String getValue(String key) {
        return data.get(key);
    }

    public ArrayList<String> getArray(String key) {
        return (ArrayList<String>) Arrays.asList(data.get(key).split(","));
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(data.get(key));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(data.get(key));
    }

    private String rmSpace(String from) {
        boolean removable = true;
        String rstring = "";
        for (int i = 0; i < from.length(); i++) {
            if (from.charAt(i) == '\"')
                removable = !removable;
            if (!(from.charAt(i) == ' ' && removable))
                rstring += from.charAt(i);
        }
        return rstring;
    }

}