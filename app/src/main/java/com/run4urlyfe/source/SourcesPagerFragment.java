package com.run4urlyfe.source;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.run4urlyfe.R;
import com.run4urlyfe.enums.DisplayType;
import com.run4urlyfe.programs.ProgramRunnerFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class SourcesPagerFragment extends Fragment {
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static SourcesPagerFragment newInstance(String name, int id) {
        SourcesPagerFragment f = new SourcesPagerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sources_pager, container, false);

        // Locate the viewpager in activity_main.xml
        mViewPager = view.findViewById(R.id.sources_viewpager);

        if (mViewPager.getAdapter() == null) {

            // Supply index input as an argument.
            Bundle freeWorkoutArgs = new Bundle();
            freeWorkoutArgs.putInt("displayType", DisplayType.FREE_WORKOUT_DISPLAY.ordinal());
            freeWorkoutArgs.putLong("templateId", -1);

            Bundle guidedWorkoutArgs = new Bundle();
            guidedWorkoutArgs.putInt("displayType", DisplayType.PROGRAM_RUNNING_DISPLAY.ordinal());
            guidedWorkoutArgs.putLong("templateId", -1);

            Bundle args = this.getArguments();
            args.putLong("machineID", -1); // if -1, then display the graph and history fragment with machine selectors
            args.putLong("machineProfile", -1);

            pagerAdapter = new FragmentPagerItemAdapter(
                    getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                    .add(R.string.free_workout, SourcesFragment.class, freeWorkoutArgs)
                    .add(R.string.program, ProgramRunnerFragment.class, guidedWorkoutArgs)
                    .add(R.string.GraphLabel, SourceGraphFragment.class, args)
                    .add(R.string.HistoryLabel, SourceHistoryFragment.class, args)
                    .create());

            mViewPager.setAdapter(pagerAdapter);

            SmartTabLayout viewPagerTab = view.findViewById(R.id.sources_pagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Fragment frag1 = pagerAdapter.getPage(position);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) getView().findViewById(R.id.sources_viewpager)).getAdapter();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {

            if (getViewPagerAdapter() != null) {

                Fragment frag1;
                for (int i = 0; i < 4; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }
}
