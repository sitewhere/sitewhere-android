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
package com.sitewhere.android.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.util.Log;

import com.google.protobuf.AbstractMessageLite;
import com.sitewhere.android.SiteWhereActivity;
import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere.Command;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere.RegisterDevice;

/**
 * Extension of {@link SiteWhereActivity} that adds support for default Google Protocol Buffers
 * interactions with remote SiteWhere instance.
 * 
 * @author Derek
 */
public class SiteWhereProtobufActivity extends SiteWhereActivity {

	/** Tag for logging */
	public static final String TAG = "SiteWhereProtobuf";

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
	 * Register a device with SiteWhere.
	 * 
	 * @param hardwareId
	 * @param specificationToken
	 * @param originator
	 * @throws SiteWhereMessagingException
	 */
	public void registerDevice(String hardwareId, String specificationToken, String originator)
			throws SiteWhereMessagingException {
		RegisterDevice.Builder rb = RegisterDevice.newBuilder();
		rb.setHardwareId(hardwareId).setSpecificationToken(specificationToken);
		sendMessage(Command.REGISTER, rb.build(), originator, "registration");
	}

	/**
	 * Handle response from registration request.
	 * 
	 * @param header
	 * @param ack
	 */
	public void handleRegistrationAck(Header header, RegistrationAck ack) {
	}

	/**
	 * Send an acknowledgement event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param message
	 * @throws SiteWhereMessagingException
	 */
	public void sendAck(String hardwareId, String originator, String message)
			throws SiteWhereMessagingException {
		SiteWhere.Acknowledge.Builder builder = SiteWhere.Acknowledge.newBuilder();
		SiteWhere.Acknowledge ack = builder.setHardwareId(hardwareId).setMessage(message).build();
		sendMessage(Command.ACKNOWLEDGE, ack, originator, "ack");
	}

	/**
	 * Send a measurement event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param name
	 * @param value
	 * @throws SiteWhereMessagingException
	 */
	public void sendMeasurement(String hardwareId, String originator, String name, double value)
			throws SiteWhereMessagingException {
		SiteWhere.DeviceMeasurements.Builder mxsb = SiteWhere.DeviceMeasurements.newBuilder();
		SiteWhere.Measurement.Builder mxb = SiteWhere.Measurement.newBuilder();
		mxb.setMeasurementId(name).setMeasurementValue(value);
		mxsb.addMeasurement(mxb.build());
		sendMessage(Command.DEVICEMEASUREMENT, mxsb.build(), originator, "measurement");
	}

	/**
	 * Send a location event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param latitude
	 * @param longitude
	 * @param elevation
	 * @throws SiteWhereMessagingException
	 */
	public void sendLocation(String hardwareId, String originator, double latitude,
			double longitude, double elevation) throws SiteWhereMessagingException {
		SiteWhere.DeviceLocation.Builder lb = SiteWhere.DeviceLocation.newBuilder();
		lb.setHardwareId(hardwareId).setLatitude(latitude).setLongitude(longitude)
				.setElevation(elevation);
		sendMessage(Command.DEVICELOCATION, lb.build(), originator, "location");
	}

	/**
	 * Send an alert event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param alertType
	 * @param message
	 * @throws SiteWhereMessagingException
	 */
	public void sendAlert(String hardwareId, String originator, String alertType, String message)
			throws SiteWhereMessagingException {
		SiteWhere.DeviceAlert.Builder ab = SiteWhere.DeviceAlert.newBuilder();
		ab.setHardwareId(hardwareId).setAlertType(alertType).setAlertMessage(message);
		sendMessage(Command.DEVICEALERT, ab.build(), originator, "alert");
	}

	/**
	 * Build message from header and message, then send it to the underlying delivery mechanism.
	 * 
	 * @param command
	 * @param message
	 * @param originator
	 * @param label
	 * @throws SiteWhereMessagingException
	 */
	protected void sendMessage(SiteWhere.Command command, AbstractMessageLite message,
			String originator, String label) throws SiteWhereMessagingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			SiteWhere.Header.Builder builder = SiteWhere.Header.newBuilder();
			builder.setCommand(command);
			if (originator != null) {
				builder.setOriginator(originator);
			}
			builder.build().writeDelimitedTo(out);
			message.writeDelimitedTo(out);
			byte[] encoded = out.toByteArray();
			StringBuffer hex = new StringBuffer();
			for (byte current : encoded) {
				hex.append(String.format("%02X ", current));
				hex.append(" ");
			}
			Log.d(TAG, hex.toString());
			sendCommand(encoded);
		} catch (IOException e) {
			throw new SiteWhereMessagingException("Problem encoding " + label + " message.", e);
		} catch (Exception e) {
			throw new SiteWhereMessagingException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.SiteWhereActivity#onReceivedSystemCommand(byte[])
	 */
	@Override
	protected void onReceivedSystemCommand(byte[] payload) {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			Header header = Device.Header.parseDelimitedFrom(stream);
			switch (header.getCommand()) {
			case REGISTER_ACK: {
				RegistrationAck ack = RegistrationAck.parseDelimitedFrom(stream);
				handleRegistrationAck(header, ack);
			}
			}
		} catch (IOException e) {
			Log.e(TAG, "Unable to process system command.", e);
		}
	}
}