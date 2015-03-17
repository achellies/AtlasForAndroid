package android.taobao.atlas.runtime;

import android.content.pm.PackageInfo;
import android.taobao.atlas.bundleInfo.BundleInfoList;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision.DexLoadException;
import android.util.Log;
import com.taobao.tao.util.Constants;
import com.taobao.weapp.tb.utils.CacheUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.osgi.framework.Bundle;

public class ClassLoadFromBundle {
    private static final String TAG = "ClassLoadFromBundle";
    private static Hashtable classNotFoundReason;
    private static int reasonCnt;
    public static List<String> sInternalBundles;

    static {
        classNotFoundReason = new Hashtable();
        reasonCnt = 0;
    }

    public static String getClassNotFoundReason(String str) {
        for (int i = 0; i < classNotFoundReason.size(); i++) {
            if ((classNotFoundReason.get(Integer.valueOf(i)) + Constants.ALIPAY_PARNER).contains(str + Constants.ALIPAY_PARNER)) {
                return classNotFoundReason.get(Integer.valueOf(i)) + Constants.ALIPAY_PARNER;
            }
        }
        return Constants.ALIPAY_PARNER;
    }

    private static void insertToReasonList(String str, String str2) {
        classNotFoundReason.put(Integer.valueOf(reasonCnt), " Not found class " + str + " because " + str2);
        int i = reasonCnt + 1;
        reasonCnt = i;
        reasonCnt = i % 10;
    }

    public static String getPackageNameFromEntryName(String str) {
        return str.substring(str.indexOf("lib/armeabi/lib") + "lib/armeabi/lib".length(), str.indexOf(".so")).replace(CacheUtils.CACHE_KEY_SEP, ".");
    }

    public static synchronized void resolveInternalBundles() {
        synchronized (ClassLoadFromBundle.class) {
            if (sInternalBundles == null || sInternalBundles.size() == 0) {
                String str = "lib/armeabi/libcom_";
                String str2 = ".so";
                List arrayList = new ArrayList();
                try {
                    Enumeration entries = new ZipFile(RuntimeVariables.androidApplication.getApplicationInfo().sourceDir).entries();
                    while (entries.hasMoreElements()) {
                        String name = ((ZipEntry) entries.nextElement()).getName();
                        if (name.startsWith(str) && name.endsWith(str2)) {
                            arrayList.add(getPackageNameFromEntryName(name));
                        }
                    }
                    sInternalBundles = arrayList;
                } catch (Throwable e) {
                    Log.e(TAG, "Exception while get bundles in assets or lib", e);
                }
            }
        }
    }

    static Class<?> loadFromUninstalledBundles(String str) throws ClassNotFoundException {
        if (sInternalBundles == null) {
            resolveInternalBundles();
        }
        BundleInfoList instance = BundleInfoList.getInstance();
        String bundleForComponet = instance.getBundleForComponet(str);
        if (bundleForComponet == null) {
            "Failed to find the bundle in BundleInfoList for component " + str;
            insertToReasonList(str, "not found in BundleInfoList!");
            return null;
        } else if (sInternalBundles != null && !sInternalBundles.contains(bundleForComponet)) {
            return null;
        } else {
            Bundle installBundle;
            List<String> linkedList = new LinkedList();
            if (instance.getDependencyForBundle(bundleForComponet) != null) {
                linkedList.addAll(instance.getDependencyForBundle(bundleForComponet));
            }
            linkedList.add(bundleForComponet);
            for (String str2 : linkedList) {
                File file = new File(new File(Framework.getProperty("android.taobao.atlas.AppDirectory"), "lib"), "lib".concat(str2.replace(".", CacheUtils.CACHE_KEY_SEP)).concat(".so"));
                if (Atlas.getInstance().getBundle(str2) == null) {
                    try {
                        if (!file.exists()) {
                            return null;
                        }
                        installBundle = Atlas.getInstance().installBundle(str2, file);
                        if (installBundle != null) {
                            "Succeed to install bundle " + str2;
                        }
                        try {
                            long currentTimeMillis = System.currentTimeMillis();
                            ((BundleImpl) installBundle).optDexFile();
                            "Succeed to dexopt bundle " + str2 + " cost time = " + (System.currentTimeMillis() - currentTimeMillis) + " ms";
                        } catch (Throwable e) {
                            Log.e(TAG, "Error while dexopt >>>", e);
                            insertToReasonList(str, "dexopt failed!");
                            if (!(e instanceof DexLoadException)) {
                                return null;
                            }
                            throw ((RuntimeException) e);
                        }
                    } catch (Throwable e2) {
                        Log.e(TAG, "Could not install bundle.", e2);
                        insertToReasonList(str, "bundle installation failed");
                        return null;
                    }
                }
            }
            installBundle = Atlas.getInstance().getBundle(bundleForComponet);
            ClassLoader classLoader = ((BundleImpl) installBundle).getClassLoader();
            if (classLoader != null) {
                try {
                    Class<?> loadClass = classLoader.loadClass(str);
                    if (loadClass != null) {
                        return loadClass;
                    }
                } catch (ClassNotFoundException e3) {
                }
            }
            throw new ClassNotFoundException("Can't find class " + str + " in BundleClassLoader: " + installBundle.getLocation());
        }
    }

    static Class<?> loadFromInstalledBundles(String str) throws ClassNotFoundException {
        BundleImpl bundleImpl;
        int i = 0;
        Class<?> cls = null;
        List<Bundle> bundles = Framework.getBundles();
        if (!(bundles == null || bundles.isEmpty())) {
            for (Bundle bundle : bundles) {
                bundleImpl = (BundleImpl) bundle;
                PackageLite packageLite = DelegateComponent.getPackage(bundleImpl.getLocation());
                if (packageLite != null && packageLite.components.contains(str)) {
                    bundleImpl.getArchive().optDexFile();
                    ClassLoader classLoader = bundleImpl.getClassLoader();
                    if (classLoader != null) {
                        try {
                            cls = classLoader.loadClass(str);
                            if (cls != null) {
                                return cls;
                            }
                        } catch (ClassNotFoundException e) {
                            throw new ClassNotFoundException("Can't find class " + str + " in BundleClassLoader: " + bundleImpl.getLocation() + " [" + (bundles == null ? 0 : bundles.size()) + "]" + "classloader is: " + (classLoader == null ? "null" : "not null") + " packageversion " + getPackageVersion() + " exception:" + e.getMessage());
                        }
                    }
                    StringBuilder append = new StringBuilder().append("Can't find class ").append(str).append(" in BundleClassLoader: ").append(bundleImpl.getLocation()).append(" [");
                    if (bundles != null) {
                        i = bundles.size();
                    }
                    throw new ClassNotFoundException(append.append(i).append("]").append(classLoader == null ? "classloader is null" : "classloader not null").append(" packageversion ").append(getPackageVersion()).toString());
                }
            }
        }
        if (!(bundles == null || bundles.isEmpty())) {
            Class<?> cls2 = null;
            for (Bundle bundle2 : Framework.getBundles()) {
                bundleImpl = (BundleImpl) bundle2;
                if (bundleImpl.getArchive().isDexOpted()) {
                    Class<?> loadClass;
                    ClassLoader classLoader2 = bundleImpl.getClassLoader();
                    if (classLoader2 != null) {
                        try {
                            loadClass = classLoader2.loadClass(str);
                            if (loadClass != null) {
                                return loadClass;
                            }
                        } catch (ClassNotFoundException e2) {
                        }
                    } else {
                        loadClass = cls2;
                    }
                    cls2 = loadClass;
                }
            }
            cls = cls2;
        }
        return cls;
    }

    private static int getPackageVersion() {
        PackageInfo packageInfo;
        try {
            packageInfo = RuntimeVariables.androidApplication.getPackageManager().getPackageInfo(RuntimeVariables.androidApplication.getPackageName(), 0);
        } catch (Throwable e) {
            Log.e(TAG, "Error to get PackageInfo >>>", e);
            packageInfo = new PackageInfo();
        }
        return packageInfo.versionCode;
    }
}
