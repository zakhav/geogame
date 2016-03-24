package com.locify.locifymobile;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.locify.locifymobile.com.locify.locifymobile.model.SearchCriteria;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchType;


/**
 * A simple {@link Fragment} subclass.
 */
public class GeoSearchFragment extends PageFragment {
    private Button searchButton;
    private EditText radiusField;
    private EditText zipField;
    private EditText cityField;
    private RadioGroup selectorGroup;

    public GeoSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_geo_search, container, false);
        searchButton = (Button)rootView.findViewById(R.id.search_btn);
        radiusField = (EditText)rootView.findViewById(R.id.radiusField);
        zipField = (EditText)rootView.findViewById(R.id.zip_edit);
        cityField = (EditText)rootView.findViewById(R.id.city_edit);

        selectorGroup = (RadioGroup)rootView.findViewById(R.id.search_opt_group);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtnClicked(v);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GeoCachingActivity geoCachingActivity = (GeoCachingActivity)getActivity();
        if(geoCachingActivity != null) {
            SearchCriteria searchCriteria = geoCachingActivity.getSearchBuffer().getCriteria();
            if(searchCriteria != null) {
                syncRadioButtons(searchCriteria);
                radiusField.setText(Integer.toString(searchCriteria.radius));
                zipField.setText(searchCriteria.zip);
                cityField.setText(searchCriteria.city);
            }
        }
    }

    private void syncRadioButtons(SearchCriteria searchCriteria) {
        int optId = R.id.search_current_location_opt;
        switch (searchCriteria.type) {
            case CURRENT_LOCATION:
                optId = R.id.search_current_location_opt;
                break;
            case ZIP:
                optId = R.id.search_zip_opt;
                break;
            case CITY:
                optId = R.id.search_city_opt;
                break;
            default:
                break;
        }
        selectorGroup.check(optId);
    }

    public void searchBtnClicked(View searchBtn) {
        if(validate()) {
            int selectedOp = selectorGroup.getCheckedRadioButtonId();
            GeoCachingActivity geoCachingActivity = (GeoCachingActivity)getActivity();
            SearchCriteria searchCriteria = geoCachingActivity.getSearchBuffer().getCriteria();

            SearchType searchType = SearchType.CURRENT_LOCATION;
            switch (selectedOp) {
                case R.id.search_current_location_opt:
                    searchType = SearchType.CURRENT_LOCATION;
                    break;
                case R.id.search_zip_opt:
                    searchType = SearchType.ZIP;
                    searchCriteria.zip = zipField.getText().toString();
                    break;
                case R.id.search_city_opt:
                    searchType = SearchType.CITY;
                    searchCriteria.city = cityField.getText().toString();
                    break;
                default:
                    break;
            }
            searchCriteria.radius = Integer.parseInt(radiusField.getText().toString());
            searchCriteria.type = searchType;
            geoCachingActivity.doSearch();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String radiusInput = radiusField.getText().toString();
        if(radiusInput.isEmpty()) {
            valid = false;
            radiusField.setError(getResources().getString(R.string.invalid_radius));
        } else {
            try {
                int radius = Integer.parseInt(radiusInput);
                radiusField.setError(null);
            } catch (NumberFormatException nfe) {
                radiusField.setError(getResources().getString(R.string.invalid_radius));
            }
        }
        int selectedOp = selectorGroup.getCheckedRadioButtonId();
        if(selectedOp == R.id.search_zip_opt) {
            String zipCode = zipField.getText().toString();
            if(zipCode.isEmpty()) {
                zipField.setError(getResources().getString(R.string.invalid_zip));
                valid = false;
            } else {
                zipField.setError(null);
            }
            cityField.setError(null);
        } else if(selectedOp == R.id.search_city_opt) {
            String city = cityField.getText().toString();
            if(city.isEmpty()) {
                cityField.setError(getResources().getString(R.string.invalid_city));
                valid = false;
            } else {
                cityField.setError(null);
            }
            zipField.setError(null);
        }
        return valid;
    }

}
