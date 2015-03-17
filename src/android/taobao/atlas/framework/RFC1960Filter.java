package android.taobao.atlas.framework;

import android.support.v4.media.TransportMediator;
import com.taobao.tao.util.Constants;
import com.taobao.tao.util.TBImageQuailtyStrategy;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import mtopsdk.common.util.SymbolExpUtil;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

final class RFC1960Filter implements Filter {
    private static final int AND_OPERATOR = 1;
    private static final int APPROX = 2;
    private static final int EQUALS = 0;
    private static final int GREATER = 3;
    private static final int LESS = 4;
    private static final int NOT_OPERATOR = 3;
    private static final Filter NULL_FILTER;
    private static final String[] OP;
    private static final int OR_OPERATOR = 2;
    private static final int PRESENT = 1;
    private static final Class<?>[] STRINGCLASS;
    private List operands;
    private int operator;

    private static final class RFC1960SimpleFilter implements Filter {
        private final int comparator;
        private final String id;
        private final String value;

        private RFC1960SimpleFilter(String str, int i, String str2) {
            this.id = str;
            this.comparator = i;
            this.value = str2;
        }

        public boolean match(ServiceReference serviceReference) {
            try {
                return match(((ServiceReferenceImpl) serviceReference).properties);
            } catch (Exception e) {
                Dictionary hashtable = new Hashtable();
                String[] propertyKeys = serviceReference.getPropertyKeys();
                for (int i = RFC1960Filter.EQUALS; i < propertyKeys.length; i += RFC1960Filter.PRESENT) {
                    hashtable.put(propertyKeys[i], serviceReference.getProperty(propertyKeys[i]));
                }
                return match(hashtable);
            }
        }

        public boolean match(Dictionary dictionary) {
            Object obj;
            Object obj2 = dictionary.get(this.id);
            if (obj2 == null) {
                obj2 = dictionary.get(this.id.toLowerCase());
            }
            if (obj2 == null) {
                Enumeration keys = dictionary.keys();
                while (keys.hasMoreElements()) {
                    String str = (String) keys.nextElement();
                    if (str.equalsIgnoreCase(this.id)) {
                        obj = dictionary.get(str);
                        break;
                    }
                }
            }
            obj = obj2;
            if (obj == null) {
                return false;
            }
            if (this.comparator == RFC1960Filter.PRESENT) {
                return true;
            }
            try {
                if (obj instanceof String) {
                    return compareString(this.value, this.comparator, (String) obj);
                }
                if (obj instanceof Number) {
                    return compareNumber(this.value, this.comparator, (Number) obj);
                }
                if (obj instanceof String[]) {
                    String[] strArr = (String[]) obj;
                    if (strArr.length == 0) {
                        return false;
                    }
                    String stripWhitespaces = this.comparator == RFC1960Filter.OR_OPERATOR ? stripWhitespaces(this.value) : this.value;
                    for (int i = RFC1960Filter.EQUALS; i < strArr.length; i += RFC1960Filter.PRESENT) {
                        if (compareString(stripWhitespaces, this.comparator, strArr[i])) {
                            return true;
                        }
                    }
                    return false;
                } else if (obj instanceof Boolean) {
                    boolean z = ((this.comparator == 0 || this.comparator == RFC1960Filter.OR_OPERATOR) && ((Boolean) obj).equals(Boolean.valueOf(this.value))) ? RFC1960Filter.PRESENT : false;
                    return z;
                } else if (obj instanceof Character) {
                    if (this.value.length() == RFC1960Filter.PRESENT) {
                        return compareTyped(new Character(this.value.charAt(RFC1960Filter.EQUALS)), this.comparator, (Character) obj);
                    }
                    return false;
                } else if (obj instanceof Vector) {
                    Vector vector = (Vector) obj;
                    Object[] objArr = new Object[vector.size()];
                    vector.copyInto(objArr);
                    return compareArray(this.value, this.comparator, objArr);
                } else if (obj instanceof Object[]) {
                    return compareArray(this.value, this.comparator, (Object[]) obj);
                } else {
                    if (obj.getClass().isArray()) {
                        for (int i2 = RFC1960Filter.EQUALS; i2 < Array.getLength(obj); i2 += RFC1960Filter.PRESENT) {
                            Object obj3 = Array.get(obj, i2);
                            if (obj3 instanceof Number) {
                                if (compareNumber(this.value, this.comparator, (Number) obj3)) {
                                    return true;
                                }
                            }
                            if ((obj3 instanceof Comparable) && compareReflective(this.value, this.comparator, (Comparable) obj3)) {
                                return true;
                            }
                        }
                        return false;
                    } else if (obj instanceof Comparable) {
                        return compareReflective(this.value, this.comparator, (Comparable) obj);
                    } else {
                        return false;
                    }
                }
            } catch (Throwable th) {
                return false;
            }
        }

        private static boolean compareString(String str, int i, String str2) {
            if (i == RFC1960Filter.OR_OPERATOR) {
                str = stripWhitespaces(str).toLowerCase();
            }
            if (i == RFC1960Filter.OR_OPERATOR) {
                str2 = stripWhitespaces(str2).toLowerCase();
            }
            switch (i) {
                case RFC1960Filter.EQUALS /*0*/:
                case RFC1960Filter.OR_OPERATOR /*2*/:
                    if (stringCompare(str.toCharArray(), RFC1960Filter.EQUALS, str2.toCharArray(), RFC1960Filter.EQUALS) == 0) {
                        return true;
                    }
                    return false;
                case RFC1960Filter.NOT_OPERATOR /*3*/:
                    if (stringCompare(str.toCharArray(), RFC1960Filter.EQUALS, str2.toCharArray(), RFC1960Filter.EQUALS) > 0) {
                        return false;
                    }
                    return true;
                case RFC1960Filter.LESS /*4*/:
                    if (stringCompare(str.toCharArray(), RFC1960Filter.EQUALS, str2.toCharArray(), RFC1960Filter.EQUALS) < 0) {
                        return false;
                    }
                    return true;
                default:
                    throw new IllegalStateException("Found illegal comparator.");
            }
        }

        private static boolean compareNumber(String str, int i, Number number) {
            if (number instanceof Integer) {
                int intValue = ((Integer) number).intValue();
                int parseInt = Integer.parseInt(str);
                switch (i) {
                    case RFC1960Filter.NOT_OPERATOR /*3*/:
                        if (intValue < parseInt) {
                            return false;
                        }
                        return true;
                    case RFC1960Filter.LESS /*4*/:
                        if (intValue > parseInt) {
                            return false;
                        }
                        return true;
                    default:
                        if (intValue == parseInt) {
                            return true;
                        }
                        return false;
                }
            } else if (number instanceof Long) {
                long longValue = ((Long) number).longValue();
                long parseLong = Long.parseLong(str);
                switch (i) {
                    case RFC1960Filter.NOT_OPERATOR /*3*/:
                        if (longValue < parseLong) {
                            return false;
                        }
                        return true;
                    case RFC1960Filter.LESS /*4*/:
                        if (longValue > parseLong) {
                            return false;
                        }
                        return true;
                    default:
                        if (longValue != parseLong) {
                            return false;
                        }
                        return true;
                }
            } else if (number instanceof Short) {
                short shortValue = ((Short) number).shortValue();
                short parseShort = Short.parseShort(str);
                switch (i) {
                    case RFC1960Filter.NOT_OPERATOR /*3*/:
                        if (shortValue < parseShort) {
                            return false;
                        }
                        return true;
                    case RFC1960Filter.LESS /*4*/:
                        if (shortValue > parseShort) {
                            return false;
                        }
                        return true;
                    default:
                        if (shortValue != parseShort) {
                            return false;
                        }
                        return true;
                }
            } else if (number instanceof Double) {
                double doubleValue = ((Double) number).doubleValue();
                double parseDouble = Double.parseDouble(str);
                switch (i) {
                    case RFC1960Filter.NOT_OPERATOR /*3*/:
                        if (doubleValue < parseDouble) {
                            return false;
                        }
                        return true;
                    case RFC1960Filter.LESS /*4*/:
                        if (doubleValue > parseDouble) {
                            return false;
                        }
                        return true;
                    default:
                        if (doubleValue != parseDouble) {
                            return false;
                        }
                        return true;
                }
            } else if (number instanceof Float) {
                float floatValue = ((Float) number).floatValue();
                float parseFloat = Float.parseFloat(str);
                switch (i) {
                    case RFC1960Filter.NOT_OPERATOR /*3*/:
                        if (floatValue < parseFloat) {
                            return false;
                        }
                        return true;
                    case RFC1960Filter.LESS /*4*/:
                        if (floatValue > parseFloat) {
                            return false;
                        }
                        return true;
                    default:
                        if (floatValue != parseFloat) {
                            return false;
                        }
                        return true;
                }
            } else {
                if (number instanceof Byte) {
                    try {
                        return compareTyped(Byte.decode(str), i, (Byte) number);
                    } catch (Throwable th) {
                    }
                }
                return compareReflective(str, i, (Comparable) number);
            }
        }

        private static boolean compareTyped(Object obj, int i, Comparable comparable) {
            switch (i) {
                case RFC1960Filter.EQUALS /*0*/:
                case RFC1960Filter.OR_OPERATOR /*2*/:
                    return comparable.equals(obj);
                case RFC1960Filter.NOT_OPERATOR /*3*/:
                    if (comparable.compareTo(obj) < 0) {
                        return false;
                    }
                    return true;
                case RFC1960Filter.LESS /*4*/:
                    return comparable.compareTo(obj) <= 0;
                default:
                    throw new IllegalStateException("Found illegal comparator.");
            }
        }

        private static boolean compareArray(String str, int i, Object[] objArr) {
            for (int i2 = RFC1960Filter.EQUALS; i2 < objArr.length; i2 += RFC1960Filter.PRESENT) {
                Object obj = objArr[i2];
                if (obj instanceof String) {
                    if (compareString(str, i, (String) obj)) {
                        return true;
                    }
                } else if (obj instanceof Number) {
                    if (compareNumber(str, i, (Number) obj)) {
                        return true;
                    }
                } else if ((obj instanceof Comparable) && compareReflective(str, i, (Comparable) obj)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean compareReflective(String str, int i, Comparable comparable) {
            boolean z = false;
            try {
                Constructor constructor = comparable.getClass().getConstructor(RFC1960Filter.STRINGCLASS);
                Object[] objArr = new Object[RFC1960Filter.PRESENT];
                objArr[RFC1960Filter.EQUALS] = str;
                z = compareTyped(constructor.newInstance(objArr), i, comparable);
            } catch (Exception e) {
            }
            return z;
        }

        private static String stripWhitespaces(String str) {
            return str.replace(' ', '\u0000');
        }

        private static int stringCompare(char[] cArr, int i, char[] cArr2, int i2) {
            if (i == cArr.length) {
                return RFC1960Filter.EQUALS;
            }
            int length = cArr.length;
            int length2 = cArr2.length;
            int i3 = i2;
            while (i < length && i3 < length2) {
                if (cArr[i] == cArr2[i3]) {
                    i += RFC1960Filter.PRESENT;
                    i3 += RFC1960Filter.PRESENT;
                } else {
                    if (cArr[i] > 'A' && cArr[i] < 'Z') {
                        cArr[i] = (char) (cArr[i] + 32);
                    }
                    if (cArr2[i3] > 'A' && cArr2[i3] < 'Z') {
                        cArr2[i3] = (char) (cArr2[i3] + 32);
                    }
                    if (cArr[i] == cArr2[i3]) {
                        i += RFC1960Filter.PRESENT;
                        i3 += RFC1960Filter.PRESENT;
                    } else if (cArr[i] == '*') {
                        length = i + RFC1960Filter.PRESENT;
                        while (stringCompare(cArr, length, cArr2, i3) != 0) {
                            i3 += RFC1960Filter.PRESENT;
                            if (length2 - i3 <= -1) {
                                return RFC1960Filter.PRESENT;
                            }
                        }
                        return RFC1960Filter.EQUALS;
                    } else if (cArr[i] < cArr2[i3]) {
                        return -1;
                    } else {
                        if (cArr[i] > cArr2[i3]) {
                            return RFC1960Filter.PRESENT;
                        }
                    }
                }
            }
            if (i == length && i3 == length2 && cArr[i - 1] == cArr2[i3 - 1]) {
                return RFC1960Filter.EQUALS;
            }
            if (cArr[i - 1] == '*' && i == length && i3 == length2) {
                return RFC1960Filter.EQUALS;
            }
            if (length < length2) {
                i3 = length;
            } else {
                i3 = length2;
            }
            if (length == i3) {
                i3 = -1;
            } else {
                i3 = RFC1960Filter.PRESENT;
            }
            return i3;
        }

        public String toString() {
            return "(" + this.id + RFC1960Filter.OP[this.comparator] + (this.value == null ? Constants.ALIPAY_PARNER : this.value) + ")";
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof RFC1960SimpleFilter)) {
                return false;
            }
            RFC1960SimpleFilter rFC1960SimpleFilter = (RFC1960SimpleFilter) obj;
            if (this.comparator == rFC1960SimpleFilter.comparator && this.id.equals(rFC1960SimpleFilter.id) && this.value.equals(rFC1960SimpleFilter.value)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }
    }

    static {
        OP = new String[]{SymbolExpUtil.SYMBOL_EQUAL, "=*", "~=", ">=", "<="};
        Class[] clsArr = new Class[PRESENT];
        clsArr[EQUALS] = String.class;
        STRINGCLASS = clsArr;
        NULL_FILTER = new Filter() {
            public boolean match(ServiceReference serviceReference) {
                return true;
            }

            public boolean match(Dictionary dictionary) {
                return true;
            }
        };
    }

    private RFC1960Filter(int i) {
        this.operands = new ArrayList(PRESENT);
        this.operator = i;
    }

    static Filter fromString(String str) throws InvalidSyntaxException {
        if (str == null) {
            return NULL_FILTER;
        }
        Stack stack = new Stack();
        try {
            int length = str.length();
            int i = -1;
            int i2 = EQUALS;
            String str2 = null;
            int i3 = -1;
            char[] toCharArray = str.toCharArray();
            stack.clear();
            int i4 = EQUALS;
            while (i4 < toCharArray.length) {
                int i5;
                int i6;
                String str3;
                int i7;
                String trim;
                switch (toCharArray[i4]) {
                    case TBImageQuailtyStrategy.CDN_SIZE_40 /*40*/:
                        char c = toCharArray[i4 + PRESENT];
                        i5 = i4;
                        while (Character.isWhitespace(c)) {
                            i4 = i5 + PRESENT;
                            c = toCharArray[i4 + PRESENT];
                            i5 = i4;
                        }
                        if (c == '&') {
                            stack.push(new RFC1960Filter(PRESENT));
                            i6 = i3;
                            str3 = str2;
                            i3 = i2;
                            i7 = i;
                            break;
                        } else if (c == '|') {
                            stack.push(new RFC1960Filter(OR_OPERATOR));
                            i6 = i3;
                            str3 = str2;
                            i3 = i2;
                            i7 = i;
                            break;
                        } else if (c == '!') {
                            stack.push(new RFC1960Filter(NOT_OPERATOR));
                            i6 = i3;
                            str3 = str2;
                            i3 = i2;
                            i7 = i;
                            break;
                        } else if (i == -1) {
                            i6 = i3;
                            str3 = str2;
                            i3 = i2;
                            i7 = i5;
                            break;
                        } else {
                            throw new InvalidSyntaxException("Surplus left paranthesis at: " + str.substring(i5), str);
                        }
                    case ')':
                        RFC1960Filter rFC1960Filter;
                        if (i != -1) {
                            if (i2 != 0) {
                                String substring;
                                if (!stack.isEmpty()) {
                                    rFC1960Filter = (RFC1960Filter) stack.peek();
                                    substring = str.substring(i2 + PRESENT, i4);
                                    if (substring.equals(Constants.VERSION) && i3 == 0) {
                                        i3 = PRESENT;
                                        substring = null;
                                    }
                                    rFC1960Filter.operands.add(new RFC1960SimpleFilter(i3, substring, null));
                                    i3 = EQUALS;
                                    i7 = -1;
                                    int i8 = i4;
                                    str3 = null;
                                    i6 = -1;
                                    i5 = i8;
                                    break;
                                } else if (i4 == length - 1) {
                                    String substring2 = str.substring(i2 + PRESENT, length - 1);
                                    if (substring2.equals(Constants.VERSION) && i3 == 0) {
                                        i3 = PRESENT;
                                        substring = null;
                                    } else {
                                        substring = substring2;
                                    }
                                    return new RFC1960SimpleFilter(i3, substring, null);
                                } else {
                                    throw new InvalidSyntaxException("Unexpected literal: " + str.substring(i4), str);
                                }
                            }
                            throw new InvalidSyntaxException("Missing operator.", str);
                        }
                        rFC1960Filter = (RFC1960Filter) stack.pop();
                        if (stack.isEmpty()) {
                            return rFC1960Filter;
                        }
                        RFC1960Filter rFC1960Filter2 = (RFC1960Filter) stack.peek();
                        if (rFC1960Filter2.operator != NOT_OPERATOR || rFC1960Filter2.operands.isEmpty()) {
                            rFC1960Filter2.operands.add(rFC1960Filter);
                            if (i4 != length - 1) {
                                i5 = i4;
                                i6 = i3;
                                str3 = str2;
                                i3 = i2;
                                i7 = i;
                                break;
                            }
                            throw new InvalidSyntaxException("Missing right paranthesis at the end.", str);
                        }
                        throw new InvalidSyntaxException("Unexpected literal: " + str.substring(i4), str);
                        break;
                    case TBImageQuailtyStrategy.CDN_SIZE_60 /*60*/:
                        if (i2 == 0 && toCharArray[i4 + PRESENT] == '=') {
                            trim = str.substring(i + PRESENT, i4).trim();
                            i6 = LESS;
                            i5 = i4 + PRESENT;
                            str3 = trim;
                            i7 = i;
                            i3 = i5;
                            break;
                        }
                        throw new InvalidSyntaxException("Unexpected character " + toCharArray[i4 + PRESENT], str);
                        break;
                    case '=':
                        i3 = i4;
                        i7 = i;
                        i6 = EQUALS;
                        i5 = i4;
                        str3 = str.substring(i + PRESENT, i4).trim();
                        break;
                    case '>':
                        if (i2 == 0 && toCharArray[i4 + PRESENT] == '=') {
                            trim = str.substring(i + PRESENT, i4).trim();
                            i6 = NOT_OPERATOR;
                            i5 = i4 + PRESENT;
                            str3 = trim;
                            i7 = i;
                            i3 = i5;
                            break;
                        }
                        throw new InvalidSyntaxException("Unexpected character " + toCharArray[i4 + PRESENT], str);
                    case TransportMediator.KEYCODE_MEDIA_PLAY /*126*/:
                        if (i2 == 0 && toCharArray[i4 + PRESENT] == '=') {
                            trim = str.substring(i + PRESENT, i4).trim();
                            i6 = OR_OPERATOR;
                            i5 = i4 + PRESENT;
                            str3 = trim;
                            i7 = i;
                            i3 = i5;
                            break;
                        }
                        throw new InvalidSyntaxException("Unexpected character " + toCharArray[i4 + PRESENT], str);
                        break;
                    default:
                        i5 = i4;
                        i6 = i3;
                        str3 = str2;
                        i3 = i2;
                        i7 = i;
                        break;
                }
                i2 = i3;
                i = i7;
                str2 = str3;
                i3 = i6;
                i4 = i5 + PRESENT;
            }
            return (RFC1960Filter) stack.pop();
        } catch (EmptyStackException e) {
            throw new InvalidSyntaxException("Filter expression not well-formed.", str);
        }
    }

    public boolean match(ServiceReference serviceReference) {
        try {
            return match(((ServiceReferenceImpl) serviceReference).properties);
        } catch (Exception e) {
            Dictionary hashtable = new Hashtable();
            String[] propertyKeys = serviceReference.getPropertyKeys();
            for (int i = EQUALS; i < propertyKeys.length; i += PRESENT) {
                hashtable.put(propertyKeys[i], serviceReference.getProperty(propertyKeys[i]));
            }
            return match(hashtable);
        }
    }

    public boolean match(Dictionary dictionary) {
        Filter[] filterArr;
        int i;
        if (this.operator == PRESENT) {
            filterArr = (Filter[]) this.operands.toArray(new Filter[this.operands.size()]);
            for (i = EQUALS; i < filterArr.length; i += PRESENT) {
                if (!filterArr[i].match(dictionary)) {
                    return false;
                }
            }
            return true;
        } else if (this.operator == OR_OPERATOR) {
            filterArr = (Filter[]) this.operands.toArray(new Filter[this.operands.size()]);
            for (i = EQUALS; i < filterArr.length; i += PRESENT) {
                if (filterArr[i].match(dictionary)) {
                    return true;
                }
            }
            return false;
        } else if (this.operator != NOT_OPERATOR) {
            throw new IllegalStateException("PARSER ERROR");
        } else if (((Filter) this.operands.get(EQUALS)).match(dictionary)) {
            return false;
        } else {
            return true;
        }
    }

    public String toString() {
        int i = EQUALS;
        if (this.operator == NOT_OPERATOR) {
            return "(!" + this.operands.get(EQUALS) + ")";
        }
        StringBuffer stringBuffer = new StringBuffer(this.operator == PRESENT ? "(&" : "(|");
        Filter[] filterArr = (Filter[]) this.operands.toArray(new Filter[this.operands.size()]);
        while (i < filterArr.length) {
            stringBuffer.append(filterArr[i]);
            i += PRESENT;
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RFC1960Filter)) {
            return false;
        }
        RFC1960Filter rFC1960Filter = (RFC1960Filter) obj;
        if (this.operands.size() != rFC1960Filter.operands.size()) {
            return false;
        }
        Filter[] filterArr = (Filter[]) this.operands.toArray(new Filter[this.operands.size()]);
        Filter[] filterArr2 = (Filter[]) rFC1960Filter.operands.toArray(new Filter[this.operands.size()]);
        for (int i = EQUALS; i < filterArr.length; i += PRESENT) {
            if (!filterArr[i].equals(filterArr2[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return toString().hashCode();
    }
}
