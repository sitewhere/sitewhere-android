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
package com.sitewhere.android.streaming.example;

import java.util.UUID;

import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.DeviceStreamAckState;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.RegistrationAckState;
import com.sitewhere.spi.device.event.IDeviceEventOriginator;

/**
 * SiteWhere sample activity.
 * 
 * @author Derek
 */
public class StreamingAudioExample extends SiteWhereHybridProtobufActivity implements
		IConnectivityWizardListener {

	/** Tag for logging */
	private static final String TAG = "StreamingAudioExample";

	/** Channel configuration */
	private static final int CHANNEL = AudioFormat.CHANNEL_OUT_DEFAULT;

	/** Encoding choice */
	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

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
		getActionBar().setTitle("Streaming Audio Example");
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
			break;
		}
		case NEW_REGISTRATION: {
			Log.d(TAG, "Device was registered successfully.");
			break;
		}
		case REGISTRATION_ERROR: {
			Log.d(TAG,
					"Error registering device. " + ack.getErrorType().name() + ": " + ack.getErrorMessage());
			break;
		}
		}
		if (ack.getState() != RegistrationAckState.REGISTRATION_ERROR) {
			createStream();
		}
	}

	/**
	 * Create a stream using a random UUID for the id.
	 */
	protected void createStream() {
		try {
			sendDeviceStreamCreate(getUniqueDeviceId(), null, UUID.randomUUID().toString(), "audio/x-ac3");
			Log.d(TAG, "Sent device stream create message.");
		} catch (SiteWhereMessagingException e) {
			Log.d(TAG, "Unable to send device stream create message.", e);
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
		Log.d(TAG, "Received ack message for stream: " + ack.getStreamId());
		switch (ack.getState()) {
		case STREAM_CREATED: {
			Log.d(TAG, "Device stream created successfully.");
			break;
		}
		case STREAM_EXISTS: {
			Log.d(TAG, "Device stream already existed.");
			break;
		}
		case STREAM_FAILED: {
			Log.d(TAG, "Unable to create device stream.");
			break;
		}
		}
		if (ack.getState() != DeviceStreamAckState.STREAM_FAILED) {
			startStreaming(ack.getStreamId());
		}
	}

	/**
	 * Start streaming data to SiteWhere for 10 seconds.
	 * 
	 * @param streamId
	 */
	protected void startStreaming(String streamId) {
		Log.d(TAG, "About to start streaming for " + streamId);

		// Find an acceptable sample rate and calculate buffer size.
		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		String sampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
		String framesPerBuffer = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);

		if (sampleRateStr == null) {
			Log.e(TAG, "Unable to determine sample rate.");
			return;
		}
		int sampleRate = Integer.parseInt(sampleRateStr);

		if (framesPerBuffer == null) {
			Log.e(TAG, "Unable to find frames per buffer.");
			return;
		}
		int bufferSize = Integer.parseInt(framesPerBuffer) * 16;

		Log.d(TAG, "Using sample rate " + sampleRate);

		byte[] buffer = new byte[bufferSize];
		Log.d(TAG, "Using buffer size " + bufferSize);

		// Create the recorder.
		AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, CHANNEL, ENCODING,
				bufferSize);
		recorder.startRecording();

		long stopTime = System.currentTimeMillis() + (10 * 1000);
		long seqNum = 0;
		while (System.currentTimeMillis() < stopTime) {
			int chunkSize = recorder.read(buffer, 0, buffer.length);
			byte[] chunk = new byte[chunkSize];
			System.arraycopy(buffer, 0, chunk, 0, chunkSize);
			try {
				sendDeviceStreamData(getUniqueDeviceId(), null, streamId, seqNum++, chunk);
				Log.d(TAG, "Sent chunk for " + streamId + " sequence number " + seqNum + ".");
			} catch (SiteWhereMessagingException e) {
				Log.e(TAG, "Error sending chunk for " + streamId + " sequence number " + seqNum + ".", e);
			}
		}

		recorder.release();
		Log.d(TAG, "Finished streaming for " + streamId);
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