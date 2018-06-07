package com.intelliviz.retirementhelper.data;

/**
 * Created by edm on 6/5/2018.
 */

public class SocialSecurityIncomeDataAccessor implements IncomeDataAccessor {
    private AgeData mStartAge;
    private double mMonthlyAmount;

    public SocialSecurityIncomeDataAccessor(AgeData startAge, double monthlyAmount) {
        mStartAge = startAge;
        mMonthlyAmount = monthlyAmount;
    }
    @Override
    public IncomeData getIncomeData(AgeData age) {
        if(age.isOnOrAfter(mStartAge)) {
            return new IncomeData(age, mMonthlyAmount, 0, 0, false);
        } else {
            return new IncomeData(age, 0, 0, 0, false);
        }
    }
}
