package com.intelliviz.income.ui;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.intelliviz.data.SavingsData;
import com.intelliviz.income.R;
import com.intelliviz.income.data.SavingsViewData;
import com.intelliviz.income.viewmodel.SavingsIncomeViewModel;
import com.intelliviz.lowlevel.data.AgeData;
import com.intelliviz.lowlevel.ui.MessageDialog;
import com.intelliviz.lowlevel.ui.NewMessageDialog;
import com.intelliviz.lowlevel.util.RetirementConstants;
import com.intelliviz.lowlevel.util.SystemUtils;

import static com.intelliviz.income.util.uiUtils.getIncomeSourceTypeString;
import static com.intelliviz.lowlevel.util.RetirementConstants.EC_FOR_SELF_OR_SPOUSE;
import static com.intelliviz.lowlevel.util.RetirementConstants.EC_ONLY_ONE_SUPPORTED;
import static com.intelliviz.lowlevel.util.RetirementConstants.EC_ONLY_TWO_SUPPORTED;
import static com.intelliviz.lowlevel.util.RetirementConstants.EC_SPOUSE_INCLUDED;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_SOURCE_ID;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_TYPE;
import static com.intelliviz.lowlevel.util.RetirementConstants.OWNER_SELF;
import static com.intelliviz.lowlevel.util.RetirementConstants.OWNER_SPOUSE;

public class SavingsIncomeEditActivity extends AppCompatActivity implements
        AgeDialog.OnAgeEditListener, MessageDialog.DialogResponse, NewMessageDialog.DialogResponse {
    private static final int START_AGE = 0;
    private static final int STOP_AGE = 1;
    private SavingsData mSD;
    private long mId;
    private int mIncomeType;
    private int mAgeType = STOP_AGE;
    private SavingsIncomeViewModel mViewModel;
    private boolean mSpouseIncluded;

    private Toolbar mToolbar;
    private CoordinatorLayout mCoordinatorLayout;
    private EditText mIncomeSourceName;
    private EditText mBalance;
    private EditText mAnnualInterest;
    private TextView mStartAgeTextView;
    private TextView mInitWithdrawPercentTextView;
    private Button mEditStartAgeButton;
    private Button mAddIncomeSourceButton;
    private TextView mOwnerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_savings_income);

        mToolbar = findViewById(R.id.income_source_toolbar);

        mOwnerTextView = findViewById(R.id.owner_text);

        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mIncomeSourceName = findViewById(R.id.name_edit_text);
        mBalance = findViewById(R.id.balance_text);
        mAnnualInterest = findViewById(R.id.annual_interest_text);
        mStartAgeTextView = findViewById(R.id.start_age_text_view);
        mInitWithdrawPercentTextView = findViewById(R.id.withdraw_percent_edit_text);
        mEditStartAgeButton = findViewById(R.id.edit_start_age_button);
        mEditStartAgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AgeData startAge = mSD.getStartAge();
                FragmentManager fm = getSupportFragmentManager();
                AgeDialog dialog = AgeDialog.newInstance(""+startAge.getYear(), ""+startAge.getMonth());
                dialog.show(fm, "");
                mAgeType = START_AGE;
            }
        });
        mAddIncomeSourceButton = findViewById(R.id.add_income_source_button);
        mAddIncomeSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateIncomeSourceData();
            }
        });

        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        mId = 0;
        if(intent != null) {
            mId = intent.getLongExtra(EXTRA_INCOME_SOURCE_ID, 0);
            mIncomeType = intent.getIntExtra(EXTRA_INCOME_TYPE, 0);
        }

        mSD = null;

        mBalance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    TextView textView = (TextView)v;
                    String formattedString;
                    String str = textView.getText().toString();
                    String value = SystemUtils.getFloatValue(str);
                    formattedString = SystemUtils.getFormattedCurrency(value);
                    if(formattedString != null) {
                        mBalance.setText(formattedString);
                    }
                }
            }
        });

        mAnnualInterest.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    TextView textView = (TextView)v;
                    String interest = textView.getText().toString();
                    interest = SystemUtils.getFloatValue(interest);
                    if(interest != null) {
                        interest += "%";
                        mAnnualInterest.setText(interest);
                    } else {
                        mAnnualInterest.setText("");
                    }
                }
            }
        });

        mInitWithdrawPercentTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    TextView textView = (TextView)v;
                    String interest = textView.getText().toString();
                    interest = SystemUtils.getFloatValue(interest);
                    if(interest != null) {
                        interest += "%";
                        mInitWithdrawPercentTextView.setText(interest);
                    } else {
                        mInitWithdrawPercentTextView.setText("");
                    }
                }
            }
        });

        SavingsIncomeViewModel.Factory factory = new
                SavingsIncomeViewModel.Factory(getApplication(), mId, mIncomeType);
        mViewModel = ViewModelProviders.of(this, factory).
                get(SavingsIncomeViewModel.class);

        mViewModel.get().observe(this, new Observer<SavingsViewData>() {
            @Override
            public void onChanged(@Nullable SavingsViewData viewData) {
                FragmentManager fm;
                mSpouseIncluded = viewData.isSpouseIncluded();
                mSD = viewData.getSavingsData();
                switch(viewData.getStatus()) {
                    case EC_ONLY_TWO_SUPPORTED:
                        fm = getSupportFragmentManager();
                        Fragment fragment = fm.findFragmentByTag("message");
                        if(fragment != null) {
                            //return;
                        }
                        MessageDialog dialog = MessageDialog.newInstance("Warning", viewData.getMessage(), viewData.getStatus(), true, null, null);
                        dialog.show(fm, "message");
                        break;
                    case EC_SPOUSE_INCLUDED:
                        fm = getSupportFragmentManager();
                        NewMessageDialog newdialog = NewMessageDialog.newInstance(EC_FOR_SELF_OR_SPOUSE, "Income Source", "Is this income source for spouse or self?", "Self", "Spouse");
                        newdialog.show(fm, "message");
                        break;
                }
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (mSD == null) {
            return;
        }

        if(!mSpouseIncluded) {
            mOwnerTextView.setVisibility(View.GONE);
        } else if(mSD.getOwner() == OWNER_SELF) {
            mOwnerTextView.setText("Self");
        } else if(mSD.getOwner() == OWNER_SPOUSE) {
            mOwnerTextView.setText("Spouse");
        }

        mInitWithdrawPercentTextView.setText(mSD.getWithdrawPercent()+"%");

        String incomeSourceName = mSD.getName();
        int type = mSD.getType();
        String incomeSourceTypeString = getIncomeSourceTypeString(this, mIncomeType);
        SystemUtils.setToolbarSubtitle(this, incomeSourceTypeString);

        String balanceString;
        balanceString = mSD.getBalance();
        balanceString = SystemUtils.getFormattedCurrency(balanceString);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setSubtitle(incomeSourceTypeString);
        }
        mIncomeSourceName.setText(incomeSourceName);
        mBalance.setText(balanceString);

        String interest = mSD.getInterest()+"%";
        mAnnualInterest.setText(interest);

        String monthlyAdditionString = SystemUtils.getFormattedCurrency(mSD.getMonthlyAddition());
        setMonthlyAddition(monthlyAdditionString);

        AgeData age;
        age = mSD.getStartAge();
        mStartAgeTextView.setText(age.toString());

        age = mSD.getStopMonthlyAdditionAge();
        if(age != null) {
            setStopMonthlyAdditionAge(age.toString());
        }

        String increase = mSD.getAnnualPercentIncrease()+"%";
        setAnnualPercentIncrease(increase);

        setShowMonths(mSD.getShowMonths() == 1);
    }

    private void updateIncomeSourceData() {
        String value = mBalance.getText().toString();
        String balance = SystemUtils.getFloatValue(value);
        if(balance == null) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, getString(R.string.balance_not_valid) + " " + value, Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        value = mAnnualInterest.getText().toString();
        String interest = SystemUtils.getFloatValue(value);
        if(interest == null) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, getString(R.string.interest_not_valid) + " " + value, Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        value = getMonthlyAddition();
        String monthlyAddition = SystemUtils.getFloatValue(value);
        if(monthlyAddition == null) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, getString(R.string.value_not_valid) + " " + value, Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        String withdrawPercent = mInitWithdrawPercentTextView.getText().toString();

        value = withdrawPercent;
        withdrawPercent = SystemUtils.getFloatValue(withdrawPercent);
        if(withdrawPercent == null) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, getString(R.string.monthly_increase_not_valid) + " " + value, Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        value = getAnnualPercentIncrease();
        String annualPercentIncrease = SystemUtils.getFloatValue(value);
        if(annualPercentIncrease == null) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, getString(R.string.annual_withdraw_increase_not_valid) + " " + value, Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        String age = mStartAgeTextView.getText().toString();
        AgeData startAge = new AgeData(age);
        // TODO need to check for valid age

        age = getStopMonthlyAdditionAge();
        AgeData stopAge = new AgeData(age);
        // TODO need to check for valid age

        String name = mIncomeSourceName.getText().toString();

        int showMonths = getShowMonths() ? 1 : 0;
        SavingsData sie = new SavingsData(mId, mSD.getType(), name, mSD.getOwner(),
                startAge, balance, interest, monthlyAddition,
                stopAge, withdrawPercent, annualPercentIncrease, showMonths);
        mViewModel.setData(sie);

        finish();
    }

    @Override
    public void onEditAge(String year, String month) {
        AgeData age = new AgeData(year, month);
        // TODO need to check for valid age
        if(mAgeType == START_AGE) {
            mStartAgeTextView.setText(age.toString());
        }
    }

    private String getStopMonthlyAdditionAge() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            return ((SavingsAdvancedFragment)fragment).getStopMonthlyAdditionAge();
        }
        return "0";
    }

    private void setStopMonthlyAdditionAge(String stopMonthlyAdditionAge) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            ((SavingsAdvancedFragment)fragment).setStopMonthlyAdditionAge(stopMonthlyAdditionAge);
        }
    }

    private String getAnnualPercentIncrease() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            return ((SavingsAdvancedFragment)fragment).getAnnualPercentIncrease();
        }
        return "0";
    }

    private void setAnnualPercentIncrease(String annualPercentIncrease) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            ((SavingsAdvancedFragment)fragment).setAnnualPercentIncrease(annualPercentIncrease);
        }
    }

    private String getMonthlyAddition() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            return ((SavingsAdvancedFragment)fragment).getMonthlyAddition();
        }
        return "0";
    }

    private void setMonthlyAddition(String monthlyIncrease) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            ((SavingsAdvancedFragment)fragment).setMonthlyAddition(monthlyIncrease);
        }
    }

    private void setShowMonths(boolean showMonths) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            ((SavingsAdvancedFragment)fragment).setShowMonths(showMonths);
        }
    }

    private boolean getShowMonths() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.savings_advanced_fragment);
        if(fragment != null && fragment instanceof SavingsAdvancedFragment) {
            return ((SavingsAdvancedFragment)fragment).getShowMonths();
        }
        return false;
    }

    @Override
    public void onGetResponse(int response, int id, boolean isOk) {
        if (response == Activity.RESULT_OK) {
            switch (id) {
                case EC_ONLY_TWO_SUPPORTED:
                    finish();
                    break;
            }
        } else {
            // terminate activity
            finish();
        }
    }

    @Override
    public void onGetResponse(int id, int button) {
        switch (id) {
            case EC_ONLY_ONE_SUPPORTED:
                finish();
                break;
            case EC_FOR_SELF_OR_SPOUSE:
                if (button == NewMessageDialog.POS_BUTTON) {
                    mSD.setOwner(RetirementConstants.OWNER_SELF);
                } else if(button == NewMessageDialog.NEG_BUTTON) {
                    mSD.setOwner(RetirementConstants.OWNER_SPOUSE);
                }
                updateUI();
                break;
        }
    }
}
