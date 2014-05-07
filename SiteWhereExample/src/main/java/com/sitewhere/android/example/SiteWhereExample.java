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

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sitewhere.android.generated.Android;
import com.sitewhere.android.generated.Android.AndroidSpecification._Header;
import com.sitewhere.android.generated.Android.AndroidSpecification.changeBackground;
import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.android.mqtt.MqttConfiguration;
import com.sitewhere.android.mqtt.MqttService;
import com.sitewhere.android.protobuf.SiteWhereProtobufActivity;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck;

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
						getWindow().getDecorView().setBackgroundColor(Color.parseColor(cb.getColor()));
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