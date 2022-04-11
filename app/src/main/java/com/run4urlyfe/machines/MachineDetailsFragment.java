package com.run4urlyfe.machines;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.run4urlyfe.Database.DBMachine;
import com.run4urlyfe.Database.DBProfile;
import com.run4urlyfe.Database.Machine;
import com.run4urlyfe.Database.Profile;
import com.run4urlyfe.Database.record.DBRecord;
import com.run4urlyfe.Database.record.Record;
import com.run4urlyfe.R;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.utils.ImageUtil;
import com.run4urlyfe.utils.Keyboard;
import com.run4urlyfe.utils.RealPathUtil;
import com.run4urlyfe.views.EditableInputView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onurkaganaldemir.ktoastlib.KToast;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MachineDetailsFragment extends Fragment {
    protected List<String> _musclesArray = new ArrayList<>();
    protected boolean[] _selections;
    TextView musclesList = null;
    EditableInputView machineName = null;
    EditableInputView machineDescription = null;
    ImageView machinePhoto = null;
    FloatingActionButton machineAction = null;
    LinearLayout machinePhotoLayout = null;
    ExerciseType selectedType = ExerciseType.STRENGTH;
    String machineNameArg = null;
    long machineIdArg = 0;
    long machineProfilIdArg = 0;
    boolean isImageFitToScreen = false;
    ExerciseDetailsPager pager = null;
    ArrayList selectMuscleList = new ArrayList();
    DBMachine mDbMachine = null;
    DBRecord mDbRecord = null;
    Machine mMachine;

    View fragmentView = null;

    ImageUtil imgUtil = null;
    private final OnLongClickListener onLongClickMachinePhoto = v -> CreatePhotoSourceDialog();
    private final OnClickListener onClickMachinePhoto = v -> CreatePhotoSourceDialog();
    boolean isCreateMuscleDialogActive = false;
    private final OnClickListener onClickMusclesList = v -> CreateMuscleDialog();
    private final OnFocusChangeListener onFocusMachineList = (arg0, arg1) -> {
        if (arg1) {
            CreateMuscleDialog();
        }
    };
    String mCurrentPhotoPath = null;
    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start,
                                  int before, int count) {
            requestForSave();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private final EditableInputView.OnTextChangedListener textChangeListener = view -> requestForSave();

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static MachineDetailsFragment newInstance(long machineId, long machineProfile) {
        MachineDetailsFragment f = new MachineDetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("machineID", machineId);
        args.putLong("machineProfile", machineProfile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.exercise_details, container, false);
        fragmentView = view;

        mDbMachine = new DBMachine(view.getContext());
        mDbRecord = new DBRecord(view.getContext());

        machineName = view.findViewById(R.id.machine_name);
        machineDescription = view.findViewById(R.id.machine_description);
        musclesList = view.findViewById(R.id.machine_muscles);
        machinePhoto = view.findViewById(R.id.machine_photo);

        machinePhotoLayout = view.findViewById(R.id.machine_photo_layout);
        machineAction = view.findViewById(R.id.actionCamera);

        imgUtil = new ImageUtil(machinePhoto);

        buildMusclesTable();

        Bundle args = this.getArguments();

        machineIdArg = args.getLong("machineID");
        machineProfilIdArg = args.getLong("machineProfile");

        // set events
        musclesList.setOnClickListener(onClickMusclesList);
        musclesList.setOnFocusChangeListener(onFocusMachineList);
        machinePhoto.setOnLongClickListener(onLongClickMachinePhoto);
        machinePhoto.setOnClickListener(v -> {
            if (isImageFitToScreen) {
                isImageFitToScreen = false;
                machinePhoto.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                machinePhoto.setAdjustViewBounds(true);
                machinePhoto.setMaxHeight((int) (getView().getHeight() * 0.2));
                machinePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
                    File f = new File(mCurrentPhotoPath);
                    if (f.exists()) {

                        isImageFitToScreen = true;

                        // Get the dimensions of the bitmap
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                        float photoW = bmOptions.outWidth;
                        float photoH = bmOptions.outHeight;

                        // Determine how much to scale down the image
                        int scaleFactor = (int) (photoW / machinePhoto.getWidth());//Math.min(photoW/targetW, photoH/targetH);machinePhoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        machinePhoto.setAdjustViewBounds(true);
                        machinePhoto.setMaxHeight((int) (photoH / scaleFactor));
                        machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                }
            }
        });
        machineAction.setOnClickListener(onClickMachinePhoto);

        mMachine = mDbMachine.getMachine(machineIdArg);
        machineNameArg = mMachine.getName();

        if (machineNameArg.isEmpty()) {
            machineName.setText("Default exercise");
        } else {
            machineName.setText(machineNameArg);
        }

        machineDescription.setText(mMachine.getDescription());
        musclesList.setText(this.getInputFromDBString(mMachine.getBodyParts()));
        mCurrentPhotoPath = mMachine.getPicture();

        if (mMachine.getType() == ExerciseType.CARDIO) {
            selectedType = mMachine.getType();
            view.findViewById(R.id.machine_muscles).setVisibility(View.GONE);
            view.findViewById(R.id.machine_muscles_textview).setVisibility(View.GONE);
        } else {
            selectedType = mMachine.getType();
            view.findViewById(R.id.machine_muscles).setVisibility(View.VISIBLE);
            view.findViewById(R.id.machine_muscles_textview).setVisibility(View.VISIBLE);
        }

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                fragmentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Here you can get the size :)

                if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
                    ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
                } else {
                    if (mMachine.getType() == ExerciseType.STRENGTH) {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_gym_bench_50dp));
                    } else if (mMachine.getType() == ExerciseType.ISOMETRIC) {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_static_50dp));
                    } else {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_training_50dp));
                    }
                    machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
                machinePhoto.setMaxHeight((int) (getView().getHeight() * 0.2));
            }
        });

        machineName.setOnTextChangeListener(textChangeListener);
        machineDescription.setOnTextChangeListener(textChangeListener);
        musclesList.addTextChangedListener(watcher);

        imgUtil.setOnDeleteImageListener(imgUtil -> {
            if (mMachine.getType() == ExerciseType.STRENGTH) {
                imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_gym_bench_50dp));
            } else if (mMachine.getType() == ExerciseType.ISOMETRIC) {
                imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_static_50dp));
            } else {
                imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_training_50dp));
            }
            machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mCurrentPhotoPath = null;
            requestForSave();
        });

        if (getParentFragment() instanceof ExerciseDetailsPager) {
            pager = (ExerciseDetailsPager) getParentFragment();
        }

        return view;
    }

    private boolean CreateMuscleDialog() {
        if (isCreateMuscleDialogActive)
            return true;

        isCreateMuscleDialogActive = true;

        Keyboard.hide(getContext(), musclesList);

        AlertDialog.Builder newMuscleBuilder = new AlertDialog.Builder(this.getActivity());

        newMuscleBuilder.setTitle(this.getResources().getString(R.string.selectMuscles));
        newMuscleBuilder.setMultiChoiceItems(_musclesArray.toArray(new CharSequence[0]), _selections, (arg0, arg1, arg2) -> {
            if (arg2) {
                // If user select a item then add it in selected items
                selectMuscleList.add(arg1);
            } else if (selectMuscleList.contains(arg1)) {
                // if the item is already selected then remove it
                selectMuscleList.remove(Integer.valueOf(arg1));
            }
        });

        // Set an EditText view to get user input
        newMuscleBuilder.setPositiveButton(getResources().getString(android.R.string.ok), (dialog, whichButton) -> {
            StringBuilder msg = new StringBuilder();
            int i = 0;
            boolean firstSelection = true;
            // ( selectMuscleList.size() > 0 ) {
            for (i = 0; i < _selections.length; i++) {
                if (_selections[i] && firstSelection) {
                    msg = new StringBuilder(_musclesArray.get(i));
                    firstSelection = false;
                } else if (_selections[i] && !firstSelection) {
                    msg.append(";").append(_musclesArray.get(i));
                }
            }
            //}
            setMuscleText(msg.toString());
            Keyboard.hide(getContext(), musclesList);

            isCreateMuscleDialogActive = false;
        });
        newMuscleBuilder.setNegativeButton(getResources().getString(android.R.string.cancel), (dialog, whichButton) -> isCreateMuscleDialogActive = false);

        newMuscleBuilder.show();

        return true;
    }

    private boolean CreatePhotoSourceDialog() {
        if (imgUtil == null)
            imgUtil = new ImageUtil();

        return imgUtil.CreatePhotoSourceDialog(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ImageUtil.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    mCurrentPhotoPath = imgUtil.getFilePath();
                    ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
                    ImageUtil.saveThumb(mCurrentPhotoPath);
                    imgUtil.galleryAddPic(this, mCurrentPhotoPath);
                    requestForSave();
                }
                break;
            case ImageUtil.REQUEST_PICK_GALERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    String realPath = RealPathUtil.getRealPath(this.getContext(), data.getData());

                    ImageUtil.setPic(machinePhoto, realPath);
                    ImageUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave();
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == Activity.RESULT_OK) {
                    Uri resultUri = result.getUri();
                    String realPath = RealPathUtil.getRealPath(this.getContext(), resultUri);

                    File SourceFile = new File(realPath);

                    File storageDir = null;
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + ".jpg";
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        return;
                    } else {
                        //We use the Run4UrLyfe directory for saving our .csv file.
                        storageDir = Environment.getExternalStoragePublicDirectory("/Run4UrLyfe/Camera/");
                        if (!storageDir.exists()) {
                            storageDir.mkdirs();
                        }
                    }
                    File DestinationFile = null;

                    try {
                        DestinationFile = imgUtil.moveFile(SourceFile, storageDir);
                        Log.v("Moving", "Moving file successful.");
                        realPath = DestinationFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("Moving", "Moving file failed.");
                    }

                    ImageUtil.setPic(machinePhoto, realPath);
                    ImageUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }

    public void setMuscleText(String t) {
        musclesList.setText(t);
    }

    public MachineDetailsFragment getThis() {
        return this;
    }

    public void requestForSave() {
        saveMachine();
    }

    public void buildMusclesTable() {
        _musclesArray.add(getActivity().getResources().getString(R.string.biceps));
        _musclesArray.add(getActivity().getResources().getString(R.string.triceps));
        _musclesArray.add(getActivity().getResources().getString(R.string.chest));
        _musclesArray.add(getActivity().getResources().getString(R.string.back));
        _musclesArray.add(getActivity().getResources().getString(R.string.abdominals));
        _musclesArray.add(getActivity().getResources().getString(R.string.quadriceps));
        _musclesArray.add(getActivity().getResources().getString(R.string.hamstrings));
        _musclesArray.add(getActivity().getResources().getString(R.string.thighs));
        _musclesArray.add(getActivity().getResources().getString(R.string.calves));
        _musclesArray.add(getActivity().getResources().getString(R.string.deltoids));
        _musclesArray.add(getActivity().getResources().getString(R.string.trapezius));
        _musclesArray.add(getActivity().getResources().getString(R.string.shoulders));
        _musclesArray.add(getActivity().getResources().getString(R.string.obliques));

        Collections.sort(_musclesArray);

        _selections = new boolean[_musclesArray.size()];
    }


    public String getMuscleNameFromId(int id) {
        String ret = "";
        try {
            ret = _musclesArray.get(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    public int getMuscleIdFromName(String pName) {
        for (int i = 0; i < _musclesArray.size(); i++) {
            if (_musclesArray.get(i).equals(pName)) return i;
        }
        return -1;
    }


    public String getDBStringFromInput(String pInput) {
        String[] data = pInput.split(";");
        StringBuilder output = new StringBuilder();

        if (pInput.isEmpty()) return "";

        int i = 0;
        if (data.length > 0) {
            output = new StringBuilder(String.valueOf(getMuscleIdFromName(data[i])));
            for (i = 1; i < data.length; i++) {
                output.append(";").append(getMuscleIdFromName(data[i]));
            }
        }

        return output.toString();
    }

    public String getInputFromDBString(String pDBString) {
        String[] data = pDBString.split(";");
        StringBuilder output = new StringBuilder();

        int i = 0;

        try {
            if (data.length > 0) {
                if (data[0].isEmpty()) return "";

                if (!data[i].equals("-1")) {
                    output = new StringBuilder(getMuscleNameFromId(Integer.parseInt(data[i])));
                    _selections[Integer.parseInt(data[i])] = true;
                    for (i = 1; i < data.length; i++) {
                        if (!data[i].equals("-1")) {
                            output.append(";").append(getMuscleNameFromId(Integer.parseInt(data[i])));
                            _selections[Integer.parseInt(data[i])] = true;
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            output = new StringBuilder();
            e.printStackTrace();
        }

        return output.toString();
    }

    public Machine getMachine() {
        Machine m = new Machine(machineName.getText(),
                machineDescription.getText(),
                selectedType,
                getDBStringFromInput(musclesList.getText().toString()), mCurrentPhotoPath, mMachine.getFavorite());
        m.setId(mMachine.getId());
        /*m.setName(machineName.getText());
        m.setDescription(machineDescription.getText());
        m.setBodyParts(getDBStringFromInput(musclesList.getText().toString()));
        m.setPicture(mCurrentPhotoPath);
        m.setFavorite(false);
        m.setType(selectedType);*/
        return m;
    }

    private boolean saveMachine() {
        boolean result = false;
        final Machine initialMachine = mMachine;
        final Machine newMachine = getMachine();
        final String lMachineName = newMachine.getName();

        if (lMachineName.isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.name_is_required).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        } else if (!initialMachine.getName().equals(lMachineName)) {
            final Machine machineWithSameName = mDbMachine.getMachine(lMachineName);
            // if an exercise exists with the same name but different types then block.
            if (machineWithSameName != null && newMachine.getId() != machineWithSameName.getId() && newMachine.getType() != machineWithSameName.getType()) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());

                dialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_warning));
                dialogBuilder.setMessage(R.string.renameMachine_error_text2);
                dialogBuilder.setPositiveButton(getResources().getText(R.string.global_yes), (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();

            } else if (machineWithSameName != null && newMachine.getId() != machineWithSameName.getId() && newMachine.getType() == machineWithSameName.getType()) {
                // if an exercise exists with the same name but with same types then merge.
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());

                dialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_warning));
                dialogBuilder.setMessage(getActivity().getResources().getText(R.string.renameMachine_warning_text));

                dialogBuilder.setPositiveButton(getResources().getText(R.string.global_yes), (dialog, which) -> {
                    // Rename all the records with that machine and rename them
                    DBRecord lDbRecord = new DBRecord(getView().getContext());
                    DBProfile mDbProfil = new DBProfile(getView().getContext());
                    Profile lProfile = mDbProfil.getProfile(machineProfilIdArg);

                    List<Record> listRecords = lDbRecord.getAllRecordByMachineStrArray(lProfile, initialMachine.getName());
                    for (Record record : listRecords) {
                        record.setExercise(newMachine.getName());
                        record.setExerciseId(machineWithSameName.getId());
                        lDbRecord.updateRecord(record);
                    }

                    mDbMachine.delete(initialMachine);

                    getActivity().onBackPressed();
                });

                dialogBuilder.setNegativeButton(getResources().getText(R.string.global_no), (dialog, which) -> {
                    // Do nothing but close the dialog
                    dialog.dismiss();
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            } else {
                this.mDbMachine.updateMachine(newMachine);

                // Rename all the records with that machine and rename them
                DBRecord lDbRecord = new DBRecord(getContext());
                DBProfile mDbProfil = new DBProfile(getContext());
                Profile lProfile = mDbProfil.getProfile(machineProfilIdArg);

                List<Record> listRecords = lDbRecord.getAllRecordByMachineIdArray(lProfile, initialMachine.getId());
                for (Record record : listRecords) {
                    record.setExercise(lMachineName);
                    lDbRecord.updateRecord(record);
                }

                result = true;
            }
        } else {

            mDbMachine.updateMachine(newMachine);
            result = true;
        }

        if (result) {
            mMachine = newMachine;
        }

        return result;
    }
}

