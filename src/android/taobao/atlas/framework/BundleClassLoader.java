package android.taobao.atlas.framework;

import android.taobao.atlas.framework.bundlestorage.Archive;
import android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision.DexLoadException;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.filecache.FileInfoBase;
import com.taobao.android.dexposed.ClassUtils;
import com.tencent.mm.sdk.platformtools.FilePathGenerator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import mtopsdk.common.util.SymbolExpUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public final class BundleClassLoader extends ClassLoader {
    private static final List<URL> EMPTY_LIST;
    static final HashSet<String> FRAMEWORK_PACKAGES;
    static final Logger log;
    BundleActivator activator;
    String activatorClassName;
    final Archive archive;
    BundleImpl bundle;
    private String[] dynamicImports;
    String[] exports;
    Map<String, BundleClassLoader> importDelegations;
    String[] imports;
    private File[] nativeLibraryDirectories;
    BundleClassLoader originalExporter;
    String[] requires;

    private static final class BundleURLHandler extends URLStreamHandler {
        private final InputStream input;

        class AnonymousClass_1 extends InputStream {
            final /* synthetic */ InputStream val$stream;

            AnonymousClass_1(InputStream inputStream) {
                this.val$stream = inputStream;
            }

            public int read() throws IOException {
                return this.val$stream.read();
            }

            public int read(byte[] bArr) throws IOException {
                return this.val$stream.read(bArr);
            }
        }

        class AnonymousClass_2 extends URLConnection {
            AnonymousClass_2(URL url) {
                super(url);
            }

            public InputStream getInputStream() throws IOException {
                return BundleURLHandler.this.input;
            }

            public void connect() throws IOException {
            }
        }

        private BundleURLHandler(InputStream inputStream) {
            this.input = new AnonymousClass_1(inputStream);
        }

        protected URLConnection openConnection(URL url) throws IOException {
            return new AnonymousClass_2(url);
        }

        protected int hashCode(URL url) {
            return this.input.hashCode();
        }
    }

    static {
        log = LoggerFactory.getInstance("BundleClassLoader");
        FRAMEWORK_PACKAGES = new HashSet();
        FRAMEWORK_PACKAGES.add("android.taobao.atlas.framework");
        FRAMEWORK_PACKAGES.add("org.osgi.framework");
        FRAMEWORK_PACKAGES.add("org.osgi.service.packageadmin");
        FRAMEWORK_PACKAGES.add("org.osgi.service.startlevel");
        EMPTY_LIST = new ArrayList();
    }

    BundleClassLoader(BundleImpl bundleImpl) throws BundleException {
        super(Object.class.getClassLoader());
        this.exports = new String[0];
        this.imports = new String[0];
        this.requires = new String[0];
        this.activatorClassName = null;
        this.activator = null;
        this.dynamicImports = null;
        this.originalExporter = null;
        this.bundle = bundleImpl;
        this.archive = bundleImpl.archive;
        if (this.archive == null) {
            throw new BundleException("Not a valid bundle: " + bundleImpl.location);
        }
        try {
            processManifest(this.archive.getManifest());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BundleException("Not a valid bundle: " + bundleImpl.location);
        }
    }

    public BundleImpl getBundle() {
        return this.bundle;
    }

    private void processManifest(Manifest manifest) throws BundleException {
        Attributes mainAttributes;
        if (manifest != null) {
            mainAttributes = manifest.getMainAttributes();
        } else {
            mainAttributes = new Attributes();
        }
        checkEE(readProperty(mainAttributes, Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT), splitString(System.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT)));
        this.exports = readProperty(mainAttributes, Constants.EXPORT_PACKAGE);
        this.imports = readProperty(mainAttributes, Constants.IMPORT_PACKAGE);
        this.dynamicImports = readProperty(mainAttributes, Constants.DYNAMICIMPORT_PACKAGE);
        this.requires = readProperty(mainAttributes, Constants.REQUIRE_BUNDLE);
        this.activatorClassName = mainAttributes.getValue(Constants.BUNDLE_ACTIVATOR);
        Hashtable hashtable = new Hashtable(mainAttributes.size());
        Object[] toArray = mainAttributes.keySet().toArray(new Object[mainAttributes.keySet().size()]);
        for (int i = 0; i < toArray.length; i++) {
            hashtable.put(toArray[i].toString(), mainAttributes.get(toArray[i]).toString());
        }
        this.bundle.headers = hashtable;
    }

    private void checkEE(String[] strArr, String[] strArr2) throws BundleException {
        if (strArr.length != 0) {
            Set hashSet = new HashSet(Arrays.asList(strArr2));
            int i = 0;
            while (i < strArr.length) {
                if (!hashSet.contains(strArr[i])) {
                    i++;
                } else {
                    return;
                }
            }
            throw new BundleException("Platform does not provide EEs " + Arrays.asList(strArr));
        }
    }

    boolean resolveBundle(boolean z, HashSet<BundleClassLoader> hashSet) throws BundleException {
        int i;
        if (Framework.DEBUG_CLASSLOADING && log.isInfoEnabled()) {
            log.info("BundleClassLoader: Resolving " + this.bundle + (z ? " (critical)" : " (not critical)"));
        }
        HashSet hashSet2;
        if (this.exports.length > 0) {
            HashSet hashSet3 = new HashSet(this.exports.length);
            for (String parsePackageString : this.exports) {
                hashSet3.add(Package.parsePackageString(parsePackageString)[0]);
            }
            hashSet2 = hashSet3;
        } else {
            hashSet2 = null;
        }
        if (this.imports.length > 0) {
            if (this.importDelegations == null) {
                this.importDelegations = new HashMap(this.imports.length);
            }
            for (int i2 = 0; i2 < this.imports.length; i2++) {
                Object obj = Package.parsePackageString(this.imports[i2])[0];
                if (!FRAMEWORK_PACKAGES.contains(obj) && this.importDelegations.get(obj) == null && (r0 == null || !r0.contains(obj))) {
                    BundleClassLoader bundleClassLoader = Framework.getImport(this.bundle, this.imports[i2], z, hashSet);
                    if (bundleClassLoader != null) {
                        if (bundleClassLoader != this) {
                            this.importDelegations.put(obj, bundleClassLoader);
                        }
                    } else if (z) {
                        throw new BundleException("Unsatisfied import " + this.imports[i2] + " for bundle " + this.bundle.toString(), new ClassNotFoundException("Unsatisfied import " + this.imports[i2]));
                    } else {
                        if (this.exports.length > 0) {
                            Framework.export(this, this.exports, false);
                        }
                        if (!Framework.DEBUG_CLASSLOADING || !log.isInfoEnabled()) {
                            return false;
                        }
                        log.info("BundleClassLoader: Missing import " + this.imports[i2] + ". Resolving attempt terminated unsuccessfully.");
                        return false;
                    }
                }
            }
        }
        if (this.exports.length > 0) {
            if (this.importDelegations == null) {
                this.importDelegations = new HashMap(this.imports.length);
            }
            for (i = 0; i < this.exports.length; i++) {
                BundleClassLoader bundleClassLoader2 = Framework.getImport(this.bundle, Package.parsePackageString(this.exports[i])[0], false, null);
                if (!(bundleClassLoader2 == null || bundleClassLoader2 == this)) {
                    this.importDelegations.put(Package.parsePackageString(this.exports[i])[0], bundleClassLoader2);
                }
            }
        }
        if (this.exports.length > 0) {
            Framework.export(this, this.exports, true);
        }
        return true;
    }

    void cleanup(boolean z) {
        ArrayList arrayList = new ArrayList();
        for (String str : this.exports) {
            Package packageR = (Package) Framework.exportedPackages.get(new Package(str, null, false));
            if (packageR != null) {
                if (packageR.importingBundles == null) {
                    Framework.exportedPackages.remove(packageR);
                    packageR.importingBundles = null;
                } else {
                    packageR.removalPending = true;
                    arrayList.add(packageR);
                }
            }
        }
        if (this.bundle != null) {
            if (z) {
                this.bundle.staleExportedPackages = (Package[]) arrayList.toArray(new Package[arrayList.size()]);
            } else {
                this.bundle.staleExportedPackages = null;
            }
        }
        if (this.importDelegations != null) {
            String[] strArr = (String[]) this.importDelegations.keySet().toArray(new String[this.importDelegations.size()]);
            for (String str2 : strArr) {
                Package packageR2 = (Package) Framework.exportedPackages.get(new Package(str2, null, false));
                if (!(packageR2 == null || packageR2.importingBundles == null)) {
                    packageR2.importingBundles.remove(this.bundle);
                    if (packageR2.importingBundles.isEmpty()) {
                        packageR2.importingBundles = null;
                        if (packageR2.removalPending) {
                            Framework.exportedPackages.remove(packageR2);
                        }
                    }
                }
            }
        }
        this.importDelegations = null;
        this.activator = null;
        this.originalExporter = null;
        if (z) {
            if (arrayList.size() == 0) {
                this.bundle = null;
            }
            this.activatorClassName = null;
            this.imports = null;
            this.dynamicImports = null;
        }
    }

    protected Class<?> findClass(String str) throws ClassNotFoundException {
        if (FRAMEWORK_PACKAGES.contains(packageOf(str))) {
            return Framework.systemClassLoader.loadClass(str);
        }
        Class<?> findOwnClass = findOwnClass(str);
        if (findOwnClass != null) {
            return findOwnClass;
        }
        if (this.dynamicImports.length > 0) {
            for (int i = 0; i < this.dynamicImports.length; i++) {
                if (this.dynamicImports[i].indexOf(com.taobao.tao.util.Constants.VERSION) > -1) {
                    Package[] packageArr = (Package[]) Framework.exportedPackages.keySet().toArray(new Package[Framework.exportedPackages.size()]);
                    for (int i2 = 0; i2 < packageArr.length; i2++) {
                        if (packageArr[i2].matches(this.dynamicImports[i])) {
                            Class<?> findDelegatedClass = findDelegatedClass(packageArr[i2].classloader, str);
                            if (findDelegatedClass != null) {
                                return findDelegatedClass;
                            }
                        }
                    }
                    continue;
                } else {
                    Package packageR = (Package) Framework.exportedPackages.get(new Package(packageOf(str), null, false));
                    if (packageR != null) {
                        findOwnClass = findDelegatedClass(packageR.classloader, str);
                        if (findOwnClass != null) {
                            return findOwnClass;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        if (this.importDelegations != null) {
            BundleClassLoader bundleClassLoader = (BundleClassLoader) this.importDelegations.get(packageOf(str));
            if (bundleClassLoader != null) {
                findOwnClass = findDelegatedClass(bundleClassLoader, str);
                if (findOwnClass != null) {
                    return findOwnClass;
                }
            }
        }
        try {
            findOwnClass = Framework.systemClassLoader.loadClass(str);
            if (findOwnClass != null) {
                return findOwnClass;
            }
        } catch (Exception e) {
        }
        throw new ClassNotFoundException("Can't find class " + str + " in BundleClassLoader: " + this.bundle.getLocation());
    }

    private Class<?> findOwnClass(String str) {
        try {
            return this.archive.findClass(str, this);
        } catch (Exception e) {
            if (!(e instanceof DexLoadException)) {
                return null;
            }
            throw ((DexLoadException) e);
        }
    }

    private static Class<?> findDelegatedClass(BundleClassLoader bundleClassLoader, String str) {
        Class<?> findLoadedClass;
        synchronized (bundleClassLoader) {
            findLoadedClass = bundleClassLoader.findLoadedClass(str);
            if (findLoadedClass == null) {
                findLoadedClass = bundleClassLoader.findOwnClass(str);
            }
        }
        return findLoadedClass;
    }

    protected URL findResource(String str) {
        String stripTrailing = stripTrailing(str);
        List findOwnResources = findOwnResources(stripTrailing, false);
        if (findOwnResources.size() > 0) {
            return (URL) findOwnResources.get(0);
        }
        List findImportedResources = findImportedResources(stripTrailing, false);
        return findImportedResources.size() > 0 ? (URL) findImportedResources.get(0) : null;
    }

    protected Enumeration<URL> findResources(String str) {
        String stripTrailing = stripTrailing(str);
        Collection findOwnResources = findOwnResources(stripTrailing, true);
        findOwnResources.addAll(findImportedResources(stripTrailing, true));
        return Collections.enumeration(findOwnResources);
    }

    private List<URL> findOwnResources(String str, boolean z) {
        try {
            return this.archive.getResources(str);
        } catch (IOException e) {
            e.printStackTrace();
            return EMPTY_LIST;
        }
    }

    private List<URL> findImportedResources(String str, boolean z) {
        if (this.bundle.state == 2 || this.importDelegations == null) {
            return EMPTY_LIST;
        }
        BundleClassLoader bundleClassLoader = (BundleClassLoader) this.importDelegations.get(packageOf(pseudoClassname(str)));
        if (bundleClassLoader == null) {
            return EMPTY_LIST;
        }
        return bundleClassLoader.originalExporter == null ? bundleClassLoader.findOwnResources(str, z) : bundleClassLoader.originalExporter.findOwnResources(str, z);
    }

    protected String findLibrary(String str) {
        String mapLibraryName = System.mapLibraryName(str);
        if (this.nativeLibraryDirectories != null) {
            for (File file : this.nativeLibraryDirectories) {
                File file2 = new File(file, mapLibraryName);
                if (file2.canRead()) {
                    return file2.getAbsolutePath();
                }
            }
        }
        File findLibrary = this.archive.findLibrary(mapLibraryName);
        if (findLibrary != null) {
            return findLibrary.getAbsolutePath();
        }
        try {
            return (String) AtlasHacks.ClassLoader_findLibrary.invoke(Framework.systemClassLoader, str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return "BundleClassLoader[Bundle" + this.bundle + "]";
    }

    private static String[] readProperty(Attributes attributes, String str) throws BundleException {
        String value = attributes.getValue(str);
        if (value == null || !value.equals(com.taobao.tao.util.Constants.ALIPAY_PARNER)) {
            return splitString(value);
        }
        return new String[0];
    }

    private static String[] splitString(String str) {
        int i = 0;
        if (str == null) {
            return new String[0];
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, SymbolExpUtil.SYMBOL_COMMA);
        if (stringTokenizer.countTokens() == 0) {
            return new String[]{str};
        }
        String[] strArr = new String[stringTokenizer.countTokens()];
        while (i < strArr.length) {
            strArr[i] = stringTokenizer.nextToken().trim();
            i++;
        }
        return strArr;
    }

    private static String stripTrailing(String str) {
        return (str.startsWith(FilePathGenerator.ANDROID_DIR_SEP) || str.startsWith("\\")) ? str.substring(1) : str;
    }

    private static String packageOf(String str) {
        int lastIndexOf = str.lastIndexOf(46);
        return lastIndexOf > -1 ? str.substring(0, lastIndexOf) : com.taobao.tao.util.Constants.ALIPAY_PARNER;
    }

    private static String pseudoClassname(String str) {
        return stripTrailing(str).replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, FileInfoBase.DIVISION).replace('/', ClassUtils.PACKAGE_SEPARATOR_CHAR).replace('\\', ClassUtils.PACKAGE_SEPARATOR_CHAR);
    }
}
