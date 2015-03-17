package android.taobao.atlas.util;


import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static final String EMPTY = "";
    public static String[] EMPTY_STRING_ARRAY = new String[0];
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    public static boolean isBlank(String str) {
        if (str != null) {
            int length = str.length();
            if (length != 0) {
                for (int i = 0; i < length; i++) {
                    if (!Character.isWhitespace(str.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    public static boolean startWith(String str, String str2) {
        if (str == null || str2 == null) {
            return false;
        }
        return str.startsWith(str2);
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static boolean equals(String str, String str2) {
        if (str == null) {
            return false;
        }
        return str.equals(str2);
    }

    public static boolean contains(String str, String str2) {
        if (str == null || str2 == null || str.indexOf(str2) < 0) {
            return false;
        }
        return true;
    }

    public static boolean contains(String[] strArr, String str) {
        if (strArr == null || str == null) {
            return false;
        }
        for (Object equals : strArr) {
            if (str.equals(equals)) {
                return true;
            }
        }
        return false;
    }

    public static String substringBefore(String str, String str2) {
        if (isEmpty(str) || str2 == null) {
            return str;
        }
        if (str2.length() == 0) {
            return EMPTY;
        }
        int indexOf = str.indexOf(str2);
        return indexOf != -1 ? str.substring(0, indexOf) : str;
    }

    public static String substringAfter(String str, String str2) {
        if (isEmpty(str)) {
            return str;
        }
        if (str2 == null) {
            return EMPTY;
        }
        int indexOf = str.indexOf(str2);
        if (indexOf == -1) {
            return EMPTY;
        }
        return str.substring(indexOf + str2.length());
    }

    public static String substringBeforeLast(String str, String str2) {
        if (isEmpty(str) || isEmpty(str2)) {
            return str;
        }
        int lastIndexOf = str.lastIndexOf(str2);
        return lastIndexOf != -1 ? str.substring(0, lastIndexOf) : str;
    }

    public static String substringAfterLast(String str, String str2) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(str2)) {
            return EMPTY;
        }
        int lastIndexOf = str.lastIndexOf(str2);
        if (lastIndexOf == -1 || lastIndexOf == str.length() - str2.length()) {
            return EMPTY;
        }
        return str.substring(lastIndexOf + str2.length());
    }

    public static String substringBetween(String str, String str2, String str3) {
        if (str == null || str2 == null || str3 == null) {
            return null;
        }
        int indexOf = str.indexOf(str2);
        if (indexOf == -1) {
            return null;
        }
        int indexOf2 = str.indexOf(str3, str2.length() + indexOf);
        if (indexOf2 != -1) {
            return str.substring(str2.length() + indexOf, indexOf2);
        }
        return null;
    }

    public static String replace(String str, String str2, String str3) {
        return replace(str, str2, str3, -1);
    }

    public static String replace(String str, String str2, String str3, int i) {
        int i2 = 64;
        if (isEmpty(str) || isEmpty(str2) || str3 == null || i == 0) {
            return str;
        }
        int indexOf = str.indexOf(str2, 0);
        if (indexOf == -1) {
            return str;
        }
        int length = str2.length();
        int length2 = str3.length() - length;
        if (length2 < 0) {
            length2 = 0;
        }
        if (i < 0) {
            i2 = 16;
        } else if (i <= 64) {
            i2 = i;
        }
        StringBuilder stringBuilder = new StringBuilder((i2 * length2) + str.length());
        i2 = 0;
        while (indexOf != -1) {
            stringBuilder.append(str.substring(i2, indexOf)).append(str3);
            i2 = indexOf + length;
            i--;
            if (i == 0) {
                break;
            }
            indexOf = str.indexOf(str2, i2);
        }
        stringBuilder.append(str.substring(i2));
        return stringBuilder.toString();
    }

    public static String getPackageNameFromEntryName(String str) {
        return str.substring(str.indexOf("libcom_") + "lib".length(), str.indexOf(".so")).replace("_", ".");
    }

    public static boolean endsWith(String str, String str2) {
        return endsWith(str, str2, false);
    }

    private static boolean endsWith(String str, String str2, boolean z) {
        if (str == null || str2 == null) {
            if (str == null && str2 == null) {
                return true;
            }
            return false;
        } else if (str2.length() > str.length()) {
            return false;
        } else {
            return str.regionMatches(z, str.length() - str2.length(), str2, 0, str2.length());
        }
    }

    public static String[] split(String str, String str2) {
        return splitWorker(str, str2, -1, false);
    }

    private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    public static String join(Object[] objArr, String str) {
        if (objArr == null) {
            return null;
        }
        return join(objArr, str, 0, objArr.length);
    }

    public static String join(Object[] objArr, String str, int i, int i2) {
        if (objArr == null) {
            return null;
        }
        if (str == null) {
            str = EMPTY;
        }
        int i3 = i2 - i;
        if (i3 <= 0) {
            return EMPTY;
        }
        int i4;
        if (objArr[i] == null) {
            i4 = 16;
        } else {
            i4 = objArr[i].toString().length();
        }
        StringBuilder stringBuilder = new StringBuilder((i4 + str.length()) * i3);
        for (i4 = i; i4 < i2; i4++) {
            if (i4 > i) {
                stringBuilder.append(str);
            }
            if (objArr[i4] != null) {
                stringBuilder.append(objArr[i4]);
            }
        }
        return stringBuilder.toString();
    }
}
