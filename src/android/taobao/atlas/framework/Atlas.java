package android.taobao.atlas.framework;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.taobao.atlas.hack.AndroidHack;
import android.taobao.atlas.hack.AssertionArrayException;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.runtime.BundleLifecycleHandler;
import android.taobao.atlas.runtime.ClassNotFoundInterceptorCallback;
import android.taobao.atlas.runtime.DelegateClassLoader;
import android.taobao.atlas.runtime.DelegateComponent;
import android.taobao.atlas.runtime.FrameworkLifecycleHandler;
import android.taobao.atlas.runtime.InstrumentationHook;
import android.taobao.atlas.runtime.PackageLite;
import android.taobao.atlas.runtime.RuntimeVariables;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkListener;

public class Atlas {
    protected static Atlas instance;
    static final Logger log;
    private BundleLifecycleHandler bundleLifecycleHandler;
    private FrameworkLifecycleHandler frameworkLifecycleHandler;

    static {
        log = LoggerFactory.getInstance("Atlas");
    }

    private Atlas() {
    }

    public static synchronized Atlas getInstance() {
        Atlas atlas;
        synchronized (Atlas.class) {
            if (instance == null) {
                instance = new Atlas();
            }
            atlas = instance;
        }
        return atlas;
    }

    public void init(Application application, Properties properties) throws AssertionArrayException, Exception {
        String packageName = application.getPackageName();
        AtlasHacks.defineAndVerify();
        ClassLoader classLoader = Atlas.class.getClassLoader();
        ClassLoader delegateClassLoader = new DelegateClassLoader(classLoader);
        Framework.systemClassLoader = classLoader;
        RuntimeVariables.delegateClassLoader = delegateClassLoader;
        RuntimeVariables.delegateResources = application.getResources();
        RuntimeVariables.androidApplication = application;
        AndroidHack.injectClassLoader(packageName, delegateClassLoader);
        AndroidHack.injectInstrumentationHook(new InstrumentationHook(AndroidHack.getInstrumentation(), application.getBaseContext()));
        injectApplication(application, packageName);
        this.bundleLifecycleHandler = new BundleLifecycleHandler();
        Framework.syncBundleListeners.add(this.bundleLifecycleHandler);
        this.frameworkLifecycleHandler = new FrameworkLifecycleHandler();
        Framework.frameworkListeners.add(this.frameworkLifecycleHandler);
        AndroidHack.hackH();
        Framework.initialize(properties);
    }

    public void injectApplication(Application application, String str) throws Exception {
        AtlasHacks.defineAndVerify();
        AndroidHack.injectApplication(str, application);
    }

    public void startup() throws BundleException {
        Framework.startup();
    }

    public void shutdown() throws BundleException {
        Framework.shutdown(false);
    }

    public Bundle getBundle(String str) {
        return Framework.getBundle(str);
    }

    public Bundle installBundle(String str, InputStream inputStream) throws BundleException {
        return Framework.installNewBundle(str, inputStream);
    }

    public Bundle installBundle(String str, File file) throws BundleException {
        return Framework.installNewBundle(str, file);
    }

    public void updateBundle(String str, InputStream inputStream) throws BundleException {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            bundle.update(inputStream);
            return;
        }
        throw new BundleException("Could not update bundle " + str + ", because could not find it");
    }

    public void updateBundle(String str, File file) throws BundleException {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            bundle.update(file);
            return;
        }
        throw new BundleException("Could not update bundle " + str + ", because could not find it");
    }

    public void installOrUpdate(String[] strArr, File[] fileArr) throws BundleException {
        Framework.installOrUpdate(strArr, fileArr);
    }

    public void uninstallBundle(String str) throws BundleException {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            BundleImpl bundleImpl = (BundleImpl) bundle;
            try {
                File archiveFile = bundleImpl.getArchive().getArchiveFile();
                if (archiveFile.canWrite()) {
                    archiveFile.delete();
                }
                bundleImpl.getArchive().purge();
                File revisionDir = bundleImpl.getArchive().getCurrentRevision().getRevisionDir();
                bundle.uninstall();
                if (revisionDir != null) {
                    Framework.deleteDirectory(revisionDir);
                    return;
                }
                return;
            } catch (Exception e) {
                log.error("uninstall bundle error: " + str + e.getMessage());
                return;
            }
        }
        throw new BundleException("Could not uninstall bundle " + str + ", because could not find it");
    }

    public List<Bundle> getBundles() {
        return Framework.getBundles();
    }

    public Resources getDelegateResources() {
        return RuntimeVariables.delegateResources;
    }

    public ClassLoader getDelegateClassLoader() {
        return RuntimeVariables.delegateClassLoader;
    }

    public Class getComponentClass(String str) throws ClassNotFoundException {
        return RuntimeVariables.delegateClassLoader.loadClass(str);
    }

    public ClassLoader getBundleClassLoader(String str) {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            return ((BundleImpl) bundle).getClassLoader();
        }
        return null;
    }

    public PackageLite getBundlePackageLite(String str) {
        return DelegateComponent.getPackage(str);
    }

    public File getBundleFile(String str) {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            return ((BundleImpl) bundle).archive.getArchiveFile();
        }
        return null;
    }

    public InputStream openAssetInputStream(String str, String str2) throws IOException {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            return ((BundleImpl) bundle).archive.openAssetInputStream(str2);
        }
        return null;
    }

    public InputStream openNonAssetInputStream(String str, String str2) throws IOException {
        Bundle bundle = Framework.getBundle(str);
        if (bundle != null) {
            return ((BundleImpl) bundle).archive.openNonAssetInputStream(str2);
        }
        return null;
    }

    public void addFrameworkListener(FrameworkListener frameworkListener) {
        Framework.addFrameworkListener(frameworkListener);
    }

    public void removeFrameworkListener(FrameworkListener frameworkListener) {
        Framework.removeFrameworkListener(frameworkListener);
    }

    public void addBundleListener(BundleListener bundleListener) {
        Framework.addBundleListener(bundleListener);
    }

    public void removeBundleListener(BundleListener bundleListener) {
        Framework.removeBundleListener(bundleListener);
    }

    public void onLowMemory() {
        this.bundleLifecycleHandler.handleLowMemory();
    }

    public void enableComponent(String str) {
        PackageLite packageLite = DelegateComponent.getPackage(str);
        if (packageLite != null && packageLite.disableComponents != null) {
            for (String str2 : packageLite.disableComponents) {
                PackageManager packageManager = RuntimeVariables.androidApplication.getPackageManager();
                ComponentName componentName = new ComponentName(RuntimeVariables.androidApplication.getPackageName(), str2);
                try {
                    packageManager.setComponentEnabledSetting(componentName, 1, 1);
                    log.debug("enableComponent: " + componentName.getClassName());
                } catch (Exception e) {
                    log.error("enableComponent error: " + componentName.getClassName() + e.getMessage());
                }
            }
        }
    }

    public void setClassNotFoundInterceptorCallback(ClassNotFoundInterceptorCallback classNotFoundInterceptorCallback) {
        Framework.setClassNotFoundCallback(classNotFoundInterceptorCallback);
    }
}
