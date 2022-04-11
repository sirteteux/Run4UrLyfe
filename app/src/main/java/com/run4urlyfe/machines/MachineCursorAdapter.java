package com.run4urlyfe.machines;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.run4urlyfe.Database.DBMachine;
import com.run4urlyfe.Database.Machine;
import com.run4urlyfe.R;
import com.run4urlyfe.enums.ExerciseType;
import com.run4urlyfe.utils.ImageUtil;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

public class MachineCursorAdapter extends CursorAdapter implements Filterable {

    private final LayoutInflater mInflater;
    DBMachine mDbMachine = null;
    MaterialFavoriteButton iFav = null;

    public MachineCursorAdapter(Context context, Cursor c, int flags, DBMachine pDbMachine) {
        super(context, c, flags);
        mDbMachine = pDbMachine;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView t0 = view.findViewById(R.id.LIST_MACHINE_ID);
        t0.setText(cursor.getString(cursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.KEY)));

        TextView t1 = view.findViewById(R.id.LIST_MACHINE_NAME);
        t1.setText(cursor.getString(cursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.NAME)));

        TextView t2 = view.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
        t2.setText(cursor.getString(cursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.DESCRIPTION)));

        ImageView i0 = view.findViewById(R.id.LIST_MACHINE_PHOTO);
        String lPath = cursor.getString(cursor.getColumnIndex(DBMachine.PICTURE));

        ExerciseType lType = ExerciseType.fromInteger(cursor.getInt(cursor.getColumnIndex(com.run4urlyfe.Database.DBMachine.TYPE)));

        if (lPath != null && !lPath.isEmpty()) {
            try {
                ImageUtil imgUtil = new ImageUtil();
                String lThumbPath = imgUtil.getThumbPath(lPath);
                ImageUtil.setThumb(i0, lThumbPath);
            } catch (Exception e) {
                if (lType == ExerciseType.STRENGTH) {
                    i0.setImageResource(R.drawable.ic_gym_bench_50dp);
                } else if (lType == ExerciseType.ISOMETRIC) {
                    i0.setImageResource(R.drawable.ic_static_50dp);
                } else {
                    i0.setImageResource(R.drawable.ic_training_50dp);
                    i0.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
                e.printStackTrace();
            }
        } else {
            if (lType == ExerciseType.STRENGTH) {
                i0.setImageResource(R.drawable.ic_gym_bench_50dp);
            } else if (lType == ExerciseType.ISOMETRIC) {
                i0.setImageResource(R.drawable.ic_static_50dp);
            } else {
                i0.setImageResource(R.drawable.ic_training_50dp);
            }

            i0.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        iFav = view.findViewById(R.id.LIST_MACHINE_FAVORITE);
        boolean bFav = cursor.getInt(6) == 1;
        iFav.setFavorite(bFav);
        iFav.setRotationDuration(500);
        iFav.setAnimateFavorite(true);
        iFav.setTag(cursor.getLong(0));

        iFav.setOnClickListener(v -> {
            MaterialFavoriteButton mFav = (MaterialFavoriteButton) v;
            boolean t = mFav.isFavorite();
            mFav.setFavoriteAnimated(!t);
            if (mDbMachine != null) {
                Machine m = mDbMachine.getMachine((long) mFav.getTag());
                m.setFavorite(!t);
                mDbMachine.updateMachine(m);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.exercise_list_row, parent, false);

    }

}
