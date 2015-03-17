package android.taobao.atlas.hack;

import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build.VERSION;
import android.taobao.atlas.hack.Hack.AssertionFailureHandler;
import android.taobao.atlas.hack.Hack.HackDeclaration;
import android.taobao.atlas.hack.Hack.HackDeclaration.HackAssertionException;
import android.taobao.atlas.hack.Hack.HackedClass;
import android.taobao.atlas.hack.Hack.HackedField;
import android.taobao.atlas.hack.Hack.HackedMethod;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.view.ContextThemeWrapper;
import dalvik.system.DexClassLoader;
import java.util.ArrayList;
import java.util.Map;

public class AtlasHacks extends HackDeclaration implements AssertionFailureHandler {
    public static HackedClass<Object> ActivityThread;
    public static HackedMethod ActivityThread_currentActivityThread;
    public static HackedField<Object, ArrayList<Application>> ActivityThread_mAllApplications;
    public static HackedField<Object, Application> ActivityThread_mInitialApplication;
    public static HackedField<Object, Instrumentation> ActivityThread_mInstrumentation;
    public static HackedField<Object, Map<String, Object>> ActivityThread_mPackages;
    public static HackedField<Object, Object> ActivityThread_sPackageManager;
    public static HackedClass<Application> Application;
    public static HackedMethod Application_attach;
    public static HackedClass<AssetManager> AssetManager;
    public static HackedMethod AssetManager_addAssetPath;
    public static HackedClass<ClassLoader> ClassLoader;
    public static HackedMethod ClassLoader_findLibrary;
    public static HackedClass<Object> ContextImpl;
    public static HackedField<Object, Resources> ContextImpl_mResources;
    public static HackedField<Object, Theme> ContextImpl_mTheme;
    public static HackedClass<ContextThemeWrapper> ContextThemeWrapper;
    public static HackedField<ContextThemeWrapper, Context> ContextThemeWrapper_mBase;
    public static HackedField<ContextThemeWrapper, Resources> ContextThemeWrapper_mResources;
    public static HackedField<ContextThemeWrapper, Theme> ContextThemeWrapper_mTheme;
    public static HackedClass<ContextWrapper> ContextWrapper;
    public static HackedField<ContextWrapper, Context> ContextWrapper_mBase;
    public static HackedClass<DexClassLoader> DexClassLoader;
    public static HackedMethod DexClassLoader_findClass;
    public static ArrayList<HackedMethod> GeneratePackageInfoList;
    public static ArrayList<HackedMethod> GetPackageInfoList;
    public static HackedClass<Object> IPackageManager;
    public static HackedClass<Object> LexFile;
    public static HackedMethod LexFile_close;
    public static HackedMethod LexFile_loadClass;
    public static HackedMethod LexFile_loadLex;
    public static HackedClass<Object> LoadedApk;
    public static HackedField<Object, String> LoadedApk_mAppDir;
    public static HackedField<Object, Application> LoadedApk_mApplication;
    public static HackedField<Object, ClassLoader> LoadedApk_mBaseClassLoader;
    public static HackedField<Object, ClassLoader> LoadedApk_mClassLoader;
    public static HackedField<Object, String> LoadedApk_mResDir;
    public static HackedField<Object, Resources> LoadedApk_mResources;
    public static HackedClass<Resources> Resources;
    public static HackedField<Resources, Object> Resources_mAssets;
    public static HackedClass<Service> Service;
    protected static final Logger log;
    public static boolean sIsIgnoreFailure;
    public static boolean sIsReflectAvailable;
    public static boolean sIsReflectChecked;
    private AssertionArrayException mExceptionArray;

    public AtlasHacks() {
        this.mExceptionArray = null;
    }

    static {
        log = LoggerFactory.getInstance(AtlasHacks.class);
        sIsReflectAvailable = false;
        sIsReflectChecked = false;
        sIsIgnoreFailure = false;
        GeneratePackageInfoList = new ArrayList();
        GetPackageInfoList = new ArrayList();
    }

    public static boolean defineAndVerify() throws AssertionArrayException {
        if (sIsReflectChecked) {
            return sIsReflectAvailable;
        }
        AtlasHacks atlasHacks = new AtlasHacks();
        try {
            Hack.setAssertionFailureHandler(atlasHacks);
            if (VERSION.SDK_INT == 11) {
                atlasHacks.onAssertionFailure(new HackAssertionException("Hack Assertion Failed: Android OS Version 11"));
            }
            allClasses();
            allConstructors();
            allFields();
            allMethods();
            if (atlasHacks.mExceptionArray != null) {
                sIsReflectAvailable = false;
                throw atlasHacks.mExceptionArray;
            }
            sIsReflectAvailable = true;
            return sIsReflectAvailable;
        } catch (Throwable e) {
            sIsReflectAvailable = false;
            log.error("HackAssertionException", e);
        } finally {
            Hack.setAssertionFailureHandler(null);
            sIsReflectChecked = true;
        }
    }

    public static void allClasses() throws HackAssertionException {
        if (VERSION.SDK_INT <= 8) {
            LoadedApk = Hack.into("android.app.ActivityThread$PackageInfo");
        } else {
            LoadedApk = Hack.into("android.app.LoadedApk");
        }
        ActivityThread = Hack.into("android.app.ActivityThread");
        Resources = Hack.into(Resources.class);
        Application = Hack.into(Application.class);
        AssetManager = Hack.into(AssetManager.class);
        IPackageManager = Hack.into("android.content.pm.IPackageManager");
        Service = Hack.into(Service.class);
        ContextImpl = Hack.into("android.app.ContextImpl");
        ContextThemeWrapper = Hack.into(ContextThemeWrapper.class);
        ContextWrapper = Hack.into("android.content.ContextWrapper");
        sIsIgnoreFailure = true;
        ClassLoader = Hack.into(ClassLoader.class);
        DexClassLoader = Hack.into(DexClassLoader.class);
        LexFile = Hack.into("dalvik.system.LexFile");
        sIsIgnoreFailure = false;
    }

    public static void allFields() throws HackAssertionException {
        ActivityThread_mInstrumentation = ActivityThread.field("mInstrumentation").ofType(Instrumentation.class);
        ActivityThread_mAllApplications = ActivityThread.field("mAllApplications").ofGenericType(ArrayList.class);
        ActivityThread_mInitialApplication = ActivityThread.field("mInitialApplication").ofType(Application.class);
        ActivityThread_mPackages = ActivityThread.field("mPackages").ofGenericType(Map.class);
        ActivityThread_sPackageManager = ActivityThread.staticField("sPackageManager").ofType(IPackageManager.getmClass());
        LoadedApk_mApplication = LoadedApk.field("mApplication").ofType(Application.class);
        LoadedApk_mResources = LoadedApk.field("mResources").ofType(Resources.class);
        LoadedApk_mResDir = LoadedApk.field("mResDir").ofType(String.class);
        LoadedApk_mClassLoader = LoadedApk.field("mClassLoader").ofType(ClassLoader.class);
        LoadedApk_mBaseClassLoader = LoadedApk.field("mBaseClassLoader").ofType(ClassLoader.class);
        LoadedApk_mAppDir = LoadedApk.field("mAppDir").ofType(String.class);
        ContextImpl_mResources = ContextImpl.field("mResources").ofType(Resources.class);
        ContextImpl_mTheme = ContextImpl.field("mTheme").ofType(Theme.class);
        sIsIgnoreFailure = true;
        ContextThemeWrapper_mBase = ContextThemeWrapper.field("mBase").ofType(Context.class);
        sIsIgnoreFailure = false;
        ContextThemeWrapper_mTheme = ContextThemeWrapper.field("mTheme").ofType(Theme.class);
        try {
            if (VERSION.SDK_INT >= 17 && ContextThemeWrapper.getmClass().getDeclaredField("mResources") != null) {
                ContextThemeWrapper_mResources = ContextThemeWrapper.field("mResources").ofType(Resources.class);
            }
        } catch (NoSuchFieldException e) {
            log.warn("Not found ContextThemeWrapper.mResources on VERSION " + VERSION.SDK_INT);
        }
        ContextWrapper_mBase = ContextWrapper.field("mBase").ofType(Context.class);
        Resources_mAssets = Resources.field("mAssets");
    }

    public static void allMethods() throws HackAssertionException {
        ActivityThread_currentActivityThread = ActivityThread.method("currentActivityThread", new Class[0]);
        AssetManager_addAssetPath = AssetManager.method("addAssetPath", String.class);
        Application_attach = Application.method("attach", Context.class);
        ClassLoader_findLibrary = ClassLoader.method("findLibrary", String.class);
        if (LexFile != null && LexFile.getmClass() != null) {
            LexFile_loadLex = LexFile.method("loadLex", String.class, Integer.TYPE);
            LexFile_loadClass = LexFile.method("loadClass", String.class, ClassLoader.class);
            LexFile_close = LexFile.method("close", new Class[0]);
            DexClassLoader_findClass = DexClassLoader.method("findClass", String.class);
        }
    }

    public static void allConstructors() throws HackAssertionException {
    }

    public boolean onAssertionFailure(HackAssertionException hackAssertionException) {
        if (!sIsIgnoreFailure) {
            if (this.mExceptionArray == null) {
                this.mExceptionArray = new AssertionArrayException("atlas hack assert failed");
            }
            this.mExceptionArray.addException(hackAssertionException);
        }
        return true;
    }
}
