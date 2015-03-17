package android.taobao.atlas.framework.bundlestorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.jar.Manifest;

public interface Archive {
    void close();

    Class<?> findClass(String str, ClassLoader classLoader) throws ClassNotFoundException;

    File findLibrary(String str);

    File getArchiveFile();

    BundleArchiveRevision getCurrentRevision();

    Manifest getManifest() throws IOException;

    List<URL> getResources(String str) throws IOException;

    boolean isDexOpted();

    BundleArchiveRevision newRevision(String str, File file, File file2) throws IOException;

    BundleArchiveRevision newRevision(String str, File file, InputStream inputStream) throws IOException;

    InputStream openAssetInputStream(String str) throws IOException;

    InputStream openNonAssetInputStream(String str) throws IOException;

    void optDexFile();

    void purge() throws Exception;
}
