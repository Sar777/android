package instinctools.android.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import instinctools.android.R;
import instinctools.android.account.GitHubAccount;
import instinctools.android.account.OnTokenAcquired;
import instinctools.android.constans.Constants;
import instinctools.android.storages.ApplicationPersistantStorage;

public class SplashActivity extends AppCompatActivity {
    public static final int PERMISSION_GET_CONTACTS = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_GET_CONTACTS && grantResults.length == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Snackbar.make(findViewById(R.id.activity_splash), R.string.msg_fail_grant_account_permissions, Snackbar.LENGTH_SHORT).show();
            else
                authOrGetAuthToken();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!ApplicationPersistantStorage.getBoolProperty(Constants.PROPERTY_FIRST_RUN)) {
            Intent intent = new Intent(this, IntroductionsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        authOrGetAuthToken();
    }

    private void authOrGetAuthToken() {
        final AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.GET_ACCOUNTS }, PERMISSION_GET_CONTACTS);
            return;
        }

        if (accountManager.getAccountsByType(GitHubAccount.TYPE).length == 0) {
            Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Account account = accountManager.getAccountsByType(GitHubAccount.TYPE)[0];
        accountManager.getAuthToken(account, GitHubAccount.TYPE, Bundle.EMPTY, this, new OnTokenAcquired(this), null);
    }
}
