package com.intelliviz.retirementhelper.ui.income;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intelliviz.retirementhelper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditPensionIncomeFragment extends Fragment {


    public EditPensionIncomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_pension_income, container, false);
    }

}
