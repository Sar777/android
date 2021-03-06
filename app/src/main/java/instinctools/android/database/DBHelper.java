package instinctools.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConstants.TABLE_REPOSITORIES_CREATE);
        db.execSQL(DBConstants.TABLE_NOTIFICATION_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}