package com.run4urlyfe.Database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.run4urlyfe.Database.bodymeasures.BodyMeasure;
import com.run4urlyfe.Database.bodymeasures.BodyPart;
import com.run4urlyfe.Database.bodymeasures.BodyPartExtensions;
import com.run4urlyfe.Database.bodymeasures.DBBodyMeasure;
import com.run4urlyfe.Database.bodymeasures.DBBodyPart;
import com.run4urlyfe.Database.cardio.DBOldCardio;
import com.run4urlyfe.Database.record.DBCardio;
import com.run4urlyfe.Database.record.DBRecord;
import com.run4urlyfe.Database.record.Record;
import com.run4urlyfe.enums.DistanceUnit;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.enums.Unit;
import com.run4urlyfe.enums.WeightUnit;
import com.run4urlyfe.utils.DateConverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


// Uses http://javacsv.sourceforge.net/com/csvreader/CsvReader.html //
public class CVSManager {

    static private final String TABLE_HEAD = "table";
    static private final String ID_HEAD = "id";

    private Context mContext = null;

    public CVSManager(Context pContext) {
        mContext = pContext;
    }

    public boolean exportDatabase(Profile pProfil) {
         /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */

         boolean ret = true;

        PrintWriter printWriter = null;
            try {
                ret &= exportBodyMeasures(pProfil);
                ret &= exportRecords(pProfil);
                ret &= exportExercise(pProfil);
                ret &= exportBodyParts(pProfil);
            } catch (Exception e) {
                //if there are any exceptions, return false
                e.printStackTrace();
                return false;
            } finally {
                if (printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return ret;
    }

    private OutputStream CreateNewFile(String name, Profile pProfil) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
        Date date = new Date();

        String fileName = "export_" + name + "_" + pProfil.getName() + "_" + dateFormat.format(date);

        ContentResolver resolver = mContext.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Run4UrLyfe");
        Uri collection = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }

        Uri file = resolver.insert(collection, contentValues);
        try {
            return resolver.openOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean exportRecords(Profile pProfil) {
        try {
            OutputStream exportFile = CreateNewFile("Records", pProfil);

            CsvWriter csvOutputSource = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

            DBRecord dbc = new DBRecord(mContext);
            dbc.open();

            Cursor cursor = dbc.getAllRecordsByProfile(pProfil);
            List<Record> records = dbc.fromCursorToList(cursor);

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutputSource.write(TABLE_HEAD);
            csvOutputSource.write(ID_HEAD);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.DATE);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.TIME);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.EXERCISE);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.EXERCISE_TYPE);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.PROFILE_KEY);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.SETS);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.REPS);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.WEIGHT);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.WEIGHT_UNIT);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.SECONDS);
            csvOutputSource.write(DBRecord.DISTANCE);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.DISTANCE_UNIT);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.DURATION);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.NOTES);
            csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.RECORD_TYPE);
            csvOutputSource.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutputSource.write(com.run4urlyfe.Database.record.DBRecord.TABLE_NAME);
                csvOutputSource.write(Long.toString(records.get(i).getId()));

                Date dateRecord = records.get(i).getDate();

                csvOutputSource.write(DateConverter.dateTimeToDBDateStr(dateRecord));
                csvOutputSource.write(DateConverter.dateTimeToDBTimeStr(dateRecord));
                csvOutputSource.write(records.get(i).getExercise());
                csvOutputSource.write(Integer.toString(ExerciseType.STRENGTH.ordinal()));
                csvOutputSource.write(Long.toString(records.get(i).getProfileId()));
                csvOutputSource.write(Integer.toString(records.get(i).getSets()));
                csvOutputSource.write(Integer.toString(records.get(i).getReps()));
                csvOutputSource.write(Float.toString(records.get(i).getWeight()));
                csvOutputSource.write(Integer.toString(records.get(i).getWeightUnit().ordinal()));
                csvOutputSource.write(Integer.toString(records.get(i).getSeconds()));
                csvOutputSource.write(Float.toString(records.get(i).getDistance()));
                csvOutputSource.write(Integer.toString(records.get(i).getDistanceUnit().ordinal()));
                csvOutputSource.write(Long.toString(records.get(i).getDuration()));
                if (records.get(i).getNote() == null) csvOutputSource.write("");
                else csvOutputSource.write(records.get(i).getNote());
                csvOutputSource.write(Integer.toString(records.get(i).getRecordType().ordinal()));
                csvOutputSource.endRecord();
            }
            csvOutputSource.close();
            dbc.closeAll();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportBodyMeasures(Profile pProfil) {
        try {
            OutputStream exportFile = CreateNewFile("BodyMeasures", pProfil);

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);
            DBBodyMeasure DBBodyMeasure = new DBBodyMeasure(mContext);
            DBBodyMeasure.open();

            DBBodyPart DBBodyPart = new DBBodyPart(mContext);

            List<BodyMeasure> bodyMeasures = DBBodyMeasure.getBodyMeasuresList(pProfil);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(ID_HEAD);
            cvsOutput.write(DBBodyMeasure.DATE);
            cvsOutput.write("bodypart_label");
            cvsOutput.write(DBBodyMeasure.MEASURE);
            cvsOutput.write(DBBodyMeasure.PROFIL_KEY);
            cvsOutput.endRecord();

            for (int i = 0; i < bodyMeasures.size(); i++) {
                cvsOutput.write(DBBodyMeasure.TABLE_NAME);
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getId()));
                Date dateRecord = bodyMeasures.get(i).getDate();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                BodyPart bp = DBBodyPart.getBodyPart(bodyMeasures.get(i).getBodyPartID());
                cvsOutput.write(bp.getName(mContext)); // Write the full name of the BodyPart
                cvsOutput.write(Float.toString(bodyMeasures.get(i).getBodyMeasure()));
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getProfileID()));

                cvsOutput.endRecord();
            }
            cvsOutput.close();
            DBBodyMeasure.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportBodyParts(Profile pProfil) {
        try {
            OutputStream exportFile = CreateNewFile("BodyParts", pProfil);
            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);
            DBBodyPart DBBodyPart = new DBBodyPart(mContext);
            DBBodyPart.open();


            List<BodyPart> bodyParts = DBBodyPart.getList();

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(DBBodyPart.KEY);
            cvsOutput.write(DBBodyPart.CUSTOM_NAME);
            cvsOutput.write(DBBodyPart.CUSTOM_PICTURE);
            cvsOutput.endRecord();

            for (BodyPart bp : bodyParts) {
                if (bp.getBodyPartResKey() == -1) { // Only custom BodyPart are exported
                    cvsOutput.write(DBBodyMeasure.TABLE_NAME);
                    cvsOutput.write(Long.toString(bp.getId()));
                    cvsOutput.write(bp.getName(mContext));
                    cvsOutput.write(bp.getCustomPicture());
                    cvsOutput.endRecord();
                }
            }
            cvsOutput.close();
            DBBodyPart.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportExercise(Profile pProfil) {
        try {
            OutputStream exportFile = CreateNewFile("Exercises", pProfil);
            CsvWriter csvOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

            DBMachine dbcMachine = new DBMachine(mContext);
            dbcMachine.open();

            List<Machine> records = dbcMachine.getAllMachinesArray();

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutput.write(TABLE_HEAD);
            csvOutput.write(ID_HEAD);
            csvOutput.write(com.run4urlyfe.Database.DBMachine.NAME);
            csvOutput.write(com.run4urlyfe.Database.DBMachine.DESCRIPTION);
            csvOutput.write(com.run4urlyfe.Database.DBMachine.TYPE);
            csvOutput.write(com.run4urlyfe.Database.DBMachine.BODYPARTS);
            csvOutput.write(com.run4urlyfe.Database.DBMachine.FAVORITES);
            //csvOutput.write(DBMachine.PICTURE_RES);
            csvOutput.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutput.write(com.run4urlyfe.Database.DBMachine.TABLE_NAME);
                csvOutput.write(Long.toString(records.get(i).getId()));
                csvOutput.write(records.get(i).getName());
                csvOutput.write(records.get(i).getDescription());
                csvOutput.write(Integer.toString(records.get(i).getType().ordinal()));
                csvOutput.write(records.get(i).getBodyParts());
                csvOutput.write(Boolean.toString(records.get(i).getFavorite()));
                //write the record in the .csv file
                csvOutput.endRecord();
            }
            csvOutput.close();
            dbcMachine.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    public boolean importDatabase(InputStream file, Profile pProfil) {

        boolean ret = true;

        try {
            CsvReader csvRecords = new CsvReader(file, ',', StandardCharsets.UTF_8);

            csvRecords.readHeaders();

            ArrayList<Record> recordsList = new ArrayList<>();

            DBMachine dbcMachine = new DBMachine(mContext);

            while (csvRecords.readRecord()) {
                switch (csvRecords.get(TABLE_HEAD)) {
                    case com.run4urlyfe.Database.record.DBRecord.TABLE_NAME: {
                        Date date = DateConverter.DBDateTimeStrToDate(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.DATE), csvRecords.get(com.run4urlyfe.Database.record.DBRecord.TIME));
                        String exercise = csvRecords.get(com.run4urlyfe.Database.record.DBRecord.EXERCISE);
                        if (dbcMachine.getMachine(exercise) != null) {
                            long exerciseId = dbcMachine.getMachine(exercise).getId();
                            ExerciseType exerciseType = dbcMachine.getMachine(exercise).getType();

                            float weights = TryGetFloat(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.WEIGHT), 0);
                            int repetition = TryGetInteger(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.REPS), 0);
                            int serie = TryGetInteger(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.SETS), 0);
                            WeightUnit unit = WeightUnit.KG;
                            if (!csvRecords.get(com.run4urlyfe.Database.record.DBRecord.WEIGHT_UNIT).isEmpty()) {
                                unit = WeightUnit.fromInteger(TryGetInteger(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.WEIGHT_UNIT), WeightUnit.KG.ordinal()));
                            }
                            int second = TryGetInteger(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.SECONDS), 0);
                            float distance = TryGetFloat(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.DISTANCE), 0);
                            int duration = TryGetInteger(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.DURATION), 0);
                            DistanceUnit distance_unit = DistanceUnit.KM;
                            if (!csvRecords.get(com.run4urlyfe.Database.record.DBRecord.DISTANCE_UNIT).isEmpty()) {
                                distance_unit = DistanceUnit.fromInteger(TryGetInteger(csvRecords.get(com.run4urlyfe.Database.record.DBRecord.DISTANCE_UNIT), DistanceUnit.KM.ordinal()));
                            }
                            String notes = csvRecords.get(com.run4urlyfe.Database.record.DBRecord.NOTES);

                            Record record = new Record(date, exercise, exerciseId, pProfil.getId(), serie, repetition, weights, unit, second, distance, distance_unit, duration, notes, exerciseType, -1);
                            recordsList.add(record);
                        } else {
                            return false;
                        }

                        break;
                    }
                    case DBOldCardio.TABLE_NAME: {
                        DBCardio dbcCardio = new DBCardio(mContext);
                        dbcCardio.open();

                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(com.run4urlyfe.Database.record.DBCardio.DATE));

                        String exercice = csvRecords.get(DBOldCardio.EXERCICE);
                        float distance = Float.parseFloat(csvRecords.get(DBOldCardio.DISTANCE));
                        int duration = Integer.parseInt(csvRecords.get(DBOldCardio.DURATION));
                        dbcCardio.addCardioRecord(date, exercice, distance, duration, pProfil.getId(), DistanceUnit.KM, -1);
                        dbcCardio.close();

                        break;
                    }
                    case com.run4urlyfe.Database.DBProfileWeight.TABLE_NAME: {
                        DBBodyMeasure dbcWeight = new DBBodyMeasure(mContext);
                        dbcWeight.open();
                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(com.run4urlyfe.Database.DBProfileWeight.DATE));

                        float weights = Float.parseFloat(csvRecords.get(com.run4urlyfe.Database.DBProfileWeight.weights));
                        dbcWeight.addBodyMeasure(date, BodyPartExtensions.WEIGHT, weights, pProfil.getId(), Unit.KG);

                        break;
                    }
                    case DBBodyMeasure.TABLE_NAME: {
                        DBBodyMeasure dbcBodyMeasure = new DBBodyMeasure(mContext);
                        dbcBodyMeasure.open();
                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DBBodyMeasure.DATE));
                        Unit unit = Unit.fromInteger(Integer.parseInt(csvRecords.get(DBBodyMeasure.UNIT))); // Mandatory. Cannot not know the Unit.
                        String bodyPartName = csvRecords.get("bodypart_label");
                        DBBodyPart dbcBodyPart = new DBBodyPart(mContext);
                        dbcBodyPart.open();
                        List<BodyPart> bodyParts = dbcBodyPart.getList();
                        for (BodyPart bp : bodyParts) {
                            if (bp.getName(mContext).equals(bodyPartName)) {
                                float measure = Float.parseFloat(csvRecords.get(DBBodyMeasure.MEASURE));
                                dbcBodyMeasure.addBodyMeasure(date, bp.getId(), measure, pProfil.getId(), unit);
                                dbcBodyPart.close();
                                break;
                            }
                        }
                        break;
                    }
                    case DBBodyPart.TABLE_NAME:
                        DBBodyPart dbcBodyPart = new DBBodyPart(mContext);
                        dbcBodyPart.open();
                        int bodyPartId = -1;
                        String customName = csvRecords.get(DBBodyPart.CUSTOM_NAME);
                        String customPicture = csvRecords.get(DBBodyPart.CUSTOM_PICTURE);
                        dbcBodyPart.add(bodyPartId, customName, customPicture, 0, BodyPartExtensions.TYPE_MUSCLE);
                        break;
                    case com.run4urlyfe.Database.DBProfile.TABLE_NAME:
                        break;
                    case com.run4urlyfe.Database.DBMachine.TABLE_NAME:
                        DBMachine dbc = new DBMachine(mContext);
                        String name = csvRecords.get(com.run4urlyfe.Database.DBMachine.NAME);
                        String description = csvRecords.get(com.run4urlyfe.Database.DBMachine.DESCRIPTION);
                        ExerciseType type = ExerciseType.fromInteger(Integer.parseInt(csvRecords.get(com.run4urlyfe.Database.DBMachine.TYPE)));
                        boolean favorite = TryGetBoolean(csvRecords.get(com.run4urlyfe.Database.DBMachine.FAVORITES), false);
                        String bodyParts = csvRecords.get(com.run4urlyfe.Database.DBMachine.BODYPARTS);

                        // Check if this machine doesn't exist
                        if (dbc.getMachine(name) == null) {
                            dbc.addMachine(name, description, type, "", favorite, bodyParts);
                        } else {
                            Machine m = dbc.getMachine(name);
                            m.setDescription(description);
                            m.setFavorite(favorite);
                            m.setBodyParts(bodyParts);
                            dbc.updateMachine(m);
                        }
                        break;
                }
            }

            csvRecords.close();

            // In case of success
            DBRecord daoRecord = new DBRecord(mContext);
            daoRecord.addList(recordsList);

        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }

    private int TryGetInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float TryGetFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean TryGetBoolean(String value, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Unit TryGetUnit(String value, Unit defaultValue) {
        Unit unit = Unit.fromString(value);
        if (unit != null) {
            return unit;
        }
        return defaultValue;
    }

}
