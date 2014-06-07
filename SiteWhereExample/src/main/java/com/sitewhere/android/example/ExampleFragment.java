package com.sitewhere.android.example;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sitewhere.android.mqtt.ui.IConnectivityPreferences;

/**
 * Fragment that implements example application.
 * 
 * @author Derek
 */
public class ExampleFragment extends Fragment {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
	 * android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.example_app, container, false);
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Button clearPrefs = (Button) getActivity().findViewById(R.id.prefs_clear);
		clearPrefs.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onClearPreferences();
			}
		});
	}

	/**
	 * Called to clear preferences for testing.
	 */
	protected void onClearPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(IConnectivityPreferences.PREF_SITEWHERE_API_URI);
		editor.remove(IConnectivityPreferences.PREF_SITEWHERE_MQTT_BROKER_URI);

		editor.commit();

		Toast toast = Toast.makeText(getActivity(), "Preferences Cleared", Toast.LENGTH_SHORT);
		toast.show();
	}
}