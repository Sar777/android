package instinctools.android;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import instinctools.android.constans.Constants;
import instinctools.android.database.DBConstants;
import instinctools.android.services.github.GithubServices;
import instinctools.android.storages.ApplicationPersistantStorage;
import instinctools.android.storages.SettingsStorage;

public class App extends Application {
    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        GithubServices.init(mContext, Constants.CLIENT_ID, Constants.CLIENT_SECRET, Constants.SCOPES, Constants.CALLBACK_URL);
        ApplicationPersistantStorage.init(mContext);
        SettingsStorage.init(mContext);

        deleteDatabase(DBConstants.DB_NAME);
    }

    public static void launchUrl(final Context context, final String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    public static Context getAppContext() {
        return mContext;
    }
}