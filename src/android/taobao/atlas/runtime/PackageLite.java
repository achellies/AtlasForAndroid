package android.taobao.atlas.runtime;

import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.util.StringUtils;
import android.util.AttributeSet;
import com.taobao.android.dexposed.ClassUtils;
import com.tencent.mm.sdk.plugin.C0272d.C0271c;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.android.agoo.p121b.MtopResponse;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PackageLite {
    private static final String XMLDISABLECOMPONENT_SSO_ALIPAY_AUTHENTICATION_SERVICE = "com.taobao.android.sso.internal.AlipayAuthenticationService";
    private static final String XMLDISABLECOMPONENT_SSO_AUTHENTICATION_SERVICE = "com.taobao.android.sso.internal.AuthenticationService";
    static final Logger log;
    public String applicationClassName;
    public int applicationDescription;
    public int applicationIcon;
    public int applicationLabel;
    public final Set<String> components;
    public final Set<String> disableComponents;
    public Bundle metaData;
    public String packageName;
    public int versionCode;
    public String versionName;

    static {
        log = LoggerFactory.getInstance("PackageInfo");
    }

    PackageLite() {
        this.components = new HashSet();
        this.disableComponents = new HashSet();
    }

    public static PackageLite parse(File file) {
        Throwable e;
        XmlResourceParser xmlResourceParser = null;
        XmlResourceParser openXmlResourceParser;
        try {
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            int intValue = ((Integer) AtlasHacks.AssetManager_addAssetPath.invoke(assetManager, file.getAbsolutePath())).intValue();
            if (intValue != 0) {
                openXmlResourceParser = assetManager.openXmlResourceParser(intValue, "AndroidManifest.xml");
            } else {
                openXmlResourceParser = assetManager.openXmlResourceParser(intValue, "AndroidManifest.xml");
            }
            if (openXmlResourceParser != null) {
                try {
                    PackageLite parse = parse(openXmlResourceParser);
                    if (parse == null) {
                        parse = new PackageLite();
                    }
                    if (openXmlResourceParser == null) {
                        return parse;
                    }
                    openXmlResourceParser.close();
                    return parse;
                } catch (Exception e2) {
                    e = e2;
                    try {
                        log.error("Exception while parse AndroidManifest.xml >>>", e);
                        if (openXmlResourceParser != null) {
                            openXmlResourceParser.close();
                        }
                        return null;
                    } catch (Throwable th) {
                        e = th;
                        xmlResourceParser = openXmlResourceParser;
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        throw e;
                    }
                }
            }
            if (openXmlResourceParser != null) {
                openXmlResourceParser.close();
            }
            return null;
        } catch (Exception e3) {
            e = e3;
            openXmlResourceParser = null;
            log.error("Exception while parse AndroidManifest.xml >>>", e);
            if (openXmlResourceParser != null) {
                openXmlResourceParser.close();
            }
            return null;
        } catch (Throwable th2) {
            e = th2;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            throw e;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static android.taobao.atlas.runtime.PackageLite parse(android.content.res.XmlResourceParser r10) throws java.lang.Exception {
        /*
        r9 = 3;
        r8 = 1;
        r7 = 2;
        r1 = 0;
        r3 = 0;
        r4 = new android.taobao.atlas.runtime.PackageLite;
        r4.<init>();
    L_0x000a:
        r0 = r10.next();
        if (r0 == r7) goto L_0x0012;
    L_0x0010:
        if (r0 != r8) goto L_0x000a;
    L_0x0012:
        if (r0 == r7) goto L_0x001e;
    L_0x0014:
        r0 = log;
        r1 = "No start tag found";
        r0.error(r1);
        r0 = r3;
    L_0x001d:
        return r0;
    L_0x001e:
        r0 = r10.getName();
        r2 = "manifest";
        r0 = r0.equals(r2);
        if (r0 != 0) goto L_0x0035;
    L_0x002b:
        r0 = log;
        r1 = "No <manifest> tag";
        r0.error(r1);
        r0 = r3;
        goto L_0x001d;
    L_0x0035:
        r0 = "package";
        r0 = r10.getAttributeValue(r3, r0);
        r4.packageName = r0;
        r0 = r4.packageName;
        if (r0 == 0) goto L_0x004a;
    L_0x0042:
        r0 = r4.packageName;
        r0 = r0.length();
        if (r0 != 0) goto L_0x0054;
    L_0x004a:
        r0 = log;
        r1 = "<manifest> does not specify package";
        r0.error(r1);
        r0 = r3;
        goto L_0x001d;
    L_0x0054:
        r0 = r1;
        r2 = r1;
    L_0x0056:
        r5 = r10.getAttributeCount();
        if (r0 >= r5) goto L_0x0073;
    L_0x005c:
        r5 = r10.getAttributeName(r0);
        r6 = "versionCode";
        r6 = r5.equals(r6);
        if (r6 == 0) goto L_0x00a2;
    L_0x0069:
        r5 = r10.getAttributeIntValue(r0, r1);
        r4.versionCode = r5;
        r2 = r2 + 1;
    L_0x0071:
        if (r2 < r7) goto L_0x00b4;
    L_0x0073:
        r0 = r10.getDepth();
        r0 = r0 + 1;
    L_0x0079:
        r1 = r10.next();
        if (r1 == r8) goto L_0x00be;
    L_0x007f:
        if (r1 != r9) goto L_0x0087;
    L_0x0081:
        r2 = r10.getDepth();
        if (r2 < r0) goto L_0x00be;
    L_0x0087:
        if (r1 == r9) goto L_0x0079;
    L_0x0089:
        r2 = 4;
        if (r1 == r2) goto L_0x0079;
    L_0x008c:
        r1 = r10.getName();
        r2 = "application";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x00ba;
    L_0x0099:
        r0 = parseApplication(r4, r10, r10);
        if (r0 != 0) goto L_0x00b7;
    L_0x009f:
        r0 = r3;
        goto L_0x001d;
    L_0x00a2:
        r6 = "versionName";
        r5 = r5.equals(r6);
        if (r5 == 0) goto L_0x0071;
    L_0x00ab:
        r5 = r10.getAttributeValue(r0);
        r4.versionName = r5;
        r2 = r2 + 1;
        goto L_0x0071;
    L_0x00b4:
        r0 = r0 + 1;
        goto L_0x0056;
    L_0x00b7:
        r0 = r4;
        goto L_0x001d;
    L_0x00ba:
        skipCurrentTag(r10);
        goto L_0x0079;
    L_0x00be:
        r0 = r4;
        goto L_0x001d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.runtime.PackageLite.parse(android.content.res.XmlResourceParser):android.taobao.atlas.runtime.PackageLite");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean parseApplication(android.taobao.atlas.runtime.PackageLite r7, org.xmlpull.v1.XmlPullParser r8, android.util.AttributeSet r9) throws java.lang.Exception {
        /*
        r6 = 3;
        r5 = 1;
        r1 = 0;
        r2 = r7.packageName;
        r0 = r1;
    L_0x0006:
        r3 = r9.getAttributeCount();
        if (r0 >= r3) goto L_0x0056;
    L_0x000c:
        r3 = r9.getAttributeName(r0);
        r4 = "name";
        r4 = r3.equals(r4);
        if (r4 == 0) goto L_0x0026;
    L_0x0019:
        r3 = r9.getAttributeValue(r0);
        r3 = buildClassName(r2, r3);
        r7.applicationClassName = r3;
    L_0x0023:
        r0 = r0 + 1;
        goto L_0x0006;
    L_0x0026:
        r4 = "icon";
        r4 = r3.equals(r4);
        if (r4 == 0) goto L_0x0036;
    L_0x002f:
        r3 = r9.getAttributeResourceValue(r0, r1);
        r7.applicationIcon = r3;
        goto L_0x0023;
    L_0x0036:
        r4 = "label";
        r4 = r3.equals(r4);
        if (r4 == 0) goto L_0x0046;
    L_0x003f:
        r3 = r9.getAttributeResourceValue(r0, r1);
        r7.applicationLabel = r3;
        goto L_0x0023;
    L_0x0046:
        r4 = "description";
        r3 = r3.equals(r4);
        if (r3 == 0) goto L_0x0023;
    L_0x004f:
        r3 = r9.getAttributeResourceValue(r0, r1);
        r7.applicationDescription = r3;
        goto L_0x0023;
    L_0x0056:
        r0 = r8.getDepth();
    L_0x005a:
        r2 = r8.next();
        if (r2 == r5) goto L_0x00bb;
    L_0x0060:
        if (r2 != r6) goto L_0x0068;
    L_0x0062:
        r3 = r8.getDepth();
        if (r3 <= r0) goto L_0x00bb;
    L_0x0068:
        if (r2 == r6) goto L_0x005a;
    L_0x006a:
        r3 = 4;
        if (r2 == r3) goto L_0x005a;
    L_0x006d:
        r2 = r8.getName();
        r3 = "meta-data";
        r3 = r2.equals(r3);
        if (r3 == 0) goto L_0x0083;
    L_0x007a:
        r2 = r7.metaData;
        r2 = parseMetaData(r8, r9, r2);
        r7.metaData = r2;
        goto L_0x005a;
    L_0x0083:
        r3 = "activity";
        r3 = r2.equals(r3);
        if (r3 == 0) goto L_0x0090;
    L_0x008c:
        parseComponentData(r7, r8, r9, r1);
        goto L_0x005a;
    L_0x0090:
        r3 = "receiver";
        r3 = r2.equals(r3);
        if (r3 == 0) goto L_0x009d;
    L_0x0099:
        parseComponentData(r7, r8, r9, r5);
        goto L_0x005a;
    L_0x009d:
        r3 = "service";
        r3 = r2.equals(r3);
        if (r3 == 0) goto L_0x00aa;
    L_0x00a6:
        parseComponentData(r7, r8, r9, r5);
        goto L_0x005a;
    L_0x00aa:
        r3 = "provider";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x00b7;
    L_0x00b3:
        parseComponentData(r7, r8, r9, r1);
        goto L_0x005a;
    L_0x00b7:
        skipCurrentTag(r8);
        goto L_0x005a;
    L_0x00bb:
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.taobao.atlas.runtime.PackageLite.parseApplication(android.taobao.atlas.runtime.PackageLite, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet):boolean");
    }

    private static Bundle parseMetaData(XmlPullParser xmlPullParser, AttributeSet attributeSet, Bundle bundle) throws XmlPullParserException, IOException {
        int i = 0;
        if (bundle == null) {
            bundle = new Bundle();
        }
        String str = null;
        String str2 = null;
        int i2 = 0;
        while (i < attributeSet.getAttributeCount()) {
            String attributeName = attributeSet.getAttributeName(i);
            if (attributeName.equals(MtopResponse.KEY_NAME)) {
                str2 = attributeSet.getAttributeValue(i);
                i2++;
            } else if (attributeName.equals(C0271c.VALUE)) {
                str = attributeSet.getAttributeValue(i);
                i2++;
            }
            if (i2 >= 2) {
                break;
            }
            i++;
        }
        if (!(str2 == null || str == null)) {
            bundle.putString(str2, str);
        }
        return bundle;
    }

    private static String buildClassName(String str, CharSequence charSequence) {
        if (charSequence == null || charSequence.length() <= 0) {
            log.error("Empty class name in package " + str);
            return null;
        }
        String obj = charSequence.toString();
        char charAt = obj.charAt(0);
        if (charAt == ClassUtils.PACKAGE_SEPARATOR_CHAR) {
            return (str + obj).intern();
        }
        if (obj.indexOf(46) < 0) {
            StringBuilder stringBuilder = new StringBuilder(str);
            stringBuilder.append(ClassUtils.PACKAGE_SEPARATOR_CHAR);
            stringBuilder.append(obj);
            return stringBuilder.toString().intern();
        } else if (charAt >= 'a' && charAt <= 'z') {
            return obj.intern();
        } else {
            log.error("Bad class name " + obj + " in package " + str);
            return null;
        }
    }

    private static void skipCurrentTag(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int depth = xmlPullParser.getDepth();
        while (true) {
            int next = xmlPullParser.next();
            if (next == 1) {
                return;
            }
            if (next == 3 && xmlPullParser.getDepth() <= depth) {
                return;
            }
        }
    }

    private static void parseComponentData(PackageLite packageLite, XmlPullParser xmlPullParser, AttributeSet attributeSet, boolean z) throws XmlPullParserException {
        int i = 0;
        String str = packageLite.packageName;
        int i2 = 0;
        while (i < attributeSet.getAttributeCount()) {
            if (attributeSet.getAttributeName(i).equals(MtopResponse.KEY_NAME)) {
                String attributeValue = attributeSet.getAttributeValue(i);
                if (attributeValue.startsWith(".")) {
                    attributeValue = str.concat(str);
                }
                packageLite.components.add(attributeValue);
                if (z && !(StringUtils.equals(attributeValue, XMLDISABLECOMPONENT_SSO_ALIPAY_AUTHENTICATION_SERVICE) && StringUtils.equals(attributeValue, XMLDISABLECOMPONENT_SSO_AUTHENTICATION_SERVICE))) {
                    packageLite.disableComponents.add(attributeValue);
                }
                i2++;
            }
            if (i2 < attributeSet.getAttributeCount()) {
                i++;
            } else {
                return;
            }
        }
    }
}
