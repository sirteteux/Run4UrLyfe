package com.run4urlyfe;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.run4urlyfe.Database.CVSManager;
import com.run4urlyfe.Database.DBMachine;
import com.run4urlyfe.Database.DBProfile;
import com.run4urlyfe.Database.DatabaseHelper;
import com.run4urlyfe.Database.Machine;
import com.run4urlyfe.Database.Profile;
import com.run4urlyfe.Database.cardio.DBOldCardio;
import com.run4urlyfe.Database.cardio.OldCardio;
import com.run4urlyfe.Database.program.DBProgram;
import com.run4urlyfe.Database.record.DBCardio;
import com.run4urlyfe.Database.record.DBSource;
import com.run4urlyfe.Database.record.DBRecord;
import com.run4urlyfe.Database.record.DBStatic;
import com.run4urlyfe.Database.record.Record;
import com.run4urlyfe.bodymeasures.BodyPartListFragment;
import com.run4urlyfe.enums.DistanceUnit;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.enums.WeightUnit;
import com.run4urlyfe.source.SourcesPagerFragment;
import com.run4urlyfe.intro.MainIntroActivity;
import com.run4urlyfe.machines.MachineFragment;
import com.run4urlyfe.programs.ProgramListFragment;
import com.run4urlyfe.utils.DateConverter;
import com.run4urlyfe.utils.ImageUtil;
import com.run4urlyfe.utils.MusicController;
import com.run4urlyfe.utils.UnitConverter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.

    public static String SOURCESPAGER = "SourcePager";
    public static String WEIGHT = "Weight";
    public static String PROFILE = "Profile";
    public static String BODYTRACKING = "BodyTracking";
    public static String BODYTRACKINGDETAILS = "BodyTrackingDetail";
    public static String ABOUT = "About";
    public static String SETTINGS = "Settings";
    public static String MACHINES = "Machines";
    public static String MACHINESDETAILS = "MachinesDetails";
    public static String WORKOUTS = "Workouts";
    public static String WORKOUTPAGER = "WorkoutPager";
    public static String PREFS_NAME = "prefsfile";
    private final int REQUEST_CODE_INTRO = 111;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT = 1001;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_IMPORT = 1002;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 103;

    private static final int EXPORT_DATABASE = 1;
    private static final int IMPORT_DATABASE = 2;
    public static final int OPEN_MUSIC_FILE = 3;

    private final MusicController musicController = new MusicController(this);
    CustomDrawerAdapter mDrawerAdapter;
    List<DrawerItem> dataList;
    /* Fragments */
    private SourcesPagerFragment mpSourcesPagerFrag = null;
    private WeightFragment mpWeightFrag = null;
    private ProfileFragment mpProfilFrag = null;
    private MachineFragment mpMachineFrag = null;
    private SettingsFragment mpSettingFrag = null;
    private AboutFragment mpAboutFrag = null;
    private BodyPartListFragment mpBodyPartListFrag = null;
    private ProgramListFragment mpWorkoutListFrag;
    private String currentFragmentName = "";
    private DBProfile mDbProfils = null;
    private Profile mCurrentProfile = null;
    private long mCurrentProfilID = -1;
    private String m_importCVSchosenDir = "";
    private Toolbar top_toolbar = null;
    /* Navigation Drawer */
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private CircularImageView roundProfile = null;
    private String mCurrentMachine = "";
    private boolean mIntro014Launched = false;
    private boolean mMigrationBD15done = false;
    private boolean mMigrationToScopedStoragedone = false;
    private long mBackPressed;
    private AppViMo appViMo;

    private final PopupMenu.OnMenuItemClickListener onMenuItemClick = item -> {
        switch (item.getItemId()) {
            case R.id.create_newprofil:
                getActivity().CreateNewProfile();
                return true;
            case R.id.change_profil:
                String[] profilListArray = getActivity().mDbProfils.getAllProfile();

                AlertDialog.Builder changeProfilbuilder = new AlertDialog.Builder(getActivity());
                changeProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil))
                        .setItems(profilListArray, (dialog, which) -> {
                            ListView lv = ((AlertDialog) dialog).getListView();
                            Object checkedItem = lv.getAdapter().getItem(which);
                            setCurrentProfile(checkedItem.toString());
                            KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                            //Toast.makeText(getApplicationContext(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Toast.LENGTH_LONG).show();
                        });
                changeProfilbuilder.show();
                return true;
            case R.id.delete_profil:
                String[] profildeleteListArray = getActivity().mDbProfils.getAllProfile();

                AlertDialog.Builder deleteProfilbuilder = new AlertDialog.Builder(getActivity());
                deleteProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil_to_delete))
                        .setItems(profildeleteListArray, (dialog, which) -> {
                            ListView lv = ((AlertDialog) dialog).getListView();
                            Object checkedItem = lv.getAdapter().getItem(which);
                            if (getCurrentProfile().getName().equals(checkedItem.toString())) {
                                KToast.errorToast(getActivity(), getActivity().getResources().getText(R.string.impossibleToDeleteProfile).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                            } else {
                                Profile profileToDelete = mDbProfils.getProfile(checkedItem.toString());
                                mDbProfils.deleteProfile(profileToDelete);
                                KToast.infoToast(getActivity(), getString(R.string.profileDeleted) + ":" + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                            }
                        });
                deleteProfilbuilder.show();
                return true;
            case R.id.rename_profil:
                getActivity().renameProfil();
                return true;
            case R.id.param_profil:
                showFragment(PROFILE);
                return true;
            default:
                return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.e("Starting MainActivity", "Starting MainActivity");

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String dayNightAuto = SP.getString("dayNightAuto", "2");
        int dayNightAutoValue;
        try {
            dayNightAutoValue = Integer.parseInt(dayNightAuto);
        } catch (NumberFormatException e) {
            dayNightAutoValue = 2;
        }
        if (dayNightAutoValue == getResources().getInteger(R.integer.dark_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SweetAlertDialog.DARK_STYLE = true;
        } else if (dayNightAutoValue == getResources().getInteger(R.integer.light_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SweetAlertDialog.DARK_STYLE = false;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    SweetAlertDialog.DARK_STYLE = true;
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                default:
                    SweetAlertDialog.DARK_STYLE = false;
            }
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadPreferences();

        top_toolbar = this.findViewById(R.id.actionToolbar);
        setSupportActionBar(top_toolbar);
        top_toolbar.setTitle(getResources().getText(R.string.app_name));

        if (savedInstanceState == null) {
            if (mpSourcesPagerFrag == null)
                mpSourcesPagerFrag = SourcesPagerFragment.newInstance(SOURCESPAGER, 6);
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);
            if (mpProfilFrag == null) mpProfilFrag = ProfileFragment.newInstance(PROFILE, 10);
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
            if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 4);
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
            if (mpBodyPartListFrag == null)
                mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);
            if (mpWorkoutListFrag == null)
                mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 11);
        } else {
            mpSourcesPagerFrag = (SourcesPagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, SOURCESPAGER);
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().getFragment(savedInstanceState, WEIGHT);
            mpProfilFrag = (ProfileFragment) getSupportFragmentManager().getFragment(savedInstanceState, PROFILE);
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS);
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().getFragment(savedInstanceState, ABOUT);
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().getFragment(savedInstanceState, MACHINES);
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().getFragment(savedInstanceState, BODYTRACKING);
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().getFragment(savedInstanceState, WORKOUTS);
        }

        appViMo = new ViewModelProvider(this).get(AppViMo.class);
        appViMo.getProfile().observe(this, profile -> {
            // Update UI
            setDrawerTitle(profile.getName());
            setPhotoProfile(profile.getPhoto());
            mCurrentProfilID = profile.getId();
            savePreferences();
        });

        DatabaseHelper.renameOldDatabase(this);

        if (DatabaseHelper.DATABASE_VERSION >= 15 && !mMigrationBD15done) {
            DBOldCardio mDbOldCardio = new DBOldCardio(this);
            DBMachine lDAOMachine = new DBMachine(this);
            if (mDbOldCardio.tableExists()) {
                DBCardio mDbCardio = new DBCardio(this);
                List<OldCardio> mList = mDbOldCardio.getAllRecords();
                for (OldCardio record : mList) {
                    Machine m = lDAOMachine.getMachine(record.getExercice());
                    String exerciseName = record.getExercice();
                    if (m != null) { // if a machine exists
                        if (m.getType() == ExerciseType.STRENGTH) { // if it is not a Cardio type
                            exerciseName = exerciseName + "-Cardio"; // add a suffix to
                        }
                    }

                    mDbCardio.addCardioRecord(record.getDate(), exerciseName, record.getDistance(), record.getDuration(), record.getProfil().getId(), DistanceUnit.KM, -1);
                }
                mDbOldCardio.dropTable();

                DBRecord daoRecord = new DBRecord(this);
                List<Record> mSourceList = daoRecord.getAllRecords();
                for (Record record : mSourceList) {
                    record.setExerciseType(ExerciseType.STRENGTH);
                    daoRecord.updateRecord(record); // Automatically update record Type
                }
                ArrayList<Machine> machineList = lDAOMachine.getAllMachinesArray();
                for (Machine record : machineList) {
                    lDAOMachine.updateMachine(record); // Reset all the fields on machines.
                }
            }
            mMigrationBD15done = true;
            savePreferences();
        }



        if (savedInstanceState == null) {
            showFragment(SOURCESPAGER, false); // Create fragment, do not add to backstack
            currentFragmentName = SOURCESPAGER;
        }

        dataList = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        DrawerItem drawerTitleItem = new DrawerItem("TITLE", R.drawable.ic_person, true);

        dataList.add(drawerTitleItem);
        dataList.add(new DrawerItem(this.getResources().getString(R.string.menu_Workout), R.drawable.ic_fitness_center, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.weightMenuLabel), R.drawable.ic_bathroom_scale, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.bodytracking), R.drawable.ic_ruler, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.SettingLabel), R.drawable.ic_settings, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.AboutLabel), R.drawable.ic_info_outline, true));

        mDrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
                dataList);

        mDrawerList.setAdapter(mDrawerAdapter);

        roundProfile = top_toolbar.findViewById(R.id.imageProfile);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                top_toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, R.string.drawer_close
        );

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        musicController.initView();

        if (!mIntro014Launched) {
            Intent intent = new Intent(this, MainIntroActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        if (mIntro014Launched) {
            initActivity();
            initDEBUGdata();
        }

        if ( !mMigrationToScopedStoragedone) { //do the migration only once.
            migrateDatabase();
        }

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bShowMP3 = SP.getBoolean("prefShowMP3", false);
        this.showMP3Toolbar(bShowMP3);
        this.checkLastBackup(SP);
    }

    private void checkLastBackup(SharedPreferences SP) {
        int milliSecondsOfDay = 86400000; // 24 * 60 * 60 * 1000
        int milliSecondsOfWeek = 604800000; // 7 * 24 * 60 * 60 * 1000
        long milliSecondsOfMonth = 2419200000L; // 4 * 7 * 24 * 60 * 60 * 1000

        long lastBackupUTCTime = SP.getLong("prefLastTimeBackupUTCTime", -1);
        int prefBackupSetting = Integer.parseInt(SP.getString("defaultBackupSetting", "0"));
        if (lastBackupUTCTime == -1 && prefBackupSetting > 0) {
            //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_never).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
            exportDatabase(getActivity().getResources().getText(R.string.backup_warning_never).toString());
        } else {
            if (prefBackupSetting == 1 && System.currentTimeMillis() - lastBackupUTCTime > milliSecondsOfDay) {
                //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_day).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                exportDatabase(getActivity().getResources().getText(R.string.backup_warning_day).toString());
            } else if (prefBackupSetting == 2 && System.currentTimeMillis() - lastBackupUTCTime > milliSecondsOfWeek) {
                //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_week).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                exportDatabase(getActivity().getResources().getText(R.string.backup_warning_week).toString());
            } else if (prefBackupSetting == 3 && System.currentTimeMillis() - lastBackupUTCTime > milliSecondsOfMonth) {
                //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_month).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                exportDatabase(getActivity().getResources().getText(R.string.backup_warning_month).toString());
            }
        }
    }

    private void initDEBUGdata() {
        if (BuildConfig.DEBUG_MODE) {
            // do something for a debug build
            DBSource lDbSource = new DBSource(this);
            if (lDbSource.getCount() == 0) {
                lDbSource.addBodyBuildingRecord(DateConverter.dateToDate(2019, 7, 1, 12, 34, 56), "Example 1", 1, 10, 40, WeightUnit.KG, "", this.getCurrentProfile().getId(), -1);
                lDbSource.addBodyBuildingRecord(DateConverter.dateToDate(2019, 6, 30, 12, 34, 56), "Example 2", 1, 10, UnitConverter.LbstoKg(60), WeightUnit.LBS, "", this.getCurrentProfile().getId(), -1);
            }
            DBCardio lDbCardio = new DBCardio(this);
            if (lDbCardio.getCount() == 0) {
                lDbCardio.addCardioRecord(DateConverter.dateToDate(2019, 7, 1), "Running Example", 1, 10000, this.getCurrentProfile().getId(), DistanceUnit.KM, -1);
                lDbCardio.addCardioRecord(DateConverter.dateToDate(2019, 7, 31), "Cardio Example", UnitConverter.MilesToKm(2), 20000, this.getCurrentProfile().getId(), DistanceUnit.MILES, -1);
            }
            DBStatic lDbStatic = new DBStatic(this);
            if (lDbStatic.getCount() == 0) {
                lDbStatic.addStaticRecord(DateConverter.dateToDate(2019, 7, 1, 12, 34, 56), "Exercise ISO 1", 1, 50, 40, this.getCurrentProfile().getId(), WeightUnit.KG, "", -1);
                lDbStatic.addStaticRecord(DateConverter.dateToDate(2019, 7, 31, 12, 34, 56), "Exercise ISO 2", 1, 60, UnitConverter.LbstoKg(40), this.getCurrentProfile().getId(), WeightUnit.LBS, "", -1);
            }
            DBProgram lDbWorkout = new DBProgram(this);
            if (lDbWorkout.getCount() == 0) {
                lDbWorkout.populate();
            }
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (getSourcesPagerFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, SOURCESPAGER, mpSourcesPagerFrag);
        if (getWeightFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WEIGHT, mpWeightFrag);
        if (getProfileFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, PROFILE, mpProfilFrag);
        if (getMachineFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, MACHINES, mpMachineFrag);
        if (getAboutFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, ABOUT, mpAboutFrag);
        if (getSettingsFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, SETTINGS, mpSettingFrag);
        if (getBodyPartFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, BODYTRACKING, mpBodyPartListFrag);
        if (getWorkoutListFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WORKOUTS, mpWorkoutListFrag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        // restore the profile picture in case it was overwritten during the menu inflate
        if (mCurrentProfile != null) setPhotoProfile(mCurrentProfile.getPhoto());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.action_profil);

        roundProfile.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.profile_actions, popup.getMenu());
            popup.setOnMenuItemClickListener(onMenuItemClick);
            popup.show();
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressWarnings("deprecation")
    private boolean migrateToScopedStorage() {
        boolean success = true;
        File folder = new File(Environment.getExternalStorageDirectory() + "/run4urlyfe");

        if (folder.exists()) { //migrate all pictures
            //Migrate profile pictures
            DBProfile daoProfile = new DBProfile(getBaseContext());
            List<Profile> profileList = daoProfile.getAllProfiles(daoProfile.getWritableDatabase());
            for (Profile profile:profileList) {
                if(!profile.getPhoto().isEmpty()) {
                    File sourceFile = new File(profile.getPhoto());
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    try {
                        File destFile = ImageUtil.moveFile(sourceFile, storageDir);
                        profile.setPhoto(destFile.getAbsolutePath());
                        daoProfile.updateProfile(profile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        success = false;
                    }
                }
            }

            //Migrate exercises pictures
            DBMachine daoMachine = new DBMachine(getBaseContext());
            List<Machine> machineList = daoMachine.getAllMachinesArray();
            for (Machine machine:machineList) {
                if(!machine.getPicture().isEmpty()) {
                    File sourceFile = new File(machine.getPicture());
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    try {
                        File destFile = ImageUtil.moveFile(sourceFile, storageDir);
                        machine.setPicture(destFile.getAbsolutePath());
                        daoMachine.updateMachine(machine);
                    } catch (IOException e) {
                        e.printStackTrace();
                        success = false;
                    }
                }
            }
        }
        return success;
    }

    @SuppressWarnings("deprecation")
    private void migrateDatabase() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Run4UrLyfe");
        if (!folder.exists()) {
            mMigrationToScopedStoragedone = true;
            savePreferences();
            return;
        }

        AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);
        exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.database_migration));
        exportDbBuilder.setMessage("Due to changes from Google, application files needs to be migrated. This can take some time if you have a lot of pictures. Please be patient.");
        exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
            if (!migrateToScopedStorage()) {
                // Display error box for information
                AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(this);
                errorDialogBuilder.setTitle(R.string.database_migration);
                errorDialogBuilder.setMessage(R.string.something_went_wrong);
                AlertDialog errorDialog = errorDialogBuilder.create();
                errorDialog.show();
            } else {
                KToast.infoToast(this, getString(R.string.database_migration_success), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
            mMigrationToScopedStoragedone = true;
            savePreferences();
        });

        AlertDialog exportDbDialog = exportDbBuilder.create();
        exportDbDialog.show();
    }

    private void exportDatabase(String autoExportMessage) {
        /*if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT);
        } else {*/
            AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);

            exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.export_database));
            exportDbBuilder.setMessage(autoExportMessage + " " + getActivity().getResources().getText(R.string.export_question) + " " + getCurrentProfile().getName() + "?");
            exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
                CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
                if (cvsMan.exportDatabase(getCurrentProfile())) {
                    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    long currentTime = System.currentTimeMillis();
                    SP.edit().putLong("prefLastTimeBackupUTCTime", currentTime).apply();
                    if(mpSettingFrag.getContext() != null) {
                        mpSettingFrag.updateLastBackupSummary(SP, currentTime);
                    }
                    KToast.successToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.export_success), Gravity.BOTTOM, KToast.LENGTH_LONG);
                } else {
                    KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.export_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                }
                dialog.dismiss();
            });

            exportDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog exportDbDialog = exportDbBuilder.create();
            exportDbDialog.show();
        //}
    }

    private void importDatabase() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, IMPORT_DATABASE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.export_database:
                exportDatabase("");
                return true;
            case R.id.import_database:
                importDatabase();
                return true;
            case R.id.action_deleteDB:
                AlertDialog.Builder deleteDbBuilder = new AlertDialog.Builder(this);

                deleteDbBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
                deleteDbBuilder.setMessage(getActivity().getResources().getText(R.string.deleteDB_warning));

                deleteDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
                    List<Profile> lList = mDbProfils.getAllProfiles(mDbProfils.getReadableDatabase());
                    for (int i = 0; i < lList.size(); i++) {
                        Profile mTempProfil = lList.get(i);
                        mDbProfils.deleteProfile(mTempProfil.getId());
                    }
                    DBMachine mDbMachines = new DBMachine(getActivity());
                    List<Machine> lList2 = mDbMachines.getAllMachinesArray();
                    for (int i = 0; i < lList2.size(); i++) {
                        Machine mTemp = lList2.get(i);
                        mDbMachines.delete(mTemp.getId());
                    }

                    // redisplay the intro
                    mIntro014Launched = false;

                    // Do nothing but close the dialog
                    dialog.dismiss();

                    finish();
                });

                deleteDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), (dialog, which) -> {
                    // Do nothing
                    dialog.dismiss();
                });

                AlertDialog deleteDbDialog = deleteDbBuilder.create();
                deleteDbDialog.show();

                return true;
            case R.id.action_apropos:
                // Display the fragment as the main content.
                showFragment(ABOUT);
                //getAboutFragment().setHasOptionsMenu(true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                exportDatabase("");
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_IMPORT) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                importDatabase();
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                musicController.chooseDirectory();
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        }
    }

        public boolean CreateNewProfile() {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this);

        newProfilBuilder.setTitle(getActivity().getResources().getText(R.string.createProfilTitle));
        newProfilBuilder.setMessage(getActivity().getResources().getText(R.string.createProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getActivity().getResources().getText(android.R.string.ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (value.isEmpty()) {
                CreateNewProfile();
            } else {
                // Create the new profile
                mDbProfils.addProfile(value);
                // Make it the current.
                setCurrentProfile(value);
            }
        });

        newProfilBuilder.setNegativeButton(getActivity().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {
            if (getCurrentProfile() == null) {
                CreateNewProfile();
            }
        });

        newProfilBuilder.show();

        return true;
    }

    public boolean renameProfil() {
        AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

        newBuilder.setTitle(getActivity().getResources().getText(R.string.renameProfilTitle));
        newBuilder.setMessage(getActivity().getResources().getText(R.string.renameProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(getCurrentProfile().getName());
        newBuilder.setView(input);

        newBuilder.setPositiveButton(getActivity().getResources().getText(android.R.string.ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (!value.isEmpty()) {
                // Get current profile
                Profile temp = getCurrentProfile();
                // Rename it
                temp.setName(value);
                // Commit it
                mDbProfils.updateProfile(temp);
                // Make it the current.
                setCurrentProfile(value);
            }
        });

        newBuilder.setNegativeButton(getActivity().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {
        });

        newBuilder.show();

        return true;
    }

    private void setDrawerTitle(String pProfilName) {
        mDrawerAdapter.getItem(0).setTitle(pProfilName);
        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void showFragment(String pFragmentName) {
        showFragment(pFragmentName, true);
    }

    private void showFragment(String pFragmentName, boolean addToBackStack) {

        if (currentFragmentName.equals(pFragmentName))
            return; // If this is already the current fragment, do no replace.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Then show the fragments
        if (pFragmentName.equals(SOURCESPAGER)) {
            ft.replace(R.id.fragment_container, getSourcesPagerFragment(), SOURCESPAGER);
        } else if (pFragmentName.equals(WEIGHT)) {
            ft.replace(R.id.fragment_container, getWeightFragment(), WEIGHT);
        } else if (pFragmentName.equals(SETTINGS)) {
            ft.replace(R.id.fragment_container, getSettingsFragment(), SETTINGS);
        } else if (pFragmentName.equals(MACHINES)) {
            ft.replace(R.id.fragment_container, getMachineFragment(), MACHINES);
        } else if (pFragmentName.equals(WORKOUTS)) {
            ft.replace(R.id.fragment_container, getWorkoutListFragment(), WORKOUTS);
        } else if (pFragmentName.equals(ABOUT)) {
            ft.replace(R.id.fragment_container, getAboutFragment(), ABOUT);
        } else if (pFragmentName.equals(BODYTRACKING)) {
            ft.replace(R.id.fragment_container, getBodyPartFragment(), BODYTRACKING);
        } else if (pFragmentName.equals(PROFILE)) {
            ft.replace(R.id.fragment_container, getProfileFragment(), PROFILE);
        }
        currentFragmentName = pFragmentName;
        ft.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    public Profile getCurrentProfile() {
        return appViMo.getProfile().getValue();
    }

    public void setCurrentProfile(String newProfilName) {
        Profile newProfil = this.mDbProfils.getProfile(newProfilName);
        setCurrentProfile(newProfil);
    }

    public void setCurrentProfile(Profile newProfil) {
        appViMo.setProfil(newProfil);
    }

    private void setPhotoProfile(String path) {
        ImageUtil imgUtil = new ImageUtil();

        // Check if path is pointing to a thumb else create it and use it.
        String thumbPath = imgUtil.getThumbPath(path);
        if (thumbPath != null) {
            ImageUtil.setPic(roundProfile, thumbPath);
            mDrawerAdapter.getItem(0).setImg(thumbPath);
        } else {
            roundProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_person));
            mDrawerAdapter.getItem(0).setImg(null); // Img has priority over Resource so it needs to be reset
            mDrawerAdapter.getItem(0).setImgResID(R.drawable.ic_person);
        }
        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    public String getCurrentMachine() {
        return mCurrentMachine;
    }

    public void setCurrentMachine(String newMachine) {
        mCurrentMachine = newMachine;
    }

    public MainActivity getActivity() {
        return this;
    }

    private void loadPreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mCurrentProfilID = settings.getLong("currentProfil", -1); // return -1 if it doesn't exist
        mIntro014Launched = settings.getBoolean("intro014Launched", false);
        mMigrationBD15done = settings.getBoolean("migrationBD15done", false);
        mMigrationToScopedStoragedone = settings.getBoolean("migrationToScopedStoragedone", false);
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("currentProfil", mCurrentProfilID);
        editor.putBoolean("intro014Launched", mIntro014Launched);
        editor.putBoolean("migrationBD15done", mMigrationBD15done);
        editor.putBoolean("migrationToScopedStoragedone", mMigrationToScopedStoragedone);
        editor.apply();
    }

    private SourcesPagerFragment getSourcesPagerFragment() {
        if (mpSourcesPagerFrag == null)
            mpSourcesPagerFrag = (SourcesPagerFragment) getSupportFragmentManager().findFragmentByTag(SOURCESPAGER);
        if (mpSourcesPagerFrag == null)
            mpSourcesPagerFrag = SourcesPagerFragment.newInstance(SOURCESPAGER, 6);

        return mpSourcesPagerFrag;
    }

    private WeightFragment getWeightFragment() {
        if (mpWeightFrag == null)
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().findFragmentByTag(WEIGHT);
        if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);

        return mpWeightFrag;
    }

    private ProfileFragment getProfileFragment() {
        if (mpProfilFrag == null)
            mpProfilFrag = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE);
        if (mpProfilFrag == null) mpProfilFrag = ProfileFragment.newInstance(PROFILE, 10);

        return mpProfilFrag;
    }

    private MachineFragment getMachineFragment() {
        if (mpMachineFrag == null)
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
        if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
        return mpMachineFrag;
    }

    private AboutFragment getAboutFragment() {
        if (mpAboutFrag == null)
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().findFragmentByTag(ABOUT);
        if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 6);

        return mpAboutFrag;
    }

    private BodyPartListFragment getBodyPartFragment() {
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().findFragmentByTag(BODYTRACKING);
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);

        return mpBodyPartListFrag;
    }


    private ProgramListFragment getWorkoutListFragment() {
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().findFragmentByTag(WORKOUTS);
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 10);

        return mpWorkoutListFrag;
    }

    private SettingsFragment getSettingsFragment() {
        if (mpSettingFrag == null)
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS);
        if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);

        return mpSettingFrag;
    }

    public Toolbar getActivityToolbar() {
        return top_toolbar;
    }

    public void restoreToolbar() {
        if (top_toolbar != null) setSupportActionBar(top_toolbar);
    }

    public void showMP3Toolbar(boolean show) {
        Toolbar mp3toolbar = this.findViewById(R.id.musicToolbar);
        if (!show) {
            mp3toolbar.setVisibility(View.GONE);
        } else {
            mp3toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                initActivity();
                mIntro014Launched = true;
                initDEBUGdata();
                this.savePreferences();
            } else {
                // Cancelled the intro. You can then e.g. finish this activity too.
                finish();
            }
        } else if (resultCode == RESULT_OK && requestCode == IMPORT_DATABASE) {
            Uri file = null;
            if (data != null) {
                file = data.getData();
                CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    inputStream = null;
                }

                if (cvsMan.importDatabase(inputStream, appViMo.getProfile().getValue())) {
                    KToast.successToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.imported_successfully), Gravity.BOTTOM, KToast.LENGTH_LONG);
                } else {
                    KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.import_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == OPEN_MUSIC_FILE) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                // Return for MusicController Choose file
                musicController.OpenMusicFileIntentResult(uri);
            }
        }
    }

    @Override
    public void onBackPressed() {
        int index = getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            super.onBackPressed();
            getActivity().getSupportActionBar().show();
        } else {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), R.string.pressBackAgain, Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }
    }

    public void initActivity() {
        mDbProfils = new DBProfile(this.getApplicationContext());

        mCurrentProfile = mDbProfils.getProfile(mCurrentProfilID);
        if (mCurrentProfile == null) {
            try {
                List<Profile> lList = mDbProfils.getAllProfiles(mDbProfils.getReadableDatabase());
                mCurrentProfile = lList.get(0);
            } catch (IndexOutOfBoundsException e) {
                this.CreateNewProfile();
            }
        }

        if (mCurrentProfile != null) setCurrentProfile(mCurrentProfile.getName());

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            selectItem(position);

            // Insert the fragment by replacing any existing fragment
            switch (position) {
                case 0:
                    showFragment(PROFILE);
                    setTitle(getString(R.string.ProfileLabel));
                    break;
                case 1:
                    showFragment(SOURCESPAGER);
                    setTitle(getResources().getText(R.string.menu_Workout));
                    break;
                case 2:
                    showFragment(WEIGHT);
                    setTitle(getResources().getText(R.string.weightMenuLabel));
                    break;
                case 3:
                    showFragment(BODYTRACKING);
                    setTitle(getResources().getText(R.string.bodytracking));
                    break;
                case 4:
                    showFragment(SETTINGS);
                    setTitle(getResources().getText(R.string.SettingLabel));
                    break;
                case 5:
                    showFragment(ABOUT);
                    setTitle(getResources().getText(R.string.AboutLabel));
                    break;
                default:
                    showFragment(SOURCESPAGER);
                    setTitle(getResources().getText(R.string.SourceLabel));
            }
        }
    }
}
