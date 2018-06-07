package com.intelliviz.retirementhelper.data;

import android.os.Bundle;

import java.util.List;

/**
 * Created by edm on 8/14/2017.
 */

public interface IncomeTypeRules {
    void setValues(Bundle bundle);
    List<BenefitData> getBenefitData();
    BenefitData getBenefitData(BenefitData benefitData);
    IncomeDataAccessor getIncomeDataAccessor();
}
