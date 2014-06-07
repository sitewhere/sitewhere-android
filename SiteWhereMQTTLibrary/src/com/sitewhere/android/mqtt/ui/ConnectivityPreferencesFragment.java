package com.sitewhere.android.mqtt.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.sitewhere.android.mqtt.R;

/**
 * Fragment used for displaying connectivity preferences.
 * 
 * @author Derek
 */
public class ConnectivityPreferencesFragment extends PreferenceFragment {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.connectivity_pref);
	}
}