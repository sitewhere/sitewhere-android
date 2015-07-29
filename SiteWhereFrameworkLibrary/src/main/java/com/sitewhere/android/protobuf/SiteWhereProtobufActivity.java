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
package com.sitewhere.android.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.util.Log;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import com.sitewhere.android.SiteWhereActivity;
import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.DeviceStreamAck;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Model;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Model.DeviceStreamData;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.SiteWhere;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.SiteWhere.Command;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.SiteWhere.RegisterDevice;

/**
 * Extension of {@link SiteWhereActivity} that adds support for default Google Protocol Buffers interactions
 * with remote SiteWhere instance.
 * 
 * @author Derek
 */
public abstract class SiteWhereProtobufActivity extends SiteWhereActivity {

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
		registerDevice(hardwareId, specificationToken, null, originator);
	}

	/**
	 * Register a device with SiteWhere. (Includes token of Site for device)
	 * 
	 * @param hardwareId
	 * @param specificationToken
	 * @param siteToken
	 * @param originator
	 * @throws SiteWhereMessagingException
	 */
	public void registerDevice(String hardwareId, String specificationToken, String siteToken,
			String originator) throws SiteWhereMessagingException {
		RegisterDevice.Builder rb = RegisterDevice.newBuilder();
		rb.setHardwareId(hardwareId).setSpecificationToken(specificationToken);
		if (siteToken != null) {
			rb.setSiteToken(siteToken);
		}
		sendMessage(Command.SEND_REGISTRATION, rb.build(), originator, "registration");
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
		sendMessage(Command.SEND_ACKNOWLEDGEMENT, ack, originator, "ack");
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
		Model.DeviceMeasurements.Builder mxsb = Model.DeviceMeasurements.newBuilder();
		Model.Measurement.Builder mxb = Model.Measurement.newBuilder();
		mxb.setMeasurementId(name).setMeasurementValue(value);
		mxsb.setHardwareId(hardwareId).addMeasurement(mxb.build());
		sendMessage(Command.SEND_DEVICE_MEASUREMENTS, mxsb.build(), originator, "measurement");
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
	public void sendLocation(String hardwareId, String originator, double latitude, double longitude,
			double elevation) throws SiteWhereMessagingException {
		Model.DeviceLocation.Builder lb = Model.DeviceLocation.newBuilder();
		lb.setHardwareId(hardwareId).setLatitude(latitude).setLongitude(longitude).setElevation(elevation);
		sendMessage(Command.SEND_DEVICE_LOCATION, lb.build(), originator, "location");
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
		Model.DeviceAlert.Builder ab = Model.DeviceAlert.newBuilder();
		ab.setHardwareId(hardwareId).setAlertType(alertType).setAlertMessage(message);
		sendMessage(Command.SEND_DEVICE_ALERT, ab.build(), originator, "alert");
	}

	/**
	 * Send a device stream create request to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param streamId
	 * @param contentType
	 * @throws SiteWhereMessagingException
	 */
	public void sendDeviceStreamCreate(String hardwareId, String originator, String streamId,
			String contentType) throws SiteWhereMessagingException {
		Model.DeviceStream.Builder builder = Model.DeviceStream.newBuilder();
		builder.setHardwareId(hardwareId).setStreamId(streamId).setContentType(contentType);
		sendMessage(Command.SEND_DEVICE_STREAM, builder.build(), originator, "device stream");
	}

	/**
	 * Handle response from device stream create request.
	 * 
	 * @param header
	 * @param ack
	 */
	public void handleDeviceStreamAck(Header header, DeviceStreamAck ack) {
	}

	/**
	 * Send a device stream data request to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param streamId
	 * @param sequenceNumber
	 * @param data
	 * @throws SiteWhereMessagingException
	 */
	public void sendDeviceStreamData(String hardwareId, String originator, String streamId,
			long sequenceNumber, byte[] data) throws SiteWhereMessagingException {
		Model.DeviceStreamData.Builder builder = Model.DeviceStreamData.newBuilder();
		builder.setHardwareId(hardwareId).setStreamId(streamId).setSequenceNumber(sequenceNumber)
				.setData(ByteString.copyFrom(data));
		sendMessage(Command.SEND_DEVICE_STREAM_DATA, builder.build(), originator, "device stream data");
	}

	/**
	 * Send request for a chunk of device stream data.
	 * 
	 * @param hardwareId
	 * @param streamId
	 * @param sequenceNumber
	 * @throws SiteWhereMessagingException
	 */
	public void requestDeviceStreamData(String hardwareId, String streamId, long sequenceNumber)
			throws SiteWhereMessagingException {
		SiteWhere.DeviceStreamDataRequest.Builder builder = SiteWhere.DeviceStreamDataRequest.newBuilder();
		builder.setHardwareId(hardwareId).setStreamId(streamId).setSequenceNumber(sequenceNumber);
		sendMessage(Command.REQUEST_DEVICE_STREAM_DATA, builder.build(), null, "request device stream data");
	}

	/**
	 * Handle device stream data being streamed from SiteWhere.
	 * 
	 * @param header
	 * @param data
	 */
	public void handleReceivedDeviceStreamData(Header header, DeviceStreamData data) {
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
	protected void sendMessage(SiteWhere.Command command, AbstractMessageLite message, String originator,
			String label) throws SiteWhereMessagingException {
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
			case ACK_REGISTRATION: {
				RegistrationAck ack = RegistrationAck.parseDelimitedFrom(stream);
				handleRegistrationAck(header, ack);
				break;
			}
			case ACK_DEVICE_STREAM: {
				DeviceStreamAck ack = DeviceStreamAck.parseDelimitedFrom(stream);
				handleDeviceStreamAck(header, ack);
				break;
			}
			case RECEIVE_DEVICE_STREAM_DATA: {
				DeviceStreamData chunk = DeviceStreamData.parseDelimitedFrom(stream);
				handleReceivedDeviceStreamData(header, chunk);
				break;
			}
			}
		} catch (IOException e) {
			Log.e(TAG, "Unable to process system command.", e);
		}
	}
}