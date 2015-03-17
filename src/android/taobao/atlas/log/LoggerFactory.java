package android.taobao.atlas.log;

public class LoggerFactory {
    public static int logLevel;

    static {
        logLevel = 3;
    }

    public static Logger getInstance(String str) {
        return getInstance(str, null);
    }

    public static Logger getInstance(Class<?> cls) {
        return getInstance(null, cls);
    }

    private static Logger getInstance(String str, Class<?> cls) {
        if (cls != null) {
            return new AndroidLogger((Class) cls);
        }
        return new AndroidLogger(str);
    }
}
