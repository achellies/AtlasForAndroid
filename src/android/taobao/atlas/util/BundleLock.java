package android.taobao.atlas.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BundleLock {
    static ReentrantReadWriteLock[] mLock;

    static {
        mLock = new ReentrantReadWriteLock[10];
        for (int i = 0; i < 10; i++) {
            mLock[i] = new ReentrantReadWriteLock();
        }
    }

    public static void WriteLock(String str) {
        mLock[hash(str)].writeLock().lock();
    }

    public static void WriteUnLock(String str) {
        mLock[hash(str)].writeLock().unlock();
    }

    public static void ReadLock(String str) {
        mLock[hash(str)].readLock().lock();
    }

    public static void ReadUnLock(String str) {
        mLock[hash(str)].readLock().unlock();
    }

    private static int hash(String str) {
        int i = 0;
        int length = str.length();
        byte[] bytes = str.getBytes();
        int i2 = 0;
        while (i < length) {
            i2 += bytes[i];
            i++;
        }
        return i2 % 10;
    }
}
