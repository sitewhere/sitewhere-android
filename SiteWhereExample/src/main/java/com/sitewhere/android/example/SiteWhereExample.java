/*
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sitewhere.android.example;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import android.app.FragmentTransaction;
import android.app.Service;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sitewhere.android.generated.Android;
import com.sitewhere.android.generated.Android.AndroidSpecification._Header;
import com.sitewhere.android.generated.Android.AndroidSpecification.changeBackground;
import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.android.mqtt.MqttService;
import com.sitewhere.android.mqtt.preferences.IMqttServicePreferences;
import com.sitewhere.android.mqtt.preferences.MqttServicePreferences;
import com.sitewhere.android.mqtt.ui.ConnectivityWizardFragment;
import com.sitewhere.android.mqtt.ui.IConnectivityWizardListener;
import com.sitewhere.android.preferences.IConnectivityPreferences;
import com.sitewhere.android.protobuf.SiteWhereProtobufActivity;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck;

/**
 * SiteWhere sample activity.
 * 
 * @author Derek
 */
public class SiteWhereExample extends SiteWhereProtobufActivity implements
		IConnectivityWizardListener {

	/** Tag for logging */
	private static final String TAG = "SiteWhereExample";

	/** Wizard shown to establish preferences */
	private ConnectivityWizardFragment wizard;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Verify that SiteWhere API location has been specified.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String apiUrl = prefs.getString(IConnectivityPreferences.PREF_SITEWHERE_API_URI, null);

		// Push current device id into MQTT settings, then get current values.
		MqttServicePreferences updated = new MqttServicePreferences();
		updated.setDeviceHardwareId(getUniqueDeviceId());
		IMqttServicePreferences mqtt = MqttServicePreferences.update(updated, this);

		if ((apiUrl == null) || (mqtt.getBrokerHostname() == null)) {
			initConnectivityWizard();
		} else {
			initExampleApplication();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#getServiceClass()
	 */
	@Override
	protected Class<? extends Service> getServiceClass() {
		return MqttService.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#getServiceConfiguration()
	 */
	@Override
	protected Parcelable getServiceConfiguration() {
		return MqttServicePreferences.read(this);
	}

	/**
	 * Adds the connectivity wizard if preferences have not been set.
	 */
	protected void initConnectivityWizard() {
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		wizard = new ConnectivityWizardFragment();
		wizard.setWizardListener(this);
		fragmentTransaction.replace(R.id.container, wizard);
		fragmentTransaction.commit();
		getActionBar().setTitle("SiteWhere Device Setup");
	}

	/**
	 * Adds the connectivity wizard if preferences have not been set.
	 */
	protected void initExampleApplication() {
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.container, new ExampleFragment());
		fragmentTransaction.commit();
		connectToSiteWhere();
		getActionBar().setTitle("SiteWhere Example");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.mqtt.ui.IConnectivityWizardListener#onWizardComplete()
	 */
	@Override
	public void onWizardComplete() {
		initExampleApplication();
		connectToSiteWhere();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.example_app_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_setup_wizard:
			initConnectivityWizard();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		disconnectFromSiteWhere();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#onConnectedToSiteWhere()
	 */
	@Override
	protected void onConnectedToSiteWhere() {
		Log.d(TAG, "Connected to SiteWhere.");
		try {
			registerDevice(getUniqueDeviceId(), "d2604433-e4eb-419b-97c7-88efe9b2cd41", null);
		} catch (SiteWhereMessagingException e) {
			Log.e(TAG, "Unable to send device registration to SiteWhere.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.android.protobuf.SiteWhereProtobufActivity#handleRegistrationAck(com.sitewhere
	 * .device.provisioning.protobuf.proto.Sitewhere.Device.Header,
	 * com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck)
	 */
	@Override
	public void handleRegistrationAck(Header header, RegistrationAck ack) {
		switch (ack.getState()) {
		case ALREADY_REGISTERED: {
			Log.d(TAG, "Device was already registered.");
			break;
		}
		case NEW_REGISTRATION: {
			Log.d(TAG, "Device was registered successfully.");
			break;
		}
		case REGISTRATION_ERROR: {
			Log.d(TAG,
					"Error registering device. " + ack.getErrorType().name() + ": "
							+ ack.getErrorMessage());
			break;
		}
		}
		try {
			sendLocation(getUniqueDeviceId(), null, 1.0, 1.0, 1.0);
		} catch (SiteWhereMessagingException e) {
			Log.e(TAG, "Unable to send location.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#onDisconnectedFromSiteWhere()
	 */
	@Override
	protected void onDisconnectedFromSiteWhere() {
		Log.d(TAG, "Disconnected from SiteWhere.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#onReceivedCustomCommand(byte[])
	 */
	@Override
	protected void onReceivedCustomCommand(byte[] payload) {
		Log.d(TAG, "Received custom command.");
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			_Header header = Android.AndroidSpecification._Header.parseDelimitedFrom(stream);
			switch (header.getCommand()) {
			case CHANGEBACKGROUND: {
				final changeBackground cb = changeBackground.parseDelimitedFrom(stream);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						getWindow().getDecorView().setBackgroundColor(
								Color.parseColor(cb.getColor()));
					}
				});
				sendAck(getUniqueDeviceId(), header.getOriginator(), "Updated background color.");
				Log.i(TAG, "Sent reponse to 'changeBackground' command.");
				break;
			}
			case PING: {
				sendAck(getUniqueDeviceId(), header.getOriginator(), "Acknowledged.");
				Log.i(TAG, "Sent reponse to 'ping' command.");
				break;
			}
			case TESTEVENTS: {
				sendMeasurement(getUniqueDeviceId(), header.getOriginator(), "engine.temp", 170.0);
				sendLocation(getUniqueDeviceId(), header.getOriginator(), 33.7550, -84.3900, 0.0);
				sendAlert(getUniqueDeviceId(), header.getOriginator(), "engine.overheat",
						"Engine is overheating!");
				Log.i(TAG, "Sent reponse to 'testEvents' command.");
				break;
			}
			}
		} catch (IOException e) {
			Log.e(TAG, "IO exception processing custom command.", e);
		} catch (SiteWhereMessagingException e) {
			Log.e(TAG, "Messaging exception processing custom command.", e);
		}
	}
}
