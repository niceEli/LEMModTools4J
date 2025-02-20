package net.LEM;

import java.io.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Install {
    // Set amount of maps from the base game
    private static final int baseMapCount = 21;

    // Set datapack location
    // Base
    private static final String baseDPFolder = "/world/datapacks/lem.base/data/lem.base/";
    // Battle
    private static final String battleDPFolder = "/world/datapacks/lem.battle/data/lem.battle/";

    public static void extractMod(String modArchivePath) {
        System.out.println("Extracting mod archive from: " + modArchivePath);
        File modArchive = new File(modArchivePath);
        File tempFolder = new File("./lem.modtools-temp");

        if (!tempFolder.exists() && !tempFolder.mkdirs()) {
            System.err.println("Failed to create the Temp directory.");
            System.exit(1);
        }

        try (FileInputStream fis = new FileInputStream(modArchive);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis);
             TarArchiveInputStream taris = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            while ((entry = taris.getNextTarEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File("./lem.modtools-temp/" + entryName);

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = taris.read(buffer)) != -1) {
                            fos.write(buffer, 0, length);
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
            File configFile = new File("./lem.modtools-temp/config.json");
            if (!configFile.exists()) {
                System.err.println("Config file not found.");
                System.exit(1);
            }

            JSONParser parser = new JSONParser();
            FileReader modConfigFile = new FileReader(configFile);
            JSONObject modConfig = (JSONObject) parser.parse(modConfigFile);
            String modNameSpaceless = ((String) modConfig.get("name")).replace(" ", "-");
            String modID = (String) modConfig.get("id");
            System.out.println("Loaded config for mod " + modConfig.get("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        extractMod(args[0]);
        loadModConfig();

        // Check if "index.jar" exists and run it using ProcessBuilder
        File jarFile = new File("./lem.modtools-temp/index.jar");
        if (jarFile.exists()) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath());
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Successfully executed index.jar");
                } else {
                    System.out.println("Failed to execute index.jar");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check if "index.ok" exists and run it through OUTKAT.Run(file);
        File okFile = new File("./lem.modtools-temp/index.ok");
        if (okFile.exists()) {
            OUTKAT.executeScript(okFile.getPath());
        }
    }
}
