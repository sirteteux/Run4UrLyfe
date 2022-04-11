package com.run4urlyfe.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.run4urlyfe.enums.ExerciseType;

import java.util.ArrayList;
import java.util.List;

public class DBMachine extends DBBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFmachines";

    public static final String KEY = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String TYPE = "type";
    public static final String PICTURE = "picture";
    public static final String BODYPARTS = "bodyparts";
    public static final String FAVORITES = "favorites"; // DEPRECATED - Specific DataBase created for this.

    public static final String TABLE_CREATE_5 = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
            + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " INTEGER);";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
            + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " INTEGER, " + BODYPARTS + " TEXT, " + PICTURE + " TEXT, " + FAVORITES + " INTEGER);"; //", " + PICTURE_RES + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME + ";";

    private final Profile mProfil = null;
    private Cursor mCursor = null;

    public DBMachine(Context context) {
        super(context);
    }


    /**
     * @param pName
     * @param pDescription
     * @param pType
     */
    public long addMachine(String pName, String pDescription, ExerciseType pType, String pPicture, boolean pFav, String pBodyParts) {

        ContentValues value = new ContentValues();

        value.put(com.run4urlyfe.Database.DBMachine.NAME, pName);
        value.put(com.run4urlyfe.Database.DBMachine.DESCRIPTION, pDescription);
        value.put(com.run4urlyfe.Database.DBMachine.TYPE, pType.ordinal());
        value.put(com.run4urlyfe.Database.DBMachine.PICTURE, pPicture);
        value.put(com.run4urlyfe.Database.DBMachine.FAVORITES, pFav);
        value.put(com.run4urlyfe.Database.DBMachine.BODYPARTS, pBodyParts);

        SQLiteDatabase db = this.getWritableDatabase();
        long new_id = db.insert(com.run4urlyfe.Database.DBMachine.TABLE_NAME, null, value);
        close();

        return new_id;
    }

    // Getting single value
    public Machine getMachine(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{KEY, NAME, DESCRIPTION, TYPE, BODYPARTS, PICTURE, FAVORITES}, KEY + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;

        Machine value = new Machine(mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.NAME)),
                mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.DESCRIPTION)),
                ExerciseType.fromInteger(mCursor.getInt(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.TYPE))),
                mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.BODYPARTS)),
                mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.PICTURE)),
                mCursor.getInt(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.FAVORITES)) == 1);

        value.setId(mCursor.getLong(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.KEY)));
        // return value
        mCursor.close();
        close();
        return value;
    }

    // Getting single value
    public Machine getMachine(String pName) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{KEY, NAME, DESCRIPTION, TYPE, BODYPARTS, PICTURE, FAVORITES}, NAME + "=?",
                new String[]{pName}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;

        Machine value = new Machine(mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.NAME)),
                mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.DESCRIPTION)),
                ExerciseType.fromInteger(mCursor.getInt(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.TYPE))),
                mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.BODYPARTS)),
                mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.PICTURE)),
                mCursor.getInt(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.FAVORITES)) == 1);

        value.setId(mCursor.getLong(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.KEY)));
        // return value
        mCursor.close();
        close();
        return value;
    }

    public boolean machineExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{NAME}, NAME + "=?",
                new String[]{name}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        return mCursor.getCount() != 0;
    }

    // Getting All Records
    private ArrayList<Machine> getMachineList(String pRequest) {
        ArrayList<Machine> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Machine value = new Machine(mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.NAME)),
                        mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.DESCRIPTION)),
                        ExerciseType.fromInteger(mCursor.getInt(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.TYPE))),
                        mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.BODYPARTS)),
                        mCursor.getString(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.PICTURE)),
                        mCursor.getInt(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.FAVORITES)) == 1);

                value.setId(mCursor.getLong(mCursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Getting All Records
    private Cursor getMachineListCursor(String pRequest) {
        ArrayList<Machine> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        return db.rawQuery(pRequest, null);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void closeCursor() {
        mCursor.close();
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public Cursor getAllMachines() {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";

        // return value list
        return getMachineListCursor(selectQuery);
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public Cursor getAllMachines(int type) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + TYPE + "=" + type + " ORDER BY "
                + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";

        // return value list
        return getMachineListCursor(selectQuery);
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public Cursor getFilteredMachines(CharSequence filterString) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + NAME + " LIKE " + "'%" + filterString + "%' " + " ORDER BY "
                + FAVORITES + " DESC," + NAME + " ASC";
        // return value list
        return getMachineListCursor(selectQuery);
    }


    /**
     * List of Machine object ordered by Favorite and Name
     */
    public void deleteAllEmptyExercises() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, NAME + " = ?",
                new String[]{""});
        db.close();
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public ArrayList<Machine> getAllMachinesArray() {
// Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";

        // return value list
        return getMachineList(selectQuery);
    }

    /**
     * @param idList List of Machine IDs to be return
     * @return List of Machine object ordered by Favorite and Name
     */
    public List<Machine> getAllMachines(List<Long> idList) {

        String ids = idList.toString();
        ids = ids.replace('[', '(');
        ids = ids.replace(']', ')');

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + " in " + ids + " ORDER BY "
                + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";

        // return value list
        return getMachineList(selectQuery);
    }

    // Getting All Machines
    public String[] getAllMachinesName() {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT  " + NAME + " FROM "
                + TABLE_NAME + " ORDER BY " + NAME + " COLLATE NOCASE ASC";
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        String[] valueList = new String[size];

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                String value = mCursor.getString(0);
                valueList[i] = value;
                i++;
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        close();
        // return value list
        return valueList;
    }

    // Updating single value
    public int updateMachine(Machine m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(com.run4urlyfe.Database.DBMachine.NAME, m.getName());
        value.put(com.run4urlyfe.Database.DBMachine.DESCRIPTION, m.getDescription());
        value.put(com.run4urlyfe.Database.DBMachine.TYPE, m.getType().ordinal());
        value.put(com.run4urlyfe.Database.DBMachine.BODYPARTS, m.getBodyParts());
        value.put(com.run4urlyfe.Database.DBMachine.PICTURE, m.getPicture());
        if (m.getFavorite()) value.put(com.run4urlyfe.Database.DBMachine.FAVORITES, 1);
        else value.put(com.run4urlyfe.Database.DBMachine.FAVORITES, 0);

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Record
    public void delete(Machine m) {
        if (m != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, KEY + " = ?",
                    new String[]{String.valueOf(m.getId())});
            db.close();
        }
    }

    // Deleting single Record
    public void delete(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Getting Profils Count
    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();

        cursor.close();
        close();

        // return count
        return value;
    }

    public void populate() {
        addMachine("Biceps", "Biceps", ExerciseType.STRENGTH, "", true, "");
        addMachine("Biceps", "Biceps ", ExerciseType.STRENGTH, "", false, "");
    }
}
