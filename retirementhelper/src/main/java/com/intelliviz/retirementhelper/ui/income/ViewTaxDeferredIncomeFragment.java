package com.intelliviz.retirementhelper.ui.income;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intelliviz.retirementhelper.R;
import com.intelliviz.retirementhelper.adapter.MilestoneAdapter;
import com.intelliviz.retirementhelper.ui.MilestoneDetailsDialog;
import com.intelliviz.retirementhelper.ui.PersonalInfoDialog;
import com.intelliviz.retirementhelper.ui.RetirementOptionsDialog;
import com.intelliviz.retirementhelper.util.BalanceData;
import com.intelliviz.retirementhelper.util.BenefitHelper;
import com.intelliviz.retirementhelper.util.DataBaseUtils;
import com.intelliviz.retirementhelper.util.MilestoneData;
import com.intelliviz.retirementhelper.util.PersonalInfoData;
import com.intelliviz.retirementhelper.util.RetirementConstants;
import com.intelliviz.retirementhelper.util.RetirementOptionsData;
import com.intelliviz.retirementhelper.util.SelectionMilestoneListener;
import com.intelliviz.retirementhelper.util.SystemUtils;
import com.intelliviz.retirementhelper.util.TaxDeferredIncomeData;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_INTENT;
import static com.intelliviz.retirementhelper.util.RetirementConstants.EXTRA_INCOME_DATA;
import static com.intelliviz.retirementhelper.util.RetirementConstants.REQUEST_PERSONAL_INFO;
import static com.intelliviz.retirementhelper.util.RetirementConstants.REQUEST_RETIRE_OPTIONS;

public class ViewTaxDeferredIncomeFragment extends Fragment implements SelectionMilestoneListener {
    public static final String VIEW_TAXDEF_INCOME_FRAG_TAG = "view taxdef income frag tag";
    private TaxDeferredIncomeData mTDID;
    private RetirementOptionsData mROD;
    private MilestoneAdapter mMilestoneAdapter;
    private List<MilestoneData> mMilestones;

    @Bind(R.id.name_text_view) TextView mIncomeSourceName;
    @Bind(R.id.annual_interest_text_view) TextView mAnnualInterest;
    @Bind(R.id.monthly_increase_text_view) TextView mMonthlyIncrease;
    @Bind(R.id.current_balance_text_view) TextView mCurrentBalance;
    @Bind(R.id.minimum_age_text_view) TextView mMinimumAge;
    @Bind(R.id.penalty_amount_text_view) TextView mPenaltyAmount;
    @Bind(R.id.recyclerview) RecyclerView mRecyclerView;
    @Bind(R.id.view_tax_defered_toolbar) Toolbar mToolbar;

    public ViewTaxDeferredIncomeFragment() {
        // Required empty public constructor
    }

    public static ViewTaxDeferredIncomeFragment newInstance(Intent intent) {
        ViewTaxDeferredIncomeFragment fragment = new ViewTaxDeferredIncomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_INTENT, intent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Intent intent = getArguments().getParcelable(EXTRA_INTENT);
            mTDID = intent.getParcelableExtra(EXTRA_INCOME_DATA);
            mROD = intent.getParcelableExtra(RetirementConstants.EXTRA_RETIREOPTIONS_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_tax_deferred_income, container, false);
        ButterKnife.bind(this, view);

        mMilestones = BenefitHelper.getMilestones(getContext(), mTDID, mROD);
        mMilestoneAdapter = new MilestoneAdapter(getContext(), mMilestones);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mMilestoneAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                linearLayoutManager.getOrientation()));
        mMilestoneAdapter.setOnSelectionMilestoneListener(this);

        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        updateUI();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.summary_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.retirement_options_item:
                intent = new Intent(getContext(), RetirementOptionsDialog.class);
                RetirementOptionsData rod = DataBaseUtils.getRetirementOptionsData(getContext());
                if (rod != null) {
                    intent.putExtra(RetirementConstants.EXTRA_RETIREOPTIONS_DATA, rod);
                }
                startActivityForResult(intent, REQUEST_RETIRE_OPTIONS);

                break;
            case R.id.personal_info_item:
                intent = new Intent(getContext(), PersonalInfoDialog.class);
                PersonalInfoData pid = DataBaseUtils.getPersonalInfoData(getContext());
                if (pid != null) {
                    intent.putExtra(RetirementConstants.EXTRA_PERSONALINFODATA, pid);
                }
                startActivityForResult(intent, REQUEST_PERSONAL_INFO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        if(mTDID == null) {
            return;
        }

        mIncomeSourceName.setText(mTDID.getName());
        String subTitle = SystemUtils.getIncomeSourceTypeString(getContext(), mTDID.getType());
        SystemUtils.setToolbarSubtitle((AppCompatActivity)getActivity(), subTitle);
        mAnnualInterest.setText(mTDID.getInterest()+"%");
        // TODO getMonthAddition vs getMonthlyIncrease in SID - spelling consistency
        mMonthlyIncrease.setText(SystemUtils.getFormattedCurrency(mTDID.getMonthAddition()));
        mMinimumAge.setText(mTDID.getMinimumAge());
        mPenaltyAmount.setText(mTDID.getPenalty() +"%");

        List<BalanceData> bd = mTDID.getBalanceDataList();
        String formattedAmount = "$0.00";
        if(bd != null && !bd.isEmpty()) {
            formattedAmount = SystemUtils.getFormattedCurrency(bd.get(0).getBalance());
        }

        mCurrentBalance.setText(String.valueOf(formattedAmount));
    }

    @Override
    public void onSelectMilestoneListener(MilestoneData msd) {
        Intent intent = new Intent(getContext(), MilestoneDetailsDialog.class);
        intent.putExtra(RetirementConstants.EXTRA_MILESTONEDATA, msd);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_RETIRE_OPTIONS:
                if (resultCode == RESULT_OK) {
                    RetirementOptionsData rod = intent.getParcelableExtra(RetirementConstants.EXTRA_RETIREOPTIONS_DATA);
                    DataBaseUtils.saveRetirementOptions(getContext(), rod);
                    mROD = rod;
                    List<MilestoneData> milestones = BenefitHelper.getMilestones(getContext(), mTDID, mROD);
                    mMilestoneAdapter.update(milestones);
                }
                break;
            case REQUEST_PERSONAL_INFO:
                if (resultCode == RESULT_OK) {
                    PersonalInfoData pid = intent.getParcelableExtra(RetirementConstants.EXTRA_PERSONALINFODATA);
                    DataBaseUtils.savePersonalInfo(getContext(), pid);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
        }
    }
}
