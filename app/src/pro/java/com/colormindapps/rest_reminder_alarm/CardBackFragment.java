package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;

public class CardBackFragment extends Fragment {

    private PeriodViewModel mPeriodViewModel;
    long sessionStart, sessionEnd;
    OnFlipCardListener parentActivity;
    String debug = "RR_CARD_BACK_FRAGMENT";

    public static CardBackFragment newInstance(long sessionStart, long sessionEnd){
        CardBackFragment fragment = new CardBackFragment();
        Bundle args = new Bundle();
        args.putLong("session_start", sessionStart);
        args.putLong("session_end", sessionEnd);
        fragment.setArguments(args);
        return fragment;
    }

    private void setParentActivity(OnFlipCardListener activity){
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.session_details_list, container, false);

        Bundle data = getArguments();
        if(data!=null){
            sessionStart = data.getLong("session_start");
            sessionEnd = data.getLong("session_end");
        }


        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_periods);
        final PeriodListAdapter adapter = new PeriodListAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter.setPeriods(parentActivity.getPeriods());

        Button button = view.findViewById(R.id.flip_to_general);
        button.setOnClickListener(v -> parentActivity.flipCard());
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            setParentActivity((OnFlipCardListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFlipCardListener");
        }
    }
}
