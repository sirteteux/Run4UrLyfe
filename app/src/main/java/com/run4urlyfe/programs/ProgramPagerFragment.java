package com.run4urlyfe.programs;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.run4urlyfe.Database.program.DBProgram;
import com.run4urlyfe.Database.program.DBProgramHistory;
import com.run4urlyfe.Database.program.ProgramHistory;
import com.run4urlyfe.Database.record.DBRecord;
import com.run4urlyfe.Database.record.Record;
import com.run4urlyfe.MainActivity;
import com.run4urlyfe.R;
import com.run4urlyfe.enums.DisplayType;
import com.run4urlyfe.source.SourcesFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.List;

public class ProgramPagerFragment extends Fragment {
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;

    private long mTemplateId;
    private final View.OnClickListener onClickToolbarItem = v -> {
        // Handle presses on the action bar items
        if (v.getId() == R.id.deleteButton) {
            deleteProgram();
        } else {
            getActivity().onBackPressed();
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramPagerFragment newInstance(long templateId) {
        ProgramPagerFragment f = new ProgramPagerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("displayType", DisplayType.PROGRAM_EDIT_DISPLAY.ordinal());
        args.putLong("templateId", templateId);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.program_pager, container, false);

        // Locate the viewpager in activity_main.xml
        mViewPager = view.findViewById(R.id.program_viewpager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();

            mTemplateId = args.getLong("templateId");

            pagerAdapter = new FragmentPagerItemAdapter(
                    getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                    .add("Info", ProgramInfoFragment.class, args)
                    .add("Editor", SourcesFragment.class, args)
                    .create());

            mViewPager.setAdapter(pagerAdapter);

            SmartTabLayout viewPagerTab = view.findViewById(R.id.program_pagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Fragment frag = pagerAdapter.getPage(position);
                    if (frag != null)
                        frag.onHiddenChanged(false); // Refresh data
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);
        Toolbar top_toolbar = view.findViewById(R.id.toolbar);
        top_toolbar.setNavigationIcon(R.drawable.ic_back);
        top_toolbar.setNavigationOnClickListener(onClickToolbarItem);

        ImageButton deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);

        // Inflate the layout for this fragment
        return view;
    }

    private void deleteProgram() {
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage("Do you want to delete this program?");

        // If yes, delete also all associated templates records
        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            // The databases needed
            DBProgram dB = new DBProgram(getContext());
            DBProgramHistory dbProgramHistory = new DBProgramHistory(getContext());
            // Suppress the machine
            dB.delete(mTemplateId);
            // Suppress the associated Sources records
            deleteRecordsAssociatedToTemplate();
            // Delete all program history.
            List<ProgramHistory> lProgramHistories = dbProgramHistory.getAll();
            for (ProgramHistory history : lProgramHistories) {
                if (history.getProgramId() == mTemplateId) {
                    dbProgramHistory.delete(history);
                }
            }

            getActivity().onBackPressed();
        });

        deleteDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), (dialog, which) -> {
            // Do nothing
            dialog.dismiss();
        });

        AlertDialog deleteDialog = deleteDialogBuilder.create();
        deleteDialog.show();
    }

    private void deleteRecordsAssociatedToTemplate() {
        DBRecord mDbRecord = new DBRecord(getContext());
        List<Record> listRecords = mDbRecord.getAllTemplateRecordByProgramArray(mTemplateId);
        for (Record record : listRecords) {
            mDbRecord.deleteRecord(record.getId());
        }
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) getView().findViewById(R.id.program_viewpager)).getAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
        }
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {

            if (getViewPagerAdapter() != null) {

                Fragment frag1;
                for (int i = 0; i < 3; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }
}
