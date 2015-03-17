package android.taobao.atlas.framework;

import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import com.tencent.mm.sdk.platformtools.MAlarmHandler;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;

public class BundleContextImpl implements BundleContext {
    static final Logger log;
    BundleImpl bundle;
    boolean isValid;

    public BundleContextImpl() {
        this.isValid = true;
    }

    static {
        log = LoggerFactory.getInstance("BundleContextImpl");
    }

    private void checkValid() {
        if (!this.isValid) {
            throw new IllegalStateException("BundleContext of bundle " + this.bundle + " used after bundle has been stopped or uninstalled.");
        }
    }

    public void addBundleListener(BundleListener bundleListener) {
        checkValid();
        List list = bundleListener instanceof SynchronousBundleListener ? Framework.syncBundleListeners : Framework.bundleListeners;
        if (this.bundle.registeredBundleListeners == null) {
            this.bundle.registeredBundleListeners = new ArrayList();
        }
        if (!this.bundle.registeredBundleListeners.contains(bundleListener)) {
            list.add(bundleListener);
            this.bundle.registeredBundleListeners.add(bundleListener);
        }
    }

    public void addFrameworkListener(FrameworkListener frameworkListener) {
        checkValid();
        if (this.bundle.registeredFrameworkListeners == null) {
            this.bundle.registeredFrameworkListeners = new ArrayList();
        }
        if (!this.bundle.registeredFrameworkListeners.contains(frameworkListener)) {
            Framework.frameworkListeners.add(frameworkListener);
            this.bundle.registeredFrameworkListeners.add(frameworkListener);
        }
    }

    public void addServiceListener(ServiceListener serviceListener, String str) throws InvalidSyntaxException {
        checkValid();
        ServiceListenerEntry serviceListenerEntry = new ServiceListenerEntry(serviceListener, str);
        if (this.bundle.registeredServiceListeners == null) {
            this.bundle.registeredServiceListeners = new ArrayList();
        }
        if (isServiceListenerRegistered(serviceListener)) {
            Framework.serviceListeners.remove(serviceListenerEntry);
        } else {
            this.bundle.registeredServiceListeners.add(serviceListener);
        }
        Framework.serviceListeners.add(serviceListenerEntry);
    }

    private boolean isServiceListenerRegistered(ServiceListener serviceListener) {
        ServiceListener[] serviceListenerArr = (ServiceListener[]) this.bundle.registeredServiceListeners.toArray(new ServiceListener[this.bundle.registeredServiceListeners.size()]);
        for (ServiceListener serviceListener2 : serviceListenerArr) {
            if (serviceListener2 == serviceListener) {
                return true;
            }
        }
        return false;
    }

    public void addServiceListener(ServiceListener serviceListener) {
        checkValid();
        try {
            addServiceListener(serviceListener, null);
        } catch (InvalidSyntaxException e) {
        }
    }

    public Filter createFilter(String str) throws InvalidSyntaxException {
        if (str != null) {
            return RFC1960Filter.fromString(str);
        }
        throw new NullPointerException();
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public Bundle getBundle(long j) {
        checkValid();
        return null;
    }

    public Bundle[] getBundles() {
        checkValid();
        List bundles = Framework.getBundles();
        Bundle[] bundleArr = (Bundle[]) bundles.toArray(new Bundle[bundles.size()]);
        Object obj = new Bundle[(bundleArr.length + 1)];
        obj[0] = Framework.systemBundle;
        System.arraycopy(bundleArr, 0, obj, 1, bundleArr.length);
        return obj;
    }

    public File getDataFile(String str) {
        checkValid();
        try {
            File file = new File(new File(this.bundle.bundleDir, "/data/"), str);
            file.getParentFile().mkdirs();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getProperty(String str) {
        return (String) Framework.properties.get(str);
    }

    public Object getService(ServiceReference serviceReference) {
        checkValid();
        if (serviceReference != null) {
            return ((ServiceReferenceImpl) serviceReference).getService(this.bundle);
        }
        throw new NullPointerException("Null service reference.");
    }

    public ServiceReference[] getServiceReferences(String str, String str2) throws InvalidSyntaxException {
        Collection collection;
        checkValid();
        Filter fromString = RFC1960Filter.fromString(str2);
        if (str == null) {
            collection = Framework.services;
        } else {
            List list = (List) Framework.classes_services.get(str);
            if (list == null) {
                return null;
            }
        }
        List arrayList = new ArrayList();
        ServiceReferenceImpl[] serviceReferenceImplArr = (ServiceReferenceImpl[]) collection.toArray(new ServiceReferenceImpl[collection.size()]);
        for (int i = 0; i < serviceReferenceImplArr.length; i++) {
            if (fromString.match(serviceReferenceImplArr[i])) {
                arrayList.add(serviceReferenceImplArr[i]);
            }
        }
        if (Framework.DEBUG_SERVICES && log.isInfoEnabled()) {
            log.info("Framework: REQUESTED SERVICES " + str + " " + str2);
            log.info("\tRETURNED " + arrayList);
        }
        return arrayList.size() == 0 ? null : (ServiceReference[]) arrayList.toArray(new ServiceReference[arrayList.size()]);
    }

    public ServiceReference getServiceReference(String str) {
        ServiceReference serviceReference = null;
        checkValid();
        int i = -1;
        long j = MAlarmHandler.NEXT_FIRE_INTERVAL;
        List list = (List) Framework.classes_services.get(str);
        if (list != null) {
            ServiceReference[] serviceReferenceArr = (ServiceReference[]) list.toArray(new ServiceReference[list.size()]);
            int i2 = 0;
            while (i2 < serviceReferenceArr.length) {
                int intValue;
                ServiceReference serviceReference2;
                int i3;
                Integer num = (Integer) serviceReferenceArr[i2].getProperty(Constants.SERVICE_RANKING);
                if (num != null) {
                    intValue = num.intValue();
                } else {
                    intValue = 0;
                }
                long longValue = ((Long) serviceReferenceArr[i2].getProperty(Constants.SERVICE_ID)).longValue();
                if (intValue > i || (intValue == i && longValue < j)) {
                    serviceReference2 = serviceReferenceArr[i2];
                    i3 = intValue;
                } else {
                    longValue = j;
                    i3 = i;
                    serviceReference2 = serviceReference;
                }
                i2++;
                serviceReference = serviceReference2;
                i = i3;
                j = longValue;
            }
            if (Framework.DEBUG_SERVICES && log.isInfoEnabled()) {
                log.info("Framework: REQUESTED SERVICE " + str);
                log.info("\tRETURNED " + serviceReference);
            }
        }
        return serviceReference;
    }

    public Bundle installBundle(String str) throws BundleException {
        if (str == null) {
            throw new IllegalArgumentException("Location must not be null");
        }
        checkValid();
        return Framework.installNewBundle(str);
    }

    public Bundle installBundle(String str, InputStream inputStream) throws BundleException {
        if (str == null) {
            throw new IllegalArgumentException("Location must not be null");
        }
        checkValid();
        return Framework.installNewBundle(str, inputStream);
    }

    public ServiceRegistration registerService(String[] strArr, Object obj, Dictionary<String, ?> dictionary) {
        checkValid();
        if (obj == null) {
            throw new IllegalArgumentException("Cannot register a null service");
        }
        Object serviceReferenceImpl = new ServiceReferenceImpl(this.bundle, obj, dictionary, strArr);
        Framework.services.add(serviceReferenceImpl);
        if (this.bundle.registeredServices == null) {
            this.bundle.registeredServices = new ArrayList();
        }
        this.bundle.registeredServices.add(serviceReferenceImpl);
        for (Object addValue : strArr) {
            Framework.addValue(Framework.classes_services, addValue, serviceReferenceImpl);
        }
        if (Framework.DEBUG_SERVICES && log.isInfoEnabled()) {
            log.info("Framework: REGISTERED SERVICE " + strArr[0]);
        }
        Framework.notifyServiceListeners(1, serviceReferenceImpl);
        return serviceReferenceImpl.registration;
    }

    public ServiceRegistration registerService(String str, Object obj, Dictionary<String, ?> dictionary) {
        return registerService(new String[]{str}, obj, (Dictionary) dictionary);
    }

    public void removeBundleListener(BundleListener bundleListener) {
        checkValid();
        (bundleListener instanceof SynchronousBundleListener ? Framework.syncBundleListeners : Framework.bundleListeners).remove(bundleListener);
        this.bundle.registeredBundleListeners.remove(bundleListener);
        if (this.bundle.registeredBundleListeners.isEmpty()) {
            this.bundle.registeredBundleListeners = null;
        }
    }

    public void removeFrameworkListener(FrameworkListener frameworkListener) {
        checkValid();
        Framework.frameworkListeners.remove(frameworkListener);
        this.bundle.registeredFrameworkListeners.remove(frameworkListener);
        if (this.bundle.registeredFrameworkListeners.isEmpty()) {
            this.bundle.registeredFrameworkListeners = null;
        }
    }

    public void removeServiceListener(ServiceListener serviceListener) {
        checkValid();
        try {
            Framework.serviceListeners.remove(new ServiceListenerEntry(serviceListener, null));
            this.bundle.registeredServiceListeners.remove(serviceListener);
            if (this.bundle.registeredServiceListeners.isEmpty()) {
                this.bundle.registeredServiceListeners = null;
            }
        } catch (InvalidSyntaxException e) {
        }
    }

    public synchronized boolean ungetService(ServiceReference serviceReference) {
        checkValid();
        return ((ServiceReferenceImpl) serviceReference).ungetService(this.bundle);
    }
}
