/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
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
package com.sitewhere.android.wso2.example;

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

import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.android.mqtt.MqttService;
import com.sitewhere.android.mqtt.preferences.IMqttServicePreferences;
import com.sitewhere.android.mqtt.preferences.MqttServicePreferences;
import com.sitewhere.android.mqtt.ui.ConnectivityWizardFragment;
import com.sitewhere.android.mqtt.ui.IConnectivityWizardListener;
import com.sitewhere.android.preferences.IConnectivityPreferences;
import com.sitewhere.android.protobuf.SiteWhereHybridProtobufActivity;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.DeviceStreamAck;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.rest.client.SiteWhereClient;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.search.DeviceAssignmentSearchResults;
import com.sitewhere.rest.model.search.SearchCriteria;
import com.sitewhere.spi.ISiteWhereClient;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.event.IDeviceEventOriginator;
import com.sitewhere.wso2.identity.ksoap2.Wso2SoapClient;

/**
 * SiteWhere sample activity.
 * 
 * @author Derek
 */
public class SiteWhereWso2IdentityExample extends SiteWhereHybridProtobufActivity implements
		IConnectivityWizardListener {

	/** Tag for logging */
	private static final String TAG = "SiteWhereWso2IdentityExample";

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
	 * Initialize the application.
	 */
	protected void initExampleApplication() {
		connectToSiteWhere();
		getActionBar().setTitle("Hybrid Protocol Example");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.mqtt.ui.IConnectivityWizardListener#onWizardComplete()
	 */
	@Override
	public void onWizardComplete() {
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.remove(wizard);
		fragmentTransaction.commit();
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
	 * @see com.sitewhere.android.protobuf.SiteWhereProtobufActivity#handleRegistrationAck(com.sitewhere
	 * .device.provisioning.protobuf.proto.Sitewhere.Device.Header,
	 * com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck)
	 */
	@Override
	public void handleRegistrationAck(Header header, RegistrationAck ack) {
		switch (ack.getState()) {
		case ALREADY_REGISTERED: {
			Log.d(TAG, "Device was already registered.");
			authenticate();
			break;
		}
		case NEW_REGISTRATION: {
			Log.d(TAG, "Device was registered successfully.");
			authenticate();
			break;
		}
		case REGISTRATION_ERROR: {
			Log.d(TAG,
					"Error registering device. " + ack.getErrorType().name() + ": " + ack.getErrorMessage());
			break;
		}
		}
	}

	/**
	 * Perform authentication against WSO2 Identity Server.
	 */
	protected void authenticate() {
		try {
			String username = "dadams";
			String password = "sitewhere";

			Wso2SoapClient client = new Wso2SoapClient();
			client.turnOffSSLVerification();
			boolean authenticated = client.authenticate(username, password);
			System.out.println("Authenticated " + username + ": " + authenticated);

			IMqttServicePreferences prefs = (IMqttServicePreferences) getServiceConfiguration();
			String url = "http://" + prefs.getBrokerHostname() + ":8080/sitewhere/api/";
			ISiteWhereClient sitewhere = new SiteWhereClient(url, "admin", "password");

			DeviceAssignmentSearchResults results = sitewhere.getAssignmentsForAsset(
					"bb105f8d-3150-41f5-b9d1-db04965668d3", "wso2", username, DeviceAssignmentStatus.Active,
					new SearchCriteria(1, 0));
			System.out.println("Found " + results.getNumResults() + " devices assigned");
			for (DeviceAssignment current : results.getResults()) {
				DeviceAssignment assignment = sitewhere.getDeviceAssignmentByToken(current.getToken());
				System.out.println("Device: " + assignment.getDevice().getAssetName());
			}
		} catch (Exception e) {
			Log.e(TAG, "Error authenticating device on WSO2 Identity Server.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.android.protobuf.SiteWhereProtobufActivity#handleDeviceStreamAck(com.sitewhere.device
	 * .communication.protobuf.proto.Sitewhere.Device.Header,
	 * com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.DeviceStreamAck)
	 */
	public void handleDeviceStreamAck(Header header, DeviceStreamAck ack) {
	}

	/**
	 * Command that changes the background color.
	 * 
	 * @param color
	 * @param originator
	 * @throws SiteWhereMessagingException
	 */
	public void changeBackground(final String color, IDeviceEventOriginator originator)
			throws SiteWhereMessagingException {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				getWindow().getDecorView().setBackgroundColor(Color.parseColor(color));
			}
		});
		sendAck(getUniqueDeviceId(), originator.getEventId(), "Updated background color.");
		Log.i(TAG, "Sent reponse to 'changeBackground' command.");
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
}