package android.taobao.atlas.hack;

import android.app.Application;
import android.app.Instrumentation;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.taobao.atlas.hack.Hack.HackDeclaration.HackAssertionException;
import android.taobao.atlas.runtime.DelegateClassLoader;
import android.taobao.atlas.runtime.DelegateResources;
import android.taobao.atlas.runtime.RuntimeVariables;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class AndroidHack {
    private static Object _mLoadedApk;
    private static Object _sActivityThread;

    final class AnonymousClass_1 implements Callback {
        final /* synthetic */ Object val$activityThread;
        final /* synthetic */ Handler val$handler;

        AnonymousClass_1(Handler handler, Object obj) {
            this.val$handler = handler;
            this.val$activityThread = obj;
        }

        public boolean handleMessage(Message message) {
            try {
                AndroidHack.ensureLoadedApk();
                this.val$handler.handleMessage(message);
                AndroidHack.ensureLoadedApk();
            } catch (Throwable th) {
                Throwable th2 = th;
                RuntimeException runtimeException;
                if ((th2 instanceof ClassNotFoundException) || th2.toString().contains("ClassNotFoundException")) {
                    if (message.what != 113) {
                        Object loadedApk = AndroidHack.getLoadedApk(RuntimeVariables.androidApplication, this.val$activityThread, RuntimeVariables.androidApplication.getPackageName());
                        if (loadedApk == null) {
                            runtimeException = new RuntimeException("loadedapk is null");
                        } else {
                            ClassLoader classLoader = (ClassLoader) AtlasHacks.LoadedApk_mClassLoader.get(loadedApk);
                            if (classLoader instanceof DelegateClassLoader) {
                                runtimeException = new RuntimeException("From Atlas:classNotFound ---", th2);
                            } else {
                                RuntimeException runtimeException2 = new RuntimeException("wrong classloader in loadedapk---" + classLoader.getClass().getName(), th2);
                            }
                        }
                    }
                } else if ((th2 instanceof ClassCastException) || th2.toString().contains("ClassCastException")) {
                    Process.killProcess(Process.myPid());
                } else {
                    runtimeException = new RuntimeException(th2);
                }
            }
            return true;
        }
    }

    static class ActvityThreadGetter implements Runnable {
        ActvityThreadGetter() {
        }

        public void run() {
            try {
                AndroidHack._sActivityThread = AtlasHacks.ActivityThread_currentActivityThread.invoke(AtlasHacks.ActivityThread.getmClass(), new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (AtlasHacks.ActivityThread_currentActivityThread) {
                AtlasHacks.ActivityThread_currentActivityThread.notify();
            }
        }
    }

    static {
        _sActivityThread = null;
        _mLoadedApk = null;
    }

    public static Object getActivityThread() throws Exception {
        if (_sActivityThread == null) {
            if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                _sActivityThread = AtlasHacks.ActivityThread_currentActivityThread.invoke(null, new Object[0]);
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                synchronized (AtlasHacks.ActivityThread_currentActivityThread) {
                    handler.post(new ActvityThreadGetter());
                    AtlasHacks.ActivityThread_currentActivityThread.wait();
                }
            }
        }
        return _sActivityThread;
    }

    public static Handler hackH() throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        try {
            Handler handler = (Handler) AtlasHacks.ActivityThread.field("mH").ofType(Hack.into("android.app.ActivityThread$H").getmClass()).get(activityThread);
            Field declaredField = Handler.class.getDeclaredField("mCallback");
            declaredField.setAccessible(true);
            declaredField.set(handler, new AnonymousClass_1(handler, activityThread));
        } catch (HackAssertionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void ensureLoadedApk() throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        Object loadedApk = getLoadedApk(RuntimeVariables.androidApplication, activityThread, RuntimeVariables.androidApplication.getPackageName());
        if (loadedApk == null) {
            loadedApk = createNewLoadedApk(RuntimeVariables.androidApplication, activityThread);
            if (loadedApk == null) {
                throw new RuntimeException("can't create loadedApk");
            }
        }
        activityThread = loadedApk;
        if (!(((ClassLoader) AtlasHacks.LoadedApk_mClassLoader.get(activityThread)) instanceof DelegateClassLoader)) {
            AtlasHacks.LoadedApk_mClassLoader.set(activityThread, RuntimeVariables.delegateClassLoader);
            AtlasHacks.LoadedApk_mResources.set(activityThread, RuntimeVariables.delegateResources);
        }
    }

    public static Object getLoadedApk(Application application, Object obj, String str) {
        WeakReference weakReference = (WeakReference) ((Map) AtlasHacks.ActivityThread_mPackages.get(obj)).get(str);
        if (weakReference == null || weakReference.get() == null) {
            return null;
        }
        _mLoadedApk = weakReference.get();
        return weakReference.get();
    }

    public static Object createNewLoadedApk(Application application, Object obj) {
        try {
            Method declaredMethod;
            ApplicationInfo applicationInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), 1152);
            application.getPackageManager();
            Resources resources = application.getResources();
            if (resources instanceof DelegateResources) {
                declaredMethod = resources.getClass().getSuperclass().getDeclaredMethod("getCompatibilityInfo", new Class[0]);
            } else {
                declaredMethod = resources.getClass().getDeclaredMethod("getCompatibilityInfo", new Class[0]);
            }
            declaredMethod.setAccessible(true);
            Class cls = Class.forName("android.content.res.CompatibilityInfo");
            Object invoke = declaredMethod.invoke(application.getResources(), new Object[0]);
            Method declaredMethod2 = AtlasHacks.ActivityThread.getmClass().getDeclaredMethod("getPackageInfoNoCheck", new Class[]{ApplicationInfo.class, cls});
            declaredMethod2.setAccessible(true);
            invoke = declaredMethod2.invoke(obj, new Object[]{applicationInfo, invoke});
            _mLoadedApk = invoke;
            return invoke;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void injectClassLoader(String str, ClassLoader classLoader) throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        Object loadedApk = getLoadedApk(RuntimeVariables.androidApplication, activityThread, str);
        if (loadedApk == null) {
            loadedApk = createNewLoadedApk(RuntimeVariables.androidApplication, activityThread);
        }
        if (loadedApk == null) {
            throw new Exception("Failed to get ActivityThread.mLoadedApk");
        }
        AtlasHacks.LoadedApk_mClassLoader.set(loadedApk, classLoader);
    }

    public static void injectApplication(String str, Application application) throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        Object loadedApk = getLoadedApk(application, activityThread, application.getPackageName());
        if (loadedApk == null) {
            throw new Exception("Failed to get ActivityThread.mLoadedApk");
        }
        AtlasHacks.LoadedApk_mApplication.set(loadedApk, application);
        AtlasHacks.ActivityThread_mInitialApplication.set(activityThread, application);
    }

    public static void injectResources(Application application, Resources resources) throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        Object loadedApk = getLoadedApk(application, activityThread, application.getPackageName());
        if (loadedApk == null) {
            activityThread = createNewLoadedApk(application, activityThread);
            if (activityThread == null) {
                throw new RuntimeException("Failed to get ActivityThread.mLoadedApk");
            }
            if (!(((ClassLoader) AtlasHacks.LoadedApk_mClassLoader.get(activityThread)) instanceof DelegateClassLoader)) {
                AtlasHacks.LoadedApk_mClassLoader.set(activityThread, RuntimeVariables.delegateClassLoader);
            }
            loadedApk = activityThread;
        }
        AtlasHacks.LoadedApk_mResources.set(loadedApk, resources);
        AtlasHacks.ContextImpl_mResources.set(application.getBaseContext(), resources);
        AtlasHacks.ContextImpl_mTheme.set(application.getBaseContext(), null);
    }

    public static Instrumentation getInstrumentation() throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread != null) {
            return (Instrumentation) AtlasHacks.ActivityThread_mInstrumentation.get(activityThread);
        }
        throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
    }

    public static void injectInstrumentationHook(Instrumentation instrumentation) throws Exception {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        AtlasHacks.ActivityThread_mInstrumentation.set(activityThread, instrumentation);
    }

    public static void injectContextHook(ContextWrapper contextWrapper, ContextWrapper contextWrapper2) {
        AtlasHacks.ContextWrapper_mBase.set(contextWrapper, contextWrapper2);
    }
}
