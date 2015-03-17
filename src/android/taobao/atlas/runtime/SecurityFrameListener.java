package android.taobao.atlas.runtime;

import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.util.ApkUtils;
import android.taobao.atlas.util.StringUtils;
import android.util.Log;
import android.widget.Toast;

public class SecurityFrameListener implements FrameworkListener {
    static final String TAG = "SecurityFrameListener";
    ShutdownProcessHandler shutdownProcessHandler;

    private class SecurityTask extends AsyncTask<String, Void, Boolean> {
        final String PUBLIC_KEY;

        private SecurityTask() {
            this.PUBLIC_KEY = Framework.getProperty("android.taobao.atlas.publickey");
        }

        protected Boolean doInBackground(String... strArr) {
            if (this.PUBLIC_KEY == null || this.PUBLIC_KEY.isEmpty()) {
                return Boolean.valueOf(true);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            List<Bundle> bundles = Atlas.getInstance().getBundles();
            if (bundles != null) {
                for (Bundle bundle : bundles) {
                    if (StringUtils.contains(ApkUtils.getApkPublicKey(Atlas.getInstance().getBundleFile(bundle.getLocation()).getAbsolutePath()), this.PUBLIC_KEY)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                        }
                    } else {
                        Log.e(SecurityFrameListener.TAG, "Security check failed. " + bundle.getLocation());
                        return Boolean.valueOf(false);
                    }
                }
            }
            return Boolean.valueOf(true);
        }

        protected void onPostExecute(Boolean bool) {
            if (bool != null && !bool.booleanValue()) {
                Toast.makeText(RuntimeVariables.androidApplication, "\u68c0\u6d4b\u5230\u5b89\u88c5\u6587\u4ef6\u88ab\u635f\u574f\uff0c\u8bf7\u5378\u8f7d\u540e\u91cd\u65b0\u5b89\u88c5\uff01", 1).show();
                SecurityFrameListener.this.shutdownProcessHandler.sendEmptyMessageDelayed(0,5000);
            }
        }
    }

    public class ShutdownProcessHandler extends Handler {
        public void handleMessage(Message message) {
            Process.killProcess(Process.myPid());
        }
    }

    public SecurityFrameListener() {
        this.shutdownProcessHandler = new ShutdownProcessHandler();
    }

    public void frameworkEvent(FrameworkEvent frameworkEvent) {
        switch (frameworkEvent.getType()) {
            case 1 /*1*/:
                if (VERSION.SDK_INT >= 11) {
                    new SecurityTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[0]);
                } else {
                    new SecurityTask().execute(new String[0]);
                }
            default:
        }
    }
}
