package net.LEM;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class oldInstall {
    // Set amount of maps from the base game
    private static final int baseMapCount = 21; // I swear I will create a cleaner solution to this later

    // Set datapack location
    // Base
    private static final String baseDPFolder = "/world/datapacks/lem.base/data/lem.base/";
    // Battle
    private static final String battleDPFolder = "/world/datapacks/lem.battle/data/lem.battle/";

    public static void extractMod(String modArchivePath) {
        System.out.println("Extracting mod archive from: " + modArchivePath);
        try (ZipFile zip = new ZipFile(modArchivePath)) {
            // Extract files
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                File entryFile = new File("./lem.modtools-temp/" + entryName);
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    try (InputStream inputStream = zip.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadModConfig() {
        try {
            // Load mod config
            JSONParser parser = new JSONParser();
            FileReader modConfigFile = new FileReader("/lem.modtools-temp/config.json");
            JSONObject modConfig = (JSONObject) parser.parse(modConfigFile);
            String modNameSpaceless = ((String) modConfig.get("name")).replace(" ", "-");
            String modID = (String) modConfig.get("id");
            System.out.println("Loaded config for mod " + modConfig.get("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Other methods...

    public static void main(String[] args) {
        extractMod(args[0]);
        loadModConfig();
        // Call other methods as needed
    }
}