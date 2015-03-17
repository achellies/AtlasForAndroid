package android.taobao.atlas.framework.bundlestorage;

import android.content.res.AssetManager;
import android.os.Build;
import android.taobao.atlas.bundleInfo.BundleInfoList;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.runtime.RuntimeVariables;
import android.taobao.atlas.util.ApkUtils;
import android.taobao.atlas.util.AtlasFileLock;
import android.taobao.atlas.util.StringUtils;
import android.text.TextUtils;
import com.alipay.mobile.quinox.classloader.InitExecutor;
import com.squareup.okhttp.internal.http.HttpTransport;
import com.tencent.mm.sdk.platformtools.FilePathGenerator;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BundleArchiveRevision {
    static final String BUNDLE_FILE_NAME = "bundle.zip";
    static final String BUNDLE_LEX_FILE = "bundle.lex";
    static final String BUNDLE_ODEX_FILE = "bundle.dex";
    static final String FILE_PROTOCOL = "file:";
    static final String REFERENCE_PROTOCOL = "reference:";
    static final Logger log;
    private final File bundleFile;
    private ClassLoader dexClassLoader;
    private DexFile dexFile;
    private boolean isDexFileUsed;
    private Manifest manifest;
    private final File revisionDir;
    private final String revisionLocation;
    private final long revisionNum;
    private ZipFile zipFile;

    class AnonymousClass_1 extends DexClassLoader {
        AnonymousClass_1(String str, String str2, String str3, ClassLoader classLoader) {
            super(str, str2, str3, classLoader);
        }

        public String findLibrary(String str) {
            String findLibrary = super.findLibrary(str);
            if (!TextUtils.isEmpty(findLibrary)) {
                return findLibrary;
            }
            File findSoLibrary = BundleArchiveRevision.this.findSoLibrary(System.mapLibraryName(str));
            if (findSoLibrary != null && findSoLibrary.exists()) {
                return findSoLibrary.getAbsolutePath();
            }
            try {
                return (String) AtlasHacks.ClassLoader_findLibrary.invoke(Framework.getSystemClassLoader(), str);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class DexLoadException extends RuntimeException {
        DexLoadException(String str) {
            super(str);
        }
    }

    static {
        log = LoggerFactory.getInstance("BundleArchiveRevision");
    }

    BundleArchiveRevision(String str, long j, File file, InputStream inputStream) throws IOException {
        Object obj = 1;
        this.revisionNum = j;
        this.revisionDir = file;
        if (!this.revisionDir.exists()) {
            this.revisionDir.mkdirs();
        }
        this.revisionLocation = FILE_PROTOCOL;
        this.bundleFile = new File(file, BUNDLE_FILE_NAME);
        ApkUtils.copyInputStreamToFile(inputStream, this.bundleFile);
        BundleInfoList instance = BundleInfoList.getInstance();
        if (instance == null || !instance.getHasSO(str)) {
            obj = null;
        }
        if (obj != null) {
            installSoLib(this.bundleFile);
        }
        updateMetadata();
    }

    BundleArchiveRevision(String str, long j, File file, File file2) throws IOException {
        int i;
        this.revisionNum = j;
        this.revisionDir = file;
        BundleInfoList instance = BundleInfoList.getInstance();
        if (instance == null || !instance.getHasSO(str)) {
            i = 0;
        } else {
            i = 1;
        }
        if (!this.revisionDir.exists()) {
            this.revisionDir.mkdirs();
        }
        if (file2.canWrite()) {
            if (isSameDriver(file, file2)) {
                this.revisionLocation = FILE_PROTOCOL;
                this.bundleFile = new File(file, BUNDLE_FILE_NAME);
                file2.renameTo(this.bundleFile);
            } else {
                this.revisionLocation = FILE_PROTOCOL;
                this.bundleFile = new File(file, BUNDLE_FILE_NAME);
                ApkUtils.copyInputStreamToFile(new FileInputStream(file2), this.bundleFile);
            }
            if (i != 0) {
                installSoLib(this.bundleFile);
            }
        } else if (Build.HARDWARE.toLowerCase().contains("mt6592") && file2.getName().endsWith(".so")) {
            this.revisionLocation = FILE_PROTOCOL;
            this.bundleFile = new File(file, BUNDLE_FILE_NAME);
            Runtime.getRuntime().exec(String.format("ln -s %s %s", new Object[]{file2.getAbsolutePath(), this.bundleFile.getAbsolutePath()}));
            if (i != 0) {
                installSoLib(file2);
            }
        } else if (AtlasHacks.LexFile == null || AtlasHacks.LexFile.getmClass() == null) {
            this.revisionLocation = REFERENCE_PROTOCOL + file2.getAbsolutePath();
            this.bundleFile = file2;
            if (i != 0) {
                installSoLib(file2);
            }
        } else {
            this.revisionLocation = FILE_PROTOCOL;
            this.bundleFile = new File(file, BUNDLE_FILE_NAME);
            ApkUtils.copyInputStreamToFile(new FileInputStream(file2), this.bundleFile);
            if (i != 0) {
                installSoLib(this.bundleFile);
            }
        }
        updateMetadata();
    }

    BundleArchiveRevision(String str, long j, File file) throws IOException {
        File file2 = new File(file, "meta");
        if (file2.exists()) {
            AtlasFileLock.getInstance().LockExclusive(file2);
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file2));
            this.revisionLocation = dataInputStream.readUTF();
            dataInputStream.close();
            AtlasFileLock.getInstance().unLock(file2);
            this.revisionNum = j;
            this.revisionDir = file;
            if (!this.revisionDir.exists()) {
                this.revisionDir.mkdirs();
            }
            if (StringUtils.startWith(this.revisionLocation, REFERENCE_PROTOCOL)) {
                this.bundleFile = new File(StringUtils.substringAfter(this.revisionLocation, REFERENCE_PROTOCOL));
                return;
            } else {
                this.bundleFile = new File(file, BUNDLE_FILE_NAME);
                return;
            }
        }
        throw new IOException("Could not find meta file in " + file.getAbsolutePath());
    }

    void updateMetadata() throws IOException {
        Throwable e;
        File file = new File(this.revisionDir, "meta");
        DataOutputStream dataOutputStream = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (AtlasFileLock.getInstance().LockExclusive(file)) {
                DataOutputStream dataOutputStream2 = new DataOutputStream(new FileOutputStream(file));
                try {
                    dataOutputStream2.writeUTF(this.revisionLocation);
                    dataOutputStream2.flush();
                    AtlasFileLock.getInstance().unLock(file);
                    if (dataOutputStream2 != null) {
                        try {
                            dataOutputStream2.close();
                            return;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (IOException e3) {
                    e = e3;
                    dataOutputStream = dataOutputStream2;
                    try {
                        throw new IOException("Could not save meta data " + file.getAbsolutePath(), e);
                    } catch (Throwable th) {
                        e = th;
                        AtlasFileLock.getInstance().unLock(file);
                        if (dataOutputStream != null) {
                            try {
                                dataOutputStream.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        throw e;
                    }
                } catch (Throwable th2) {
                    e = th2;
                    dataOutputStream = dataOutputStream2;
                    AtlasFileLock.getInstance().unLock(file);
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    throw e;
                }
            }
            log.error("Failed to get fileLock for " + file.getAbsolutePath());
            AtlasFileLock.getInstance().unLock(file);
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
        } catch (IOException e5) {
            e = e5;
            throw new IOException("Could not save meta data " + file.getAbsolutePath(), e);
        }
    }

    public long getRevisionNum() {
        return this.revisionNum;
    }

    public File getRevisionDir() {
        return this.revisionDir;
    }

    public File getRevisionFile() {
        return this.bundleFile;
    }

    public File findSoLibrary(String str) {
        File file = new File(String.format("%s%s%s%s", new Object[]{this.revisionDir, File.separator, "lib", File.separator}), str);
        return (file.exists() && file.isFile()) ? file : null;
    }

    public boolean isDexOpted() {
        if (AtlasHacks.LexFile == null || AtlasHacks.LexFile.getmClass() == null) {
            return new File(this.revisionDir, BUNDLE_ODEX_FILE).exists();
        }
        return new File(this.revisionDir, BUNDLE_LEX_FILE).exists();
    }

    public synchronized void optDexFile() {
        if (!isDexOpted()) {
            if (AtlasHacks.LexFile == null || AtlasHacks.LexFile.getmClass() == null) {
                File file = new File(this.revisionDir, BUNDLE_ODEX_FILE);
                long currentTimeMillis = System.currentTimeMillis();
                try {
                    if (!AtlasFileLock.getInstance().LockExclusive(file)) {
                        log.error("Failed to get file lock for " + this.bundleFile.getAbsolutePath());
                    }
                    if (file.length() <= 0) {
                        InitExecutor.optDexFile(this.bundleFile.getAbsolutePath(), file.getAbsolutePath());
                        loadDex(file);
                        AtlasFileLock.getInstance().unLock(file);
                        "bundle archieve dexopt bundle " + this.bundleFile.getAbsolutePath() + " cost time = " + (System.currentTimeMillis() - currentTimeMillis) + " ms";
                    }
                } catch (Throwable e) {
                    log.error("Failed optDexFile '" + this.bundleFile.getAbsolutePath() + "' >>> ", e);
                } finally {
                    currentTimeMillis = AtlasFileLock.getInstance();
                    currentTimeMillis.unLock(file);
                }
            } else {
                DexClassLoader dexClassLoader = new DexClassLoader(this.bundleFile.getAbsolutePath(), this.revisionDir.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
            }
        }
    }

    private synchronized void loadDex(File file) throws IOException {
        if (this.dexFile == null) {
            this.dexFile = DexFile.loadDex(this.bundleFile.getAbsolutePath(), file.getAbsolutePath(), 0);
        }
    }

    public void installSoLib(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String name = zipEntry.getName();
                String str = "armeabi";
                if (Build.CPU_ABI.contains("x86")) {
                    str = "x86";
                }
                if (name.indexOf(String.format("%s%s", new Object[]{"lib/", str})) != -1) {
                    str = String.format("%s%s%s%s%s", new Object[]{this.revisionDir, File.separator, "lib", File.separator, name.substring(name.lastIndexOf(File.separator) + 1, name.length())});
                    if (zipEntry.isDirectory()) {
                        File file2 = new File(str);
                        if (!file2.exists()) {
                            file2.mkdirs();
                        }
                    } else {
                        File file3 = new File(str.substring(0, str.lastIndexOf(FilePathGenerator.ANDROID_DIR_SEP)));
                        if (!file3.exists()) {
                            file3.mkdirs();
                        }
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str));
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                        byte[] bArr = new byte[HttpTransport.DEFAULT_CHUNK_LENGTH];
                        for (int read = bufferedInputStream.read(bArr); read != -1; read = bufferedInputStream.read(bArr)) {
                            bufferedOutputStream.write(bArr, 0, read);
                        }
                        bufferedOutputStream.close();
                    }
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream openAssetInputStream(String str) throws IOException {
        try {
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            if (((Integer) AtlasHacks.AssetManager_addAssetPath.invoke(assetManager, this.bundleFile.getAbsolutePath())).intValue() != 0) {
                return assetManager.open(str);
            }
        } catch (Throwable e) {
            log.error("Exception while openNonAssetInputStream >>>", e);
        } catch (Throwable e2) {
            log.error("Exception while openNonAssetInputStream >>>", e2);
        } catch (InvocationTargetException e3) {
            log.error("Exception while openNonAssetInputStream >>>", e3.getTargetException());
        }
        return null;
    }

    public InputStream openNonAssetInputStream(String str) throws IOException {
        try {
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            int intValue = ((Integer) AtlasHacks.AssetManager_addAssetPath.invoke(assetManager, this.bundleFile.getAbsolutePath())).intValue();
            if (intValue != 0) {
                return assetManager.openNonAssetFd(intValue, str).createInputStream();
            }
        } catch (Throwable e) {
            log.error("Exception while openNonAssetInputStream >>>", e);
        } catch (Throwable e2) {
            log.error("Exception while openNonAssetInputStream >>>", e2);
        } catch (InvocationTargetException e3) {
            log.error("Exception while openNonAssetInputStream >>>", e3.getTargetException());
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.jar.Manifest getManifest() throws java.io.IOException {
        /*
        r7 = this;
        r2 = 0;
        r0 = r7.manifest;
        if (r0 == 0) goto L_0x0008;
    L_0x0005:
        r0 = r7.manifest;
    L_0x0007:
        return r0;
    L_0x0008:
        r0 = android.content.res.AssetManager.class;
        r0 = r0.newInstance();	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r0 = (android.content.res.AssetManager) r0;	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r1 = android.taobao.atlas.hack.AtlasHacks.AssetManager_addAssetPath;	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r3 = 1;
        r3 = new java.lang.Object[r3];	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r4 = 0;
        r5 = r7.bundleFile;	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r5 = r5.getAbsolutePath();	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r3[r4] = r5;	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r1 = r1.invoke(r0, r3);	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r1 = (java.lang.Integer) r1;	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        r1 = r1.intValue();	 Catch:{ Exception -> 0x006f, all -> 0x007f }
        if (r1 == 0) goto L_0x0099;
    L_0x002a:
        r1 = "OSGI.MF";
        r1 = r0.open(r1);	 Catch:{ FileNotFoundException -> 0x0040, Exception -> 0x0068, all -> 0x007f }
        r0 = new java.util.jar.Manifest;	 Catch:{ FileNotFoundException -> 0x0096, Exception -> 0x0094 }
        r0.<init>(r1);	 Catch:{ FileNotFoundException -> 0x0096, Exception -> 0x0094 }
        r7.manifest = r0;	 Catch:{ FileNotFoundException -> 0x0096, Exception -> 0x0094 }
        r0 = r7.manifest;	 Catch:{ FileNotFoundException -> 0x0096, Exception -> 0x0094 }
        if (r1 == 0) goto L_0x0007;
    L_0x003c:
        r1.close();
        goto L_0x0007;
    L_0x0040:
        r0 = move-exception;
        r0 = r2;
    L_0x0042:
        r1 = log;	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r3.<init>();	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r4 = "Could not find OSGI.MF in ";
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r4 = r7.bundleFile;	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r4 = r4.getAbsolutePath();	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
        r1.warn(r3);	 Catch:{ Exception -> 0x008d, all -> 0x0089 }
    L_0x0061:
        if (r0 == 0) goto L_0x0066;
    L_0x0063:
        r0.close();
    L_0x0066:
        r0 = r2;
        goto L_0x0007;
    L_0x0068:
        r0 = move-exception;
        r1 = r2;
    L_0x006a:
        r0.printStackTrace();	 Catch:{ Exception -> 0x0092 }
        r0 = r1;
        goto L_0x0061;
    L_0x006f:
        r0 = move-exception;
        r1 = r2;
    L_0x0071:
        r3 = log;	 Catch:{ all -> 0x0086 }
        r4 = "Exception while parse OSGI.MF >>>";
        r3.error(r4, r0);	 Catch:{ all -> 0x0086 }
        if (r1 == 0) goto L_0x0066;
    L_0x007b:
        r1.close();
        goto L_0x0066;
    L_0x007f:
        r0 = move-exception;
    L_0x0080:
        if (r2 == 0) goto L_0x0085;
    L_0x0082:
        r2.close();
    L_0x0085:
        throw r0;
    L_0x0086:
        r0 = move-exception;
        r2 = r1;
        goto L_0x0080;
    L_0x0089:
        r1 = move-exception;
        r2 = r0;
        r0 = r1;
        goto L_0x0080;
    L_0x008d:
        r1 = move-exception;
        r6 = r1;
        r1 = r0;
        r0 = r6;
        goto L_0x0071;
    L_0x0092:
        r0 = move-exception;
        goto L_0x0071;
    L_0x0094:
        r0 = move-exception;
        goto L_0x006a;
    L_0x0096:
        r0 = move-exception;
        r0 = r1;
        goto L_0x0042;
    L_0x0099:
        r0 = r2;
        goto L_0x0061;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision.getManifest():java.util.jar.Manifest");
    }

    Class<?> findClass(String str, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            if (AtlasHacks.LexFile == null || AtlasHacks.LexFile.getmClass() == null) {
                if (!isDexOpted()) {
                    optDexFile();
                }
                if (this.dexFile == null) {
                    loadDex(new File(this.revisionDir, BUNDLE_ODEX_FILE));
                }
                Class<?> loadClass = this.dexFile.loadClass(str, classLoader);
                this.isDexFileUsed = true;
                return loadClass;
            }
            if (this.dexClassLoader == null) {
                File file = new File(RuntimeVariables.androidApplication.getFilesDir().getParentFile(), "lib");
                this.dexClassLoader = new AnonymousClass_1(this.bundleFile.getAbsolutePath(), this.revisionDir.getAbsolutePath(), file.getAbsolutePath(), classLoader);
            }
            return (Class) AtlasHacks.DexClassLoader_findClass.invoke(this.dexClassLoader, str);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (InvocationTargetException e2) {
            return null;
        } catch (Throwable e3) {
            if (!(e3 instanceof ClassNotFoundException)) {
                if (e3 instanceof DexLoadException) {
                    throw ((DexLoadException) e3);
                }
                log.error("Exception while find class in archive revision: " + this.bundleFile.getAbsolutePath(), e3);
            }
            return null;
        }
    }

    List<URL> getResources(String str) throws IOException {
        List<URL> arrayList = new ArrayList();
        ensureZipFile();
        if (!(this.zipFile == null || this.zipFile.getEntry(str) == null)) {
            try {
                arrayList.add(new URL("jar:" + this.bundleFile.toURL() + "!/" + str));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return arrayList;
    }

    void close() throws Exception {
        if (this.zipFile != null) {
            this.zipFile.close();
        }
        if (this.dexFile != null) {
            this.dexFile.close();
        }
    }

    private boolean isSameDriver(File file, File file2) {
        return StringUtils.equals(StringUtils.substringBetween(file.getAbsolutePath(), FilePathGenerator.ANDROID_DIR_SEP, FilePathGenerator.ANDROID_DIR_SEP), StringUtils.substringBetween(file2.getAbsolutePath(), FilePathGenerator.ANDROID_DIR_SEP, FilePathGenerator.ANDROID_DIR_SEP));
    }

    private void ensureZipFile() throws IOException {
        if (this.zipFile == null) {
            this.zipFile = new ZipFile(this.bundleFile, 1);
        }
    }
}
