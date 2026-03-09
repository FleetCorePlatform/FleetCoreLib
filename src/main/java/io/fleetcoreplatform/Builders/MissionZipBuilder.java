package io.fleetcoreplatform.Builders;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Aggregates distinct mission plan files into a single, deployable ZIP archive. Provides a managed
 * stream wrapper to incrementally add payloads targeted to specific drone units.
 */
public class MissionZipBuilder implements Closeable {
    private final ZipOutputStream zipStream;
    private final File archive;

    /**
     * Initializes the builder by creating a temporary archive container on the host filesystem.
     *
     * @param missionUUID A global identifier bridging the constituent missions, utilized for the
     *     archive filename.
     * @throws IOException If the temporary file directory is inaccessible or unwritable.
     */
    public MissionZipBuilder(String missionUUID) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = "mission_" + missionUUID + ".bundle.zip";

        this.archive = new File(tmpDir, fileName);

        this.zipStream = new ZipOutputStream(new FileOutputStream(archive));
    }

    /**
     * Injects a specific mission plan stream into the overarching zip bundle. The entry filename is
     * determined by the target drone's callsign or logical name.
     *
     * @param thingName The unique identifier assigned to the drone hardware (used as the zip entry
     *     name).
     * @param missionInputStream The raw byte stream of the generated `.plan` file.
     * @return The current builder instance for chaining.
     * @throws IOException If the stream fails to read or the archive write is interrupted.
     */
    public MissionZipBuilder mission(String thingName, InputStream missionInputStream)
            throws IOException {
        ZipEntry zipEntry = new ZipEntry(thingName);
        this.zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = missionInputStream.read(bytes)) >= 0) {
            this.zipStream.write(bytes, 0, length);
        }

        this.zipStream.closeEntry();

        return this;
    }

    /**
     * Seals the archive, finalizing the zip structure and flushing all pending writes to disk.
     *
     * @return The finalized `File` handle pointing to the zip archive.
     * @throws IOException If standard stream closing fails.
     */
    public File build() throws IOException {
        this.zipStream.close();
        return archive;
    }

    /**
     * Ensures all internal zip streams are safely terminated when the builder context exits.
     *
     * @throws IOException If the underlying stream throws during closure.
     */
    @Override
    public void close() throws IOException {
        if (zipStream != null) {
            zipStream.close();
        }
    }
}
