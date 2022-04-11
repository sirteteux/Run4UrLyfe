    package com.run4urlyfe.source;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.run4urlyfe.BtnClickListener;
import com.run4urlyfe.Database.Profile;
import com.run4urlyfe.Database.program.DBProgram;
import com.run4urlyfe.Database.program.Program;
import com.run4urlyfe.Database.record.DBRecord;
import com.run4urlyfe.Database.record.Record;
import com.run4urlyfe.MainActivity;
import com.run4urlyfe.R;
import com.run4urlyfe.RecordEditorDialogbox;
import com.run4urlyfe.enums.DisplayType;
import com.run4urlyfe.enums.DistanceUnit;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.enums.ProgramRecordStatus;
import com.run4urlyfe.enums.WeightUnit;
import com.run4urlyfe.utils.DateConverter;
import com.run4urlyfe.utils.Keyboard;
import com.run4urlyfe.utils.OnCustomEventListener;
import com.run4urlyfe.utils.UnitConverter;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RecordArrayAdapter extends ArrayAdapter {

    private final DBProgram mDbWorkout;
    private final Activity mActivity;
    private final int mFirstColorOdd = 0;
    private final Context mContext;
    private final DisplayType mDisplayType;
    private final DBRecord mDbRecord;
    List<Record> mRecordList;
    private LayoutInflater mInflater;
    private BtnClickListener mAction2ClickListener = null;
    private OnCustomEventListener mProgramCompletedListener;

    public RecordArrayAdapter(Activity activity, Context context, List<Record> objects, DisplayType displayType, BtnClickListener clickAction2) {
        super(context, R.layout.row_source, objects);
        mActivity = activity;
        mContext = context;
        //mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDisplayType = displayType;
        mRecordList = objects;
        mAction2ClickListener = clickAction2;
        mDbRecord = new DBRecord(context);
        mDbWorkout = new DBProgram(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Record record = mRecordList.get(position);
        ViewHolder viewHolder;

        if (view == null) {

            // inflate the layout
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.row_source, null);

            viewHolder = new ViewHolder();
            viewHolder.ExerciseName = view.findViewById(R.id.MACHINE_CELL);
            viewHolder.CardView = view.findViewById(R.id.CARDVIEW);

            viewHolder.Separator = view.findViewById(R.id.SEPARATOR_CELL);
            viewHolder.Date = view.findViewById(R.id.DATE_CELL);
            viewHolder.Time = view.findViewById(R.id.TIME_CELL);
            viewHolder.FirstColValue = view.findViewById(R.id.SERIE_CELL);
            viewHolder.FirstColLabel = view.findViewById(R.id.SERIE_LABEL);
            viewHolder.SecondColValue = view.findViewById(R.id.REPETITION_CELL);
            viewHolder.SecondColLabel = view.findViewById(R.id.REP_LABEL);
            viewHolder.ThirdColValue = view.findViewById(R.id.weights_CELL);
            viewHolder.ThirdColLabel = view.findViewById(R.id.WEIGHT_LABEL);
            viewHolder.BtActionDelete = view.findViewById(R.id.deleteButton);
            viewHolder.BtActionMoveUp = view.findViewById(R.id.moveUpButton);
            viewHolder.BtActionMoveDown = view.findViewById(R.id.moveDownButton);
            viewHolder.BtActionSuccess = view.findViewById(R.id.successButton);
            viewHolder.BtActionFailed = view.findViewById(R.id.failedButton);
            viewHolder.BtActionEdit = view.findViewById(R.id.editButton);
            viewHolder.BtActionCopy = view.findViewById(R.id.copyButton);

            viewHolder.SecondColumn = view.findViewById(R.id.second_column);
            viewHolder.TemplateName = view.findViewById(R.id.TEMPLATE_NAME_CELL);
            viewHolder.TemplateFirstColLabel = view.findViewById(R.id.TEMPLATE_SERIE_CELL);
            viewHolder.TemplateSecondColLabel = view.findViewById(R.id.TEMPLATE_REPETITION_CELL);
            viewHolder.TemplateThirdColValue = view.findViewById(R.id.TEMPLATE_weights_CELL);

            viewHolder.RestTimeCardView = view.findViewById(R.id.restTimeCardView);
            viewHolder.RestTimeTextView = view.findViewById(R.id.restTimeTextView);

            UpdateDisplayTypeUI(viewHolder);

            // store the holder with the view.
            view.setTag(viewHolder);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolder) view.getTag();
        }

        if (position % 2 == mFirstColorOdd) {
            viewHolder.CardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_odd));
            viewHolder.RestTimeCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_odd));
        } else {
            viewHolder.CardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_even));
            viewHolder.RestTimeCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_even));
        }

        UpdateRecordTypeUI(record, viewHolder);
        UpdateValues(record, position, viewHolder);

        return view;
    }

    private void UpdateValues(Record record, int position, ViewHolder viewHolder) {
        ExerciseType exerciseType = record.getExerciseType();

        viewHolder.BtActionDelete.setTag(record.getId());
        viewHolder.BtActionDelete.setOnClickListener(v -> showDeleteDialog(record));

        viewHolder.BtActionEdit.setTag(record.getId());
        viewHolder.BtActionEdit.setOnClickListener(v -> showEditorDialog(record, position, viewHolder));

        if (record.getProgramRecordStatus() == ProgramRecordStatus.PENDING) {
            viewHolder.FirstColValue.setText("-");
            viewHolder.SecondColValue.setText("-");
            viewHolder.ThirdColValue.setText("-");
        } else {
            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getReps()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getWeight(), record.getWeightUnit()));
            } else if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getSeconds()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getWeight(), record.getWeightUnit()));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(record.getDistance(), record.getDistanceUnit()));
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(record.getDuration()));
            }
        }

        if (record.getTemplateRecordId() != -1) {
            // get program name
            Program program = mDbWorkout.get(record.getTemplateId());
            Record templateRecord = mDbRecord.getRecord(record.getTemplateRecordId());
            if (program != null) {
                showTemplateRow(View.VISIBLE, viewHolder);
                viewHolder.TemplateName.setText(program.getName());
                if (templateRecord != null) {
                    if (exerciseType == ExerciseType.STRENGTH) {
                        viewHolder.TemplateFirstColLabel.setText(String.valueOf(templateRecord.getSets()));
                        viewHolder.TemplateSecondColLabel.setText(String.valueOf(templateRecord.getReps()));
                        viewHolder.TemplateThirdColValue.setText(weigthToString(templateRecord.getWeight(), templateRecord.getWeightUnit()));
                    } else if (exerciseType == ExerciseType.ISOMETRIC) {
                        viewHolder.TemplateFirstColLabel.setText(String.valueOf(templateRecord.getSets()));
                        viewHolder.TemplateSecondColLabel.setText(String.valueOf(templateRecord.getSeconds()));
                        viewHolder.TemplateThirdColValue.setText(weigthToString(templateRecord.getWeight(), templateRecord.getWeightUnit()));
                    } else if (exerciseType == ExerciseType.CARDIO) {
                        viewHolder.TemplateFirstColLabel.setText(distanceToString(templateRecord.getDistance(), templateRecord.getDistanceUnit()));
                        viewHolder.TemplateThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(templateRecord.getDuration()));
                    }
                }
            }
        } else {
            showTemplateRow(View.GONE, viewHolder);
        }

        if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY || mDisplayType == DisplayType.HISTORY_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());
            viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
            viewHolder.Time.setText(DateConverter.dateToLocalTimeStr(record.getDate(), mContext));

            if (isSeparatorNeeded(position, record.getDate())) {
                viewHolder.Separator.setText("- " + DateConverter.dateToLocalDateStr(record.getDate(), mContext) + " -");
                viewHolder.Separator.setVisibility(View.VISIBLE);
            } else {
                viewHolder.Separator.setText("");
                viewHolder.Separator.setVisibility(View.GONE);
            }

            if (mDisplayType == DisplayType.HISTORY_DISPLAY) {
                viewHolder.BtActionCopy.setVisibility(View.GONE);
            } else {
                viewHolder.BtActionCopy.setTag(record.getId());
                viewHolder.BtActionCopy.setOnClickListener(v -> {
                    if (mAction2ClickListener != null)
                        mAction2ClickListener.onBtnClick(v);
                });
            }

        } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());

            viewHolder.Separator.setVisibility(View.GONE);

            if (record.getRestTime() != 0) {
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                viewHolder.RestTimeTextView.setText(getContext().getString(R.string.rest_time_row) + record.getRestTime() + getContext().getString(R.string.sec));
            } else {
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                viewHolder.RestTimeTextView.setText("No Rest");
            }

            viewHolder.BtActionMoveDown.setTag(record.getId());
            viewHolder.BtActionMoveDown.setOnClickListener(v -> {
                // Go DOWN
                int oldIndex = mRecordList.indexOf(record);
                if (oldIndex == mRecordList.size() - 1) return;

                Collections.swap(mRecordList, oldIndex + 1, oldIndex);
                Record record1 = mRecordList.get(oldIndex + 1);
                record1.setTemplateOrder(mRecordList.indexOf(record1));
                mDbRecord.updateRecord(record1);
                Record record2 = mRecordList.get(oldIndex);
                record2.setTemplateOrder(mRecordList.indexOf(record2));
                mDbRecord.updateRecord(record2);
                notifyDataSetChanged();
            });

            viewHolder.BtActionMoveUp.setTag(record.getId());
            viewHolder.BtActionMoveUp.setOnClickListener(v -> {
                // Go UP
                int oldIndex = mRecordList.indexOf(record);
                if (oldIndex == 0) return;

                Collections.swap(mRecordList, oldIndex - 1, oldIndex);
                Record record1 = mRecordList.get(oldIndex - 1);
                record1.setTemplateOrder(mRecordList.indexOf(record1));
                mDbRecord.updateRecord(record1);
                Record record2 = mRecordList.get(oldIndex);
                record2.setTemplateOrder(mRecordList.indexOf(record2));
                mDbRecord.updateRecord(record2);
                notifyDataSetChanged();
            });
        } else if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY || mDisplayType == DisplayType.PROGRAM_PREVIEW_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());
            viewHolder.Separator.setVisibility(View.GONE);

            if (record.getRestTime() != 0) {
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                viewHolder.RestTimeTextView.setText(getContext().getString(R.string.rest_time_row) + record.getRestTime() + getContext().getString(R.string.sec));
            } else {
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                viewHolder.RestTimeTextView.setText("No Rest");
            }

            if (mDisplayType == DisplayType.PROGRAM_PREVIEW_DISPLAY) {
                viewHolder.Date.setText("");
                viewHolder.Time.setText("");
            } else {
                if (record.getProgramRecordStatus() == ProgramRecordStatus.SUCCESS || record.getProgramRecordStatus() == ProgramRecordStatus.FAILED) {
                    viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
                    viewHolder.Time.setText(DateConverter.dateToLocalTimeStr(record.getDate(), mContext));
                }
            }

            if (record.getProgramRecordStatus() == ProgramRecordStatus.PENDING) {
                viewHolder.BtActionSuccess.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_inactive));
                viewHolder.BtActionSuccess.setBackgroundColor(Color.TRANSPARENT);
                viewHolder.BtActionFailed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_inactive));
                viewHolder.BtActionFailed.setBackgroundColor(Color.TRANSPARENT);
            } else if (record.getProgramRecordStatus() == ProgramRecordStatus.SUCCESS) {
                viewHolder.BtActionSuccess.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_active));
                viewHolder.BtActionSuccess.setBackgroundColor(Color.parseColor("#00AF80"));
                viewHolder.BtActionFailed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_inactive));
                viewHolder.BtActionFailed.setBackgroundColor(Color.TRANSPARENT);
            } else {
                viewHolder.BtActionSuccess.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_inactive));
                viewHolder.BtActionSuccess.setBackgroundColor(Color.TRANSPARENT);
                viewHolder.BtActionFailed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_active));
                viewHolder.BtActionFailed.setBackgroundColor(Color.RED);
            }


            long key = record.getId();

            Record templateRecord = mDbRecord.getRecord(record.getTemplateRecordId());

            viewHolder.BtActionSuccess.setTag(key);
            viewHolder.BtActionSuccess.setOnClickListener(v -> {
                if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY) {
                    if (record.getProgramRecordStatus() != ProgramRecordStatus.SUCCESS) {
                        record.setSets(templateRecord.getSets());
                        record.setReps(templateRecord.getReps());
                        record.setWeight(templateRecord.getWeight());
                        record.setWeightUnit(templateRecord.getWeightUnit());
                        record.setSeconds(templateRecord.getSeconds());
                        record.setDistance(templateRecord.getDistance());
                        record.setDistanceUnit(templateRecord.getDistanceUnit());
                        record.setDuration(templateRecord.getDuration());
                        record.setProgramRecordStatus(ProgramRecordStatus.SUCCESS);
                        record.setDate(DateConverter.getNewDate());
                        mDbRecord.updateRecord(record);
                        UpdateRecordTypeUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                        boolean programComplete = true;
                        for (Record rec : mRecordList) {
                            if (rec.getProgramRecordStatus() != ProgramRecordStatus.FAILED && rec.getProgramRecordStatus() != ProgramRecordStatus.SUCCESS) {
                                programComplete = false;
                                break;
                            }
                        }
                        if (programComplete) {
                            if (mProgramCompletedListener != null)
                                mProgramCompletedListener.onEvent("");
                        }
                    } else {
                        record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                        mDbRecord.updateRecord(record);
                        UpdateRecordTypeUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                    }
                } else {
                    KToast.errorToast(mActivity, mActivity.getString(R.string.please_start_program_first), Gravity.BOTTOM, KToast.LENGTH_AUTO);
                }
            });

            viewHolder.BtActionFailed.setTag(key);
            viewHolder.BtActionFailed.setOnClickListener(v -> {
                if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY) {
                    if (record.getProgramRecordStatus() != ProgramRecordStatus.FAILED) {
                        //Display Editor
                        record.setDate(DateConverter.getNewDate());
                        record.setProgramRecordStatus(ProgramRecordStatus.FAILED);
                        mDbRecord.updateRecord(record);
                        UpdateValues(record, position, viewHolder);
                        showEditorDialog(record, position, viewHolder);
                        boolean programComplete = true;
                        for (Record rec : mRecordList) {
                            if (rec.getProgramRecordStatus() != ProgramRecordStatus.FAILED && rec.getProgramRecordStatus() != ProgramRecordStatus.SUCCESS) {
                                programComplete = false;
                                break;
                            }
                        }
                        if (programComplete) {
                            if (mProgramCompletedListener != null)
                                mProgramCompletedListener.onEvent("");
                        }
                    } else {
                        record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                        mDbRecord.updateRecord(record);
                        UpdateRecordTypeUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                    }
                } else {
                    KToast.errorToast(mActivity, mActivity.getString(R.string.please_start_program_first), Gravity.BOTTOM, KToast.LENGTH_AUTO);
                }
            });
        }
    }

    private void showTemplateRow(int visibility, ViewHolder viewHolder) {
        viewHolder.TemplateName.setVisibility(visibility);
        viewHolder.TemplateFirstColLabel.setVisibility(visibility);
        viewHolder.TemplateSecondColLabel.setVisibility(visibility);
        viewHolder.TemplateThirdColValue.setVisibility(visibility);
    }

    private void showEditorDialog(Record record, int position, ViewHolder viewHolder) {
        RecordEditorDialogbox recordEditorDialogbox = new RecordEditorDialogbox(mActivity, record, mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY);
        recordEditorDialogbox.setOnCancelListener(dialog -> {
            if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY) {
                record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                mDbRecord.updateRecord(record);
                UpdateRecordTypeUI(record, viewHolder);
                UpdateValues(record, position, viewHolder);
                notifyDataSetChanged();
            }
            Keyboard.hide(getContext(), viewHolder.CardView);
        });
        recordEditorDialogbox.setOnDismissListener(dialog -> {
            if (!recordEditorDialogbox.isCancelled()) {
                notifyDataSetChanged();
                Keyboard.hide(getContext(), viewHolder.CardView);
            }
        });
        recordEditorDialogbox.show();
    }

    private void UpdateRecordTypeUI(Record record, ViewHolder viewHolder) {
        ExerciseType pExerciseType = record.getExerciseType();

        switch (pExerciseType) {
            case CARDIO:
                viewHolder.SecondColumn.setVisibility(View.GONE);
                viewHolder.FirstColLabel.setText(mContext.getString(R.string.DistanceLabel));
                viewHolder.ThirdColLabel.setText(mContext.getString(R.string.DurationLabel));
                break;
            case STRENGTH:
                viewHolder.SecondColumn.setVisibility(View.VISIBLE);
                viewHolder.FirstColLabel.setText(mContext.getString(R.string.SerieLabel));
                viewHolder.SecondColLabel.setText(mContext.getString(R.string.RepetitionLabel_short));
                viewHolder.ThirdColLabel.setText(mContext.getString(R.string.weightsLabel));
                break;
            case ISOMETRIC:
                viewHolder.SecondColumn.setVisibility(View.VISIBLE);
                viewHolder.FirstColLabel.setText(mContext.getString(R.string.SerieLabel));
                viewHolder.SecondColLabel.setText(mContext.getString(R.string.SecondsLabel_short));
                viewHolder.ThirdColLabel.setText(mContext.getString(R.string.weightsLabel));
                break;
        }
    }

    private void UpdateDisplayTypeUI(ViewHolder viewHolder) {
        switch (mDisplayType) {
            case FREE_WORKOUT_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.VISIBLE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                break;
            case HISTORY_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                break;
            case PROGRAM_EDIT_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.VISIBLE);
                viewHolder.BtActionMoveUp.setVisibility(View.VISIBLE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                break;
            case PROGRAM_PREVIEW_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.GONE);
                viewHolder.BtActionDelete.setVisibility(View.GONE);
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                break;
            case PROGRAM_RUNNING_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.VISIBLE);
                viewHolder.BtActionFailed.setVisibility(View.VISIBLE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String weigthToString(float weight, WeightUnit unit) {
        weight = UnitConverter.weightConverter(weight, WeightUnit.KG, unit);
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(weight) + unit.toString();
    }

    private String distanceToString(float distance, DistanceUnit unit) {
        String defaultUnit = mContext.getString(R.string.KmUnitLabel);
        if (unit == DistanceUnit.MILES) {
            distance = UnitConverter.KmToMiles(distance); // Always convert to KM
            defaultUnit = mContext.getString(R.string.MilesUnitLabel);
        }
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(distance) + defaultUnit;
    }

    private boolean isSeparatorNeeded(int position, Date date) {
        // Add separator if needed
        if (position == 0) {
            return true;
        } else {
            Record record = mRecordList.get(position - 1);
            Date datePrevious = record.getDate();
            String dateString = DateConverter.dateTimeToDBDateStr(date);
            String datePreviousString = DateConverter.dateTimeToDBDateStr(datePrevious);
            return !datePreviousString.equals(dateString);
        }
    }

    public void setRecords(List<Record> data) {
        mRecordList.clear();
        mRecordList.addAll(data);
        notifyDataSetChanged();
    }

    public DisplayType getDisplayType() {
        return mDisplayType;
    }

    private void showDeleteDialog(final Record record) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getContext().getString(R.string.DeleteRecordDialog))
                .setContentText(getContext().getString(R.string.areyousure))
                .setCancelText(getContext().getString(R.string.global_no))
                .setConfirmText(getContext().getString(R.string.global_yes))
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    int ret = mDbRecord.deleteRecord(record.getId());
                    if (ret != 0) mRecordList.remove(record);
                    notifyDataSetChanged();

                    KToast.infoToast(mActivity, getContext().getString(R.string.removedid), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    sDialog.dismissWithAnimation();
                })
                .show();
    }

    public void setOnProgramCompletedListener(OnCustomEventListener eventListener) {
        mProgramCompletedListener = eventListener;
    }

    private Profile getProfile() {
        return ((MainActivity) mActivity).getCurrentProfile();
    }

    // View lookup cache
    private static class ViewHolder {

        CardView CardView;
        TextView Separator;
        TextView ExerciseName;
        TextView Date;
        TextView Time;
        TextView FirstColValue;
        TextView FirstColLabel;
        LinearLayout SecondColumn;
        TextView SecondColValue;
        TextView SecondColLabel;
        TextView ThirdColValue;
        TextView ThirdColLabel;

        TextView TemplateName;
        TextView TemplateFirstColLabel;
        TextView TemplateSecondColLabel;
        TextView TemplateThirdColValue;

        ImageView BtActionDelete;
        ImageView BtActionEdit;
        ImageView BtActionCopy;
        ImageView BtActionMoveUp;
        ImageView BtActionMoveDown;
        ImageView BtActionFailed;
        ImageView BtActionSuccess;

        CardView RestTimeCardView;
        TextView RestTimeTextView;
    }
}
