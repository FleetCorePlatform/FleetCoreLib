package io.fleetcoreplatform.Builders;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MissionZipBuilder implements Closeable {
    private final ZipOutputStream zipStream;
    private final File archive;

    public MissionZipBuilder(String missionUUID) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = "mission_" + missionUUID + ".bundle.zip";

        this.archive = new File(tmpDir, fileName);

        this.zipStream = new ZipOutputStream(new FileOutputStream(archive));
    }

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

    public File build() throws IOException {
        this.zipStream.close();
        return archive;
    }

    @Override
    public void close() throws IOException {
        if (zipStream != null) {
            zipStream.close();
        }
    }
}
