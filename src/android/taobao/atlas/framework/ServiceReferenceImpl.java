package android.taobao.atlas.framework;

import com.taobao.weapp.data.WeAppDataParser;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

final class ServiceReferenceImpl implements ServiceReference {
    private static final HashSet<String> forbidden;
    private static long nextServiceID;
    Bundle bundle;
    private HashMap<Bundle, Object> cachedServices;
    private final boolean isServiceFactory;
    final Dictionary<String, Object> properties;
    ServiceRegistration registration;
    private Object service;
    final Map<Bundle, Integer> useCounters;

    private final class ServiceRegistrationImpl implements ServiceRegistration {
        private ServiceRegistrationImpl() {
        }

        public ServiceReference getReference() {
            if (ServiceReferenceImpl.this.service != null) {
                return ServiceReferenceImpl.this;
            }
            throw new IllegalStateException("Service has already been uninstalled");
        }

        public void setProperties(Dictionary<String, ?> dictionary) {
            if (ServiceReferenceImpl.this.service == null) {
                throw new IllegalStateException("Service has already been uninstalled");
            }
            HashMap hashMap = new HashMap(ServiceReferenceImpl.this.properties.size());
            Enumeration keys = ServiceReferenceImpl.this.properties.keys();
            while (keys.hasMoreElements()) {
                String str = (String) keys.nextElement();
                String toLowerCase = str.toLowerCase(Locale.US);
                if (hashMap.containsKey(toLowerCase)) {
                    throw new IllegalArgumentException("Properties contain the same key in different case variants");
                }
                hashMap.put(toLowerCase, str);
            }
            keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                str = (String) keys.nextElement();
                Object obj = dictionary.get(str);
                String toLowerCase2 = str.toLowerCase(Locale.US);
                if (!ServiceReferenceImpl.forbidden.contains(toLowerCase2)) {
                    Object obj2 = hashMap.get(toLowerCase2);
                    if (obj2 != null) {
                        if (obj2.equals(str)) {
                            ServiceReferenceImpl.this.properties.remove(obj2);
                        } else {
                            throw new IllegalArgumentException("Properties already exists in a different case variant");
                        }
                    }
                    ServiceReferenceImpl.this.properties.put(str, obj);
                }
            }
            Framework.notifyServiceListeners(2, ServiceReferenceImpl.this);
        }

        public void unregister() {
            if (ServiceReferenceImpl.this.service == null) {
                throw new IllegalStateException("Service has already been uninstalled");
            }
            Framework.unregisterService(ServiceReferenceImpl.this);
            ServiceReferenceImpl.this.service = null;
        }
    }

    static {
        nextServiceID = 0;
        forbidden = new HashSet();
        forbidden.add(Constants.SERVICE_ID.toLowerCase(Locale.US));
        forbidden.add(Constants.OBJECTCLASS.toLowerCase(Locale.US));
    }

    ServiceReferenceImpl(Bundle bundle, Object obj, Dictionary<String, ?> dictionary, String[] strArr) {
        this.useCounters = new HashMap(0);
        this.cachedServices = null;
        if (obj instanceof ServiceFactory) {
            this.isServiceFactory = true;
        } else {
            this.isServiceFactory = false;
            checkService(obj, strArr);
        }
        this.bundle = bundle;
        this.service = obj;
        this.properties = dictionary == null ? new Hashtable() : new Hashtable(dictionary.size());
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                String str = (String) keys.nextElement();
                this.properties.put(str, dictionary.get(str));
            }
        }
        this.properties.put(Constants.OBJECTCLASS, strArr);
        Dictionary dictionary2 = this.properties;
        String str2 = Constants.SERVICE_ID;
        long j = nextServiceID + 1;
        nextServiceID = j;
        dictionary2.put(str2, Long.valueOf(j));
        Integer num = dictionary == null ? null : (Integer) dictionary.get(Constants.SERVICE_RANKING);
        this.properties.put(Constants.SERVICE_RANKING, Integer.valueOf(num == null ? 0 : num.intValue()));
        this.registration = new ServiceRegistrationImpl();
    }

    private void checkService(Object obj, String[] strArr) {
        int i = 0;
        while (i < strArr.length) {
            try {
                if (Class.forName(strArr[i], false, obj.getClass().getClassLoader()).isInstance(obj)) {
                    i++;
                } else {
                    throw new IllegalArgumentException("Service " + obj.getClass().getName() + " does not implement the interface " + strArr[i]);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Interface " + strArr[i] + " implemented by service " + obj.getClass().getName() + " cannot be located: " + e.getMessage());
            }
        }
    }

    void invalidate() {
        this.service = null;
        this.useCounters.clear();
        this.bundle = null;
        this.registration = null;
        if (this.cachedServices != null) {
            this.cachedServices = null;
        }
        String[] propertyKeys = getPropertyKeys();
        for (Object remove : propertyKeys) {
            this.properties.remove(remove);
        }
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public Object getProperty(String str) {
        Object obj = this.properties.get(str);
        if (obj != null) {
            return obj;
        }
        obj = this.properties.get(str.toLowerCase(Locale.US));
        if (obj != null) {
            return obj;
        }
        Object obj2;
        Enumeration keys = this.properties.keys();
        while (keys.hasMoreElements()) {
            String str2 = (String) keys.nextElement();
            if (str2.equalsIgnoreCase(str)) {
                obj2 = this.properties.get(str2);
                break;
            }
        }
        obj2 = obj;
        return obj2;
    }

    public String[] getPropertyKeys() {
        ArrayList arrayList = new ArrayList(this.properties.size());
        Enumeration keys = this.properties.keys();
        while (keys.hasMoreElements()) {
            arrayList.add(keys.nextElement());
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    public Bundle[] getUsingBundles() {
        Bundle[] bundleArr;
        synchronized (this.useCounters) {
            if (this.useCounters.isEmpty()) {
                bundleArr = null;
            } else {
                bundleArr = (Bundle[]) this.useCounters.keySet().toArray(new Bundle[this.useCounters.size()]);
            }
        }
        return bundleArr;
    }

    Object getService(Bundle bundle) {
        if (this.service == null) {
            return null;
        }
        synchronized (this.useCounters) {
            Object valueOf;
            Integer num = (Integer) this.useCounters.get(bundle);
            if (num == null) {
                valueOf = Integer.valueOf(1);
            } else {
                valueOf = Integer.valueOf(num.intValue() + 1);
            }
            this.useCounters.put(bundle, valueOf);
            if (this.isServiceFactory) {
                if (this.cachedServices == null) {
                    this.cachedServices = new HashMap();
                }
                valueOf = this.cachedServices.get(bundle);
                if (valueOf != null) {
                    return valueOf;
                }
                try {
                    Object service = ((ServiceFactory) this.service).getService(bundle, this.registration);
                    checkService(service, (String[]) this.properties.get(Constants.OBJECTCLASS));
                    this.cachedServices.put(bundle, service);
                    return service;
                } catch (Throwable e) {
                    Framework.notifyFrameworkListeners(2, null, e);
                    return null;
                }
            }
            valueOf = this.service;
            return valueOf;
        }
    }

    boolean ungetService(Bundle bundle) {
        synchronized (this.useCounters) {
            if (this.service == null) {
                return false;
            }
            Integer num = (Integer) this.useCounters.get(bundle);
            if (num == null) {
                return false;
            } else if (num.intValue() == 1) {
                this.useCounters.remove(bundle);
                if (this.isServiceFactory) {
                    ((ServiceFactory) this.service).ungetService(bundle, this.registration, this.cachedServices.get(bundle));
                    this.cachedServices.remove(bundle);
                }
                return false;
            } else {
                this.useCounters.put(bundle, Integer.valueOf(num.intValue() - 1));
                return true;
            }
        }
    }

    public String toString() {
        return "ServiceReference{" + this.service + WeAppDataParser.KEY_SURFIX;
    }
}
