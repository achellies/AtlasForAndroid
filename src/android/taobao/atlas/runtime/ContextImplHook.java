package android.taobao.atlas.runtime;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.util.StringUtils;
import android.text.TextUtils;
import org.osgi.framework.BundleException;

public class ContextImplHook extends ContextWrapper {
    static final Logger log;
    private ClassLoader classLoader;

    static {
        log = LoggerFactory.getInstance("ContextImplHook");
    }

    public ContextImplHook(Context context, ClassLoader classLoader) {
        super(context);
        this.classLoader = null;
        this.classLoader = classLoader;
    }

    public Resources getResources() {
        return RuntimeVariables.delegateResources;
    }

    public AssetManager getAssets() {
        return RuntimeVariables.delegateResources.getAssets();
    }

    public PackageManager getPackageManager() {
        return getApplicationContext().getPackageManager();
    }

    public ClassLoader getClassLoader() {
        if (this.classLoader != null) {
            return this.classLoader;
        }
        return super.getClassLoader();
    }

    public void startActivity(Intent intent) {
        String packageName;
        Object obj = null;
        if (intent.getComponent() != null) {
            packageName = intent.getComponent().getPackageName();
            obj = intent.getComponent().getClassName();
        } else {
            ResolveInfo resolveActivity = getBaseContext().getPackageManager().resolveActivity(intent, 0);
            if (resolveActivity == null || resolveActivity.activityInfo == null) {
                packageName = null;
            } else {
                packageName = resolveActivity.activityInfo.packageName;
                obj = resolveActivity.activityInfo.name;
            }
        }
        if (!StringUtils.equals(getBaseContext().getPackageName(), packageName)) {
            super.startActivity(intent);
        } else if (DelegateComponent.locateComponent(obj) != null) {
            super.startActivity(intent);
        } else {
            try {
                if (ClassLoadFromBundle.loadFromUninstalledBundles(obj) != null) {
                    super.startActivity(intent);
                    return;
                }
            } catch (ClassNotFoundException e) {
                log.info("Can't find class " + obj + " in all bundles.");
            }
            try {
                if (Framework.getSystemClassLoader().loadClass(obj) != null) {
                    super.startActivity(intent);
                }
            } catch (ClassNotFoundException e2) {
                log.error("Can't find class " + obj);
                if (Framework.getClassNotFoundCallback() != null) {
                    if (intent.getComponent() == null && !TextUtils.isEmpty(obj)) {
                        intent.setClassName(this, obj);
                    }
                    if (intent.getComponent() != null) {
                        Framework.getClassNotFoundCallback().returnIntent(intent);
                    }
                }
            }
        }
    }

    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        String packageName;
        String str = null;
        if (intent.getComponent() != null) {
            packageName = intent.getComponent().getPackageName();
            str = intent.getComponent().getClassName();
        } else {
            ResolveInfo resolveService = getBaseContext().getPackageManager().resolveService(intent, 0);
            if (resolveService == null || resolveService.serviceInfo == null) {
                packageName = null;
            } else {
                packageName = resolveService.serviceInfo.packageName;
                str = resolveService.serviceInfo.name;
            }
        }
        if (!StringUtils.equals(getBaseContext().getPackageName(), packageName)) {
            return super.bindService(intent, serviceConnection, i);
        }
        packageName = DelegateComponent.locateComponent(str);
        if (packageName != null) {
            BundleImpl bundleImpl = (BundleImpl) Framework.getBundle(packageName);
            if (bundleImpl != null) {
                try {
                    bundleImpl.startBundle();
                } catch (BundleException e) {
                    log.error(e.getMessage() + " Caused by: ", e.getNestedException());
                }
            }
            return super.bindService(intent, serviceConnection, i);
        }
        try {
            if (ClassLoadFromBundle.loadFromUninstalledBundles(str) != null) {
                return super.bindService(intent, serviceConnection, i);
            }
        } catch (ClassNotFoundException e2) {
            log.info("Can't find class " + str + " in all bundles.");
        }
        try {
            if (Framework.getSystemClassLoader().loadClass(str) != null) {
                return super.bindService(intent, serviceConnection, i);
            }
        } catch (ClassNotFoundException e3) {
            log.error("Can't find class " + str);
        }
        return false;
    }

    public ComponentName startService(Intent intent) {
        String packageName;
        String className;
        if (intent.getComponent() != null) {
            packageName = intent.getComponent().getPackageName();
            className = intent.getComponent().getClassName();
        } else {
            ResolveInfo resolveService = getBaseContext().getPackageManager().resolveService(intent, 0);
            if (resolveService == null || resolveService.serviceInfo == null) {
                className = null;
                packageName = null;
            } else {
                packageName = resolveService.serviceInfo.packageName;
                className = resolveService.serviceInfo.name;
            }
        }
        if (!StringUtils.equals(getBaseContext().getPackageName(), packageName)) {
            return super.startService(intent);
        }
        packageName = DelegateComponent.locateComponent(className);
        if (packageName != null) {
            BundleImpl bundleImpl = (BundleImpl) Framework.getBundle(packageName);
            if (bundleImpl != null) {
                try {
                    bundleImpl.startBundle();
                } catch (BundleException e) {
                    log.error(e.getMessage() + " Caused by: ", e.getNestedException());
                }
            }
            return super.startService(intent);
        }
        try {
            if (ClassLoadFromBundle.loadFromUninstalledBundles(className) != null) {
                return super.startService(intent);
            }
        } catch (ClassNotFoundException e2) {
            log.info("Can't find class " + className + " in all bundles.");
        }
        try {
            if (Framework.getSystemClassLoader().loadClass(className) != null) {
                return super.startService(intent);
            }
            return null;
        } catch (ClassNotFoundException e3) {
            log.error("Can't find class " + className);
            return null;
        }
    }
}
