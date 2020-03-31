package sep_group_7.SurreyMeets;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class SurreyMeets extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
