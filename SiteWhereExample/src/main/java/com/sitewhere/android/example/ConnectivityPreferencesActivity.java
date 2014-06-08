package com.sitewhere.android.example;

import android.app.Activity;
import android.os.Bundle;

import com.sitewhere.android.mqtt.preferences.ConnectivityPreferencesFragment;

/**
 * Activity that displays connectivity preferences.
 * 
 * @author Derek
 */
public class ConnectivityPreferencesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new ConnectivityPreferencesFragment()).commit();
	}
}