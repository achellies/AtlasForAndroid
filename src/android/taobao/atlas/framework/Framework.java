package android.taobao.atlas.framework;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Build.VERSION;
import android.taobao.atlas.framework.bundlestorage.BundleArchive;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.runtime.ClassNotFoundInterceptorCallback;
import android.taobao.atlas.runtime.RuntimeVariables;
import android.taobao.atlas.util.AtlasFileLock;
import android.taobao.atlas.util.BundleLock;
import android.taobao.atlas.util.StringUtils;
import com.taobao.android.taotv.mediaplayer.api.PlayerConstant;
import com.tencent.mm.sdk.platformtools.C0264v;
import com.tencent.mm.sdk.platformtools.FilePathGenerator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import mtopsdk.common.util.SymbolExpUtil;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

public final class Framework {
    private static final AdminPermission ADMIN_PERMISSION;
    private static String BASEDIR = null;
    private static String BUNDLE_LOCATION = null;
    static int CLASSLOADER_BUFFER_SIZE = 0;
    static boolean DEBUG_BUNDLES = false;
    static boolean DEBUG_CLASSLOADING = false;
    static boolean DEBUG_PACKAGES = false;
    static boolean DEBUG_SERVICES = false;
    static final String FRAMEWORK_VERSION = "0.9.0";
    static int LOG_LEVEL;
    static String STORAGE_LOCATION;
    private static boolean STRICT_STARTUP;
    static List<BundleListener> bundleListeners;
    static Map<String, Bundle> bundles;
    private static ClassNotFoundInterceptorCallback classNotFoundCallback;
    static Map<String, List<ServiceReference>> classes_services;
    static Map<Package, Package> exportedPackages;
    static List<FrameworkListener> frameworkListeners;
    static boolean frameworkStartupShutdown;
    static int initStartlevel;
    static final Logger log;
    static boolean mIsEnableBundleInstallWhenFindClass;
    static Map<String, String> mMapForComAndBundles;
    static Properties properties;
    static boolean restart;
    static List<ServiceListenerEntry> serviceListeners;
    static List<ServiceReference> services;
    static int startlevel;
    static List<BundleListener> syncBundleListeners;
    static SystemBundle systemBundle;
    static ClassLoader systemClassLoader;
    static List<String> writeAheads;

    static final class ServiceListenerEntry implements EventListener {
        final Filter filter;
        final ServiceListener listener;

        ServiceListenerEntry(ServiceListener serviceListener, String str) throws InvalidSyntaxException {
            this.listener = serviceListener;
            this.filter = str == null ? null : RFC1960Filter.fromString(str);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ServiceListenerEntry)) {
                return false;
            }
            return this.listener.equals(((ServiceListenerEntry) obj).listener);
        }

        public int hashCode() {
            return (this.filter != null ? this.filter.hashCode() >> 8 : 0) + this.listener.hashCode();
        }

        public String toString() {
            return this.listener + " " + this.filter;
        }
    }

    private static final class SystemBundle implements Bundle, PackageAdmin, StartLevel {
        private final Dictionary<String, String> props;
        private final ServiceReference[] registeredServices;
        int state;

        class AnonymousClass_1 extends Thread {
            final /* synthetic */ boolean val$restart;

            AnonymousClass_1(boolean z) {
                this.val$restart = z;
            }

            public void run() {
                Framework.shutdown(this.val$restart);
            }
        }

        class AnonymousClass_2 extends Thread {
            final /* synthetic */ int val$targetLevel;

            AnonymousClass_2(int i) {
                this.val$targetLevel = i;
            }

            public void run() {
                List bundles = Framework.getBundles();
                SystemBundle.this.setLevel((Bundle[]) bundles.toArray(new Bundle[bundles.size()]), this.val$targetLevel, false);
                Framework.notifyFrameworkListeners(8, Framework.systemBundle, null);
                Framework.storeMetadata();
            }
        }

        class AnonymousClass_3 extends Thread {
            final /* synthetic */ Bundle[] val$bundleArray;

            AnonymousClass_3(Bundle[] bundleArr) {
                this.val$bundleArray = bundleArr;
            }

            public void run() {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.taobao.atlas.framework.Framework.SystemBundle.AnonymousClass_3.run():void. bs: []
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:82)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JavaClass.getCode(JavaClass.java:45)
	at jadx.gui.treemodel.JClass.getContent(JClass.java:74)
	at jadx.gui.ui.ContentArea.<init>(ContentArea.java:66)
	at jadx.gui.ui.ContentPanel.<init>(ContentPanel.java:28)
	at jadx.gui.ui.TabbedPane.getCodePanel(TabbedPane.java:112)
	at jadx.gui.ui.TabbedPane.showCode(TabbedPane.java:69)
	at jadx.gui.ui.MainWindow.treeClickAction(MainWindow.java:241)
	at jadx.gui.ui.MainWindow.access$1000(MainWindow.java:66)
	at jadx.gui.ui.MainWindow$15.mouseClicked(MainWindow.java:519)
	at java.awt.AWTEventMulticaster.mouseClicked(AWTEventMulticaster.java:270)
	at java.awt.Component.processMouseEvent(Component.java:6528)
	at javax.swing.JComponent.processMouseEvent(JComponent.java:3322)
	at java.awt.Component.processEvent(Component.java:6290)
	at java.awt.Container.processEvent(Container.java:2234)
	at java.awt.Component.dispatchEventImpl(Component.java:4881)
	at java.awt.Container.dispatchEventImpl(Container.java:2292)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.LightweightDispatcher.retargetMouseEvent(Container.java:4898)
	at java.awt.LightweightDispatcher.processMouseEvent(Container.java:4542)
	at java.awt.LightweightDispatcher.dispatchEvent(Container.java:4462)
	at java.awt.Container.dispatchEventImpl(Container.java:2278)
	at java.awt.Window.dispatchEventImpl(Window.java:2739)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:751)
	at java.awt.EventQueue.access$500(EventQueue.java:97)
	at java.awt.EventQueue$3.run(EventQueue.java:702)
	at java.awt.EventQueue$3.run(EventQueue.java:696)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.awt.EventQueue$4.run(EventQueue.java:724)
	at java.awt.EventQueue$4.run(EventQueue.java:722)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.awt.EventQueue.dispatchEvent(EventQueue.java:721)
	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
*/
                /*
                r9 = this;
                r2 = 0;
                r4 = android.taobao.atlas.framework.Framework.exportedPackages;
                monitor-enter(r4);
                r0 = r9.val$bundleArray;	 Catch:{ all -> 0x0047 }
                if (r0 != 0) goto L_0x002f;	 Catch:{ all -> 0x0047 }
            L_0x0008:
                r0 = android.taobao.atlas.framework.Framework.getBundles();	 Catch:{ all -> 0x0047 }
                r1 = r0.size();	 Catch:{ all -> 0x0047 }
                r1 = new org.osgi.framework.Bundle[r1];	 Catch:{ all -> 0x0047 }
                r0 = r0.toArray(r1);	 Catch:{ all -> 0x0047 }
                r0 = (org.osgi.framework.Bundle[]) r0;	 Catch:{ all -> 0x0047 }
                r0 = (org.osgi.framework.Bundle[]) r0;	 Catch:{ all -> 0x0047 }
                r3 = r0;	 Catch:{ all -> 0x0047 }
            L_0x001b:
                r5 = new java.util.ArrayList;	 Catch:{ all -> 0x0047 }
                r0 = r3.length;	 Catch:{ all -> 0x0047 }
                r5.<init>(r0);	 Catch:{ all -> 0x0047 }
                r1 = r2;	 Catch:{ all -> 0x0047 }
            L_0x0022:
                r0 = r3.length;	 Catch:{ all -> 0x0047 }
                if (r1 >= r0) goto L_0x004a;	 Catch:{ all -> 0x0047 }
            L_0x0025:
                r0 = r3[r1];	 Catch:{ all -> 0x0047 }
                r6 = android.taobao.atlas.framework.Framework.systemBundle;	 Catch:{ all -> 0x0047 }
                if (r0 != r6) goto L_0x0033;	 Catch:{ all -> 0x0047 }
            L_0x002b:
                r0 = r1 + 1;	 Catch:{ all -> 0x0047 }
                r1 = r0;	 Catch:{ all -> 0x0047 }
                goto L_0x0022;	 Catch:{ all -> 0x0047 }
            L_0x002f:
                r0 = r9.val$bundleArray;	 Catch:{ all -> 0x0047 }
                r3 = r0;	 Catch:{ all -> 0x0047 }
                goto L_0x001b;	 Catch:{ all -> 0x0047 }
            L_0x0033:
                r0 = r3[r1];	 Catch:{ all -> 0x0047 }
                r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ all -> 0x0047 }
                r6 = r0.classloader;	 Catch:{ all -> 0x0047 }
                if (r6 == 0) goto L_0x0041;	 Catch:{ all -> 0x0047 }
            L_0x003b:
                r0 = r0.classloader;	 Catch:{ all -> 0x0047 }
                r0 = r0.originalExporter;	 Catch:{ all -> 0x0047 }
                if (r0 == 0) goto L_0x002b;	 Catch:{ all -> 0x0047 }
            L_0x0041:
                r0 = r3[r1];	 Catch:{ all -> 0x0047 }
                r5.add(r0);	 Catch:{ all -> 0x0047 }
                goto L_0x002b;
            L_0x0047:
                r0 = move-exception;
                monitor-exit(r4);
                throw r0;
            L_0x004a:
                r0 = r5.isEmpty();	 Catch:{ all -> 0x0047 }
                if (r0 == 0) goto L_0x0052;	 Catch:{ all -> 0x0047 }
            L_0x0050:
                monitor-exit(r4);	 Catch:{ all -> 0x0047 }
            L_0x0051:
                return;	 Catch:{ all -> 0x0047 }
            L_0x0052:
                r0 = android.taobao.atlas.framework.Framework.DEBUG_PACKAGES;	 Catch:{ all -> 0x0047 }
                if (r0 == 0) goto L_0x0077;	 Catch:{ all -> 0x0047 }
            L_0x0056:
                r0 = android.taobao.atlas.framework.Framework.log;	 Catch:{ all -> 0x0047 }
                r0 = r0.isDebugEnabled();	 Catch:{ all -> 0x0047 }
                if (r0 == 0) goto L_0x0077;	 Catch:{ all -> 0x0047 }
            L_0x005e:
                r0 = android.taobao.atlas.framework.Framework.log;	 Catch:{ all -> 0x0047 }
                r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0047 }
                r1.<init>();	 Catch:{ all -> 0x0047 }
                r3 = "REFRESHING PACKAGES FROM BUNDLES ";	 Catch:{ all -> 0x0047 }
                r1 = r1.append(r3);	 Catch:{ all -> 0x0047 }
                r1 = r1.append(r5);	 Catch:{ all -> 0x0047 }
                r1 = r1.toString();	 Catch:{ all -> 0x0047 }
                r0.debug(r1);	 Catch:{ all -> 0x0047 }
            L_0x0077:
                r6 = new java.util.HashSet;	 Catch:{ all -> 0x0047 }
                r6.<init>();	 Catch:{ all -> 0x0047 }
            L_0x007c:
                r0 = r5.isEmpty();	 Catch:{ all -> 0x0047 }
                if (r0 != 0) goto L_0x00ca;	 Catch:{ all -> 0x0047 }
            L_0x0082:
                r0 = 0;	 Catch:{ all -> 0x0047 }
                r0 = r5.remove(r0);	 Catch:{ all -> 0x0047 }
                r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ all -> 0x0047 }
                r1 = r6.contains(r0);	 Catch:{ all -> 0x0047 }
                if (r1 != 0) goto L_0x007c;	 Catch:{ all -> 0x0047 }
            L_0x008f:
                r1 = android.taobao.atlas.framework.Framework.SystemBundle.this;	 Catch:{ all -> 0x0047 }
                r3 = 1;	 Catch:{ all -> 0x0047 }
                r7 = r1.getExportedPackages(r0, r3);	 Catch:{ all -> 0x0047 }
                if (r7 == 0) goto L_0x00c2;	 Catch:{ all -> 0x0047 }
            L_0x0098:
                r3 = r2;	 Catch:{ all -> 0x0047 }
            L_0x0099:
                r1 = r7.length;	 Catch:{ all -> 0x0047 }
                if (r3 >= r1) goto L_0x00c2;	 Catch:{ all -> 0x0047 }
            L_0x009c:
                r1 = r7[r3];	 Catch:{ all -> 0x0047 }
                r1 = (android.taobao.atlas.framework.Package) r1;	 Catch:{ all -> 0x0047 }
                r8 = r1.importingBundles;	 Catch:{ all -> 0x0047 }
                if (r8 != 0) goto L_0x00a8;	 Catch:{ all -> 0x0047 }
            L_0x00a4:
                r1 = r3 + 1;	 Catch:{ all -> 0x0047 }
                r3 = r1;	 Catch:{ all -> 0x0047 }
                goto L_0x0099;	 Catch:{ all -> 0x0047 }
            L_0x00a8:
                r8 = r1.importingBundles;	 Catch:{ all -> 0x0047 }
                r1 = r1.importingBundles;	 Catch:{ all -> 0x0047 }
                r1 = r1.size();	 Catch:{ all -> 0x0047 }
                r1 = new org.osgi.framework.Bundle[r1];	 Catch:{ all -> 0x0047 }
                r1 = r8.toArray(r1);	 Catch:{ all -> 0x0047 }
                r1 = (org.osgi.framework.Bundle[]) r1;	 Catch:{ all -> 0x0047 }
                r1 = (org.osgi.framework.Bundle[]) r1;	 Catch:{ all -> 0x0047 }
                r1 = java.util.Arrays.asList(r1);	 Catch:{ all -> 0x0047 }
                r5.addAll(r1);	 Catch:{ all -> 0x0047 }
                goto L_0x00a4;	 Catch:{ all -> 0x0047 }
            L_0x00c2:
                r1 = r0.classloader;	 Catch:{ all -> 0x0047 }
                if (r1 == 0) goto L_0x007c;	 Catch:{ all -> 0x0047 }
            L_0x00c6:
                r6.add(r0);	 Catch:{ all -> 0x0047 }
                goto L_0x007c;	 Catch:{ all -> 0x0047 }
            L_0x00ca:
                r0 = android.taobao.atlas.framework.Framework.DEBUG_PACKAGES;	 Catch:{ all -> 0x0047 }
                if (r0 == 0) goto L_0x00ef;	 Catch:{ all -> 0x0047 }
            L_0x00ce:
                r0 = android.taobao.atlas.framework.Framework.log;	 Catch:{ all -> 0x0047 }
                r0 = r0.isDebugEnabled();	 Catch:{ all -> 0x0047 }
                if (r0 == 0) goto L_0x00ef;	 Catch:{ all -> 0x0047 }
            L_0x00d6:
                r0 = android.taobao.atlas.framework.Framework.log;	 Catch:{ all -> 0x0047 }
                r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0047 }
                r1.<init>();	 Catch:{ all -> 0x0047 }
                r3 = "UPDATE GRAPH IS ";	 Catch:{ all -> 0x0047 }
                r1 = r1.append(r3);	 Catch:{ all -> 0x0047 }
                r1 = r1.append(r6);	 Catch:{ all -> 0x0047 }
                r1 = r1.toString();	 Catch:{ all -> 0x0047 }
                r0.debug(r1);	 Catch:{ all -> 0x0047 }
            L_0x00ef:
                r0 = r6.size();	 Catch:{ all -> 0x0047 }
                r5 = new org.osgi.framework.Bundle[r0];	 Catch:{ all -> 0x0047 }
                r1 = -1;	 Catch:{ all -> 0x0047 }
                r0 = android.taobao.atlas.framework.Framework.getBundles();	 Catch:{ all -> 0x0047 }
                r3 = r0.size();	 Catch:{ all -> 0x0047 }
                r3 = new org.osgi.framework.Bundle[r3];	 Catch:{ all -> 0x0047 }
                r0 = r0.toArray(r3);	 Catch:{ all -> 0x0047 }
                r0 = (org.osgi.framework.Bundle[]) r0;	 Catch:{ all -> 0x0047 }
                r0 = (org.osgi.framework.Bundle[]) r0;	 Catch:{ all -> 0x0047 }
                r3 = r2;	 Catch:{ all -> 0x0047 }
            L_0x0109:
                r7 = r0.length;	 Catch:{ all -> 0x0047 }
                if (r3 >= r7) goto L_0x011d;	 Catch:{ all -> 0x0047 }
            L_0x010c:
                r7 = r0[r3];	 Catch:{ all -> 0x0047 }
                r7 = r6.contains(r7);	 Catch:{ all -> 0x0047 }
                if (r7 == 0) goto L_0x011a;	 Catch:{ all -> 0x0047 }
            L_0x0114:
                r1 = r1 + 1;	 Catch:{ all -> 0x0047 }
                r7 = r0[r3];	 Catch:{ all -> 0x0047 }
                r5[r1] = r7;	 Catch:{ all -> 0x0047 }
            L_0x011a:
                r3 = r3 + 1;	 Catch:{ all -> 0x0047 }
                goto L_0x0109;	 Catch:{ all -> 0x0047 }
            L_0x011d:
                r3 = android.taobao.atlas.framework.Framework.startlevel;	 Catch:{ all -> 0x0047 }
                r0 = android.taobao.atlas.framework.Framework.SystemBundle.this;	 Catch:{ all -> 0x0047 }
                r1 = 0;	 Catch:{ all -> 0x0047 }
                r6 = 1;	 Catch:{ all -> 0x0047 }
                r0.setLevel(r5, r1, r6);	 Catch:{ all -> 0x0047 }
                r1 = r2;	 Catch:{ all -> 0x0047 }
            L_0x0127:
                r0 = r5.length;	 Catch:{ all -> 0x0047 }
                if (r1 >= r0) goto L_0x0144;
            L_0x012a:
                r0 = r5[r1];	 Catch:{ Exception -> 0x013f }
                r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ Exception -> 0x013f }
                r0 = r0.classloader;	 Catch:{ Exception -> 0x013f }
                r6 = 0;	 Catch:{ Exception -> 0x013f }
                r0.cleanup(r6);	 Catch:{ Exception -> 0x013f }
                r0 = r5[r1];	 Catch:{ Exception -> 0x013f }
                r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ Exception -> 0x013f }
                r6 = 0;	 Catch:{ Exception -> 0x013f }
                r0.staleExportedPackages = r6;	 Catch:{ Exception -> 0x013f }
            L_0x013b:
                r0 = r1 + 1;
                r1 = r0;
                goto L_0x0127;
            L_0x013f:
                r0 = move-exception;
                r0.printStackTrace();	 Catch:{ all -> 0x0047 }
                goto L_0x013b;	 Catch:{ all -> 0x0047 }
            L_0x0144:
                r1 = r2;	 Catch:{ all -> 0x0047 }
            L_0x0145:
                r0 = r5.length;	 Catch:{ all -> 0x0047 }
                if (r1 >= r0) goto L_0x015d;	 Catch:{ all -> 0x0047 }
            L_0x0148:
                r0 = r5[r1];	 Catch:{ all -> 0x0047 }
                r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ all -> 0x0047 }
                r0 = r0.classloader;	 Catch:{ all -> 0x0047 }
                r6 = r0.exports;	 Catch:{ all -> 0x0047 }
                r6 = r6.length;	 Catch:{ all -> 0x0047 }
                if (r6 <= 0) goto L_0x0159;	 Catch:{ all -> 0x0047 }
            L_0x0153:
                r6 = r0.exports;	 Catch:{ all -> 0x0047 }
                r7 = 0;	 Catch:{ all -> 0x0047 }
                android.taobao.atlas.framework.Framework.export(r0, r6, r7);	 Catch:{ all -> 0x0047 }
            L_0x0159:
                r0 = r1 + 1;	 Catch:{ all -> 0x0047 }
                r1 = r0;	 Catch:{ all -> 0x0047 }
                goto L_0x0145;	 Catch:{ all -> 0x0047 }
            L_0x015d:
                r1 = r2;	 Catch:{ all -> 0x0047 }
            L_0x015e:
                r0 = r5.length;	 Catch:{ all -> 0x0047 }
                if (r1 >= r0) goto L_0x0179;
            L_0x0161:
                r0 = r5[r1];	 Catch:{ BundleException -> 0x0174 }
                r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ BundleException -> 0x0174 }
                r0 = r0.classloader;	 Catch:{ BundleException -> 0x0174 }
                r2 = 1;	 Catch:{ BundleException -> 0x0174 }
                r6 = new java.util.HashSet;	 Catch:{ BundleException -> 0x0174 }
                r6.<init>();	 Catch:{ BundleException -> 0x0174 }
                r0.resolveBundle(r2, r6);	 Catch:{ BundleException -> 0x0174 }
            L_0x0170:
                r0 = r1 + 1;
                r1 = r0;
                goto L_0x015e;
            L_0x0174:
                r0 = move-exception;
                r0.printStackTrace();	 Catch:{ all -> 0x0047 }
                goto L_0x0170;	 Catch:{ all -> 0x0047 }
            L_0x0179:
                r0 = android.taobao.atlas.framework.Framework.SystemBundle.this;	 Catch:{ all -> 0x0047 }
                r1 = 1;	 Catch:{ all -> 0x0047 }
                r0.setLevel(r5, r3, r1);	 Catch:{ all -> 0x0047 }
                r0 = 4;	 Catch:{ all -> 0x0047 }
                r1 = android.taobao.atlas.framework.Framework.systemBundle;	 Catch:{ all -> 0x0047 }
                r2 = 0;	 Catch:{ all -> 0x0047 }
                android.taobao.atlas.framework.Framework.notifyFrameworkListeners(r0, r1, r2);	 Catch:{ all -> 0x0047 }
                monitor-exit(r4);	 Catch:{ all -> 0x0047 }
                goto L_0x0051;
                */
                throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.framework.Framework.SystemBundle.AnonymousClass_3.run():void");
            }
        }

        SystemBundle() {
            this.props = new Hashtable();
            this.props.put(Constants.BUNDLE_NAME, Constants.SYSTEM_BUNDLE_LOCATION);
            this.props.put(Constants.BUNDLE_VERSION, Framework.FRAMEWORK_VERSION);
            this.props.put(Constants.BUNDLE_VENDOR, "Atlas");
            ServiceReferenceImpl serviceReferenceImpl = new ServiceReferenceImpl(this, this, null, new String[]{StartLevel.class.getName(), PackageAdmin.class.getName()});
            Framework.addValue(Framework.classes_services, StartLevel.class.getName(), serviceReferenceImpl);
            Framework.addValue(Framework.classes_services, PackageAdmin.class.getName(), serviceReferenceImpl);
            Framework.services.add(serviceReferenceImpl);
            this.registeredServices = new ServiceReference[]{serviceReferenceImpl};
        }

        public long getBundleId() {
            return 0;
        }

        public Dictionary<String, String> getHeaders() {
            return this.props;
        }

        public String getLocation() {
            return Constants.SYSTEM_BUNDLE_LOCATION;
        }

        public ServiceReference[] getRegisteredServices() {
            return this.registeredServices;
        }

        public URL getResource(String str) {
            return getClass().getResource(str);
        }

        public ServiceReference[] getServicesInUse() {
            return null;
        }

        public int getState() {
            return this.state;
        }

        public boolean hasPermission(Object obj) {
            return true;
        }

        public void start() throws BundleException {
        }

        public void stop() throws BundleException {
            shutdownThread(false);
        }

        public void uninstall() throws BundleException {
            throw new BundleException("Cannot uninstall the System Bundle");
        }

        public void update() throws BundleException {
            shutdownThread(true);
        }

        private void shutdownThread(boolean z) {
            new AnonymousClass_1(z).start();
        }

        public void update(InputStream inputStream) throws BundleException {
            shutdownThread(true);
        }

        public void update(File file) throws BundleException {
            shutdownThread(true);
        }

        public int getBundleStartLevel(Bundle bundle) {
            if (bundle == this) {
                return 0;
            }
            BundleImpl bundleImpl = (BundleImpl) bundle;
            if (bundleImpl.state != 1) {
                return bundleImpl.currentStartlevel;
            }
            throw new IllegalArgumentException("Bundle " + bundle + " has been uninstalled");
        }

        public int getInitialBundleStartLevel() {
            return Framework.initStartlevel;
        }

        public int getStartLevel() {
            return Framework.startlevel;
        }

        public boolean isBundlePersistentlyStarted(Bundle bundle) {
            if (bundle == this) {
                return true;
            }
            BundleImpl bundleImpl = (BundleImpl) bundle;
            if (bundleImpl.state != 1) {
                return bundleImpl.persistently;
            }
            throw new IllegalArgumentException("Bundle " + bundle + " has been uninstalled");
        }

        public void setBundleStartLevel(Bundle bundle, int i) {
            if (bundle == this) {
                throw new IllegalArgumentException("Cannot set the start level for the system bundle.");
            }
            BundleImpl bundleImpl = (BundleImpl) bundle;
            if (bundleImpl.state == 1) {
                throw new IllegalArgumentException("Bundle " + bundle + " has been uninstalled");
            } else if (i <= 0) {
                throw new IllegalArgumentException("Start level " + i + " is not a valid level");
            } else {
                bundleImpl.currentStartlevel = i;
                bundleImpl.updateMetadata();
                if (i <= Framework.startlevel && bundle.getState() != 32 && bundleImpl.persistently) {
                    try {
                        bundleImpl.startBundle();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Framework.notifyFrameworkListeners(2, bundle, e);
                    }
                } else if (i <= Framework.startlevel) {
                } else {
                    if (bundle.getState() != 4 || bundle.getState() != 2) {
                        try {
                            bundleImpl.stopBundle();
                        } catch (Throwable e2) {
                            Framework.notifyFrameworkListeners(2, bundle, e2);
                        }
                    }
                }
            }
        }

        public void setInitialBundleStartLevel(int i) {
            if (i <= 0) {
                throw new IllegalArgumentException("Start level " + i + " is not a valid level");
            }
            Framework.initStartlevel = i;
        }

        public void setStartLevel(int i) {
            if (i <= 0) {
                throw new IllegalArgumentException("Start level " + i + " is not a valid level");
            }
            new AnonymousClass_2(i).start();
        }

        @SuppressLint({"UseSparseArrays"})
        private void setLevel(Bundle[] bundleArr, int i, boolean z) {
            if (Framework.startlevel != i) {
                int i2 = i > Framework.startlevel ? 1 : 0;
                int i3 = i2 != 0 ? i - Framework.startlevel : Framework.startlevel - i;
                Map hashMap = new HashMap(0);
                int i4 = 0;
                while (i4 < bundleArr.length) {
                    if (bundleArr[i4] != Framework.systemBundle && (z || ((BundleImpl) bundleArr[i4]).persistently)) {
                        int i5;
                        BundleImpl bundleImpl = (BundleImpl) bundleArr[i4];
                        if (i2 != 0) {
                            i5 = (bundleImpl.currentStartlevel - Framework.startlevel) - 1;
                        } else {
                            i5 = Framework.startlevel - bundleImpl.currentStartlevel;
                        }
                        if (i5 >= 0 && i5 < i3) {
                            Framework.addValue(hashMap, Integer.valueOf(i5), bundleImpl);
                        }
                    }
                    i4++;
                }
                for (int i6 = 0; i6 < i3; i6++) {
                    if (i2 != 0) {
                        Framework.startlevel++;
                    } else {
                        Framework.startlevel--;
                    }
                    List list = (List) hashMap.get(Integer.valueOf(i6));
                    if (list != null) {
                        BundleImpl[] bundleImplArr = (BundleImpl[]) list.toArray(new BundleImpl[list.size()]);
                        for (i4 = 0; i4 < bundleImplArr.length; i4++) {
                            if (i2 != 0) {
                                try {
                                    System.out.println("STARTING " + bundleImplArr[i4].location);
                                    bundleImplArr[i4].startBundle();
                                } catch (Throwable e) {
                                    e.getNestedException().printStackTrace();
                                    e.printStackTrace();
                                    Framework.notifyFrameworkListeners(2, Framework.systemBundle, e);
                                } catch (Throwable e2) {
                                    e2.printStackTrace();
                                    Framework.notifyFrameworkListeners(2, Framework.systemBundle, e2);
                                }
                            } else if (bundleImplArr[i4].getState() != 1) {
                                System.out.println("STOPPING " + bundleImplArr[i4].location);
                                bundleImplArr[(bundleImplArr.length - i4) - 1].stopBundle();
                            }
                        }
                    }
                }
                Framework.startlevel = i;
            }
        }

        public ExportedPackage[] getExportedPackages(Bundle bundle) {
            return getExportedPackages(bundle, false);
        }

        private ExportedPackage[] getExportedPackages(Bundle bundle, boolean z) {
            synchronized (Framework.exportedPackages) {
                if (bundle != null) {
                    if (bundle != Framework.systemBundle) {
                        BundleImpl bundleImpl = (BundleImpl) bundle;
                        if (bundleImpl.state == 1) {
                            ExportedPackage[] exportedPackageArr;
                            if (z) {
                                exportedPackageArr = bundleImpl.staleExportedPackages;
                            } else {
                                exportedPackageArr = null;
                            }
                            return exportedPackageArr;
                        }
                        String[] strArr = bundleImpl.classloader.exports;
                        if (strArr == null) {
                            return null;
                        }
                        ArrayList arrayList = new ArrayList();
                        for (String str : strArr) {
                            Package packageR = (Package) Framework.exportedPackages.get(new Package(str, null, false));
                            if (packageR != null && packageR.classloader == bundleImpl.classloader) {
                                if (packageR.resolved) {
                                    arrayList.add(packageR);
                                } else {
                                    try {
                                        packageR.classloader.resolveBundle(true, new HashSet());
                                        arrayList.add(packageR);
                                    } catch (BundleException e) {
                                    }
                                }
                            }
                        }
                        if (bundleImpl.staleExportedPackages != null) {
                            arrayList.addAll(Arrays.asList(bundleImpl.staleExportedPackages));
                        }
                        System.out.println("\tBundle " + bundleImpl + " has exported packages " + arrayList);
                        return arrayList.isEmpty() ? null : (ExportedPackage[]) arrayList.toArray(new ExportedPackage[arrayList.size()]);
                    }
                }
                return (ExportedPackage[]) Framework.exportedPackages.keySet().toArray(new ExportedPackage[Framework.exportedPackages.size()]);
            }
        }

        public org.osgi.service.packageadmin.ExportedPackage getExportedPackage(java.lang.String r7) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.taobao.atlas.framework.Framework.SystemBundle.getExportedPackage(java.lang.String):org.osgi.service.packageadmin.ExportedPackage. bs: []
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:82)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JavaClass.getCode(JavaClass.java:45)
	at jadx.gui.treemodel.JClass.getContent(JClass.java:74)
	at jadx.gui.ui.ContentArea.<init>(ContentArea.java:66)
	at jadx.gui.ui.ContentPanel.<init>(ContentPanel.java:28)
	at jadx.gui.ui.TabbedPane.getCodePanel(TabbedPane.java:112)
	at jadx.gui.ui.TabbedPane.showCode(TabbedPane.java:69)
	at jadx.gui.ui.MainWindow.treeClickAction(MainWindow.java:241)
	at jadx.gui.ui.MainWindow.access$1000(MainWindow.java:66)
	at jadx.gui.ui.MainWindow$15.mouseClicked(MainWindow.java:519)
	at java.awt.AWTEventMulticaster.mouseClicked(AWTEventMulticaster.java:270)
	at java.awt.Component.processMouseEvent(Component.java:6528)
	at javax.swing.JComponent.processMouseEvent(JComponent.java:3322)
	at java.awt.Component.processEvent(Component.java:6290)
	at java.awt.Container.processEvent(Container.java:2234)
	at java.awt.Component.dispatchEventImpl(Component.java:4881)
	at java.awt.Container.dispatchEventImpl(Container.java:2292)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.LightweightDispatcher.retargetMouseEvent(Container.java:4898)
	at java.awt.LightweightDispatcher.processMouseEvent(Container.java:4542)
	at java.awt.LightweightDispatcher.dispatchEvent(Container.java:4462)
	at java.awt.Container.dispatchEventImpl(Container.java:2278)
	at java.awt.Window.dispatchEventImpl(Window.java:2739)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:751)
	at java.awt.EventQueue.access$500(EventQueue.java:97)
	at java.awt.EventQueue$3.run(EventQueue.java:702)
	at java.awt.EventQueue$3.run(EventQueue.java:696)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.awt.EventQueue$4.run(EventQueue.java:724)
	at java.awt.EventQueue$4.run(EventQueue.java:722)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.awt.EventQueue.dispatchEvent(EventQueue.java:721)
	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
*/
            /*
            r6 = this;
            r1 = 0;
            r2 = android.taobao.atlas.framework.Framework.exportedPackages;
            monitor-enter(r2);
            r0 = android.taobao.atlas.framework.Framework.exportedPackages;	 Catch:{ all -> 0x002d }
            r3 = new android.taobao.atlas.framework.Package;	 Catch:{ all -> 0x002d }
            r4 = 0;	 Catch:{ all -> 0x002d }
            r5 = 0;	 Catch:{ all -> 0x002d }
            r3.<init>(r7, r4, r5);	 Catch:{ all -> 0x002d }
            r0 = r0.get(r3);	 Catch:{ all -> 0x002d }
            r0 = (android.taobao.atlas.framework.Package) r0;	 Catch:{ all -> 0x002d }
            if (r0 != 0) goto L_0x0018;	 Catch:{ all -> 0x002d }
        L_0x0015:
            monitor-exit(r2);	 Catch:{ all -> 0x002d }
            r0 = r1;	 Catch:{ all -> 0x002d }
        L_0x0017:
            return r0;	 Catch:{ all -> 0x002d }
        L_0x0018:
            r3 = r0.resolved;	 Catch:{ all -> 0x002d }
            if (r3 != 0) goto L_0x0027;
        L_0x001c:
            r3 = r0.classloader;	 Catch:{ BundleException -> 0x0029 }
            r4 = 1;	 Catch:{ BundleException -> 0x0029 }
            r5 = new java.util.HashSet;	 Catch:{ BundleException -> 0x0029 }
            r5.<init>();	 Catch:{ BundleException -> 0x0029 }
            r3.resolveBundle(r4, r5);	 Catch:{ BundleException -> 0x0029 }
        L_0x0027:
            monitor-exit(r2);
            goto L_0x0017;
        L_0x0029:
            r0 = move-exception;
            monitor-exit(r2);	 Catch:{ all -> 0x002d }
            r0 = r1;
            goto L_0x0017;
        L_0x002d:
            r0 = move-exception;
            monitor-exit(r2);
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.framework.Framework.SystemBundle.getExportedPackage(java.lang.String):org.osgi.service.packageadmin.ExportedPackage");
        }

        public void refreshPackages(Bundle[] bundleArr) {
            new AnonymousClass_3(bundleArr).start();
        }

        public String toString() {
            return "SystemBundle";
        }
    }

    static android.taobao.atlas.framework.BundleImpl installNewBundle(java.lang.String r7, java.io.File r8) throws org.osgi.framework.BundleException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:4:0x000f in [B:4:0x000f, B:8:0x002c, B:3:0x000e]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:62)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JavaClass.getCode(JavaClass.java:45)
	at jadx.gui.treemodel.JClass.getContent(JClass.java:74)
	at jadx.gui.ui.ContentArea.<init>(ContentArea.java:66)
	at jadx.gui.ui.ContentPanel.<init>(ContentPanel.java:28)
	at jadx.gui.ui.TabbedPane.getCodePanel(TabbedPane.java:112)
	at jadx.gui.ui.TabbedPane.showCode(TabbedPane.java:69)
	at jadx.gui.ui.MainWindow.treeClickAction(MainWindow.java:241)
	at jadx.gui.ui.MainWindow.access$1000(MainWindow.java:66)
	at jadx.gui.ui.MainWindow$15.mouseClicked(MainWindow.java:519)
	at java.awt.AWTEventMulticaster.mouseClicked(AWTEventMulticaster.java:270)
	at java.awt.Component.processMouseEvent(Component.java:6528)
	at javax.swing.JComponent.processMouseEvent(JComponent.java:3322)
	at java.awt.Component.processEvent(Component.java:6290)
	at java.awt.Container.processEvent(Container.java:2234)
	at java.awt.Component.dispatchEventImpl(Component.java:4881)
	at java.awt.Container.dispatchEventImpl(Container.java:2292)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.LightweightDispatcher.retargetMouseEvent(Container.java:4898)
	at java.awt.LightweightDispatcher.processMouseEvent(Container.java:4542)
	at java.awt.LightweightDispatcher.dispatchEvent(Container.java:4462)
	at java.awt.Container.dispatchEventImpl(Container.java:2278)
	at java.awt.Window.dispatchEventImpl(Window.java:2739)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:751)
	at java.awt.EventQueue.access$500(EventQueue.java:97)
	at java.awt.EventQueue$3.run(EventQueue.java:702)
	at java.awt.EventQueue$3.run(EventQueue.java:696)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.awt.EventQueue$4.run(EventQueue.java:724)
	at java.awt.EventQueue$4.run(EventQueue.java:722)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.awt.EventQueue.dispatchEvent(EventQueue.java:721)
	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
*/
        /*
        android.taobao.atlas.util.BundleLock.WriteLock(r7);	 Catch:{ all -> 0x002b }
        r0 = getBundle(r7);	 Catch:{ all -> 0x002b }
        r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ all -> 0x002b }
        if (r0 == 0) goto L_0x000f;
    L_0x000b:
        android.taobao.atlas.util.BundleLock.WriteUnLock(r7);
    L_0x000e:
        return r0;
    L_0x000f:
        r1 = new java.io.File;	 Catch:{ all -> 0x002b }
        r0 = STORAGE_LOCATION;	 Catch:{ all -> 0x002b }
        r1.<init>(r0, r7);	 Catch:{ all -> 0x002b }
        r0 = new android.taobao.atlas.framework.BundleImpl;	 Catch:{ all -> 0x002b }
        r3 = new android.taobao.atlas.framework.BundleContextImpl;	 Catch:{ all -> 0x002b }
        r3.<init>();	 Catch:{ all -> 0x002b }
        r4 = 0;	 Catch:{ all -> 0x002b }
        r6 = 1;	 Catch:{ all -> 0x002b }
        r2 = r7;	 Catch:{ all -> 0x002b }
        r5 = r8;	 Catch:{ all -> 0x002b }
        r0.<init>(r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x002b }
        storeMetadata();	 Catch:{ all -> 0x002b }
        android.taobao.atlas.util.BundleLock.WriteUnLock(r7);
        goto L_0x000e;
    L_0x002b:
        r0 = move-exception;
        android.taobao.atlas.util.BundleLock.WriteUnLock(r7);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.framework.Framework.installNewBundle(java.lang.String, java.io.File):android.taobao.atlas.framework.BundleImpl");
    }

    static android.taobao.atlas.framework.BundleImpl installNewBundle(java.lang.String r7, java.io.InputStream r8) throws org.osgi.framework.BundleException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:4:0x000f in [B:4:0x000f, B:8:0x002c, B:3:0x000e]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:62)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JavaClass.getCode(JavaClass.java:45)
	at jadx.gui.treemodel.JClass.getContent(JClass.java:74)
	at jadx.gui.ui.ContentArea.<init>(ContentArea.java:66)
	at jadx.gui.ui.ContentPanel.<init>(ContentPanel.java:28)
	at jadx.gui.ui.TabbedPane.getCodePanel(TabbedPane.java:112)
	at jadx.gui.ui.TabbedPane.showCode(TabbedPane.java:69)
	at jadx.gui.ui.MainWindow.treeClickAction(MainWindow.java:241)
	at jadx.gui.ui.MainWindow.access$1000(MainWindow.java:66)
	at jadx.gui.ui.MainWindow$15.mouseClicked(MainWindow.java:519)
	at java.awt.AWTEventMulticaster.mouseClicked(AWTEventMulticaster.java:270)
	at java.awt.Component.processMouseEvent(Component.java:6528)
	at javax.swing.JComponent.processMouseEvent(JComponent.java:3322)
	at java.awt.Component.processEvent(Component.java:6290)
	at java.awt.Container.processEvent(Container.java:2234)
	at java.awt.Component.dispatchEventImpl(Component.java:4881)
	at java.awt.Container.dispatchEventImpl(Container.java:2292)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.LightweightDispatcher.retargetMouseEvent(Container.java:4898)
	at java.awt.LightweightDispatcher.processMouseEvent(Container.java:4542)
	at java.awt.LightweightDispatcher.dispatchEvent(Container.java:4462)
	at java.awt.Container.dispatchEventImpl(Container.java:2278)
	at java.awt.Window.dispatchEventImpl(Window.java:2739)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:751)
	at java.awt.EventQueue.access$500(EventQueue.java:97)
	at java.awt.EventQueue$3.run(EventQueue.java:702)
	at java.awt.EventQueue$3.run(EventQueue.java:696)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.awt.EventQueue$4.run(EventQueue.java:724)
	at java.awt.EventQueue$4.run(EventQueue.java:722)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.awt.EventQueue.dispatchEvent(EventQueue.java:721)
	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
*/
        /*
        android.taobao.atlas.util.BundleLock.WriteLock(r7);	 Catch:{ all -> 0x002b }
        r0 = getBundle(r7);	 Catch:{ all -> 0x002b }
        r0 = (android.taobao.atlas.framework.BundleImpl) r0;	 Catch:{ all -> 0x002b }
        if (r0 == 0) goto L_0x000f;
    L_0x000b:
        android.taobao.atlas.util.BundleLock.WriteUnLock(r7);
    L_0x000e:
        return r0;
    L_0x000f:
        r1 = new java.io.File;	 Catch:{ all -> 0x002b }
        r0 = STORAGE_LOCATION;	 Catch:{ all -> 0x002b }
        r1.<init>(r0, r7);	 Catch:{ all -> 0x002b }
        r0 = new android.taobao.atlas.framework.BundleImpl;	 Catch:{ all -> 0x002b }
        r3 = new android.taobao.atlas.framework.BundleContextImpl;	 Catch:{ all -> 0x002b }
        r3.<init>();	 Catch:{ all -> 0x002b }
        r5 = 0;	 Catch:{ all -> 0x002b }
        r6 = 1;	 Catch:{ all -> 0x002b }
        r2 = r7;	 Catch:{ all -> 0x002b }
        r4 = r8;	 Catch:{ all -> 0x002b }
        r0.<init>(r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x002b }
        storeMetadata();	 Catch:{ all -> 0x002b }
        android.taobao.atlas.util.BundleLock.WriteUnLock(r7);
        goto L_0x000e;
    L_0x002b:
        r0 = move-exception;
        android.taobao.atlas.util.BundleLock.WriteUnLock(r7);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.framework.Framework.installNewBundle(java.lang.String, java.io.InputStream):android.taobao.atlas.framework.BundleImpl");
    }

    static {
        log = LoggerFactory.getInstance("Framework");
        bundles = new ConcurrentHashMap();
        services = new ArrayList();
        classes_services = new HashMap();
        bundleListeners = new ArrayList();
        syncBundleListeners = new ArrayList();
        serviceListeners = new ArrayList();
        frameworkListeners = new ArrayList();
        exportedPackages = new ConcurrentHashMap();
        startlevel = 0;
        writeAheads = new ArrayList();
        initStartlevel = 1;
        frameworkStartupShutdown = false;
        restart = false;
        mMapForComAndBundles = new HashMap();
        mIsEnableBundleInstallWhenFindClass = false;
        ADMIN_PERMISSION = new AdminPermission();
    }

    private Framework() {
    }

    static void startup() throws BundleException {
        int i;
        int property;
        frameworkStartupShutdown = true;
        System.out.println("---------------------------------------------------------");
        System.out.println("  Atlas OSGi 0.9.0 on " + Build.MODEL + FilePathGenerator.ANDROID_DIR_SEP + Build.CPU_ABI + FilePathGenerator.ANDROID_DIR_SEP + VERSION.RELEASE + " starting ...");
        System.out.println("---------------------------------------------------------");
        long currentTimeMillis = System.currentTimeMillis();
        boolean property2 = getProperty("osgi.init", false);
        if (property2) {
            i = -1;
        } else {
            i = restoreProfile();
            restart = true;
        }
        if (i == -1) {
            restart = false;
            File file = new File(STORAGE_LOCATION);
            if (property2 && file.exists()) {
                System.out.println("Purging storage ...");
                try {
                    deleteDirectory(file);
                } catch (Throwable e) {
                    throw new RuntimeException("deleteDirectory failed", e);
                }
            }
            try {
                file.mkdirs();
                Integer.getInteger("osgi.maxLevel", Integer.valueOf(1)).intValue();
                initStartlevel = getProperty("osgi.startlevel.bundle", 1);
                property = getProperty("osgi.startlevel.framework", 1);
            } catch (Throwable e2) {
                throw new RuntimeException("mkdirs failed", e2);
            }
        }
        property = i;
        systemBundle.setLevel((Bundle[]) getBundles().toArray(new Bundle[bundles.size()]), property, false);
        frameworkStartupShutdown = false;
        if (!restart) {
            try {
                storeProfile();
            } catch (Throwable e22) {
                throw new RuntimeException("storeProfile failed", e22);
            }
        }
        long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
        System.out.println("---------------------------------------------------------");
        System.out.println("  Framework " + (restart ? "restarted" : "started") + " in " + currentTimeMillis2 + " milliseconds.");
        System.out.println("---------------------------------------------------------");
        System.out.flush();
        systemBundle.state = 32;
        try {
            notifyFrameworkListeners(1, systemBundle, null);
        } catch (Throwable e222) {
            throw new RuntimeException("notifyFrameworkListeners failed", e222);
        }
    }

    public static ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    public static List<Bundle> getBundles() {
        List<Bundle> arrayList = new ArrayList(bundles.size());
        synchronized (bundles) {
            arrayList.addAll(bundles.values());
        }
        return arrayList;
    }

    public static Bundle getBundle(String str) {
        return (Bundle) bundles.get(str);
    }

    public static Bundle getBundle(long j) {
        return null;
    }

    static void shutdown(boolean z) {
        System.out.println("---------------------------------------------------------");
        System.out.println("  Atlas OSGi shutting down ...");
        System.out.println("  Bye !");
        System.out.println("---------------------------------------------------------");
        systemBundle.state = 16;
        systemBundle.setLevel((Bundle[]) getBundles().toArray(new Bundle[bundles.size()]), 0, true);
        bundles.clear();
        systemBundle.state = 1;
        if (z) {
            try {
                startup();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public static void initialize(Properties properties) {
        if (properties == null) {
            properties = new Properties();
        }
        properties = properties;
        File filesDir = RuntimeVariables.androidApplication.getFilesDir();
        if (filesDir == null || !filesDir.exists()) {
            filesDir = RuntimeVariables.androidApplication.getFilesDir();
        }
        BASEDIR = properties.getProperty("android.taobao.atlas.basedir", filesDir.getAbsolutePath());
        BUNDLE_LOCATION = properties.getProperty("android.taobao.atlas.jars", "file:" + BASEDIR);
        CLASSLOADER_BUFFER_SIZE = getProperty("android.taobao.atlas.classloader.buffersize", (int) PlayerConstant.PLAYER_PLATFORM_WMWEB);
        LOG_LEVEL = getProperty("android.taobao.atlas.log.level", 6);
        DEBUG_BUNDLES = getProperty("android.taobao.atlas.debug.bundles", false);
        DEBUG_PACKAGES = getProperty("android.taobao.atlas.debug.packages", false);
        DEBUG_SERVICES = getProperty("android.taobao.atlas.debug.services", false);
        DEBUG_CLASSLOADING = getProperty("android.taobao.atlas.debug.classloading", false);
        if (getProperty("android.taobao.atlas.debug", false)) {
            System.out.println("SETTING ALL DEBUG FLAGS");
            LOG_LEVEL = 3;
            DEBUG_BUNDLES = true;
            DEBUG_PACKAGES = true;
            DEBUG_SERVICES = true;
            DEBUG_CLASSLOADING = true;
        }
        STRICT_STARTUP = getProperty("android.taobao.atlas.strictStartup", false);
        String property = properties.getProperty("org.osgi.framework.system.packages");
        if (property != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(property, SymbolExpUtil.SYMBOL_COMMA);
            int countTokens = stringTokenizer.countTokens();
            for (int i = 0; i < countTokens; i++) {
                BundleClassLoader.FRAMEWORK_PACKAGES.add(stringTokenizer.nextToken().trim());
            }
        }
        properties.put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, System.getProperty("java.specification.name") + FilePathGenerator.ANDROID_DIR_SEP + System.getProperty("java.specification.version"));
        Properties properties2 = properties;
        String str = Constants.FRAMEWORK_OS_NAME;
        Object property2 = System.getProperty("os.name");
        if (property2 == null) {
            property2 = "undefined";
        }
        properties2.put(str, property2);
        properties2 = properties;
        str = Constants.FRAMEWORK_OS_VERSION;
        property2 = System.getProperty("os.version");
        if (property2 == null) {
            property2 = "undefined";
        }
        properties2.put(str, property2);
        properties2 = properties;
        str = Constants.FRAMEWORK_PROCESSOR;
        property2 = System.getProperty("os.arch");
        if (property2 == null) {
            property2 = "undefined";
        }
        properties2.put(str, property2);
        properties.put(Constants.FRAMEWORK_VERSION, FRAMEWORK_VERSION);
        properties.put(Constants.FRAMEWORK_VENDOR, "Atlas");
        property2 = Locale.getDefault().getLanguage();
        properties2 = properties;
        str = Constants.FRAMEWORK_LANGUAGE;
        if (property2 == null) {
            property2 = C0264v.ENGLISH;
        }
        properties2.put(str, property2);
        STORAGE_LOCATION = properties.getProperty("android.taobao.atlas.storage", properties.getProperty("org.osgi.framework.dir", BASEDIR + File.separatorChar + "storage")) + File.separatorChar;
        launch();
        notifyFrameworkListeners(0, systemBundle, null);
    }

    private static void launch() {
        systemBundle = new SystemBundle();
        systemBundle.state = 8;
    }

    public static boolean getProperty(String str, boolean z) {
        if (properties == null) {
            return z;
        }
        String str2 = (String) properties.get(str);
        return str2 != null ? Boolean.valueOf(str2).booleanValue() : z;
    }

    public static int getProperty(String str, int i) {
        if (properties == null) {
            return i;
        }
        String str2 = (String) properties.get(str);
        return str2 != null ? Integer.parseInt(str2) : i;
    }

    public static String getProperty(String str) {
        if (properties == null) {
            return null;
        }
        return (String) properties.get(str);
    }

    public static String getProperty(String str, String str2) {
        return properties == null ? str2 : (String) properties.get(str);
    }

    protected static void warning(String str) throws RuntimeException {
        if (getProperty("android.taobao.atlas.strictStartup", false)) {
            throw new RuntimeException(str);
        }
        System.err.println("WARNING: " + str);
    }

    private static void storeProfile() {
        BundleImpl[] bundleImplArr = (BundleImpl[]) getBundles().toArray(new BundleImpl[bundles.size()]);
        for (BundleImpl updateMetadata : bundleImplArr) {
            updateMetadata.updateMetadata();
        }
        storeMetadata();
    }

    static void storeMetadata() {
        File file;
        Throwable e;
        try {
            file = new File(STORAGE_LOCATION, "meta");
            try {
                if (!AtlasFileLock.getInstance().LockExclusive(file)) {
                    log.error("Failed to get fileLock for " + file.getAbsolutePath());
                    AtlasFileLock.getInstance().unLock(file);
                } else if (file.length() > 0) {
                    AtlasFileLock.getInstance().unLock(file);
                } else {
                    DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
                    dataOutputStream.writeInt(startlevel);
                    String join = StringUtils.join(writeAheads.toArray(), SymbolExpUtil.SYMBOL_COMMA);
                    if (join == null) {
                        join = com.taobao.tao.util.Constants.ALIPAY_PARNER;
                    }
                    dataOutputStream.writeUTF(join);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    AtlasFileLock.getInstance().unLock(file);
                }
            } catch (IOException e2) {
                e = e2;
                try {
                    log.error("Could not save meta data.", e);
                    AtlasFileLock.getInstance().unLock(file);
                } catch (Throwable th) {
                    e = th;
                    AtlasFileLock.getInstance().unLock(file);
                    throw e;
                }
            }
        } catch (IOException e3) {
            e = e3;
            file = null;
            log.error("Could not save meta data.", e);
            AtlasFileLock.getInstance().unLock(file);
        } catch (Throwable th2) {
            e = th2;
            file = null;
            AtlasFileLock.getInstance().unLock(file);
            throw e;
        }
    }

    private static int restoreProfile() {
        try {
            System.out.println("Restoring profile");
            File file = new File(STORAGE_LOCATION, "meta");
            if (file.exists()) {
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                int readInt = dataInputStream.readInt();
                String[] split = StringUtils.split(dataInputStream.readUTF(), SymbolExpUtil.SYMBOL_COMMA);
                if (split != null) {
                    writeAheads.addAll(Arrays.asList(split));
                }
                dataInputStream.close();
                if (!getProperty("android.taobao.atlas.auto.load", true)) {
                    return readInt;
                }
                File file2 = new File(STORAGE_LOCATION);
                mergeWalsDir(new File(STORAGE_LOCATION, "wal"), file2);
                File[] listFiles = file2.listFiles(new FilenameFilter() {
                    public boolean accept(File file, String str) {
                        if (str.matches("^[0-9]*")) {
                            return false;
                        }
                        return true;
                    }
                });
                int i = 0;
                while (i < listFiles.length) {
                    if (listFiles[i].isDirectory() && new File(listFiles[i], "meta").exists()) {
                        try {
                            System.out.println("RESTORED BUNDLE " + new BundleImpl(listFiles[i], new BundleContextImpl()).location);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e.getCause());
                        }
                    }
                    i++;
                }
                return readInt;
            }
            System.out.println("Profile not found, performing clean start ...");
            return -1;
        } catch (Exception e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    private static void mergeWalsDir(File file, File file2) {
        if (writeAheads != null && writeAheads.size() > 0) {
            for (int i = 0; i < writeAheads.size(); i++) {
                if (writeAheads.get(i) != null) {
                    File file3 = new File(file, (String) writeAheads.get(i));
                    if (file3 != null) {
                        try {
                            if (file3.exists()) {
                                File[] listFiles = file3.listFiles();
                                if (listFiles != null) {
                                    for (File file4 : listFiles) {
                                        if (file4.isDirectory()) {
                                            File file5 = new File(file2, file4.getName());
                                            if (file5.exists()) {
                                                File[] listFiles2 = file4.listFiles(new FilenameFilter() {
                                                    public boolean accept(File file, String str) {
                                                        return str.startsWith(BundleArchive.REVISION_DIRECTORY);
                                                    }
                                                });
                                                if (listFiles2 != null) {
                                                    for (File file6 : listFiles2) {
                                                        if (new File(file6, "meta").exists()) {
                                                            file6.renameTo(new File(file5, file6.getName()));
                                                        }
                                                    }
                                                }
                                            } else {
                                                file4.renameTo(file5);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            log.error("Error while merge wal dir", e);
                        }
                    }
                    writeAheads.set(i, null);
                }
            }
        }
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteDirectory(File file) {
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isDirectory()) {
                deleteDirectory(listFiles[i]);
            } else {
                listFiles[i].delete();
            }
        }
        file.delete();
    }

    static void checkAdminPermission() {
        AccessController.checkPermission(ADMIN_PERMISSION);
    }

    static BundleImpl installNewBundle(String str) throws BundleException {
        try {
            String str2 = str.indexOf(":") > -1 ? str : BUNDLE_LOCATION + File.separatorChar + str;
            return installNewBundle(str2, new URL(str2).openConnection().getInputStream());
        } catch (Throwable e) {
            throw new BundleException("Cannot retrieve bundle from " + str, e);
        }
    }

    static void installOrUpdate(String[] strArr, File[] fileArr) throws BundleException {
        if (strArr == null || fileArr == null || strArr.length != fileArr.length) {
            throw new IllegalArgumentException("locations and files must not be null and must be same length");
        }
        String valueOf = String.valueOf(System.currentTimeMillis());
        File file = new File(new File(STORAGE_LOCATION, "wal"), valueOf);
        file.mkdirs();
        int i = 0;
        while (i < strArr.length) {
            if (!(strArr[i] == null || fileArr[i] == null)) {
                try {
                    BundleLock.WriteLock(strArr[i]);
                    Bundle bundle = getBundle(strArr[i]);
                    if (bundle != null) {
                        bundle.update(fileArr[i]);
                    } else {
                        BundleImpl bundleImpl = new BundleImpl(new File(file, strArr[i]), strArr[i], new BundleContextImpl(), null, fileArr[i], false);
                    }
                    BundleLock.WriteUnLock(strArr[i]);
                } catch (Throwable th) {
                    BundleLock.WriteUnLock(strArr[i]);
                }
            }
            i++;
        }
        writeAheads.add(valueOf);
        storeMetadata();
    }

    static void unregisterService(ServiceReference serviceReference) {
        services.remove(serviceReference);
        removeValue(classes_services, (String[]) serviceReference.getProperty(Constants.OBJECTCLASS), serviceReference);
        BundleImpl bundleImpl = (BundleImpl) serviceReference.getBundle();
        bundleImpl.registeredServices.remove(serviceReference);
        if (bundleImpl.registeredServices.isEmpty()) {
            bundleImpl.registeredServices = null;
        }
        notifyServiceListeners(4, serviceReference);
        if (DEBUG_SERVICES && log.isInfoEnabled()) {
            log.info("Framework: UNREGISTERED SERVICE " + serviceReference);
        }
    }

    static void notifyBundleListeners(int i, Bundle bundle) {
        int i2 = 0;
        if (!syncBundleListeners.isEmpty() || !bundleListeners.isEmpty()) {
            BundleEvent bundleEvent = new BundleEvent(i, bundle);
            BundleListener[] bundleListenerArr = (BundleListener[]) syncBundleListeners.toArray(new BundleListener[syncBundleListeners.size()]);
            for (BundleListener bundleChanged : bundleListenerArr) {
                bundleChanged.bundleChanged(bundleEvent);
            }
            if (!bundleListeners.isEmpty()) {
                bundleListenerArr = (BundleListener[]) bundleListeners.toArray(new BundleListener[bundleListeners.size()]);
                while (i2 < bundleListenerArr.length) {
                    bundleListenerArr[i2].bundleChanged(bundleEvent);
                    i2++;
                }
            }
        }
    }

    static void addFrameworkListener(FrameworkListener frameworkListener) {
        frameworkListeners.add(frameworkListener);
    }

    static void removeFrameworkListener(FrameworkListener frameworkListener) {
        frameworkListeners.remove(frameworkListener);
    }

    static void addBundleListener(BundleListener bundleListener) {
        bundleListeners.add(bundleListener);
    }

    static void removeBundleListener(BundleListener bundleListener) {
        bundleListeners.remove(bundleListener);
    }

    static void notifyFrameworkListeners(int i, Bundle bundle, Throwable th) {
        if (!frameworkListeners.isEmpty()) {
            FrameworkEvent frameworkEvent = new FrameworkEvent(i, bundle, th);
            FrameworkListener[] frameworkListenerArr = (FrameworkListener[]) frameworkListeners.toArray(new FrameworkListener[frameworkListeners.size()]);
            for (FrameworkListener frameworkEvent2 : frameworkListenerArr) {
                frameworkEvent2.frameworkEvent(frameworkEvent);
            }
        }
    }

    static void notifyServiceListeners(int i, ServiceReference serviceReference) {
        if (!serviceListeners.isEmpty()) {
            ServiceEvent serviceEvent = new ServiceEvent(i, serviceReference);
            ServiceListenerEntry[] serviceListenerEntryArr = (ServiceListenerEntry[]) serviceListeners.toArray(new ServiceListenerEntry[serviceListeners.size()]);
            int i2 = 0;
            while (i2 < serviceListenerEntryArr.length) {
                if (serviceListenerEntryArr[i2].filter == null || serviceListenerEntryArr[i2].filter.match(((ServiceReferenceImpl) serviceReference).properties)) {
                    serviceListenerEntryArr[i2].listener.serviceChanged(serviceEvent);
                }
                i2++;
            }
        }
    }

    static void clearBundleTrace(BundleImpl bundleImpl) {
        int i = 0;
        if (bundleImpl.registeredFrameworkListeners != null) {
            frameworkListeners.removeAll(bundleImpl.registeredFrameworkListeners);
            bundleImpl.registeredFrameworkListeners = null;
        }
        if (bundleImpl.registeredServiceListeners != null) {
            serviceListeners.removeAll(bundleImpl.registeredServiceListeners);
            bundleImpl.registeredServiceListeners = null;
        }
        if (bundleImpl.registeredBundleListeners != null) {
            bundleListeners.removeAll(bundleImpl.registeredBundleListeners);
            syncBundleListeners.removeAll(bundleImpl.registeredBundleListeners);
            bundleImpl.registeredBundleListeners = null;
        }
        ServiceReference[] registeredServices = bundleImpl.getRegisteredServices();
        if (registeredServices != null) {
            for (int i2 = 0; i2 < registeredServices.length; i2++) {
                unregisterService(registeredServices[i2]);
                ((ServiceReferenceImpl) registeredServices[i2]).invalidate();
            }
            bundleImpl.registeredServices = null;
        }
        ServiceReference[] servicesInUse = bundleImpl.getServicesInUse();
        while (i < servicesInUse.length) {
            ((ServiceReferenceImpl) servicesInUse[i]).ungetService(bundleImpl);
            i++;
        }
    }

    static void addValue(Map map, Object obj, Object obj2) {
        List list = (List) map.get(obj);
        if (list == null) {
            list = new ArrayList();
        }
        list.add(obj2);
        map.put(obj, list);
    }

    static void removeValue(Map map, Object[] objArr, Object obj) {
        for (int i = 0; i < objArr.length; i++) {
            List list = (List) map.get(objArr[i]);
            if (list != null) {
                list.remove(obj);
                if (list.isEmpty()) {
                    map.remove(objArr[i]);
                } else {
                    map.put(objArr[i], list);
                }
            }
        }
    }

    static void export(BundleClassLoader bundleClassLoader, String[] strArr, boolean z) {
        synchronized (exportedPackages) {
            if (DEBUG_PACKAGES && log.isDebugEnabled()) {
                log.debug("Bundle " + bundleClassLoader.bundle + " registers " + (z ? "resolved" : "unresolved") + " packages " + Arrays.asList(strArr));
            }
            for (String str : strArr) {
                Package packageR = new Package(str, bundleClassLoader, z);
                Package packageR2 = (Package) exportedPackages.get(packageR);
                if (packageR2 == null) {
                    exportedPackages.put(packageR, packageR);
                    if (DEBUG_PACKAGES && log.isDebugEnabled()) {
                        log.debug("REGISTERED PACKAGE " + packageR);
                    }
                } else if (packageR2.importingBundles == null && packageR.updates(packageR2)) {
                    exportedPackages.remove(packageR2);
                    exportedPackages.put(packageR, packageR);
                    if (DEBUG_PACKAGES && log.isDebugEnabled()) {
                        log.debug("REPLACED PACKAGE " + packageR2 + " WITH " + packageR);
                    }
                }
            }
        }
    }

    static android.taobao.atlas.framework.BundleClassLoader getImport(android.taobao.atlas.framework.BundleImpl r6, java.lang.String r7, boolean r8, java.util.HashSet<android.taobao.atlas.framework.BundleClassLoader> r9) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.taobao.atlas.framework.Framework.getImport(android.taobao.atlas.framework.BundleImpl, java.lang.String, boolean, java.util.HashSet):android.taobao.atlas.framework.BundleClassLoader. bs: []
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:82)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JavaClass.getCode(JavaClass.java:45)
	at jadx.gui.treemodel.JClass.getContent(JClass.java:74)
	at jadx.gui.ui.ContentArea.<init>(ContentArea.java:66)
	at jadx.gui.ui.ContentPanel.<init>(ContentPanel.java:28)
	at jadx.gui.ui.TabbedPane.getCodePanel(TabbedPane.java:112)
	at jadx.gui.ui.TabbedPane.showCode(TabbedPane.java:69)
	at jadx.gui.ui.MainWindow.treeClickAction(MainWindow.java:241)
	at jadx.gui.ui.MainWindow.access$1000(MainWindow.java:66)
	at jadx.gui.ui.MainWindow$15.mouseClicked(MainWindow.java:519)
	at java.awt.AWTEventMulticaster.mouseClicked(AWTEventMulticaster.java:270)
	at java.awt.Component.processMouseEvent(Component.java:6528)
	at javax.swing.JComponent.processMouseEvent(JComponent.java:3322)
	at java.awt.Component.processEvent(Component.java:6290)
	at java.awt.Container.processEvent(Container.java:2234)
	at java.awt.Component.dispatchEventImpl(Component.java:4881)
	at java.awt.Container.dispatchEventImpl(Container.java:2292)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.LightweightDispatcher.retargetMouseEvent(Container.java:4898)
	at java.awt.LightweightDispatcher.processMouseEvent(Container.java:4542)
	at java.awt.LightweightDispatcher.dispatchEvent(Container.java:4462)
	at java.awt.Container.dispatchEventImpl(Container.java:2278)
	at java.awt.Window.dispatchEventImpl(Window.java:2739)
	at java.awt.Component.dispatchEvent(Component.java:4703)
	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:751)
	at java.awt.EventQueue.access$500(EventQueue.java:97)
	at java.awt.EventQueue$3.run(EventQueue.java:702)
	at java.awt.EventQueue$3.run(EventQueue.java:696)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.awt.EventQueue$4.run(EventQueue.java:724)
	at java.awt.EventQueue$4.run(EventQueue.java:722)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:75)
	at java.awt.EventQueue.dispatchEvent(EventQueue.java:721)
	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
*/
        /*
        r2 = 0;
        r0 = DEBUG_PACKAGES;
        if (r0 == 0) goto L_0x0031;
    L_0x0005:
        r0 = log;
        r0 = r0.isDebugEnabled();
        if (r0 == 0) goto L_0x0031;
    L_0x000d:
        r0 = log;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r3 = "Bundle ";
        r1 = r1.append(r3);
        r1 = r1.append(r6);
        r3 = " requests package ";
        r1 = r1.append(r3);
        r1 = r1.append(r7);
        r1 = r1.toString();
        r0.debug(r1);
    L_0x0031:
        r3 = exportedPackages;
        monitor-enter(r3);
        r0 = exportedPackages;	 Catch:{ all -> 0x00c4 }
        r1 = new android.taobao.atlas.framework.Package;	 Catch:{ all -> 0x00c4 }
        r4 = 0;	 Catch:{ all -> 0x00c4 }
        r5 = 0;	 Catch:{ all -> 0x00c4 }
        r1.<init>(r7, r4, r5);	 Catch:{ all -> 0x00c4 }
        r0 = r0.get(r1);	 Catch:{ all -> 0x00c4 }
        r0 = (android.taobao.atlas.framework.Package) r0;	 Catch:{ all -> 0x00c4 }
        if (r0 == 0) goto L_0x004b;	 Catch:{ all -> 0x00c4 }
    L_0x0045:
        r1 = r0.resolved;	 Catch:{ all -> 0x00c4 }
        if (r1 != 0) goto L_0x004e;	 Catch:{ all -> 0x00c4 }
    L_0x0049:
        if (r8 != 0) goto L_0x004e;	 Catch:{ all -> 0x00c4 }
    L_0x004b:
        monitor-exit(r3);	 Catch:{ all -> 0x00c4 }
        r0 = r2;	 Catch:{ all -> 0x00c4 }
    L_0x004d:
        return r0;	 Catch:{ all -> 0x00c4 }
    L_0x004e:
        r1 = r0.classloader;	 Catch:{ all -> 0x00c4 }
        r4 = r6.classloader;	 Catch:{ all -> 0x00c4 }
        if (r1 != r4) goto L_0x0057;	 Catch:{ all -> 0x00c4 }
    L_0x0054:
        monitor-exit(r3);	 Catch:{ all -> 0x00c4 }
        r0 = r1;	 Catch:{ all -> 0x00c4 }
        goto L_0x004d;	 Catch:{ all -> 0x00c4 }
    L_0x0057:
        if (r8 == 0) goto L_0x0070;	 Catch:{ all -> 0x00c4 }
    L_0x0059:
        r4 = r0.resolved;	 Catch:{ all -> 0x00c4 }
        if (r4 != 0) goto L_0x0070;	 Catch:{ all -> 0x00c4 }
    L_0x005d:
        r4 = r0.classloader;	 Catch:{ all -> 0x00c4 }
        r4 = r9.contains(r4);	 Catch:{ all -> 0x00c4 }
        if (r4 != 0) goto L_0x0070;
    L_0x0065:
        r4 = r6.classloader;	 Catch:{ Exception -> 0x00bd }
        r9.add(r4);	 Catch:{ Exception -> 0x00bd }
        r4 = r0.classloader;	 Catch:{ Exception -> 0x00bd }
        r5 = 1;	 Catch:{ Exception -> 0x00bd }
        r4.resolveBundle(r5, r9);	 Catch:{ Exception -> 0x00bd }
    L_0x0070:
        r2 = r0.importingBundles;	 Catch:{ all -> 0x00c4 }
        if (r2 != 0) goto L_0x007b;	 Catch:{ all -> 0x00c4 }
    L_0x0074:
        r2 = new java.util.ArrayList;	 Catch:{ all -> 0x00c4 }
        r2.<init>();	 Catch:{ all -> 0x00c4 }
        r0.importingBundles = r2;	 Catch:{ all -> 0x00c4 }
    L_0x007b:
        r2 = r0.importingBundles;	 Catch:{ all -> 0x00c4 }
        r2 = r2.contains(r6);	 Catch:{ all -> 0x00c4 }
        if (r2 != 0) goto L_0x0088;	 Catch:{ all -> 0x00c4 }
    L_0x0083:
        r0 = r0.importingBundles;	 Catch:{ all -> 0x00c4 }
        r0.add(r6);	 Catch:{ all -> 0x00c4 }
    L_0x0088:
        r0 = DEBUG_PACKAGES;	 Catch:{ all -> 0x00c4 }
        if (r0 == 0) goto L_0x00ba;	 Catch:{ all -> 0x00c4 }
    L_0x008c:
        r0 = log;	 Catch:{ all -> 0x00c4 }
        r0 = r0.isDebugEnabled();	 Catch:{ all -> 0x00c4 }
        if (r0 == 0) goto L_0x00ba;	 Catch:{ all -> 0x00c4 }
    L_0x0094:
        r0 = log;	 Catch:{ all -> 0x00c4 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00c4 }
        r2.<init>();	 Catch:{ all -> 0x00c4 }
        r4 = "REQUESTED PACKAGE ";	 Catch:{ all -> 0x00c4 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x00c4 }
        r2 = r2.append(r7);	 Catch:{ all -> 0x00c4 }
        r4 = ", RETURNED DELEGATION TO ";	 Catch:{ all -> 0x00c4 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x00c4 }
        r4 = r1.bundle;	 Catch:{ all -> 0x00c4 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x00c4 }
        r2 = r2.toString();	 Catch:{ all -> 0x00c4 }
        r0.debug(r2);	 Catch:{ all -> 0x00c4 }
    L_0x00ba:
        monitor-exit(r3);	 Catch:{ all -> 0x00c4 }
        r0 = r1;	 Catch:{ all -> 0x00c4 }
        goto L_0x004d;	 Catch:{ all -> 0x00c4 }
    L_0x00bd:
        r0 = move-exception;	 Catch:{ all -> 0x00c4 }
        r0.printStackTrace();	 Catch:{ all -> 0x00c4 }
        monitor-exit(r3);	 Catch:{ all -> 0x00c4 }
        r0 = r2;
        goto L_0x004d;
    L_0x00c4:
        r0 = move-exception;
        monitor-exit(r3);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.framework.Framework.getImport(android.taobao.atlas.framework.BundleImpl, java.lang.String, boolean, java.util.HashSet):android.taobao.atlas.framework.BundleClassLoader");
    }

    public static boolean isFrameworkStartupShutdown() {
        return frameworkStartupShutdown;
    }

    public static ClassNotFoundInterceptorCallback getClassNotFoundCallback() {
        return classNotFoundCallback;
    }

    public static void setClassNotFoundCallback(ClassNotFoundInterceptorCallback classNotFoundInterceptorCallback) {
        classNotFoundCallback = classNotFoundInterceptorCallback;
    }
}
