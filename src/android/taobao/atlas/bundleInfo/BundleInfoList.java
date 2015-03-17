package android.taobao.atlas.bundleInfo;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BundleInfoList {
    private static BundleInfoList singleton;
    private final String TAG;
    private List<BundleInfo> mBundleInfoList;

    public static class BundleInfo {
        public List<String> Components;
        public List<String> DependentBundles;
        public String bundleName;
        public boolean hasSO;
    }

    public BundleInfoList() {
        this.TAG = "BundleInfoList";
    }

    public static synchronized BundleInfoList getInstance() {
        BundleInfoList bundleInfoList;
        synchronized (BundleInfoList.class) {
            if (singleton == null) {
                singleton = new BundleInfoList();
            }
            bundleInfoList = singleton;
        }
        return bundleInfoList;
    }

    public synchronized boolean init(LinkedList<BundleInfo> linkedList) {
        boolean z;
        if (this.mBundleInfoList != null || linkedList == null) {
            Log.i("BundleInfoList", "BundleInfoList initialization failed.");
            z = false;
        } else {
            this.mBundleInfoList = linkedList;
            z = true;
        }
        return z;
    }

    public List<String> getDependencyForBundle(String str) {
        if (this.mBundleInfoList == null || this.mBundleInfoList.size() == 0) {
            return null;
        }
        for (BundleInfo bundleInfo : this.mBundleInfoList) {
            if (bundleInfo.bundleName.equals(str)) {
                List<String> arrayList = new ArrayList();
                if (!(bundleInfo == null || bundleInfo.DependentBundles == null)) {
                    for (int i = 0; i < bundleInfo.DependentBundles.size(); i++) {
                        if (!TextUtils.isEmpty((CharSequence) bundleInfo.DependentBundles.get(i))) {
                            arrayList.add(bundleInfo.DependentBundles.get(i));
                        }
                    }
                }
                return arrayList;
            }
        }
        return null;
    }

    public boolean getHasSO(String str) {
        if (this.mBundleInfoList == null || this.mBundleInfoList.size() == 0) {
            return false;
        }
        for (BundleInfo bundleInfo : this.mBundleInfoList) {
            if (bundleInfo.bundleName.equals(str)) {
                return bundleInfo.hasSO;
            }
        }
        return false;
    }

    public String getBundleForComponet(String str) {
        if (this.mBundleInfoList == null || this.mBundleInfoList.size() == 0) {
            return null;
        }
        for (BundleInfo bundleInfo : this.mBundleInfoList) {
            for (String equals : bundleInfo.Components) {
                if (equals.equals(str)) {
                    return bundleInfo.bundleName;
                }
            }
        }
        return null;
    }

    public List<String> getAllBundleNames() {
        if (this.mBundleInfoList == null || this.mBundleInfoList.size() == 0) {
            return null;
        }
        LinkedList linkedList = new LinkedList();
        for (BundleInfo bundleInfo : this.mBundleInfoList) {
            linkedList.add(bundleInfo.bundleName);
        }
        return linkedList;
    }

    public BundleInfo getBundleInfo(String str) {
        if (this.mBundleInfoList == null || this.mBundleInfoList.size() == 0) {
            return null;
        }
        for (BundleInfo bundleInfo : this.mBundleInfoList) {
            if (bundleInfo.bundleName.equals(str)) {
                return bundleInfo;
            }
        }
        return null;
    }

    public void print() {
        if (this.mBundleInfoList != null && this.mBundleInfoList.size() != 0) {
            for (BundleInfo bundleInfo : this.mBundleInfoList) {
                Log.i("BundleInfoList", "BundleName: " + bundleInfo.bundleName);
                for (String str : bundleInfo.Components) {
                    Log.i("BundleInfoList", "****components: " + str);
                }
                for (String str2 : bundleInfo.DependentBundles) {
                    Log.i("BundleInfoList", "****dependancy: " + str2);
                }
            }
        }
    }
}
