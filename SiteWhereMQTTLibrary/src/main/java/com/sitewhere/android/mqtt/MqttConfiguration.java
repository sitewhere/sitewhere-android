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
package com.sitewhere.android.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Configuration parameters passed in to MQTT service.
 * 
 * @author Derek
 */
public class MqttConfiguration implements Parcelable {

	/** MQTT server hostname */
	private String hostname;

	/** MQTT server port */
	private int port;

	/** Device hardware id */
	private String hardwareId;

	public MqttConfiguration(String hostname, int port, String hardwareId) {
		this.hostname = hostname;
		this.port = port;
		this.hardwareId = hardwareId;
	}

	public MqttConfiguration(Parcel parcel) {
		setHostname(parcel.readString());
		setPort(parcel.readInt());
		setHardwareId(parcel.readString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel parcel, int arg) {
		parcel.writeString(getHostname());
		parcel.writeInt(getPort());
		parcel.writeString(getHardwareId());
	}

	/** Provide standard creator method */
	public static final Parcelable.Creator<MqttConfiguration> CREATOR = new Parcelable.Creator<MqttConfiguration>() {
		public MqttConfiguration createFromParcel(Parcel parcel) {
			return new MqttConfiguration(parcel);
		}

		public MqttConfiguration[] newArray(int size) {
			return new MqttConfiguration[size];
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MqttConfiguration)) {
			return false;
		}
		MqttConfiguration other = (MqttConfiguration) o;
		if (!(getHostname().equals(other.getHostname()))) {
			return false;
		}
		if (!(getPort() == other.getPort())) {
			return false;
		}
		if (!(getHardwareId().equals(other.getHardwareId()))) {
			return false;
		}
		return true;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHardwareId() {
		return hardwareId;
	}

	public void setHardwareId(String hardwareId) {
		this.hardwareId = hardwareId;
	}
}