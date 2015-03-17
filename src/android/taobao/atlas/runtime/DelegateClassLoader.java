package android.taobao.atlas.runtime;

import android.taobao.atlas.framework.Framework;
import java.util.List;
import org.osgi.framework.Bundle;

public class DelegateClassLoader extends ClassLoader {
    public DelegateClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public Class<?> loadClass(String str) throws ClassNotFoundException {
        return super.loadClass(str);
    }

    protected Class<?> findClass(String str) throws ClassNotFoundException {
        Class<?> loadFromInstalledBundles = ClassLoadFromBundle.loadFromInstalledBundles(str);
        if (loadFromInstalledBundles == null) {
            loadFromInstalledBundles = ClassLoadFromBundle.loadFromUninstalledBundles(str);
        }
        if (loadFromInstalledBundles != null) {
            return loadFromInstalledBundles;
        }
        throw new ClassNotFoundException("Can't find class " + str + printExceptionInfo() + " " + ClassLoadFromBundle.getClassNotFoundReason(str));
    }

    private String printExceptionInfo() {
        StringBuilder stringBuilder = new StringBuilder("installed bundles: ");
        List bundles = Framework.getBundles();
        if (!(bundles == null || bundles.isEmpty())) {
            for (Bundle bundle : Framework.getBundles()) {
                if (bundle.getLocation().contains("com.ut")) {
                    stringBuilder.append(bundle.getLocation().toUpperCase());
                } else {
                    stringBuilder.append(bundle.getLocation());
                }
                stringBuilder.append(":");
            }
        }
        return stringBuilder.toString();
    }
}
