package com.sitewhere.android.mqtt.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sitewhere.android.mqtt.R;

/**
 * Allows MQTT settings to be entered, stored, and loaded for an application.
 * 
 * @author Derek
 */
public class MqttSettingsFragment extends Fragment {

	public MqttSettingsFragment() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
	 * android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.mqtt_settings, container, false);
		return rootView;
	}
}