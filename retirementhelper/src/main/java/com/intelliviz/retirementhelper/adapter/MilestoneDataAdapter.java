package com.intelliviz.retirementhelper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelliviz.income.data.MilestoneData;
import com.intelliviz.income.util.SelectMilestoneDataListener;
import com.intelliviz.lowlevel.util.AgeUtils;
import com.intelliviz.lowlevel.util.SystemUtils;
import com.intelliviz.retirementhelper.R;

import java.util.List;

/**
 * @author Ed Muhlestein
 * Created on 8/5/2017.
 */

public class MilestoneDataAdapter extends RecyclerView.Adapter<MilestoneDataAdapter.MilestoneDataHolder> {
    private List<MilestoneData> mMilestones;
    private Context mContext;
    private SelectMilestoneDataListener mListener;

    public MilestoneDataAdapter(Context context, List<MilestoneData> milestones) {
        mContext = context;
        mMilestones = milestones;
    }

    @Override
    public MilestoneDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.milestone_item_layout, parent, false);
        return new MilestoneDataHolder(view);
    }

    @Override
    public void onBindViewHolder(MilestoneDataHolder holder, int position) {
        holder.bindMilestone(position);
    }

    @Override
    public int getItemCount() {
        if(mMilestones != null) {
            return mMilestones.size();
        } else {
            return 0;
        }
    }

    public void update(List<MilestoneData> milestones) {
        mMilestones.clear();
        mMilestones.addAll(milestones);
        notifyDataSetChanged();
    }

    public void setOnSelectionMilestoneDataListener (SelectMilestoneDataListener listener) {
        mListener = listener;
    }

    class MilestoneDataHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private TextView mMilestoneTextView;
        private TextView mMonthlyAmountTextView;
        private LinearLayout mLinearLayout;
        private MilestoneData mMSD;

        private MilestoneDataHolder(View itemView) {
            super(itemView);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.milestone_item_layout);
            mMilestoneTextView = (TextView) itemView.findViewById(R.id.milestone_text_view);
            mMonthlyAmountTextView = (TextView) itemView.findViewById(R.id.monthly_amount_text_view);
            itemView.setOnClickListener(this);
        }

        private void bindMilestone(int position) {
            mMSD = mMilestones.get(position);

            double monthlyBenefit = mMSD.getMonthlyBenefit();
            double endBalance = mMSD.getEndBalance();
            final int sdk = android.os.Build.VERSION.SDK_INT;
            double annualAmount = monthlyBenefit * 12;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                if(endBalance == 0) {
                    mLinearLayout.setBackground( mContext.getResources().getDrawable(R.drawable.red_ripple_effect) );
                } else {
                    if(endBalance < annualAmount) {
                        mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.yellow_ripple_effect));
                    } else {
                        mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.green_ripple_effect));
                    }
                }
            } else {
                if(endBalance == 0) {
                    mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.red_ripple_effect));
                } else {
                    if(endBalance < annualAmount) {
                        mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.yellow_ripple_effect));
                    } else {
                        mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.green_ripple_effect));
                    }
                }
            }

            double monthlyAmount = mMSD.getMonthlyBenefit();
            String formattedCurrency = SystemUtils.getFormattedCurrency(monthlyAmount);
            mMonthlyAmountTextView.setText(formattedCurrency);
            mMilestoneTextView.setText(AgeUtils.getFormattedAge(mMSD.getStartAge()));
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onSelectMilestone(mMSD);
            }
        }
    }
}
