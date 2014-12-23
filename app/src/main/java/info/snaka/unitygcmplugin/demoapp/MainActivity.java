package info.snaka.unitygcmplugin.demoapp;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import info.snaka.unitygcmplugin.GcmRegistrar;


public class MainActivity extends ActionBarActivity {

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();

        // 前回取得したレジストレーションIDを削除（デバッグのため毎回キャッシュを削除してるが通常はキャッシュされたIDを使用して問題無いはず）
        GcmRegistrar.clearCache(ctx);

        // キャッシュからレジストレーションIDを取得
        String regId = GcmRegistrar.getRegistrationId(ctx);

        // レジストレーションIDが取得できなかった場合はバックグラウンドスレッドで再取得する
        if (regId.isEmpty()) {
            GcmRegistrar.registerInBackground(ctx);
        }
    }

}
