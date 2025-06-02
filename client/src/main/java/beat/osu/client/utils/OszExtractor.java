package beat.osu.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OszExtractor {

    private static void clearDirectory(File directory) {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    clearDirectory(file); 
                }
                file.delete(); 
            }
        }
    }

    public static void extractOsz(File oszFile, File outputDir) throws IOException {
        clearDirectory(outputDir);

        System.out.println("extracting osz file: " + oszFile.getAbsolutePath());
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(oszFile));
        ZipEntry zipEntry;

        while ((zipEntry = zis.getNextEntry()) != null) {
            String originalName = zipEntry.getName();
            String lowerName = originalName.toLowerCase();

            String newName = originalName;
            if (lowerName.endsWith(".mp3")) {
                newName = "audio.mp3";
            }

            File newFile = new File(outputDir, newName);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zis.closeEntry();
        }

        zis.close();
    }
}
