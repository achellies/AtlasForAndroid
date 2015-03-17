package android.taobao.atlas.runtime;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Looper;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.util.StringUtils;
import com.taobao.dp.DeviceSecuritySDK;
import com.taobao.open.OpenBase;
import com.taobao.securityjni.StaticDataStore;
import com.taobao.wireless.security.sdk.staticdataencrypt.IStaticDataEncryptComponent;
import mtopsdk.common.util.SymbolExpUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServicePermission;
import org.osgi.framework.SynchronousBundleListener;

public class BundleLifecycleHandler implements SynchronousBundleListener {
    static final Logger log;

    private class BundleStartTask extends AsyncTask<Bundle, Void, Void> {
        private BundleStartTask() {
        }

        protected Void doInBackground(Bundle... bundleArr) {
            BundleLifecycleHandler.this.started(bundleArr[0]);
            return null;
        }
    }

    static {
        log = LoggerFactory.getInstance("BundleLifecycleHandler");
    }

    @SuppressLint({"NewApi"})
    public void bundleChanged(BundleEvent bundleEvent) {
        switch (bundleEvent.getType()) {
            case DeviceSecuritySDK.ENVIRONMENT_ONLINE /*0*/:
                loaded(bundleEvent.getBundle());
            case OpenBase.OAUTH_CREATE /*1*/:
                installed(bundleEvent.getBundle());
            case StaticDataStore.SECURITY_KEY_TYPE /*2*/:
                if (isLewaOS()) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    started(bundleEvent.getBundle());
                } else if (Framework.isFrameworkStartupShutdown()) {
                    BundleStartTask bundleStartTask = new BundleStartTask();
                    if (VERSION.SDK_INT > 11) {
                        bundleStartTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Bundle[]{bundleEvent.getBundle()});
                        return;
                    }
                    bundleStartTask.execute(new Bundle[]{bundleEvent.getBundle()});
                } else {
                    started(bundleEvent.getBundle());
                }
            case StaticDataStore.INVALID_KEY_TYPE /*4*/:
                stopped(bundleEvent.getBundle());
            case IStaticDataEncryptComponent.GCRY_CIPHER_SERPENT128 /*8*/:
                updated(bundleEvent.getBundle());
            case IStaticDataEncryptComponent.GCRY_CIPHER_AES128 /*16*/:
                uninstalled(bundleEvent.getBundle());
            default:
        }
    }

    private void loaded(Bundle bundle) {
        long currentTimeMillis = System.currentTimeMillis();
        BundleImpl bundleImpl = (BundleImpl) bundle;
        try {
            DelegateResources.newDelegateResources(RuntimeVariables.androidApplication, RuntimeVariables.delegateResources);
        } catch (Throwable e) {
            log.error("Could not load resource in bundle " + bundleImpl.getLocation(), e);
        }
        if (DelegateComponent.getPackage(bundle.getLocation()) == null) {
            PackageLite parse = PackageLite.parse(bundleImpl.getArchive().getArchiveFile());
            log.info("Bundle installation info " + bundle.getLocation() + ":" + parse.components);
            DelegateComponent.putPackage(bundle.getLocation(), parse);
        }
        log.info("loaded() spend " + (System.currentTimeMillis() - currentTimeMillis) + " milliseconds");
    }

    private void installed(Bundle bundle) {
    }

    private void updated(Bundle bundle) {
    }

    private void uninstalled(Bundle bundle) {
        DelegateComponent.removePackage(bundle.getLocation());
    }

    private void started(Bundle bundle) {
        BundleImpl bundleImpl = (BundleImpl) bundle;
        long currentTimeMillis = System.currentTimeMillis();
        String str = (String) bundleImpl.getHeaders().get("Bundle-Application");
        if (StringUtils.isNotEmpty(str)) {
            String[] strArr;
            String[] split = StringUtils.split(str, SymbolExpUtil.SYMBOL_COMMA);
            if (split == null || split.length == 0) {
                strArr = new String[]{str};
            } else {
                strArr = split;
            }
            if (strArr != null) {
                for (String str2 : strArr) {
                    String trim = StringUtils.trim(str2);
                    if (StringUtils.isNotEmpty(trim)) {
                        try {
                            Application newApplication;
                            int i;
                            for (Application newApplication2 : DelegateComponent.apkApplications.values()) {
                                if (newApplication2.getClass().getName().equals(trim)) {
                                    i = 1;
                                    break;
                                }
                            }
                            i = 0;
                            if (i == 0) {
                                newApplication2 = newApplication(trim, bundleImpl.getClassLoader());
                                newApplication2.onCreate();
                                DelegateComponent.apkApplications.put("system:" + trim, newApplication2);
                            }
                        } catch (Throwable th) {
                            log.error("Error to start application", th);
                        }
                    }
                }
            }
        } else {
            PackageLite packageLite = DelegateComponent.getPackage(bundleImpl.getLocation());
            if (packageLite != null) {
                str2 = packageLite.applicationClassName;
                if (StringUtils.isNotEmpty(str2)) {
                    try {
                        newApplication(str2, bundleImpl.getClassLoader()).onCreate();
                    } catch (Throwable th2) {
                        log.error("Error to start application >>>", th2);
                    }
                }
            }
        }
        log.info("started() spend " + (System.currentTimeMillis() - currentTimeMillis) + " milliseconds");
    }

    protected static Application newApplication(String str, ClassLoader classLoader) throws Exception {
        Class loadClass = classLoader.loadClass(str);
        if (loadClass == null) {
            throw new ClassNotFoundException(str);
        }
        Application application = (Application) loadClass.newInstance();
        AtlasHacks.Application_attach.invoke(application, RuntimeVariables.androidApplication);
        return application;
    }

    private void stopped(Bundle bundle) {
        Application application = (Application) DelegateComponent.apkApplications.get(bundle.getLocation());
        if (application != null) {
            application.onTerminate();
            DelegateComponent.apkApplications.remove(bundle.getLocation());
        }
    }

    public void handleLowMemory() {
    }

    private boolean isLewaOS() {
        try {
            return StringUtils.isNotEmpty((String) Class.forName("android.os.SystemProperties").getDeclaredMethod(ServicePermission.GET, new Class[]{String.class}).invoke(null, new Object[]{"ro.lewa.version"}));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
