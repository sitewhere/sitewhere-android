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

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.android.mqtt.MqttConfiguration;
import com.sitewhere.android.mqtt.MqttService;
import com.sitewhere.android.protobuf.SiteWhereProtobufActivity;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAckState;

/**
 * SiteWhere sample activity.
 * 
 * @author Derek
 */
public class SiteWhereExample extends SiteWhereProtobufActivity {

	/** Tag for logging */
	private static final String TAG = "SiteWhereExample";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sitewhere_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new RegsitrationFragment())
					.commit();
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
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		Button connect = (Button) findViewById(R.id.sw_reg_connect);
		connect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onConnectButtonClicked(v);
			}
		});

		Button disconnect = (Button) findViewById(R.id.sw_reg_disconnect);
		disconnect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
	}

	/**
	 * Connects to SiteWhere as result of clicking 'Connect' button.
	 * 
	 * @param v
	 */
	protected void onConnectButtonClicked(View v) {
		connectToSiteWhere();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#configureServiceIntent(android.content.Intent)
	 */
	@Override
	protected void configureServiceIntent(Intent intent) {
		MqttConfiguration config = new MqttConfiguration("192.168.1.68", 1883, getUniqueDeviceId());
		intent.putExtra(MqttService.EXTRA_CONFIGURATION, config);
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
			registerDevice(getUniqueDeviceId(), "7dfd6d63-5e8d-4380-be04-fc5c73801dfb", null);
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
		if (ack.getState() != RegistrationAckState.REGISTRATION_ERROR) {
			sendData();
		}
	}

	/**
	 * Once registered, send measurement.
	 */
	public void sendData() {
		try {
			sendMeasurement(getUniqueDeviceId(), null, "engine.temp", 125.0);
			Log.d(TAG, "Sent measurement.");
		} catch (SiteWhereMessagingException e) {
			Log.e(TAG, "Error sending measurement.", e);
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
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class RegsitrationFragment extends Fragment {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
		 * android.view.ViewGroup, android.os.Bundle)
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.registration, container, false);
			return rootView;
		}
	}
}