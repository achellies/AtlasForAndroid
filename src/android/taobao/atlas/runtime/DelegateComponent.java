package android.taobao.atlas.runtime;

import android.app.Application;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DelegateComponent {
    static Map<String, Application> apkApplications;
    private static Map<String, PackageLite> apkPackages;
    static final Logger log;

    static {
        log = LoggerFactory.getInstance("DelegateComponent");
        apkPackages = new ConcurrentHashMap();
        apkApplications = new HashMap();
    }

    public static PackageLite getPackage(String str) {
        return (PackageLite) apkPackages.get(str);
    }

    public static void putPackage(String str, PackageLite packageLite) {
        apkPackages.put(str, packageLite);
    }

    public static void removePackage(String str) {
        apkPackages.remove(str);
    }

    public static String locateComponent(String str) {
        for (Entry entry : apkPackages.entrySet()) {
            if (((PackageLite) entry.getValue()).components.contains(str)) {
                return (String) entry.getKey();
            }
        }
        return null;
    }
}
