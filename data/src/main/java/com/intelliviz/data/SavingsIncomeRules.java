package com.intelliviz.data;

import com.intelliviz.lowlevel.data.AgeData;

/**
 * Created by edm on 10/19/2017.
 */

public class SavingsIncomeRules extends BaseSavingsIncomeRules implements IncomeTypeRules {

    public SavingsIncomeRules(String ownerBirthDate, AgeData endAge, String otherBirthdate) {
        super(ownerBirthDate, endAge, otherBirthdate);
    }

    @Override
    protected double getPenaltyAmount(AgeData age, double amount) {
        return 0;
    }

    @Override
    protected boolean isPenalty(AgeData age) {
        return false;
    }

    @Override
    public IncomeDataAccessor getIncomeDataAccessor() {
        return new SavingIncomeDataAccessor(getIncomeData());
    }
}
