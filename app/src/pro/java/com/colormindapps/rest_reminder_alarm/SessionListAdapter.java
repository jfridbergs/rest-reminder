package com.colormindapps.rest_reminder_alarm;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;

public class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.SessionViewHolder> {
    private OnSessionListener onSessionListener;
    private Context mContext;
    class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView sessionDateView, sessionTimeView, sessionIdView;
        OnSessionListener onSessionListener;

        private SessionViewHolder(View itemView, OnSessionListener onSessionListener){
            super(itemView);
            sessionIdView = itemView.findViewById(R.id.session_id);
            sessionDateView = itemView.findViewById(R.id.date);
            sessionTimeView = itemView.findViewById(R.id.time_from_to);
            this.onSessionListener = onSessionListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Session current = mSessions.get(getAdapterPosition());
            int sessionId = current.getSessionId();
            long sessionStart = current.getSessionStart();
            long sessionEnd = current.getSessionEnd();
            onSessionListener.onSessionClick(sessionId, sessionStart, sessionEnd);
        }
    }

    private final LayoutInflater mInflater;
    private List<Session> mSessions;
    private int sessionId;

    SessionListAdapter(Context context, OnSessionListener onSessionListener){
        this.onSessionListener = onSessionListener;
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = mInflater.inflate(R.layout.recyclerview_sessions, parent, false);
        return new SessionViewHolder(itemView, onSessionListener);
    }

    @Override
    public void onBindViewHolder(SessionViewHolder holder, int position){
        if(mSessions != null){
            Session current = mSessions.get(position);
            String date = RReminder.getSessionDateString(0,current.getSessionStart());
            String sessionStartTime = RReminder.getTimeString(mContext.getApplicationContext(), current.getSessionStart()).toString();
            String sessionEndTime;
            sessionEndTime = RReminder.getTimeString(mContext.getApplicationContext(),current.getSessionEnd()).toString();
            String time = sessionStartTime +" - "+ sessionEndTime;
            String sessionId = "Session ID: " + current.getSessionId();
            holder.sessionIdView.setText(sessionId);
            holder.sessionDateView.setText(date);
            holder.sessionTimeView.setText(time);
        } else {
            holder.sessionIdView.setText("No ID");
            holder.sessionDateView.setText("No word");
            holder.sessionTimeView.setText("No time");
        }
    }

    void setSessions(List<Session> sessions){
        mSessions = sessions;
        notifyDataSetChanged();

    }






    @Override
    public int getItemCount(){
        if(mSessions !=null){
            return mSessions.size();
        } else { return 0;}
    }
}
