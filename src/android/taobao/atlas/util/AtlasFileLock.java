package android.taobao.atlas.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Process;
import android.taobao.atlas.runtime.RuntimeVariables;
import android.util.Log;

public class AtlasFileLock {
    private static final String TAG = "AtlasFileLock";
    private static String processName;
    private static AtlasFileLock singleton;
    private Map<String, FileLockCount> mRefCountMap;

    private class FileLockCount {
        FileLock mFileLock;
        int mRefCount;

        FileLockCount(FileLock fileLock, int i) {
            this.mFileLock = fileLock;
            this.mRefCount = i;
        }
    }

    public AtlasFileLock() {
        this.mRefCountMap = new HashMap();
    }

    static {
        int myPid = Process.myPid();
        if (RuntimeVariables.androidApplication.getApplicationContext() != null) {
            for (RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) RuntimeVariables.androidApplication.getApplicationContext().getSystemService("activity")).getRunningAppProcesses()) {
                if (runningAppProcessInfo.pid == myPid) {
                    processName = runningAppProcessInfo.processName;
                }
            }
        }
    }

    public static AtlasFileLock getInstance() {
        if (singleton == null) {
            singleton = new AtlasFileLock();
        }
        return singleton;
    }

    private int RefCntInc(String str, FileLock fileLock) {
        Integer valueOf;
        Integer.valueOf(0);
        if (this.mRefCountMap.containsKey(str)) {
            FileLockCount fileLockCount = (FileLockCount) this.mRefCountMap.get(str);
            int i = fileLockCount.mRefCount;
            fileLockCount.mRefCount = i + 1;
            valueOf = Integer.valueOf(i);
        } else {
            valueOf = Integer.valueOf(1);
            this.mRefCountMap.put(str, new FileLockCount(fileLock, valueOf.intValue()));
        }
        return valueOf.intValue();
    }

    private int RefCntDec(String str) {
        Integer valueOf = Integer.valueOf(0);
        if (this.mRefCountMap.containsKey(str)) {
            FileLockCount fileLockCount = (FileLockCount) this.mRefCountMap.get(str);
            int i = fileLockCount.mRefCount - 1;
            fileLockCount.mRefCount = i;
            valueOf = Integer.valueOf(i);
            if (valueOf.intValue() <= 0) {
                this.mRefCountMap.remove(str);
            }
        }
        return valueOf.intValue();
    }

    public boolean LockExclusive(File file) {
        if (file == null) {
            return false;
        }
        try {
            FileChannel channel = new RandomAccessFile(file.getAbsolutePath(), "rw").getChannel();
            if (channel == null) {
                return false;
            }
            Log.i(TAG, processName + " attempting to FileLock " + file);
            FileLock lock = channel.lock();
            if (!lock.isValid()) {
                return false;
            }
            RefCntInc(file.getAbsolutePath(), lock);
            Log.i(TAG, processName + " FileLock " + file + " Suc! ");
            return true;
        } catch (Exception e) {
            Log.e(TAG, processName + " FileLock " + file + " FAIL! " + e.getMessage());
            return false;
        }
    }

    public void unLock(File file) {
        if (file == null || this.mRefCountMap.containsKey(file.getAbsolutePath())) {
            FileLock fileLock = ((FileLockCount) this.mRefCountMap.get(file.getAbsolutePath())).mFileLock;
            if (fileLock != null && fileLock.isValid()) {
                try {
                    if (RefCntDec(file.getAbsolutePath()) <= 0) {
                        fileLock.release();
                        Log.i(TAG, processName + " FileLock " + file.getAbsolutePath() + " SUC! ");
                    }
                } catch (IOException e) {
                }
            }
        }
    }
}
