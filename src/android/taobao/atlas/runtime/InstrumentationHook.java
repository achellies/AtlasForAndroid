package android.taobao.atlas.runtime;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.app.Instrumentation.ActivityResult;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.taobao.atlas.framework.BundleClassLoader;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.util.StringUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.taobao.tao.detail.export.DetailConstants;
import java.util.List;
import org.osgi.framework.BundleException;

public class InstrumentationHook extends Instrumentation {
    static final Logger log;
    private Context context;
    private Instrumentation mBase;

    private static interface ExecStartActivityCallback {
        ActivityResult execStartActivity();
    }

    class AnonymousClass_1 implements ExecStartActivityCallback {
        final /* synthetic */ IBinder val$contextThread;
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ int val$requestCode;
        final /* synthetic */ Activity val$target;
        final /* synthetic */ IBinder val$token;
        final /* synthetic */ Context val$who;

        AnonymousClass_1(Context context, IBinder iBinder, IBinder iBinder2, Activity activity, Intent intent, int i) {
            this.val$who = context;
            this.val$contextThread = iBinder;
            this.val$token = iBinder2;
            this.val$target = activity;
            this.val$intent = intent;
            this.val$requestCode = i;
        }

        public ActivityResult execStartActivity() {
            return InstrumentationHook.this.mBase.execStartActivity(this.val$who, this.val$contextThread, this.val$token, this.val$target, this.val$intent, this.val$requestCode);
        }
    }

    class AnonymousClass_2 implements ExecStartActivityCallback {
        final /* synthetic */ IBinder val$contextThread;
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ int val$requestCode;
        final /* synthetic */ Activity val$target;
        final /* synthetic */ IBinder val$token;
        final /* synthetic */ Context val$who;

        AnonymousClass_2(Context context, IBinder iBinder, IBinder iBinder2, Activity activity, Intent intent, int i, Bundle bundle) {
            this.val$who = context;
            this.val$contextThread = iBinder;
            this.val$token = iBinder2;
            this.val$target = activity;
            this.val$intent = intent;
            this.val$requestCode = i;
            this.val$options = bundle;
        }

        public ActivityResult execStartActivity() {
            return InstrumentationHook.this.mBase.execStartActivity(this.val$who, this.val$contextThread, this.val$token, this.val$target, this.val$intent, this.val$requestCode, this.val$options);
        }
    }

    class AnonymousClass_3 implements ExecStartActivityCallback {
        final /* synthetic */ IBinder val$contextThread;
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ int val$requestCode;
        final /* synthetic */ Fragment val$target;
        final /* synthetic */ IBinder val$token;
        final /* synthetic */ Context val$who;

        AnonymousClass_3(Context context, IBinder iBinder, IBinder iBinder2, Fragment fragment, Intent intent, int i) {
            this.val$who = context;
            this.val$contextThread = iBinder;
            this.val$token = iBinder2;
            this.val$target = fragment;
            this.val$intent = intent;
            this.val$requestCode = i;
        }

        public ActivityResult execStartActivity() {
            return InstrumentationHook.this.mBase.execStartActivity(this.val$who, this.val$contextThread, this.val$token, this.val$target, this.val$intent, this.val$requestCode);
        }
    }

    class AnonymousClass_4 implements ExecStartActivityCallback {
        final /* synthetic */ IBinder val$contextThread;
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ int val$requestCode;
        final /* synthetic */ Fragment val$target;
        final /* synthetic */ IBinder val$token;
        final /* synthetic */ Context val$who;

        AnonymousClass_4(Context context, IBinder iBinder, IBinder iBinder2, Fragment fragment, Intent intent, int i, Bundle bundle) {
            this.val$who = context;
            this.val$contextThread = iBinder;
            this.val$token = iBinder2;
            this.val$target = fragment;
            this.val$intent = intent;
            this.val$requestCode = i;
            this.val$options = bundle;
        }

        public ActivityResult execStartActivity() {
            return InstrumentationHook.this.mBase.execStartActivity(this.val$who, this.val$contextThread, this.val$token, this.val$target, this.val$intent, this.val$requestCode, this.val$options);
        }
    }

    static {
        log = LoggerFactory.getInstance("InstrumentationHook");
    }

    public InstrumentationHook(Instrumentation instrumentation, Context context) {
        this.context = context;
        this.mBase = instrumentation;
    }

    public ActivityResult execStartActivity(Context context, IBinder iBinder, IBinder iBinder2, Activity activity, Intent intent, int i) {
        return execStartActivityInternal(this.context, intent, new AnonymousClass_1(context, iBinder, iBinder2, activity, intent, i));
    }

    @TargetApi(16)
    public ActivityResult execStartActivity(Context context, IBinder iBinder, IBinder iBinder2, Activity activity, Intent intent, int i, Bundle bundle) {
        return execStartActivityInternal(this.context, intent, new AnonymousClass_2(context, iBinder, iBinder2, activity, intent, i, bundle));
    }

    @TargetApi(14)
    public ActivityResult execStartActivity(Context context, IBinder iBinder, IBinder iBinder2, Fragment fragment, Intent intent, int i) {
        return execStartActivityInternal(this.context, intent, new AnonymousClass_3(context, iBinder, iBinder2, fragment, intent, i));
    }

    @TargetApi(16)
    public ActivityResult execStartActivity(Context context, IBinder iBinder, IBinder iBinder2, Fragment fragment, Intent intent, int i, Bundle bundle) {
        return execStartActivityInternal(this.context, intent, new AnonymousClass_4(context, iBinder, iBinder2, fragment, intent, i, bundle));
    }

    private ActivityResult execStartActivityInternal(Context context, Intent intent, ExecStartActivityCallback execStartActivityCallback) {
        String packageName;
        Object className;
        if (intent.getComponent() != null) {
            packageName = intent.getComponent().getPackageName();
            className = intent.getComponent().getClassName();
        } else {
            ResolveInfo resolveActivity = context.getPackageManager().resolveActivity(intent, 0);
            if (resolveActivity == null || resolveActivity.activityInfo == null) {
                className = null;
                packageName = null;
            } else {
                packageName = resolveActivity.activityInfo.packageName;
                className = resolveActivity.activityInfo.name;
            }
        }
        if (!StringUtils.equals(context.getPackageName(), packageName)) {
            return execStartActivityCallback.execStartActivity();
        }
        if (DelegateComponent.locateComponent(className) != null) {
            return execStartActivityCallback.execStartActivity();
        }
        try {
            if (ClassLoadFromBundle.loadFromUninstalledBundles(className) != null) {
                return execStartActivityCallback.execStartActivity();
            }
        } catch (ClassNotFoundException e) {
            log.info("Can't find class " + className + " in all bundles.");
        }
        try {
            if (Framework.getSystemClassLoader().loadClass(className) != null) {
                return execStartActivityCallback.execStartActivity();
            }
            return null;
        } catch (ClassNotFoundException e2) {
            log.error("Can't find class " + className);
            if (Framework.getClassNotFoundCallback() == null) {
                return null;
            }
            if (intent.getComponent() == null && !TextUtils.isEmpty(className)) {
                intent.setClassName(context, className);
            }
            if (intent.getComponent() == null) {
                return null;
            }
            Framework.getClassNotFoundCallback().returnIntent(intent);
            return null;
        }
    }

    public Activity newActivity(Class<?> cls, Context context, IBinder iBinder, Application application, Intent intent, ActivityInfo activityInfo, CharSequence charSequence, Activity activity, String str, Object obj) throws InstantiationException, IllegalAccessException {
        Activity newActivity = this.mBase.newActivity(cls, context, iBinder, application, intent, activityInfo, charSequence, activity, str, obj);
        if (RuntimeVariables.androidApplication.getPackageName().equals(activityInfo.packageName) && AtlasHacks.ContextThemeWrapper_mResources != null) {
            AtlasHacks.ContextThemeWrapper_mResources.set(newActivity, RuntimeVariables.delegateResources);
        }
        return newActivity;
    }

    public Activity newActivity(ClassLoader classLoader, String str, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Activity newActivity;
        String str2;
        try {
            newActivity = this.mBase.newActivity(classLoader, str, intent);
        } catch (ClassNotFoundException e) {
            ClassNotFoundException classNotFoundException = e;
            CharSequence property = Framework.getProperty("android.taobao.atlas.welcome", "com.taobao.tao.welcome.Welcome");
            if (TextUtils.isEmpty(property)) {
                str2 = "com.taobao.tao.welcome.Welcome";
            } else {
                CharSequence charSequence = property;
            }
            if (TextUtils.isEmpty(str2)) {
                throw classNotFoundException;
            }
            List runningTasks = ((ActivityManager) this.context.getSystemService(DetailConstants.SECKILL_LIST_TYPE)).getRunningTasks(1);
            if (runningTasks != null && runningTasks.size() > 0 && ((RunningTaskInfo) runningTasks.get(0)).numActivities > 1 && Framework.getClassNotFoundCallback() != null) {
                if (intent.getComponent() == null) {
                    intent.setClassName(this.context, str);
                }
                Framework.getClassNotFoundCallback().returnIntent(intent);
            }
            log.warn("Could not find activity class: " + str);
            log.warn("Redirect to welcome activity: " + str2);
            newActivity = this.mBase.newActivity(classLoader, str2, intent);
        }
        if ((classLoader instanceof DelegateClassLoader) && AtlasHacks.ContextThemeWrapper_mResources != null) {
            AtlasHacks.ContextThemeWrapper_mResources.set(newActivity, RuntimeVariables.delegateResources);
        }
        return newActivity;
    }

    public void callActivityOnCreate(Activity activity, Bundle bundle) {
        if (RuntimeVariables.androidApplication.getPackageName().equals(activity.getPackageName())) {
            ContextImplHook contextImplHook = new ContextImplHook(activity.getBaseContext(), activity.getClass().getClassLoader());
            if (!(AtlasHacks.ContextThemeWrapper_mBase == null || AtlasHacks.ContextThemeWrapper_mBase.getField() == null)) {
                AtlasHacks.ContextThemeWrapper_mBase.set(activity, contextImplHook);
            }
            AtlasHacks.ContextWrapper_mBase.set(activity, contextImplHook);
            if (activity.getClass().getClassLoader() instanceof BundleClassLoader) {
                try {
                    ((BundleClassLoader) activity.getClass().getClassLoader()).getBundle().startBundle();
                } catch (BundleException e) {
                    log.error(e.getMessage() + " Caused by: ", e.getNestedException());
                }
            }
            Object property = Framework.getProperty("android.taobao.atlas.welcome", "com.taobao.tao.welcome.Welcome");
            if (TextUtils.isEmpty(property)) {
                property = "com.taobao.tao.welcome.Welcome";
            }
            if (activity.getClass().getName().equals(property)) {
                this.mBase.callActivityOnCreate(activity, null);
                return;
            } else {
                this.mBase.callActivityOnCreate(activity, bundle);
                return;
            }
        }
        this.mBase.callActivityOnCreate(activity, bundle);
    }

    @TargetApi(18)
    public UiAutomation getUiAutomation() {
        return this.mBase.getUiAutomation();
    }

    public void onCreate(Bundle bundle) {
        this.mBase.onCreate(bundle);
    }

    public void start() {
        this.mBase.start();
    }

    public void onStart() {
        this.mBase.onStart();
    }

    public boolean onException(Object obj, Throwable th) {
        return this.mBase.onException(obj, th);
    }

    public void sendStatus(int i, Bundle bundle) {
        this.mBase.sendStatus(i, bundle);
    }

    public void finish(int i, Bundle bundle) {
        this.mBase.finish(i, bundle);
    }

    public void setAutomaticPerformanceSnapshots() {
        this.mBase.setAutomaticPerformanceSnapshots();
    }

    public void startPerformanceSnapshot() {
        this.mBase.startPerformanceSnapshot();
    }

    public void endPerformanceSnapshot() {
        this.mBase.endPerformanceSnapshot();
    }

    public void onDestroy() {
        this.mBase.onDestroy();
    }

    public Context getContext() {
        return this.mBase.getContext();
    }

    public ComponentName getComponentName() {
        return this.mBase.getComponentName();
    }

    public Context getTargetContext() {
        return this.mBase.getTargetContext();
    }

    public boolean isProfiling() {
        return this.mBase.isProfiling();
    }

    public void startProfiling() {
        this.mBase.startProfiling();
    }

    public void stopProfiling() {
        this.mBase.stopProfiling();
    }

    public void setInTouchMode(boolean z) {
        this.mBase.setInTouchMode(z);
    }

    public void waitForIdle(Runnable runnable) {
        this.mBase.waitForIdle(runnable);
    }

    public void waitForIdleSync() {
        this.mBase.waitForIdleSync();
    }

    public void runOnMainSync(Runnable runnable) {
        this.mBase.runOnMainSync(runnable);
    }

    public Activity startActivitySync(Intent intent) {
        return this.mBase.startActivitySync(intent);
    }

    public void addMonitor(ActivityMonitor activityMonitor) {
        this.mBase.addMonitor(activityMonitor);
    }

    public ActivityMonitor addMonitor(IntentFilter intentFilter, ActivityResult activityResult, boolean z) {
        return this.mBase.addMonitor(intentFilter, activityResult, z);
    }

    public ActivityMonitor addMonitor(String str, ActivityResult activityResult, boolean z) {
        return this.mBase.addMonitor(str, activityResult, z);
    }

    public boolean checkMonitorHit(ActivityMonitor activityMonitor, int i) {
        return this.mBase.checkMonitorHit(activityMonitor, i);
    }

    public Activity waitForMonitor(ActivityMonitor activityMonitor) {
        return this.mBase.waitForMonitor(activityMonitor);
    }

    public Activity waitForMonitorWithTimeout(ActivityMonitor activityMonitor, long j) {
        return this.mBase.waitForMonitorWithTimeout(activityMonitor, j);
    }

    public void removeMonitor(ActivityMonitor activityMonitor) {
        this.mBase.removeMonitor(activityMonitor);
    }

    public boolean invokeMenuActionSync(Activity activity, int i, int i2) {
        return this.mBase.invokeMenuActionSync(activity, i, i2);
    }

    public boolean invokeContextMenuAction(Activity activity, int i, int i2) {
        return this.mBase.invokeContextMenuAction(activity, i, i2);
    }

    public void sendStringSync(String str) {
        this.mBase.sendStringSync(str);
    }

    public void sendKeySync(KeyEvent keyEvent) {
        this.mBase.sendKeySync(keyEvent);
    }

    public void sendKeyDownUpSync(int i) {
        this.mBase.sendKeyDownUpSync(i);
    }

    public void sendCharacterSync(int i) {
        this.mBase.sendCharacterSync(i);
    }

    public void sendPointerSync(MotionEvent motionEvent) {
        this.mBase.sendPointerSync(motionEvent);
    }

    public void sendTrackballEventSync(MotionEvent motionEvent) {
        this.mBase.sendTrackballEventSync(motionEvent);
    }

    public Application newApplication(ClassLoader classLoader, String str, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return this.mBase.newApplication(classLoader, str, context);
    }

    public void callApplicationOnCreate(Application application) {
        this.mBase.callApplicationOnCreate(application);
    }

    public void callActivityOnDestroy(Activity activity) {
        this.mBase.callActivityOnDestroy(activity);
    }

    public void callActivityOnRestoreInstanceState(Activity activity, Bundle bundle) {
        this.mBase.callActivityOnRestoreInstanceState(activity, bundle);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle bundle) {
        this.mBase.callActivityOnPostCreate(activity, bundle);
    }

    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        this.mBase.callActivityOnNewIntent(activity, intent);
    }

    public void callActivityOnStart(Activity activity) {
        this.mBase.callActivityOnStart(activity);
    }

    public void callActivityOnRestart(Activity activity) {
        this.mBase.callActivityOnRestart(activity);
    }

    public void callActivityOnResume(Activity activity) {
        this.mBase.callActivityOnResume(activity);
    }

    public void callActivityOnStop(Activity activity) {
        this.mBase.callActivityOnStop(activity);
    }

    public void callActivityOnSaveInstanceState(Activity activity, Bundle bundle) {
        this.mBase.callActivityOnSaveInstanceState(activity, bundle);
    }

    public void callActivityOnPause(Activity activity) {
        this.mBase.callActivityOnPause(activity);
    }

    public void callActivityOnUserLeaving(Activity activity) {
        this.mBase.callActivityOnUserLeaving(activity);
    }

    public void startAllocCounting() {
        this.mBase.startAllocCounting();
    }

    public void stopAllocCounting() {
        this.mBase.stopAllocCounting();
    }

    public Bundle getAllocCounts() {
        return this.mBase.getAllocCounts();
    }

    public Bundle getBinderCounts() {
        return this.mBase.getBinderCounts();
    }
}
