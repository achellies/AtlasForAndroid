package android.taobao.atlas.log;

public interface Logger {
    void debug(String str);

    void error(String str);

    void error(String str, Throwable th);

    void error(StringBuffer stringBuffer, Throwable th);

    void fatal(String str);

    void fatal(String str, Throwable th);

    void info(String str);

    boolean isDebugEnabled();

    boolean isErrorEnabled();

    boolean isFatalEnabled();

    boolean isInfoEnabled();

    boolean isVerboseEnabled();

    boolean isWarnEnabled();

    void verbose(String str);

    void warn(String str);

    void warn(String str, Throwable th);

    void warn(StringBuffer stringBuffer, Throwable th);
}
