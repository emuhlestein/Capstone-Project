package com.intelliviz.retirementhelper.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edm on 6/5/2018.
 */

public class SavingIncomeDataAccessor implements IncomeDataAccessor {
    private List<IncomeData> mIncomeData;
    Map<Integer, IncomeData> mIncomeDataMap;

    public SavingIncomeDataAccessor(List<IncomeData> incomeData) {
        mIncomeData = incomeData;

        mIncomeDataMap = new HashMap<>();
        for(IncomeData bData : mIncomeData) {
            int year = bData.getAge().getYear();
            mIncomeDataMap.put(year, bData);
        }
    }

    @Override
    public IncomeData getIncomeData(AgeData age) {
        return mIncomeDataMap.get(age.getYear());
    }
}
