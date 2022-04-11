package com.run4urlyfe.Database.cardio;

import com.run4urlyfe.Database.Profile;

import java.util.Date;

/* DataBase Object */
public class OldCardio {
    private final Date mDate;
    private final String mExercice;
    private final float mDistance;
    private final long mDuration;
    private final Profile mProfil;
    private long id;

    public OldCardio(Date pDate, String pExercice, float pDistance, long pDuration, Profile pProfil) {
        super();
        this.mDate = pDate;
        this.mExercice = pExercice;
        this.mDistance = pDistance;
        this.mDuration = pDuration;
        this.mProfil = pProfil;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return mDate;
    }

    public String getExercice() {
        return mExercice;
    }

    public float getDistance() {
        return mDistance;
    }

    public long getDuration() {
        return mDuration;
    }

    public Profile getProfil() {
        return mProfil;
    }
}
