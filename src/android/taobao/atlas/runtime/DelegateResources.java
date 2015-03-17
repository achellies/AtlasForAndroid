package android.taobao.atlas.runtime;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.hack.AndroidHack;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.tencent.mm.sdk.platformtools.FilePathGenerator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mtopsdk.common.util.SymbolExpUtil;
import org.osgi.framework.Bundle;

public class DelegateResources extends Resources {
    static final Logger log;
    private Map<String, Integer> resIdentifierMap;

    static {
        log = LoggerFactory.getInstance("DelegateResources");
    }

    public DelegateResources(AssetManager assetManager, Resources resources) {
        super(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
        this.resIdentifierMap = new ConcurrentHashMap();
    }

    public static void newDelegateResources(Application application, Resources resources) throws Exception {
        List<Bundle> bundles = Framework.getBundles();
        if (bundles != null && !bundles.isEmpty()) {
            Resources delegateResources;
            List<String> arrayList = new ArrayList();
            arrayList.add(application.getApplicationInfo().sourceDir);
            for (Bundle bundle : bundles) {
                arrayList.add(((BundleImpl) bundle).getArchive().getArchiveFile().getAbsolutePath());
            }
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            for (String str : arrayList) {
                AtlasHacks.AssetManager_addAssetPath.invoke(assetManager, str);
            }
            if (resources == null || !resources.getClass().getName().equals("android.content.res.MiuiResources")) {
                delegateResources = new DelegateResources(assetManager, resources);
            } else {
                Constructor declaredConstructor = Class.forName("android.content.res.MiuiResources").getDeclaredConstructor(new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class});
                declaredConstructor.setAccessible(true);
                delegateResources = (Resources) declaredConstructor.newInstance(new Object[]{assetManager, resources.getDisplayMetrics(), resources.getConfiguration()});
            }
            RuntimeVariables.delegateResources = delegateResources;
            AndroidHack.injectResources(application, delegateResources);
            if (log.isDebugEnabled()) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("newDelegateResources [");
                for (int i = 0; i < arrayList.size(); i++) {
                    if (i > 0) {
                        stringBuffer.append(SymbolExpUtil.SYMBOL_COMMA);
                    }
                    stringBuffer.append((String) arrayList.get(i));
                }
                stringBuffer.append("]");
                log.debug(stringBuffer.toString());
            }
        }
    }

    public int getIdentifier(String str, String str2, String str3) {
        int identifier = super.getIdentifier(str, str2, str3);
        if (identifier != 0) {
            return identifier;
        }
        if (str2 == null && str3 == null) {
            str = str.substring(str.indexOf(FilePathGenerator.ANDROID_DIR_SEP) + 1);
            str2 = str.substring(str.indexOf(":") + 1, str.indexOf(FilePathGenerator.ANDROID_DIR_SEP));
        }
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return 0;
        }
        List bundles = Framework.getBundles();
        if (!(bundles == null || bundles.isEmpty())) {
            for (Bundle bundle : Framework.getBundles()) {
                String location = bundle.getLocation();
                String str4 = location + ":" + str;
                if (!this.resIdentifierMap.isEmpty() && this.resIdentifierMap.containsKey(str4)) {
                    int intValue = ((Integer) this.resIdentifierMap.get(str4)).intValue();
                    if (intValue != 0) {
                        return intValue;
                    }
                }
                BundleImpl bundleImpl = (BundleImpl) bundle;
                if (bundleImpl.getArchive().isDexOpted()) {
                    ClassLoader classLoader = bundleImpl.getClassLoader();
                    if (classLoader != null) {
                        try {
                            StringBuilder stringBuilder = new StringBuilder(location);
                            stringBuilder.append(".R$");
                            stringBuilder.append(str2);
                            identifier = getFieldValueOfR(classLoader.loadClass(stringBuilder.toString()), str);
                            if (identifier != 0) {
                                this.resIdentifierMap.put(str4, Integer.valueOf(identifier));
                                return identifier;
                            }
                        } catch (ClassNotFoundException e) {
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return 0;
    }

    public String getString(int i) throws NotFoundException {
        if (i == 33816578 || i == 262146) {
            return "Web View";
        }
        return super.getString(i);
    }

    private static int getFieldValueOfR(Class<?> cls, String str) {
        if (cls != null) {
            try {
                Field declaredField = cls.getDeclaredField(str);
                if (declaredField != null) {
                    if (!declaredField.isAccessible()) {
                        declaredField.setAccessible(true);
                    }
                    return ((Integer) declaredField.get(null)).intValue();
                }
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException e2) {
            } catch (IllegalArgumentException e3) {
            }
        }
        return 0;
    }
}
