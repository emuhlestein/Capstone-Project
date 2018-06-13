package com.intelliviz.income.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.os.Bundle;

import com.intelliviz.income.data.AgeData;
import com.intelliviz.income.data.IncomeData;
import com.intelliviz.income.data.IncomeDataAccessor;
import com.intelliviz.income.data.IncomeTypeRules;

import java.util.List;

import static com.intelliviz.income.db.entity.SavingsIncomeEntity.TABLE_NAME;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_ANNUAL_PERCENT_INCREASE;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_MONTHLY_ADDITION;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_SHOW_MONTHS;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_SOURCE_BALANCE;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_SOURCE_INTEREST;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_START_AGE;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_STOP_AGE;
import static com.intelliviz.income.util.RetirementConstants.EXTRA_INCOME_WITHDRAW_PERCENT;


/**
 * Created by edm on 10/2/2017.
 */
@Entity(tableName = TABLE_NAME)
public class SavingsIncomeEntity extends IncomeSourceEntityBase {
    public static final String TABLE_NAME = "savings_income";
    public static final String BALANCE_FIELD = "balance";
    public static final String INTEREST_FIELD = "interest";
    public static final String MONTHLY_ADDITION_FIELD = "monthly_addition";
    public static final String START_AGE_FIELD = "start_age";
    public static final String STOP_MONTHLY_ADDITION_AGE_FIELD = "stop_monthly_addition_age";
    public static final String WITHDRAW_PERCENT_FIELD = "withdraw_percent";
    public static final String ANNUAL_PERCENT_INCREASE_FIELD = "annual_percent_increase";
    public static final String SHOW_MONTHLY_AMOUNTS = "show_monthly_amounts";

    @TypeConverters({AgeConverter.class})
    @ColumnInfo(name = START_AGE_FIELD)
    private AgeData mStartAge;

    @ColumnInfo(name = BALANCE_FIELD)
    private String mBalance;

    @ColumnInfo(name = INTEREST_FIELD)
    private String mInterest;

    @ColumnInfo(name = MONTHLY_ADDITION_FIELD)
    private String mMonthlyAddition;

    @TypeConverters({AgeConverter.class})
    @ColumnInfo(name = STOP_MONTHLY_ADDITION_AGE_FIELD)
    private AgeData mStopMonthlyAdditionAge;

    @ColumnInfo(name = WITHDRAW_PERCENT_FIELD)
    private String mWithdrawPercent;

    @ColumnInfo(name = ANNUAL_PERCENT_INCREASE_FIELD)
    private String mAnnualPercentIncrease;

    @ColumnInfo(name = SHOW_MONTHLY_AMOUNTS)
    private int mShowMonths;

    @Ignore
    private IncomeTypeRules mRules;

    @Ignore
    public SavingsIncomeEntity(long id, int type) {
        super(id, type, "");
        mStartAge = new AgeData(0);
        mBalance = "0";
        mInterest = "0";
        mMonthlyAddition = "0";
        mStopMonthlyAdditionAge = new AgeData(0);
        mWithdrawPercent = "0";
        mAnnualPercentIncrease = "0";
        mShowMonths = 0;
    }

    public SavingsIncomeEntity(long id, int type, String name, AgeData startAge, String balance, String interest,
                               String monthlyAddition, AgeData stopMonthlyAdditionAge,
                               String withdrawPercent, String annualPercentIncrease, int showMonths) {
        super(id, type, name);
        mStartAge = startAge;
        mBalance = balance;
        mInterest = interest;
        mMonthlyAddition = monthlyAddition;
        mStopMonthlyAdditionAge = stopMonthlyAdditionAge;
        mWithdrawPercent = withdrawPercent;
        mAnnualPercentIncrease = annualPercentIncrease;
        mShowMonths = showMonths;
    }

    public AgeData getStartAge() {
        return mStartAge;
    }

    public void setStartAge(AgeData startAge) {
        mStartAge = startAge;
    }

    public String getBalance() {
        return mBalance;
    }

    public void setBalance(String balance) {
        mBalance = balance;
    }

    public String getInterest() {
        return mInterest;
    }

    public void setInterest(String interest) {
        mInterest = interest;
    }

    public String getMonthlyAddition() {
        return mMonthlyAddition;
    }

    public void setMonthlyAddition(String monthlyAddition) {
        mMonthlyAddition = monthlyAddition;
    }

    public AgeData getStopMonthlyAdditionAge() {
        return mStopMonthlyAdditionAge;
    }

    public void setStopMonthlyAdditionAge(AgeData stopMonthlyAdditionAgeAge) {
        mStopMonthlyAdditionAge = stopMonthlyAdditionAgeAge;
    }

    public String getWithdrawPercent() {
        return mWithdrawPercent;
    }

    public void setWithdrawPercent(String withdrawPercent) {
        mWithdrawPercent = withdrawPercent;
    }

    public String getAnnualPercentIncrease() {
        return mAnnualPercentIncrease;
    }

    public void setAnnualPercentIncrease(String annualPercentIncrease) {
        mAnnualPercentIncrease = annualPercentIncrease;
    }

    public int getShowMonths() {
        return mShowMonths;
    }

    public void setShowMonths(int showMonths) {
        mShowMonths = showMonths;
    }

    public void setRules(IncomeTypeRules rules) {
        mRules = rules;

        Bundle bundle = new Bundle();
        bundle.putDouble(EXTRA_INCOME_SOURCE_BALANCE, Double.parseDouble(mBalance));
        bundle.putDouble(EXTRA_INCOME_SOURCE_INTEREST, Double.parseDouble(mInterest));
        bundle.putDouble(EXTRA_INCOME_MONTHLY_ADDITION, Double.parseDouble(mMonthlyAddition));
        bundle.putDouble(EXTRA_INCOME_WITHDRAW_PERCENT, Double.parseDouble(mWithdrawPercent));
        bundle.putDouble(EXTRA_ANNUAL_PERCENT_INCREASE, Double.parseDouble(mAnnualPercentIncrease));
        bundle.putParcelable(EXTRA_INCOME_START_AGE, mStartAge);
        bundle.putParcelable(EXTRA_INCOME_STOP_AGE, mStopMonthlyAdditionAge);
        bundle.putInt(EXTRA_INCOME_SHOW_MONTHS, mShowMonths);
        mRules.setValues(bundle);
    }

    @Override
    public List<IncomeData> getIncomeData() {
        if(mRules != null) {
            return mRules.getIncomeData();
        } else {
            return null;
        }
    }

    @Override
    public IncomeDataAccessor getIncomeDataAccessor() {
        if(mRules != null) {
            return mRules.getIncomeDataAccessor();
        } else {
            return null;
        }
    }

    public IncomeData getIncomeData(IncomeData incomeData) {
        if(mRules != null) {
            return mRules.getIncomeData(incomeData);
        } else {
            return null;
        }
    }
}