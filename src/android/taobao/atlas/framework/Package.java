package android.taobao.atlas.framework;

import com.taobao.tao.util.Constants;
import java.util.List;
import java.util.StringTokenizer;
import mtopsdk.common.util.SymbolExpUtil;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;

final class Package implements ExportedPackage {
    final BundleClassLoader classloader;
    List<Bundle> importingBundles;
    final String pkg;
    boolean removalPending;
    boolean resolved;
    private final short[] version;

    Package(String str, BundleClassLoader bundleClassLoader, boolean z) {
        this.importingBundles = null;
        this.removalPending = false;
        this.resolved = false;
        String[] parsePackageString = parsePackageString(str);
        this.pkg = parsePackageString[0];
        this.version = getVersionNumber(parsePackageString[1]);
        this.classloader = bundleClassLoader;
        this.resolved = z;
    }

    public Bundle getExportingBundle() {
        return this.classloader.bundle;
    }

    public Bundle[] getImportingBundles() {
        if (this.importingBundles == null) {
            return new Bundle[]{this.classloader.bundle};
        }
        Bundle[] bundleArr = new Bundle[(this.importingBundles.size() + 1)];
        this.importingBundles.toArray(bundleArr);
        bundleArr[this.importingBundles.size()] = this.classloader.bundle;
        return bundleArr;
    }

    public String getName() {
        return this.pkg;
    }

    public String getSpecificationVersion() {
        return this.version == null ? null : this.version[0] + "." + this.version[1] + "." + this.version[2];
    }

    public boolean isRemovalPending() {
        return this.removalPending;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Package)) {
            return false;
        }
        Package packageR = (Package) obj;
        if (this.classloader == null) {
            return matches(this.pkg, this.version, packageR.pkg, packageR.version);
        }
        return obj.hashCode() == hashCode();
    }

    public String toString() {
        if (this.version == null) {
            return this.pkg;
        }
        return this.pkg + "; specification-version=" + getSpecificationVersion() + (this.resolved ? Constants.ALIPAY_PARNER : " (UNRESOLVED)");
    }

    public int hashCode() {
        return this.pkg.hashCode();
    }

    static String[] parsePackageString(String str) {
        if (str.indexOf(SymbolExpUtil.SYMBOL_SEMICOLON) > -1) {
            return new String[]{str.substring(0, str.indexOf(SymbolExpUtil.SYMBOL_SEMICOLON)).trim(), str.substring(str.indexOf(SymbolExpUtil.SYMBOL_SEMICOLON) + 1).trim()};
        }
        return new String[]{str.trim(), Constants.ALIPAY_PARNER};
    }

    boolean matches(String str) {
        String[] parsePackageString = parsePackageString(str);
        return matches(this.pkg, this.version, parsePackageString[0], getVersionNumber(parsePackageString[1]));
    }

    private static boolean matches(String str, short[] sArr, String str2, short[] sArr2) {
        int indexOf = str2.indexOf(42);
        if (indexOf > -1) {
            if (indexOf == 0) {
                return true;
            }
            String substring = str2.substring(0, indexOf);
            if (!substring.endsWith(".")) {
                return false;
            }
            if (!str.startsWith(substring.substring(0, substring.length() - 1))) {
                return false;
            }
        } else if (!str.equals(str2)) {
            return false;
        }
        if (sArr == null || sArr2 == null) {
            return true;
        }
        for (indexOf = 0; indexOf < 3; indexOf++) {
            if (sArr[indexOf] > sArr2[indexOf]) {
                return false;
            }
        }
        return true;
    }

    boolean updates(Package packageR) {
        if (this.version == null || packageR.version == null) {
            return true;
        }
        for (int i = 0; i < 3; i++) {
            if (this.version[i] < packageR.version[i]) {
                return false;
            }
        }
        return true;
    }

    static boolean matches(String str, String str2) {
        String[] parsePackageString = parsePackageString(str);
        String[] parsePackageString2 = parsePackageString(str2);
        return matches(parsePackageString[0], getVersionNumber(parsePackageString[1]), parsePackageString2[0], getVersionNumber(parsePackageString2[1]));
    }

    private static short[] getVersionNumber(String str) {
        if (!str.startsWith("specification-version=")) {
            return null;
        }
        String trim = str.substring(22).trim();
        if (trim.startsWith("\"")) {
            trim = trim.substring(1);
        }
        if (trim.endsWith("\"")) {
            trim = trim.substring(0, trim.length() - 1);
        }
        StringTokenizer stringTokenizer = new StringTokenizer(trim, ".");
        short[] sArr = new short[]{(short) 0, (short) 0, (short) 0};
        int i = 0;
        while (stringTokenizer.hasMoreTokens() && i <= 2) {
            sArr[i] = Short.parseShort(stringTokenizer.nextToken());
            i++;
        }
        return sArr;
    }
}
