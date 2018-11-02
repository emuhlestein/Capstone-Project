package com.intelliviz.data;

import android.os.Bundle;

import com.intelliviz.lowlevel.data.AgeData;
import com.intelliviz.lowlevel.util.AgeUtils;
import com.intelliviz.lowlevel.util.RetirementConstants;

import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_ANNUAL_PERCENT_INCREASE;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_MONTHLY_ADDITION;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_SHOW_MONTHS;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_SOURCE_BALANCE;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_SOURCE_INTEREST;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_START_AGE;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_STOP_AGE;
import static com.intelliviz.lowlevel.util.RetirementConstants.EXTRA_INCOME_WITHDRAW_PERCENT;
import static com.intelliviz.lowlevel.util.RetirementConstants.OWNER_PRIMARY;


/**
 * Created by edm on 12/30/2017.
 */

public abstract class BaseSavingsIncomeRules implements IncomeTypeRules {
    private int mOwner;
    private String mOwnerBirthdate;
    private String mOtherBirthdate;
    private AgeData mStartAge; // age at which withdraws begin
    private AgeData mEndAge; // end of life
    private AgeData mStopAge; // age at which monthly deposits stop
    private double mBalance; // balance
    private double mInterest; // annual interest (APR)
    private double mMonthlyDeposit; // amount that is deposited each month
    private double mInitialWithdrawPercent; // The percentage of balance for initial withdraw.
    private double mAnnualPercentIncrease; // percent to increase withdraw
    private boolean mShowMonths;
    private RetirementOptions mRO;

    private double mCurrentBalance;
    private AgeData mCurrentAge;
    private boolean mMakeWithdraws;

    BaseSavingsIncomeRules(RetirementOptions ro, boolean makeWithdraws) {
        mRO = ro;
        mOwnerBirthdate = ro.getPrimaryBirthdate();
        mEndAge = ro.getEndAge();
        mOtherBirthdate = ro.getSpouseBirthdate();
        mMakeWithdraws = makeWithdraws;
    }

    public int getOwner() {
        return mOwner;
    }

    public RetirementOptions getRetirementOptions() { return mRO; }

    protected abstract IncomeData createIncomeData(AgeData age, double monthlyAmount, double balance);

    public void setValues(Bundle bundle) {
        mOwner = bundle.getInt(RetirementConstants.EXTRA_INCOME_OWNER);
        mBalance = bundle.getDouble(EXTRA_INCOME_SOURCE_BALANCE);
        mInterest = bundle.getDouble(EXTRA_INCOME_SOURCE_INTEREST);
        mMonthlyDeposit = bundle.getDouble(EXTRA_INCOME_MONTHLY_ADDITION);
        mInitialWithdrawPercent = bundle.getDouble(EXTRA_INCOME_WITHDRAW_PERCENT);
        mAnnualPercentIncrease = bundle.getDouble(EXTRA_ANNUAL_PERCENT_INCREASE);
        mStartAge = bundle.getParcelable(EXTRA_INCOME_START_AGE);
        mStopAge = bundle.getParcelable(EXTRA_INCOME_STOP_AGE);
        mShowMonths = bundle.getInt(EXTRA_INCOME_SHOW_MONTHS) == 1;

        if(mOwner == RetirementConstants.OWNER_SPOUSE) {
            mOwnerBirthdate = mRO.getSpouseBirthdate();
            mOtherBirthdate = mRO.getPrimaryBirthdate();
        }

        AgeData currentAge = AgeUtils.getAge(mOwnerBirthdate);

        // no age can be before current age.
        if (mStartAge.isBefore(currentAge)) {
            mStartAge = new AgeData(currentAge.getNumberOfMonths());
        }

        if (mStopAge.isBefore(currentAge)) {
            mStopAge = new AgeData(currentAge.getNumberOfMonths());
        }

        mCurrentBalance = mBalance;
        mCurrentAge = currentAge;
    }

    @Override
    public IncomeData getIncomeData() {
        return null;
    }

    @Override
    public IncomeData getIncomeData(AgeData primaryAge) {
        double monthlyWithdraw = 0;
        double monthlyDeposit = mMonthlyDeposit;
        double initWithdrawPercent = mInitialWithdrawPercent / 100;
        double monthlyInterest = mInterest / 1200;
        AgeData startAge = mStartAge;

        AgeData age = convertAge(primaryAge);

        if(age.equals(mCurrentAge)) {
            monthlyWithdraw = mCurrentBalance * initWithdrawPercent / 12;
            return new IncomeData(primaryAge, monthlyWithdraw, mCurrentBalance, 0, null);
        }

        if(age.isBefore(mCurrentAge)) {
            return new IncomeData();
        }

        int numMonths = age.diff(mCurrentAge);

        startAge = new AgeData(mCurrentAge.getNumberOfMonths() + numMonths);
        monthlyWithdraw = 0;

        int month = mCurrentAge.getNumberOfMonths();
        int totalMonths = month + numMonths;

        for (; month < totalMonths; month++) {
            AgeData currentAge = new AgeData(month);
            if(mMakeWithdraws) {
                monthlyWithdraw = getMonthlyWithdraw(currentAge, startAge, monthlyWithdraw, mCurrentBalance, initWithdrawPercent);
            } else {
                monthlyWithdraw = mCurrentBalance * initWithdrawPercent / 12;
            }

//            if (currentAge.isOnOrAfter(mStartAge)) {
//                if (currentAge.equals(mStartAge)) {
//                    monthlyWithdraw = balance * initWithdrawPercent / 12;
//                } else {
//                    if (currentAge.getMonth() == 0) {
//                        monthlyWithdraw = monthlyWithdraw + (monthlyWithdraw * mAnnualPercentIncrease / 100);
//                    }
//                }
//            } else {
//                monthlyWithdraw = 0;
//            }

            if(mMakeWithdraws) {
                mCurrentBalance -= monthlyWithdraw;
                if (mCurrentBalance < 0) {
                    mCurrentBalance += monthlyWithdraw;
                    monthlyWithdraw = mCurrentBalance;
                    mCurrentBalance = 0;
                }
            }

            if (currentAge.isOnOrAfter(mStopAge)) {
                monthlyDeposit = 0;
            }
            mCurrentBalance += monthlyDeposit;

            double amount = mCurrentBalance * monthlyInterest;
            mCurrentBalance += amount;
        }

        mCurrentAge = new AgeData(month);

        return new IncomeData(mCurrentAge, monthlyWithdraw, mCurrentBalance, 0, "");
    }

    @Override
    public IncomeData getIncomeData(IncomeData incomeData) {
        /*
        if(incomeData == null) {
            AgeData currentAge = AgeUtils.getAge(mPrimartBirthdate);
            return new IncomeData(currentAge, 0, mStartBalance, BI_GOOD, null);
        } else {
            AgeData age = incomeData.getAge();
            age = new AgeData(age.getNumberOfMonths()+1);
            double balance = incomeData.getBalance();
            balance += mMonthlyDeposit;
            double amount = balance * mMonthlyInterest;
            balance += amount;
            double monthlyAmount = balance * mInitialWithdrawPercent;
            return new IncomeData(age, monthlyAmount, balance, BI_GOOD, null);
        }
        */
        return null;
    }
/*
    public List<IncomeData> getIncomeData() {
        double monthlyWithdraw = 0;
        double balance = mBalance;
        double monthlyDeposit = mMonthlyDeposit;
        double initWithdrawPercent = mInitialWithdrawPercent / 100;
        double monthlyInterest = mInterest / 1200;
        AgeData currentAge = AgeUtils.getAge(mOwnerBirthdate);

        List<IncomeData> listAmountDate = new ArrayList<>();

        for (int month = currentAge.getNumberOfMonths(); month <= mEndAge.getNumberOfMonths(); month++) {
            AgeData age = new AgeData(month);
            if (age.isOnOrAfter(mStartAge)) {
                if (age.equals(mStartAge)) {
                    monthlyWithdraw = balance * initWithdrawPercent / 12;
                } else {
                    if (age.getMonth() == 0) {
                        monthlyWithdraw = monthlyWithdraw + (monthlyWithdraw * mAnnualPercentIncrease / 100);
                    }
                }
            } else {
                monthlyWithdraw = 0;
            }

            balance -= monthlyWithdraw;
            if(balance < 0) {
                balance += monthlyWithdraw;
                monthlyWithdraw = balance;
                balance = 0;
            }

            listAmountDate.add(createIncomeData(age, monthlyWithdraw, balance));

            if (age.isOnOrAfter(mStopAge)) {
                monthlyDeposit = 0;
            }
            balance += monthlyDeposit;

            double amount = balance * monthlyInterest;
            balance += amount;
        }

        return listAmountDate;
    }
    */

    private double getMonthlyWithdraw(AgeData age, AgeData startAge, double monthlyWithdraw, double balance, double initWithdrawPercent) {
        if (age.isOnOrAfter(startAge)) {
            if (age.equals(startAge)) {
                monthlyWithdraw = balance * initWithdrawPercent / 12;
            } else {
                if (age.getMonth() == 0) {
                    monthlyWithdraw = monthlyWithdraw + (monthlyWithdraw * mAnnualPercentIncrease / 100);
                }
            }
        } else {
            monthlyWithdraw = 0;
        }

        return monthlyWithdraw;
    }

    private AgeData convertAge(AgeData age) {
        if(mOwner == OWNER_PRIMARY) {
            return age;
        } else {
            return AgeUtils.getAge(mRO.getPrimaryBirthdate(), mRO.getSpouseBirthdate(), age);
        }
    }
}
