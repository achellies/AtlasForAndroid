package android.taobao.atlas.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.taobao.atlas.hack.AssertionArrayException;
import android.taobao.atlas.hack.AtlasHacks;
import android.taobao.atlas.log.Logger;
import android.taobao.atlas.log.LoggerFactory;
import android.taobao.atlas.runtime.PackageLite;

public class ApkUtils {
	static final int SYSTEM_ROOT_STATE_DISABLE = 0;
	static final int SYSTEM_ROOT_STATE_ENABLE = 1;
	static final int SYSTEM_ROOT_STATE_UNKNOW = -1;
	static final Logger log;
	private static int systemRootState;

	static {
		log = LoggerFactory.getInstance("ApkUtils");
		systemRootState = SYSTEM_ROOT_STATE_UNKNOW;
	}

	private static boolean assertAtlasHacks() {
		try {
			return AtlasHacks.defineAndVerify();
		} catch (AssertionArrayException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static final PackageInfo parsePackageInfo(Context context, String str) {
		if (!assertAtlasHacks()) {
			return null;
		}
		try {
			PackageLite parse = PackageLite.parse(new File(str));
			PackageInfo packageInfo = new PackageInfo();
			packageInfo.packageName = parse.packageName;
			packageInfo.versionCode = parse.versionCode;
			packageInfo.versionName = parse.versionName;
			packageInfo.applicationInfo = new ApplicationInfo();
			packageInfo.applicationInfo.metaData = parse.metaData;
			return packageInfo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final String[] getApkPublicKey(String str) {
		//TODO  impl
		return null;
	}

	private static Certificate[] loadCertificates(JarFile jarFile, JarEntry jarEntry, byte[] bArr) {

		//TODO impl
		return null;}

	private static final String bytesToHexString(byte[] bArr) {
		StringBuilder stringBuilder = new StringBuilder();
		if (bArr == null || bArr.length <= 0) {
			return null;
		}
		for (int i = SYSTEM_ROOT_STATE_DISABLE; i < bArr.length; i += SYSTEM_ROOT_STATE_ENABLE) {
			String toHexString = Integer.toHexString(bArr[i] & 255);//TODO 255
			if (toHexString.length() < 2) {
				stringBuilder.append(SYSTEM_ROOT_STATE_DISABLE);
			}
			stringBuilder.append(toHexString);
		}
		return stringBuilder.toString();
	}

	public static final void chmod(File file) {
		if (file != null && !file.exists()) {
			file.mkdirs();
			try {
				Runtime.getRuntime().exec("chmod 555 " + file.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isRootSystem() {
		if (systemRootState == SYSTEM_ROOT_STATE_UNKNOW) {
			String[] strArr = new String[]{"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
			int length = strArr.length;
			for (int i = SYSTEM_ROOT_STATE_DISABLE; i < length; i += SYSTEM_ROOT_STATE_ENABLE) {
				if (new File(strArr[i], "su").exists()) {
					systemRootState = SYSTEM_ROOT_STATE_ENABLE;
					return true;
				}
			}
			return false;
		} else if (systemRootState == SYSTEM_ROOT_STATE_ENABLE) {
			return true;
		} else {
			return false;
		}
	}

	public static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
		FileOutputStream fileOutputStream = null;
		Throwable th;
		FileChannel fileChannel = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			try {
				fileChannel = fileOutputStream.getChannel();
				byte[] bArr = new byte[4096];
				while (true) {
					int read = inputStream.read(bArr);
					if (read <= 0) {
						break;
					}
					fileChannel.write(ByteBuffer.wrap(bArr, SYSTEM_ROOT_STATE_DISABLE, read));
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (fileChannel != null) {
					try {
						fileChannel.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (Exception e22) {
						e22.printStackTrace();
					}
				}
			} catch (Throwable th2) {
				th = th2;
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}
				if (fileChannel != null) {
					try {
						fileChannel.close();
					} catch (Exception e4) {
						e4.printStackTrace();
					}
				}
				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (Exception e42) {
						e42.printStackTrace();
					}
				}
				throw th;
			}
		} catch (Throwable th3) {
			th = th3;
			Object obj = fileChannel;
			if (inputStream != null) {
				inputStream.close();
			}
			if (fileChannel != null) {
				fileChannel.close();
			}
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			
		}
	}
}
