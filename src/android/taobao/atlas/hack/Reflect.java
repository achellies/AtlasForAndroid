package android.taobao.atlas.hack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflect {
    public static boolean sIsReflectAvailable;

    static {
        sIsReflectAvailable = true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.reflect.Method getMethod(java.lang.Class<?> r2, java.lang.String r3, java.lang.Class<?>... r4) {
        /*
        r0 = r2.getDeclaredMethod(r3, r4);	 Catch:{ SecurityException -> 0x0009, NoSuchMethodException -> 0x000f }
        r1 = 1;
        r0.setAccessible(r1);	 Catch:{ SecurityException -> 0x0009, NoSuchMethodException -> 0x000f }
    L_0x0008:
        return r0;
    L_0x0009:
        r0 = move-exception;
        r0.printStackTrace();
    L_0x000d:
        r0 = 0;
        goto L_0x0008;
    L_0x000f:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.hack.Reflect.getMethod(java.lang.Class, java.lang.String, java.lang.Class[]):java.lang.reflect.Method");
    }

    public static Object invokeMethod(Method method, Object obj, Object... objArr) {
        try {
            method.setAccessible(true);
            return method.invoke(obj, objArr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object fieldGet(Class<?> cls, Object obj, String str) {
        try {
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            return null;
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static Object fieldGet(Field field, Object obj) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean fieldSet(java.lang.reflect.Field r2, java.lang.Object r3, java.lang.Object r4) {
        /*
        r0 = 1;
        r1 = 1;
        r2.setAccessible(r1);	 Catch:{ SecurityException -> 0x0009, IllegalArgumentException -> 0x000f, IllegalAccessException -> 0x0014 }
        r2.set(r3, r4);	 Catch:{ SecurityException -> 0x0009, IllegalArgumentException -> 0x000f, IllegalAccessException -> 0x0014 }
    L_0x0008:
        return r0;
    L_0x0009:
        r0 = move-exception;
        r0.printStackTrace();
    L_0x000d:
        r0 = 0;
        goto L_0x0008;
    L_0x000f:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x000d;
    L_0x0014:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.hack.Reflect.fieldSet(java.lang.reflect.Field, java.lang.Object, java.lang.Object):boolean");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean fieldSet(java.lang.Class<?> r3, java.lang.Object r4, java.lang.String r5, java.lang.Object r6) {
        /*
        r0 = 1;
        r1 = r3.getDeclaredField(r5);	 Catch:{ SecurityException -> 0x000d, NoSuchFieldException -> 0x0013, IllegalArgumentException -> 0x0018, IllegalAccessException -> 0x001d }
        r2 = 1;
        r1.setAccessible(r2);	 Catch:{ SecurityException -> 0x000d, NoSuchFieldException -> 0x0013, IllegalArgumentException -> 0x0018, IllegalAccessException -> 0x001d }
        r1.set(r4, r6);	 Catch:{ SecurityException -> 0x000d, NoSuchFieldException -> 0x0013, IllegalArgumentException -> 0x0018, IllegalAccessException -> 0x001d }
    L_0x000c:
        return r0;
    L_0x000d:
        r0 = move-exception;
        r0.printStackTrace();
    L_0x0011:
        r0 = 0;
        goto L_0x000c;
    L_0x0013:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0011;
    L_0x0018:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0011;
    L_0x001d:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0011;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.hack.Reflect.fieldSet(java.lang.Class, java.lang.Object, java.lang.String, java.lang.Object):boolean");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.reflect.Field getField(java.lang.Class<?> r2, java.lang.String r3) {
        /*
        r0 = r2.getDeclaredField(r3);	 Catch:{ SecurityException -> 0x0009, NoSuchFieldException -> 0x000f }
        r1 = 1;
        r0.setAccessible(r1);	 Catch:{ SecurityException -> 0x0009, NoSuchFieldException -> 0x000f }
    L_0x0008:
        return r0;
    L_0x0009:
        r0 = move-exception;
        r0.printStackTrace();
    L_0x000d:
        r0 = 0;
        goto L_0x0008;
    L_0x000f:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.hack.Reflect.getField(java.lang.Class, java.lang.String):java.lang.reflect.Field");
    }
}
