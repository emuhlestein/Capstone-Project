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
import com.intelliviz.retirementhelper.db.entity.PensionIncomeEntity;
import com.intelliviz.retirementhelper.util.RetirementConstants;
import com.intelliviz.retirementhelper.util.SystemUtils;
import com.intelliviz.retirementhelper.viewmodel.PensionIncomeDetailsViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.intelliviz.retirementhelper.util.RetirementConstants.EXTRA_INCOME_SOURCE_ID;
import static com.intelliviz.retirementhelper.util.RetirementConstants.INCOME_TYPE_PENSION;

public class PensionIncomeDetailsActivity extends AppCompatActivity {

    private IncomeDetailsAdapter mAdapter;
    private List<IncomeDetails> mIncomeDetails;
    private PensionIncomeDetailsViewModel mViewModel;
    private PensionIncomeEntity mPIE;
    private long mId;

    @BindView(R.id.income_source_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.appbar)
    android.support.design.widget.AppBarLayout mAppBarLayout;

    @BindView(R.id.name_text_view)
    TextView mNameTextView;

    @BindView(R.id.min_age_text_view)
    TextView mStartAgeTextView;

    @BindView(R.id.monthly_benefit_text_view)
    TextView mMonthlyBenefitTextView;

    @BindView(R.id.expanded_text_layout)
    LinearLayout mExpandedTextLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.editPensionFAB)
    FloatingActionButton mEditPensionFAB;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pension_income_details);
        ButterKnife.bind(this);

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
                    mCollapsingToolbarLayout.setTitle(getApplicationName(PensionIncomeDetailsActivity.this));
                } else {
                    isShow = false;
                    mExpandedTextLayout.setVisibility(View.VISIBLE);
                    mCollapsingToolbarLayout.setTitle("");
                }
            }
        });

        mIncomeDetails = new ArrayList<>();
        mAdapter = new IncomeDetailsAdapter(this, mIncomeDetails);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // The FAB will pop up an activity to allow a new income source to be edited
        mEditPensionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PensionIncomeDetailsActivity.this, PensionIncomeEditActivity.class);
                intent.putExtra(RetirementConstants.EXTRA_INCOME_SOURCE_ID, mId);
                intent.putExtra(RetirementConstants.EXTRA_ACTIVITY_RESULT, RetirementConstants.ACTIVITY_RESULT);
                startActivityForResult(intent, RetirementConstants.ACTIVITY_RESULT);
            }
        });


        // For adding dividing line between views
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
        //        linearLayoutManager.getOrientation()));
        PensionIncomeDetailsViewModel.Factory factory = new
                PensionIncomeDetailsViewModel.Factory(getApplication(), mId);
        mViewModel = ViewModelProviders.of(this, factory).
                get(PensionIncomeDetailsViewModel.class);

        mViewModel.getList().observe(this, new Observer<List<BenefitData>>() {
            @Override
            public void onChanged(@Nullable List<BenefitData> listBenefitData) {

                List<IncomeDetails> incomeDetails = new ArrayList<>();
                for(BenefitData benefitData : listBenefitData) {
                    AgeData age = benefitData.getAge();
                    String amount = SystemUtils.getFormattedCurrency(benefitData.getMonthlyAmount());
                    String line1 = age.toString() + "   " + amount;
                    IncomeDetails incomeDetail;
                    incomeDetail = new IncomeDetails(line1, benefitData.getBalanceState(), "");
                    incomeDetails.add(incomeDetail);
                }
                mAdapter.update(incomeDetails);
            }
        });

        mViewModel.get().observe(this, new Observer<PensionIncomeEntity>() {
            @Override
            public void onChanged(@Nullable PensionIncomeEntity pie) {
                mPIE = pie;
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
            AgeData minAge = bundle.getParcelable(RetirementConstants.EXTRA_INCOME_SOURCE_START_AGE);
            String monthlyBenefit = bundle.getString(RetirementConstants.EXTRA_INCOME_SOURCE_BENEFIT);

            PensionIncomeEntity pie = new PensionIncomeEntity(mId, INCOME_TYPE_PENSION, name,
                    minAge, monthlyBenefit);
            mViewModel.setData(pie);

        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void updateUI() {
        if(mPIE == null) {
            return;
        }

        SystemUtils.setToolbarSubtitle(this, "Pension - " + mPIE.getName());

        mNameTextView.setText(mPIE.getName());

        AgeData age = mPIE.getMinAge();
        mStartAgeTextView.setText(age.toString());

        String formattedValue = SystemUtils.getFormattedCurrency(mPIE.getMonthlyBenefit());
        mMonthlyBenefitTextView.setText(formattedValue);
    }

    public String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
