package info.snaka.unitygcmplugin;

import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;


/**
 *  Notification 経由でアプリを起動できるように Unity 標準の UnityPlayerActivity を拡張する
 */
public class CustomUnityPlayerActivity extends UnityPlayerActivity {
    private final String TAG = "CustomUnityPlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "***** CustomUnityPlayerActivity onCreate");
    }
}
