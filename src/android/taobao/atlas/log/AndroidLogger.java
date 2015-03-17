package android.taobao.atlas.log;

import android.util.Log;

public class AndroidLogger implements Logger {
    private final String category;

    public AndroidLogger(String str) {
        this.category = str;
    }

    public AndroidLogger(Class<?> cls) {
        this(cls.getSimpleName());
    }

    public void verbose(String str) {
        String str2 = this.category;
    }

    public void debug(String str) {
        String str2 = this.category;
    }

    public void info(String str) {
        Log.i(this.category, str);
    }

    public void warn(String str) {
        String str2 = this.category;
    }

    public void warn(String str, Throwable th) {
        String str2 = this.category;
    }

    public void warn(StringBuffer stringBuffer, Throwable th) {
        warn(stringBuffer.toString(), th);
    }

    public void error(String str) {
        Log.e(this.category, str);
    }

    public void error(String str, Throwable th) {
        Log.e(this.category, str, th);
    }

    public void error(StringBuffer stringBuffer, Throwable th) {
        error(stringBuffer.toString(), th);
    }

    public void fatal(String str) {
        error(str);
    }

    public void fatal(String str, Throwable th) {
        error(str, th);
    }

    public boolean isVerboseEnabled() {
        return LoggerFactory.logLevel <= 2;
    }

    public boolean isDebugEnabled() {
        return LoggerFactory.logLevel <= 3;
    }

    public boolean isInfoEnabled() {
        return LoggerFactory.logLevel <= 4;
    }

    public boolean isWarnEnabled() {
        return LoggerFactory.logLevel <= 5;
    }

    public boolean isErrorEnabled() {
        return LoggerFactory.logLevel <= 6;
    }

    public boolean isFatalEnabled() {
        return LoggerFactory.logLevel <= 6;
    }
}
