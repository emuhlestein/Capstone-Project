package com.intelliviz.retirementhelper.ui.income;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelliviz.retirementhelper.R;
import com.intelliviz.retirementhelper.adapter.IncomeDetailsAdapter;
import com.intelliviz.retirementhelper.data.AgeData;
import com.intelliviz.retirementhelper.data.BenefitData;
import com.intelliviz.retirementhelper.data.IncomeDetails;
import com.intelliviz.retirementhelper.data.MilestoneData;
import com.intelliviz.retirementhelper.db.entity.SavingsIncomeEntity;
import com.intelliviz.retirementhelper.ui.RetirementDetailsActivity;
import com.intelliviz.retirementhelper.util.RetirementConstants;
import com.intelliviz.retirementhelper.util.SelectMilestoneDataListener;
import com.intelliviz.retirementhelper.util.SystemUtils;
import com.intelliviz.retirementhelper.viewmodel.SavingsIncomeDetailsViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.intelliviz.retirementhelper.util.RetirementConstants.EXTRA_INCOME_SOURCE_ID;
import static com.intelliviz.retirementhelper.util.RetirementConstants.INCOME_TYPE_SAVINGS;

public class SavingsIncomeDetailsActivity extends AppCompatActivity
        implements SelectMilestoneDataListener {

    private IncomeDetailsAdapter mAdapter;
    private List<IncomeDetails> mIncomeDetails;
    private SavingsIncomeDetailsViewModel mViewModel;
    private SavingsIncomeEntity mSIE;
    private long mId;
    private int mSavingsType;

    @BindView(R.id.income_source_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.appbar)
    android.support.design.widget.AppBarLayout mAppBarLayout;

    @BindView(R.id.name_text_view)
    TextView mNameTextView;

    @BindView(R.id.start_age_text_view)
    TextView mStartAgeTextView;

    @BindView(R.id.annual_interest_text_view)
    TextView mAnnualInterestTextView;

    @BindView(R.id.monthly_interest_text_view)
    TextView mMonthlyIncreaseTextView;

    @BindView(R.id.balance_text_view)
    TextView mBalanceTextView;

    @BindView(R.id.expanded_text_layout)
    LinearLayout mExpandedTextLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.editSavingsFAB)
    FloatingActionButton mEditSavingsFAB;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_income_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        mId = 0;
        if(intent != null) {
            mId = intent.getLongExtra(EXTRA_INCOME_SOURCE_ID, 0);
        }

        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.white));

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    mExpandedTextLayout.setVisibility(View.GONE);
                    mCollapsingToolbarLayout.setTitle(getApplicationName(SavingsIncomeDetailsActivity.this));
                } else {
                    isShow = false;
                    mExpandedTextLayout.setVisibility(View.VISIBLE);
                    mCollapsingToolbarLayout.setTitle("");
                }
            }
        });

        //mAppBarLayout.addOnOffsetChangedListener(new ScrollingHelper(mAppBarLayout.getTotalScrollRange(), this));

        mIncomeDetails = new ArrayList<>();
        mAdapter = new IncomeDetailsAdapter(this, mIncomeDetails);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // The FAB will pop up an activity to allow a new income source to be edited
        mEditSavingsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavingsIncomeDetailsActivity.this, SavingsIncomeEditActivity.class);
                intent.putExtra(RetirementConstants.EXTRA_INCOME_SOURCE_ID, mId);
                intent.putExtra(RetirementConstants.EXTRA_ACTIVITY_RESULT, RetirementConstants.ACTIVITY_RESULT);
                startActivityForResult(intent, RetirementConstants.ACTIVITY_RESULT);
            }
        });

        SavingsIncomeDetailsViewModel.Factory factory = new
                SavingsIncomeDetailsViewModel.Factory(getApplication(), mId);
        mViewModel = ViewModelProviders.of(this, factory).
                get(SavingsIncomeDetailsViewModel.class);

        mViewModel.getList().observe(this, new Observer<List<BenefitData>>() {
            @Override
            public void onChanged(@Nullable List<BenefitData> benefitDataList) {
                List<IncomeDetails> incomeDetails = new ArrayList<>();
                for(BenefitData benefitData : benefitDataList) {
                    AgeData age = benefitData.getAge();
                    String balance = SystemUtils.getFormattedCurrency(benefitData.getBalance());
                    String amount = SystemUtils.getFormattedCurrency(benefitData.getMonthlyAmount());
                    String line1 = age.toString() + "   " + amount + "  " + balance;

                    int status = benefitData.getBalanceState();
                    if(benefitData.isPenalty()) {
                        status = 0;
                    }
                    IncomeDetails incomeDetail = new IncomeDetails(line1, status, "");
                    incomeDetails.add(incomeDetail);
                }
                mAdapter.update(incomeDetails);
            }
        });

        mViewModel.get().observe(this, new Observer<SavingsIncomeEntity>() {
            @Override
            public void onChanged(@Nullable SavingsIncomeEntity tdie) {
                mSIE = tdie;
                updateUI();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.update();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if(requestCode == RetirementConstants.ACTIVITY_RESULT) {
            Bundle bundle = intent.getExtras();
            String name = bundle.getString(RetirementConstants.EXTRA_INCOME_SOURCE_NAME);
            AgeData startAge = bundle.getParcelable(RetirementConstants.EXTRA_INCOME_SOURCE_START_AGE);
            String balance = bundle.getString(RetirementConstants.EXTRA_INCOME_SOURCE_BALANCE);
            String interest = bundle.getString(RetirementConstants.EXTRA_INCOME_SOURCE_INTEREST);
            String monthlyAddition = bundle.getString(RetirementConstants.EXTRA_INCOME_SOURCE_INCREASE);
            int withdrawMode = bundle.getInt(RetirementConstants.EXTRA_WITHDRAW_MODE);
            String withdrawAmount = bundle.getString(RetirementConstants.EXTRA_WITHDRAW_MODE_AMOUNT);
            String annualPercentIncrease = bundle.getString(RetirementConstants.EXTRA_ANNUAL_PERCENT_INCREASE);

            SavingsIncomeEntity tdid = new SavingsIncomeEntity(mId, INCOME_TYPE_SAVINGS, name,
                    balance, interest, monthlyAddition, startAge, withdrawMode, withdrawAmount, annualPercentIncrease);
            mViewModel.setData(tdid);

        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void updateUI() {
        if(mSIE == null) {
            return;
        }

        SystemUtils.setToolbarSubtitle(this, "401(k) - " + mSIE.getName());

        mNameTextView.setText(mSIE.getName());

        AgeData age = mSIE.getStartAge();
        mStartAgeTextView.setText(age.toString());

        String formattedValue = SystemUtils.getFormattedCurrency(mSIE.getMonthlyAddition());
        mMonthlyIncreaseTextView.setText(formattedValue);

        formattedValue = mSIE.getInterest() + "%";
        mAnnualInterestTextView.setText(formattedValue);

        formattedValue = SystemUtils.getFormattedCurrency(mSIE.getBalance());
        mBalanceTextView.setText(formattedValue);
    }

    @Override
    public void onSelectMilestone(MilestoneData msd) {
        Intent intent = new Intent(this, RetirementDetailsActivity.class);
        intent.putExtra("milestone", msd);
        startActivity(intent);
    }

    public String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}