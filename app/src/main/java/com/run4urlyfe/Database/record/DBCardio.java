package com.run4urlyfe.Database.record;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.run4urlyfe.Database.Profile;
import com.run4urlyfe.R;
import com.run4urlyfe.enums.DistanceUnit;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.enums.ProgramRecordStatus;
import com.run4urlyfe.enums.RecordType;
import com.run4urlyfe.enums.WeightUnit;
import com.run4urlyfe.graph.GraphData;
import com.run4urlyfe.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBCardio extends DBRecord {

    public static final int DISTANCE_FCT = 0;
    public static final int DURATION_FCT = 1;
    public static final int SPEED_FCT = 2;
    public static final int MAXDURATION_FCT = 3;
    public static final int MAXDISTANCE_FCT = 4;
    public static final int NBSERIE_FCT = 5;

    private static final String OLD_TABLE_NAME = "EFcardio";

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + DISTANCE + "," + DURATION + "," + PROFILE_KEY + "," + TIME + "," + DISTANCE_UNIT;

    public DBCardio(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * @param pDate
     * @param pMachine
     * @param pDistance
     * @param pDuration
     * @param pProfilId
     * @return
     */
    public long addCardioRecord(Date pDate, String pMachine, float pDistance, long pDuration, long pProfilId, DistanceUnit pDistanceUnit, long pTemplateRecordId) {
        return addRecord(pDate, pMachine, ExerciseType.CARDIO, 0, 0, 0, WeightUnit.KG, 0, pDistance, pDistanceUnit, pDuration, "", pProfilId, pTemplateRecordId, RecordType.FREE_RECORD_TYPE);
    }

    public long addCardioRecordToProgramTemplate(long pTemplateId, long pTemplateSessionId, Date pDate, String pExerciseName, float pDistance, DistanceUnit pDistanceUnit, long pDuration, int restTime) {
        return addRecord(pDate, pExerciseName, ExerciseType.CARDIO, 0, 0, 0,
                WeightUnit.KG, "", pDistance, pDistanceUnit, pDuration, 0, -1,
                RecordType.TEMPLATE_TYPE, -1, pTemplateId, pTemplateSessionId,
                restTime, ProgramRecordStatus.NONE);
    }

    // Getting Function records
    public List<GraphData> getFunctionRecords(Profile pProfil, String pMachine,
                                              int pFunction) {

        boolean lfilterMachine = true;
        boolean lfilterFunction = true;
        String selectQuery = null;

        if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterMachine = false;
        }

        if (pFunction == com.run4urlyfe.Database.record.DBCardio.DISTANCE_FCT) {
            selectQuery = "SELECT SUM(" + DISTANCE + "), " + LOCAL_DATE + " FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfil.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == com.run4urlyfe.Database.record.DBCardio.DURATION_FCT) {
            selectQuery = "SELECT SUM(" + DURATION + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfil.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == com.run4urlyfe.Database.record.DBCardio.SPEED_FCT) {
            selectQuery = "SELECT SUM(" + DISTANCE + ") / SUM(" + DURATION + ")," + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfil.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == com.run4urlyfe.Database.record.DBCardio.MAXDISTANCE_FCT) {
            selectQuery = "SELECT MAX(" + DISTANCE + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfil.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        }
        // case "MEAN" : selectQuery = "SELECT SUM("+ SERIE + "*" + REPETITION +
        // "*" + WEIGHT +") FROM " + TABLE_NAME + " WHERE " + EXERCISE + "=\"" +
        // pMachine + "\" AND " + DATE + "=\"" + pDate + "\" ORDER BY " + KEY +
        // " DESC";
        // break;

        List<GraphData> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        double i = 0;

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Date date = DateConverter.DBDateStrToDate(mCursor.getString(1));

                GraphData value = new GraphData(DateConverter.nbDays(date),
                        mCursor.getDouble(0));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        // return value list
        return valueList;
    }
}
