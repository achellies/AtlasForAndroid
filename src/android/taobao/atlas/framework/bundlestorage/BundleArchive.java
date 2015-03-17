package android.taobao.atlas.framework.bundlestorage;

import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.Manifest;

public class BundleArchive implements Archive {
    public static final String REVISION_DIRECTORY = "version";
    private File bundleDir;
    private final BundleArchiveRevision currentRevision;
    private final SortedMap<Long, BundleArchiveRevision> revisions;

    public BundleArchive(String str, File file) throws IOException {
        this.revisions = new TreeMap();
        String[] list = file.list();
        if (list != null) {
            for (String str2 : list) {
                if (str2.startsWith(REVISION_DIRECTORY)) {
                    long parseLong = Long.parseLong(StringUtils.substringAfter(str2, "."));
                    if (parseLong > 0) {
                        this.revisions.put(Long.valueOf(parseLong), null);
                    }
                }
            }
        }
        if (this.revisions.isEmpty()) {
            throw new IOException("No valid revisions in bundle archive directory: " + file);
        }
        this.bundleDir = file;
        long longValue = ((Long) this.revisions.lastKey()).longValue();
        BundleArchiveRevision bundleArchiveRevision = new BundleArchiveRevision(str, longValue, new File(file, "version." + String.valueOf(longValue)));
        this.revisions.put(Long.valueOf(longValue), bundleArchiveRevision);
        this.currentRevision = bundleArchiveRevision;
    }

    public BundleArchive(String str, File file, InputStream inputStream) throws IOException {
        this.revisions = new TreeMap();
        this.bundleDir = file;
        BundleArchiveRevision bundleArchiveRevision = new BundleArchiveRevision(str, 1, new File(file, "version." + String.valueOf(1)), inputStream);
        this.revisions.put(Long.valueOf(1), bundleArchiveRevision);
        this.currentRevision = bundleArchiveRevision;
    }

    public BundleArchive(String str, File file, File file2) throws IOException {
        this.revisions = new TreeMap();
        this.bundleDir = file;
        BundleArchiveRevision bundleArchiveRevision = new BundleArchiveRevision(str, 1, new File(file, "version." + String.valueOf(1)), file2);
        this.revisions.put(Long.valueOf(1), bundleArchiveRevision);
        this.currentRevision = bundleArchiveRevision;
    }

    public BundleArchiveRevision newRevision(String str, File file, InputStream inputStream) throws IOException {
        long longValue = 1 + ((Long) this.revisions.lastKey()).longValue();
        BundleArchiveRevision bundleArchiveRevision = new BundleArchiveRevision(str, longValue, new File(file, "version." + String.valueOf(longValue)), inputStream);
        this.revisions.put(Long.valueOf(longValue), bundleArchiveRevision);
        return bundleArchiveRevision;
    }

    public BundleArchiveRevision newRevision(String str, File file, File file2) throws IOException {
        long longValue = 1 + ((Long) this.revisions.lastKey()).longValue();
        BundleArchiveRevision bundleArchiveRevision = new BundleArchiveRevision(str, longValue, new File(file, "version." + String.valueOf(longValue)), file2);
        this.revisions.put(Long.valueOf(longValue), bundleArchiveRevision);
        return bundleArchiveRevision;
    }

    public BundleArchiveRevision getCurrentRevision() {
        return this.currentRevision;
    }

    public File getArchiveFile() {
        return this.currentRevision.getRevisionFile();
    }

    public File getBundleDir() {
        return this.bundleDir;
    }

    public boolean isDexOpted() {
        return this.currentRevision.isDexOpted();
    }

    public void optDexFile() {
        this.currentRevision.optDexFile();
    }

    public InputStream openAssetInputStream(String str) throws IOException {
        return this.currentRevision.openAssetInputStream(str);
    }

    public InputStream openNonAssetInputStream(String str) throws IOException {
        return this.currentRevision.openNonAssetInputStream(str);
    }

    public Manifest getManifest() throws IOException {
        return this.currentRevision.getManifest();
    }

    public Class<?> findClass(String str, ClassLoader classLoader) throws ClassNotFoundException {
        return this.currentRevision.findClass(str, classLoader);
    }

    public File findLibrary(String str) {
        return this.currentRevision.findSoLibrary(str);
    }

    public List<URL> getResources(String str) throws IOException {
        return this.currentRevision.getResources(str);
    }

    public void purge() throws Exception {
        if (this.revisions.size() > 1) {
            long revisionNum = this.currentRevision.getRevisionNum();
            for (Long longValue : this.revisions.keySet()) {
                long longValue2 = longValue.longValue();
                if (longValue2 != revisionNum) {
                    File file = new File(this.bundleDir, "version." + String.valueOf(longValue2));
                    if (file.exists()) {
                        Framework.deleteDirectory(file);
                    }
                }
            }
            this.revisions.clear();
            this.revisions.put(Long.valueOf(revisionNum), this.currentRevision);
        }
    }

    public void close() {
    }
}
