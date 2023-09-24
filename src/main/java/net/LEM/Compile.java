package net.LEM;

import java.io.*;
import java.util.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.json.JSONObject;

public class Compile {

    public static JSONObject modconfig; // Define the modconfig object
    public static String modNameSpaceless; // Define modNameSpaceless

    public static List<String> getFilePaths(String directory) {
        List<String> filePaths = new ArrayList<>();
        File dir = new File(directory);

        // List all files in the directory and its subdirectories
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                filePaths.addAll(getFilePaths(file.getAbsolutePath()));
            } else {
                filePaths.add(file.getAbsolutePath());
            }
        }

        return filePaths;
    }

    public static void deleteItem(String item) {
        File file = new File(item);

        // Attempt to delete the item, handle exceptions
        if (!file.exists()) {
            System.out.println(item + " cannot be found! Skipping..");
        } else if (file.isDirectory()) {
            if (!file.delete()) {
                System.out.println("Failed to delete directory: " + item);
            }
        } else {
            if (!file.delete()) {
                System.out.println("Failed to delete file: " + item);
            }
        }
    }

    public static void loadModConfig() throws IOException {
        // Load mod config
        String modConfigPath = "./src/lebmod.json";
        BufferedReader reader = new BufferedReader(new FileReader(modConfigPath));
        StringBuilder modConfigJson = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            modConfigJson.append(line);
        }
        reader.close();

        // Parse JSON
        modconfig = new JSONObject(modConfigJson.toString());

        // Create spaceless mod name variable
        modNameSpaceless = modconfig.getString("name").replace(" ", "-");

        // Create mod ID variable
        String modID = modconfig.getString("id");

        System.out.println("Loaded config for mod " + modconfig.getString("name"));
    }

    public static void removeUnused(String directory) {
        // Remove unused data
        List<String> itemsToRemove = Arrays.asList(
                "advancements", "DIM1", "DIM-1", "datapacks", "dimensions",
                "playerdata", "scripts", "stats", "icon.png", "level.dat",
                "level.dat_old", "session.lock"
        );

        for (String item : itemsToRemove) {
            String fullPath = directory + "/" + item;
            deleteItem(fullPath);
        }
    }

    public static void compileMod() throws IOException {
        // Path to folder which needs to be compiled
        String directory = "./lem.modtools-temp";

        // Copy files to temp folder
        File srcDir = new File("src");
        File destDir = new File(directory);
        copyDirectory(srcDir, destDir);

        // Rename lebmod.json to config.json
        File configFile = new File(directory + "./lebmod.json");
        File renamedFile = new File(directory + "./config.json");
        configFile.renameTo(renamedFile);

        // Remove unused data
        if (modconfig.getBoolean("hassmall")) {
            System.out.println("Cleaning up unused files for Small map type");
            removeUnused(directory + "./world/small");
        }
        if (modconfig.getBoolean("haslarge")) {
            System.out.println("Cleaning up unused files for Large map type");
            removeUnused(directory + "./world/large");
        }
        if (modconfig.getBoolean("haslargeplus")) {
            System.out.println("Cleaning up unused files for Large+ map type");
            removeUnused(directory + "./world/largeplus");
        }
        if (modconfig.getBoolean("hasremastered")) {
            System.out.println("Cleaning up unused files for Remastered map type");
            removeUnused(directory + "./world/remastered");
        }

        // Create a TGZ archive
        String outputFileName = "../output/" + modNameSpaceless + ".lemmod";
        try (FileOutputStream fos = new FileOutputStream(outputFileName);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {

            // Add files to the archive
            List<String> filePaths = getFilePaths(directory);
            for (String filePath : filePaths) {
                File file = new File(filePath);
                TarArchiveEntry entry = new TarArchiveEntry(file, file.getName());
                tos.putArchiveEntry(entry);

                // Write file content to the archive
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = bis.read(buffer)) != -1) {
                        tos.write(buffer, 0, length);
                    }
                }
                tos.closeArchiveEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Change directory back to the starting point
        System.setProperty("user.dir", "../");

        // Remove the temporary mod folder
        File tempModFolder = new File(directory);
        if (tempModFolder.exists()) {
            if (!tempModFolder.delete()) {
                System.out.println("Failed to delete temporary mod folder");
            }
        }

        System.out.println("All files compiled successfully!");
    }

    public static void main(String[] args) throws IOException {
        // Compile it
        loadModConfig();
        compileMod();
    }

    public static void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            String[] files = source.list();

            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);

                    // Recursively copy sub-directories
                    copyDirectory(srcFile, destFile);
                }
            }
        } else {
            // Copy the file content
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }

            fis.close();
            fos.close();
        }
    }
}
