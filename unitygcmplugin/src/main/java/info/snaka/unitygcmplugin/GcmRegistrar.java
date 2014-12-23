package info.snaka.unitygcmplugin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.unity3d.player.UnityPlayer;

import java.io.IOException;

/**
 * クライアントをGCMサービスに登録しレジストレーションIDを取得する
 * Created by snaka on 2014/12/21.
 */
public class GcmRegistrar {
    private static final String TAG = "GcmRegistrar";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // レジストレーションIDを保存するキー
    private static final String PROPERTY_REG_ID = "registration_id";

    // レジストレーションIDに関連づくアプリのバージョンを保存するキー
    private static final String PROPERTY_APP_VERSION = "appVersion";

    // Google Developer Consoleで登録した「プロジェクト番号」を格納しているAndroidManifest.xmlのmeta-dataの名前
    private static final String META_PROJECT_NUMBER = "apiProjectNumber";


    /**
     * Google Play Services が端末で有効かどうかをチェックして無効であればダイアログを表示
     * @param activity
     * @return
     */
    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * 前回取得済みの RegistrationID を取得する
     * @param ctx
     * @return
     */
    public static String getRegistrationId(Context ctx) {
        // SharedPreference から前回取得済みの Registration ID を取得
        final SharedPreferences prefs = getGCMPreferences(ctx);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // アプリのバージョンが前回から変わってたら Registration ID を取得し直す
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(ctx);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        Log.i(TAG, "Registration ID from SharedPrefs: " + registrationId);
        return registrationId;
    }


    /**
     * キャッシュ(SharedPrefs)に保存されたレジストレーションIDを削除する
     * @param ctx
     */
    public static void clearCache(Context ctx) {
        final SharedPreferences prefs = getGCMPreferences(ctx);
        prefs.edit()
                .remove(PROPERTY_REG_ID)
                .remove(PROPERTY_APP_VERSION)
                .commit();
    }

    /**
     * バックグラウンドのスレッドでレジストレーションIDを取得
     * @param ctx
     */
    public static void registerInBackground(final Context ctx) {
        final AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            /**
             * GCMサービスにクライアントを登録して Registration ID を取得
             * @param params
             * @return
             */
            @Override
            protected String doInBackground(Void... params) {
                String regid = "";
                try {
                    Log.i(TAG, "バックグラウンドで Registration ID の取得開始");

                    // Google Developer Console で登録したプロジェクトの「プロジェクト番号」を AndroidManifest から取得
                    String senderId = getProjectNumber(ctx);

                    // GCMサービスを使用してクライアントを登録
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
                    regid = gcm.register(senderId);
                }
                catch (IOException ex) {
                    Log.e(TAG, "Registration ID 取得失敗: " + ex.getMessage());
                }
                Log.i(TAG, "Regstration ID : " + regid);
                return regid;
            }

            /**
             * 取得した Registration ID を SharedPreferences に保存
             * @param regid
             */
            @Override
            protected void onPostExecute(String regid) {
                // SharedPreferences に Registration ID を保存しておく
                storeRegistrationId(ctx, regid);
                UnityPlayer.UnitySendMessage("_GcmEvents", "OnRegister", regid);
            }
        };
        task.execute(null, null, null);
    }

    /**
     * AndroidManifest.xmlからプロジェクト番号の値を取得
     * @param ctx
     * @return
     */
    private static String getProjectNumber(Context ctx) {
        ApplicationInfo appInfo;
        try {
            appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
        }
        catch (PackageManager.NameNotFoundException ex) {
            throw new RuntimeException("アプリケーション情報の取得に失敗", ex);
        }
        String number = appInfo.metaData.getString(GcmRegistrar.META_PROJECT_NUMBER, "");
        number = number.replace("!", "");
        Log.d(TAG, "***** Project Number : [" + number + "]");

        return number;
    }

    /**
     * Registration ID を SharedPreferences に保存する
     * @param ctx
     * @param regid
     */
    private static void storeRegistrationId(Context ctx, String regid) {
        final SharedPreferences prefs = getGCMPreferences(ctx);
        int appVersion = getAppVersion(ctx);
        Log.i(TAG, "Registration ID に関連づいたアプリのバージョン: " + appVersion);

        // SharedPreferences の編集
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * アプリのバージョンを取得
     * @param ctx
     * @return
     */
    private static int getAppVersion(Context ctx) {
        try {
            PackageInfo pkgInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return pkgInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("パッケージ名が取得できません: " + e);
        }
    }

    /**
     * GCM関係の設定を保存するSharePreferences
     * @param ctx
     * @return
     */
    private static SharedPreferences getGCMPreferences(Context ctx) {
        return ctx.getSharedPreferences(GcmRegistrar.class.getSimpleName(), ctx.MODE_PRIVATE);
    }


}
