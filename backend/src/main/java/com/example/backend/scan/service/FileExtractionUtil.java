package com.example.backend.scan.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileExtractionUtil {

    private static final Logger log = LoggerFactory.getLogger(FileExtractionUtil.class);

    private static final int MAX_ENTRIES = 10000;
    private static final long MAX_SIZE = 1024L * 1024L * 500L; // 500 MB max unzipped size

    /**
     * Extraire un fichier zip vers un dossier de destination cible
     * en appliquant une protection contre Zip Slip et des limites de taille/fichiers.
     *
     * @param zipPath Le fichier zip source
     * @param targetDir Le dossier de destination
     * @throws IOException Si l'extraction échoue
     */
    public static void extractZipSafely(Path zipPath, Path targetDir) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String canonicalDestinationDirPath = targetDir.toFile().getCanonicalPath();

        try (InputStream is = Files.newInputStream(zipPath);
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry zipEntry;
            int entries = 0;
            long totalSize = 0;

            while ((zipEntry = zis.getNextEntry()) != null) {
                entries++;
                if (entries > MAX_ENTRIES) {
                    throw new IOException("Too many entries in zip file");
                }

                File destinationFile = new File(targetDir.toFile(), zipEntry.getName());
                String canonicalDestinationFile = destinationFile.getCanonicalPath();

                // Protection Zip Slip
                if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    if (!destinationFile.exists()) {
                        Files.createDirectories(destinationFile.toPath());
                    }
                } else {
                    File parent = destinationFile.getParentFile();
                    if (!parent.exists()) {
                        Files.createDirectories(parent.toPath());
                    }

                    // On lit par buffer et on vérifie la taille limite d'extraction
                    try (java.io.OutputStream fos = Files.newOutputStream(destinationFile.toPath())) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) >= 0) {
                            fos.write(buffer, 0, length);
                            totalSize += length;
                            if (totalSize > MAX_SIZE) {
                                throw new IOException("Zip payload exceeds max limit of 500MB");
                            }
                        }
                    }
                }
            }
        }
        log.info("Extraction successfully completed to {}", targetDir);
    }

    /**
     * Supprime de façon récursive un répertoire
     * 
     * @param directoryToBeDeleted Le répertoire à supprimer
     * @return true si suppression réussie
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
