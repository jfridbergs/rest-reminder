package com.colormindapps.rest_reminder_alarm;


import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;

public class PeriodListAdapter extends RecyclerView.Adapter<PeriodListAdapter.PeriodViewHolder> {
    private Context mContext;
    class PeriodViewHolder extends RecyclerView.ViewHolder  {
        private final TextView periodTimeView, periodExtendedView, periodEndedView;
        private final RelativeLayout periodLayout;

        private PeriodViewHolder(View itemView){
            super(itemView);
            periodTimeView = itemView.findViewById(R.id.period_time);
            periodExtendedView = itemView.findViewById(R.id.period_extended);
            periodEndedView = itemView.findViewById(R.id.period_ended);
            periodLayout = itemView.findViewById(R.id.period_layout);


        }

    }

    private final LayoutInflater mInflater;
    private List<Period> mPeriods;

    PeriodListAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public PeriodViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = mInflater.inflate(R.layout.recyclerview_periods, parent, false);
        return new PeriodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PeriodViewHolder holder, int position){
        if(mPeriods != null){

            Period current = mPeriods.get(position);
            int periodType = current.getType();
            int extendCount = current.getExtendCount();
            int periodEnded = current.getEnded();
            switch(periodType){
                case 1:{
                    holder.periodLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.work));
                    break;
                }
                case 2: {
                    holder.periodLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.rest));
                    break;
                }
                case 3:{
                    if(extendCount>=3){
                        holder.periodLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.red));
                    } else {
                        holder.periodLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.work));
                    }
                    break;
                }
                case 4:{
                    if(extendCount>=3){
                        holder.periodLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.rest));
                    } else {
                        holder.periodLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.work));
                    }
                    break;
                }
                default:
                    break;

            }
            String endTime = RReminder.getTimeString(mContext.getApplicationContext(), current.getEndTime()).toString();

            String time = endTime;
            holder.periodTimeView.setText(time);

            if(extendCount>0){
                holder.periodExtendedView.setText("ext: "+extendCount);
            } else {
                holder.periodExtendedView.setText("ext: -");
            }

            if(periodEnded==1){
                holder.periodEndedView.setText("forced to end");
            } else {
                holder.periodEndedView.setText("full length");
            }

        } else {
            holder.periodTimeView.setText("no time");
            holder.periodExtendedView.setText("no value");
            holder.periodEndedView.setText("No boolean");
        }
    }

    void setPeriods(List<Period> periods){
        mPeriods = periods;
        notifyDataSetChanged();

    }





    @Override
    public int getItemCount(){
        if(mPeriods !=null){
            return mPeriods.size();
        } else { return 0;}
    }
}
