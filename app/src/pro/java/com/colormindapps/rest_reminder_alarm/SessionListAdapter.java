package com.colormindapps.rest_reminder_alarm;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;

public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.SessionViewHolder> {
    private OnSessionListener onSessionListener;
    private Context mContext;
    class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView sessionDateView, sessionTimeView;
        OnSessionListener onSessionListener;

        private SessionViewHolder(View itemView, OnSessionListener onSessionListener){
            super(itemView);
            sessionDateView = itemView.findViewById(R.id.date);
            sessionTimeView = itemView.findViewById(R.id.time_from_to);
            this.onSessionListener = onSessionListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Period current = mSessions.get(getAdapterPosition());
            long sessionStartTime = current.getStartTime();
            onSessionListener.onSessionClick(sessionStartTime);
        }
    }

    private final LayoutInflater mInflater;
    private List<Period> mSessions;
    private long[] mEndTimeValues;

    SessionListAdapter(Context context, OnSessionListener onSessionListener){
        this.onSessionListener = onSessionListener;
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new SessionViewHolder(itemView, onSessionListener);
    }

    @Override
    public void onBindViewHolder(SessionViewHolder holder, int position){
        if(mSessions != null){
            Period current = mSessions.get(position);
            String date = RReminder.getSessionDateString(current.getStartTime());
            String sessionStartTime = RReminder.getTimeString(mContext.getApplicationContext(), current.getStartTime()).toString();
            String sessionEndTime = RReminder.getTimeString(mContext.getApplicationContext(), mEndTimeValues[position]).toString();
            String time = sessionStartTime +" - "+ sessionEndTime;
            holder.sessionDateView.setText(date);
            holder.sessionTimeView.setText(time);
        } else {
            holder.sessionDateView.setText("No word");
            holder.sessionTimeView.setText("No time");
        }
    }

    void setSessions(List<Period> periods){
        mSessions = periods;
        notifyDataSetChanged();
    }

    void setEndTimeValues(long[] endTimes){
        mEndTimeValues = endTimes;
    }

    @Override
    public int getItemCount(){
        if(mSessions !=null){
            return mSessions.size();
        } else { return 0;}
    }
}
