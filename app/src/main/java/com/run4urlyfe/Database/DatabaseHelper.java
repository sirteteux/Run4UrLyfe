package com.run4urlyfe.Database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import com.run4urlyfe.Database.bodymeasures.BodyMeasure;
import com.run4urlyfe.Database.bodymeasures.BodyPartExtensions;
import com.run4urlyfe.Database.bodymeasures.DBBodyMeasure;
import com.run4urlyfe.Database.bodymeasures.DBBodyPart;
import com.run4urlyfe.Database.program.DBProgram;
import com.run4urlyfe.Database.program.DBProgramHistory;
import com.run4urlyfe.Database.record.DBSource;
import com.run4urlyfe.Database.record.DBRecord;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.enums.Unit;
import com.run4urlyfe.utils.DateConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 23;
    public static final String OLD09_DATABASE_NAME = "run4urlyfe";
    public static final String DATABASE_NAME = "run4urlyfe.db";
    private static DatabaseHelper sInstance;
    private Context mContext = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public static void renameOldDatabase(Activity activity) {
        File oldDatabaseFile = activity.getDatabasePath(OLD09_DATABASE_NAME);
        if (oldDatabaseFile.exists()) {
            File newDatabaseFile = new File(oldDatabaseFile.getParentFile(), DATABASE_NAME);
            oldDatabaseFile.renameTo(newDatabaseFile);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(com.run4urlyfe.Database.record.DBRecord.TABLE_CREATE);
        db.execSQL(com.run4urlyfe.Database.DBProfile.TABLE_CREATE);
        db.execSQL(com.run4urlyfe.Database.DBProfileWeight.TABLE_CREATE);
        db.execSQL(com.run4urlyfe.Database.DBMachine.TABLE_CREATE);
        db.execSQL(DBBodyMeasure.TABLE_CREATE);
        db.execSQL(DBBodyPart.TABLE_CREATE);
        db.execSQL(DBProgram.TABLE_CREATE);
        db.execSQL(DBProgramHistory.TABLE_CREATE);
        initBodyPartTable(db);
    }

    @Override
    public void onUpgrade(
            final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 1:
                    //NOT SUPPORTED ANYMORE
                    //db.execSQL(DBCardio.TABLE_CREATE);
                    break;
                case 2:
                    //NOT SUPPORTED ANYMOREdb.execSQL(DBCardio.TABLE_CREATE);
                    break;
                case 3:
                    //NOT SUPPORTED ANYMOREdb.execSQL(DBCardio.TABLE_DROP);
                    //NOT SUPPORTED ANYMOREdb.execSQL(DBCardio.TABLE_CREATE);
                    break;
                case 4:
                    db.execSQL("ALTER TABLE " + DBSource.TABLE_NAME + " ADD COLUMN " + DBSource.NOTES + " TEXT");
                    db.execSQL("ALTER TABLE " + DBSource.TABLE_NAME + " ADD COLUMN " + DBSource.WEIGHT_UNIT + " INTEGER DEFAULT 0");
                    break;
                case 5:
                    db.execSQL(com.run4urlyfe.Database.DBMachine.TABLE_CREATE_5);
                    db.execSQL("ALTER TABLE " + DBSource.TABLE_NAME + " ADD COLUMN " + DBSource.EXERCISE_KEY + " INTEGER");
                    break;
                case 6:
                    if (!isFieldExist(db, com.run4urlyfe.Database.DBMachine.TABLE_NAME, com.run4urlyfe.Database.DBMachine.BODYPARTS))
                        db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBMachine.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBMachine.BODYPARTS + " TEXT");
                    break;
                case 7:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBMachine.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBMachine.PICTURE + " TEXT");
                    break;
                case 8:
                    db.execSQL("ALTER TABLE " + DBSource.TABLE_NAME + " ADD COLUMN " + DBSource.TIME + " TEXT");
                    break;
                case 9:
                    db.execSQL(DBBodyMeasure.TABLE_CREATE);
                    break;
                case 10:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBMachine.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBMachine.FAVORITES + " INTEGER");
                    break;
                case 11:
                    db.execSQL("ALTER TABLE " + DBSource.TABLE_NAME + " RENAME TO tmp_table_name");
                    db.execSQL(DBSource.TABLE_CREATE);
                    db.execSQL("INSERT INTO " + DBSource.TABLE_NAME + " SELECT * FROM tmp_table_name");
                    // do not delete old table here in case of issue
                    break;
                case 12:
                    // Delete old table table
                    db.execSQL("DROP TABLE IF EXISTS tmp_table_name");
                    break;
                case 13:
                    // Update profile database
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBProfile.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBProfile.SIZE + " INTEGER");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBProfile.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBProfile.BIRTHDAY + " DATE");
                    break;
                case 14:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBProfile.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBProfile.PHOTO + " TEXT");
                    break;
                case 15:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.DISTANCE + " REAL");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.DURATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.EXERCISE_TYPE + " INTEGER DEFAULT " + ExerciseType.STRENGTH.ordinal());
                    break;
                case 16:
                    db.execSQL("ALTER TABLE " + DBBodyMeasure.TABLE_NAME + " ADD COLUMN " + DBBodyMeasure.UNIT + " INTEGER");
                    migrateWeightTable(db);
                    break;
                case 17:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.DBProfile.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.DBProfile.GENDER + " INTEGER");
                    break;
                case 18:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.SECONDS + " INTEGER DEFAULT 0");
                    break;
                case 19:
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + DBRecord.DISTANCE_UNIT + " INTEGER DEFAULT 0");
                    break;
                case 20:
                    db.execSQL(DBBodyPart.TABLE_CREATE);
                    initBodyPartTable(db);
                    break;
                case 21:
                    db.execSQL(DBProgram.TABLE_CREATE);
                    db.execSQL(DBProgramHistory.TABLE_CREATE);

                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.RECORD_TYPE + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.TEMPLATE_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.TEMPLATE_RECORD_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.TEMPLATE_SESSION_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.TEMPLATE_REST_TIME + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.TEMPLATE_ORDER + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + com.run4urlyfe.Database.record.DBRecord.TABLE_NAME + " ADD COLUMN " + com.run4urlyfe.Database.record.DBRecord.TEMPLATE_RECORD_STATUS + " INTEGER DEFAULT 3");
                    break;
                case 22:
                    // update all unitless bodymeasures based on BODYPART_ID
                    upgradeBodyMeasureUnits(db);
                    break;
                case 23:
                    long sizeBodyPartId = addInitialBodyPart(db, BodyPartExtensions.SIZE, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
                    DBProfile daoProfile = new DBProfile(mContext);
                    DBBodyMeasure DBBodyMeasure = new DBBodyMeasure(mContext);

                    // Get Size unit preference
                    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mContext);
                    String defaultSizeUnitString = SP.getString("defaultSizeUnit", String.valueOf(Unit.CM.ordinal()));
                    int defaultSizeUnitInteger;
                    try {
                        defaultSizeUnitInteger = Integer.parseInt(defaultSizeUnitString);
                    } catch (NumberFormatException e) {
                        defaultSizeUnitInteger = Unit.CM.ordinal();
                    }
                    Unit defaultSizeUnit = Unit.fromInteger(defaultSizeUnitInteger);

                    List<Profile> profileList = daoProfile.getAllProfiles(db);
                    for (Profile profile:profileList) {
                        DBBodyMeasure.addBodyMeasure(db, DateConverter.getNewDate(), sizeBodyPartId, profile.getSize(), profile.getId(), defaultSizeUnit);
                    }
                    break;
            }
            upgradeTo++;
        }
    }

    @Override
    public void onDowngrade(
            final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        int upgradeTo = oldVersion - 1;
        while (upgradeTo >= newVersion) {
            switch (upgradeTo) {
                case 2:
                    break;
                case 3:
                    //db.execSQL("ALTER TABLE "+ DBSource.TABLE_NAME + " DROP COLUMN " + DBSource.NOTES);
                    //db.execSQL("ALTER TABLE "+ DBSource.TABLE_NAME + " DROP COLUMN " + DBSource.UNIT);
                    break;
                case 4:
                    //db.execSQL(DBMachine.TABLE_DROP);
                    //db.execSQL("ALTER TABLE "+ DBSource.TABLE_NAME + " DROP COLUMN " + DBSource.MACHINE_KEY );
                    break;
                case 5:
                    //db.execSQL("ALTER TABLE "+ DBMachine.TABLE_NAME + " DROP COLUMN " + DBMachine.BODYPARTS );
                    break;
                case 20:
                    // Delete WORKOUT TABLE
                    db.delete(DBProgram.TABLE_NAME, null, null);
                    break;
            }
            upgradeTo--;
        }
    }

    // This method will return if your table exist a field or not
    public boolean isFieldExist(SQLiteDatabase db, String tableName, String fieldName) {
        boolean isExist = true;
        Cursor res;

        try {
            res = db.rawQuery("SELECT " + fieldName + " FROM " + tableName, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }

        return isExist;
    }

    public boolean tableExists(SQLiteDatabase db, String tableName) {
        boolean isExist = true;
        Cursor res;

        try {
            res = db.rawQuery("SELECT * FROM " + tableName, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }
        return isExist;
    }

    private void migrateWeightTable(SQLiteDatabase db) {
        List<ProfileWeight> valueList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + com.run4urlyfe.Database.DBProfileWeight.TABLE_NAME;
        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                ContentValues value = new ContentValues();

                value.put(DBBodyMeasure.DATE, mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBProfileWeight.DATE)));
                value.put(DBBodyMeasure.BODYPART_ID, BodyPartExtensions.WEIGHT);
                value.put(DBBodyMeasure.MEASURE, mCursor.getFloat(mCursor.getColumnIndex(com.run4urlyfe.Database.DBProfileWeight.weights)));
                value.put(DBBodyMeasure.PROFIL_KEY, mCursor.getLong(mCursor.getColumnIndex(com.run4urlyfe.Database.DBProfileWeight.PROFIL_KEY)));

                db.insert(DBBodyMeasure.TABLE_NAME, null, value);
            } while (mCursor.moveToNext());
            mCursor.close();
            //db.close(); // Closing database connection
        }
    }

    private void upgradeBodyMeasureUnits(SQLiteDatabase db) {
        DBBodyMeasure DBBodyMeasure = new DBBodyMeasure(mContext);
        String selectQuery = "SELECT * FROM " + DBBodyMeasure.TABLE_NAME + " ORDER BY date(" + DBBodyMeasure.DATE + ") DESC";
        List<BodyMeasure> valueList = DBBodyMeasure.getMeasuresList(db, selectQuery);

        for (BodyMeasure bodyMeasure : valueList) {
            switch (bodyMeasure.getBodyPartID()) {
                case BodyPartExtensions.LEFTBICEPS:
                case BodyPartExtensions.RIGHTBICEPS:
                case BodyPartExtensions.CHEST:
                case BodyPartExtensions.WAIST:
                case BodyPartExtensions.BEHIND:
                case BodyPartExtensions.LEFTTHIGH:
                case BodyPartExtensions.RIGHTTHIGH:
                case BodyPartExtensions.LEFTCALVES:
                case BodyPartExtensions.RIGHTCALVES:
                    bodyMeasure.setUnit(Unit.CM);
                    break;
                case BodyPartExtensions.WEIGHT:
                    bodyMeasure.setUnit(Unit.KG);
                    break;
                case BodyPartExtensions.MUSCLES:
                case BodyPartExtensions.WATER:
                case BodyPartExtensions.FAT:
                    bodyMeasure.setUnit(Unit.PERCENTAGE);
                    break;
            }
            DBBodyMeasure.updateMeasure(db, bodyMeasure);
        }
    }

    public void initBodyPartTable(SQLiteDatabase db) {
        int display_order = 0;

        addInitialBodyPart(db, BodyPartExtensions.LEFTBICEPS, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTBICEPS, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.CHEST, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.WAIST, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.BEHIND, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.LEFTTHIGH, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTTHIGH, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.LEFTCALVES, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTCALVES, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.WEIGHT, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.MUSCLES, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.WATER, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.FAT, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.SIZE, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
    }

    public long addInitialBodyPart(SQLiteDatabase db, long pKey, String pCustomName, String pCustomPicture, int pDisplay, int pType) {
        ContentValues value = new ContentValues();

        value.put(DBBodyPart.KEY, pKey);
        value.put(DBBodyPart.BODYPART_RESID, pKey);
        value.put(DBBodyPart.CUSTOM_NAME, pCustomName);
        value.put(DBBodyPart.CUSTOM_PICTURE, pCustomPicture);
        value.put(DBBodyPart.DISPLAY_ORDER, pDisplay);
        value.put(DBBodyPart.TYPE, pType);

        return db.insert(DBBodyPart.TABLE_NAME, null, value);
    }
}
