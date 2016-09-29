package xyz.truenight.latte;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Throwable origin = new Throwable("Ex");
        Throwable th = Latte.clone(origin); // handle recursive reference

        Log.d(TAG, "is equal: " + Latte.equal(origin, th));
        Log.d(TAG, th.toString());
    }
}
