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
package com.sitewhere.android.mqtt.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sitewhere.android.mqtt.MqttService;

/**
 * Wraps actions related to preferences for {@link MqttService}.
 * 
 * @author Derek
 */
public class MqttServicePreferences implements IMqttServicePreferences {

	/** Indentifier for loading preferences for SiteWhere MQTT */
	public static final String IDENTIFIER = "com.sitewhere.android.mqtt";

	/** Default MQTT broker port */
	public static final int DEFAULT_BROKER_PORT = 1883;

	/** MQTT broker host name */
	private String brokerHostname;

	/** MQTT broker port */
	private Integer brokerPort = DEFAULT_BROKER_PORT;

	/** Device hardware id */
	private String deviceHardwareId;

	/**
	 * Read current values of preferences.
	 * 
	 * @param context
	 * @return
	 */
	public static IMqttServicePreferences read(Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		return MqttServicePreferences.loadFrom(prefs);
	}

	/**
	 * Update selected preference values. (Fields with null values are not set).
	 * 
	 * @param updated
	 * @param context
	 * @return
	 */
	public static IMqttServicePreferences update(MqttServicePreferences updated, Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		return MqttServicePreferences.saveTo(updated, prefs);
	}

	/**
	 * Gets {@link SharedPreferences} relevant to {@link MqttService}.
	 * 
	 * @param context
	 * @return
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(IDENTIFIER, Context.MODE_MULTI_PROCESS);
	}

	/**
	 * Load values from {@link SharedPreferences}.
	 * 
	 * @param prefs
	 * @return
	 */
	protected static MqttServicePreferences loadFrom(SharedPreferences prefs) {
		MqttServicePreferences mqtt = new MqttServicePreferences();
		mqtt.setBrokerHostname(prefs.getString(
				IMqttServicePreferences.PREF_SITEWHERE_MQTT_BROKER_HOSTNAME, null));
		mqtt.setBrokerPort(prefs.getInt(IMqttServicePreferences.PREF_SITEWHERE_MQTT_BROKER_PORT,
				DEFAULT_BROKER_PORT));
		mqtt.setDeviceHardwareId(prefs.getString(
				IMqttServicePreferences.PREF_SITEWHERE_DEVICE_HARDWARE_ID, null));
		return mqtt;
	}

	/**
	 * Save values to {@link SharedPreferences}.
	 * 
	 * @param updated
	 * @param prefs
	 * @return
	 */
	protected static MqttServicePreferences saveTo(MqttServicePreferences updated,
			SharedPreferences prefs) {
		Editor editor = prefs.edit();
		if (updated.getBrokerHostname() != null) {
			editor.putString(IMqttServicePreferences.PREF_SITEWHERE_MQTT_BROKER_HOSTNAME,
					updated.getBrokerHostname());
		}
		if (updated.getBrokerPort() != null) {
			editor.putInt(IMqttServicePreferences.PREF_SITEWHERE_MQTT_BROKER_PORT,
					updated.getBrokerPort());
		}
		if (updated.getDeviceHardwareId() != null) {
			editor.putString(IMqttServicePreferences.PREF_SITEWHERE_DEVICE_HARDWARE_ID,
					updated.getDeviceHardwareId());
		}
		editor.commit();
		return MqttServicePreferences.loadFrom(prefs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.mqtt.preferences.IMqttConnectivityPreferences#getBrokerHostname()
	 */
	public String getBrokerHostname() {
		return brokerHostname;
	}

	public void setBrokerHostname(String brokerHostname) {
		this.brokerHostname = brokerHostname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.mqtt.preferences.IMqttServicePreferences#getBrokerPort()
	 */
	public Integer getBrokerPort() {
		return brokerPort;
	}

	public void setBrokerPort(Integer brokerPort) {
		this.brokerPort = brokerPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.mqtt.preferences.IMqttServicePreferences#getDeviceHardwareId()
	 */
	public String getDeviceHardwareId() {
		return deviceHardwareId;
	}

	public void setDeviceHardwareId(String deviceHardwareId) {
		this.deviceHardwareId = deviceHardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof MqttServicePreferences) {
			MqttServicePreferences other = (MqttServicePreferences) o;
			if ((brokerHostname == null) || (!brokerHostname.equals(other.getBrokerHostname()))) {
				return false;
			}
			if ((brokerPort == null) || (!brokerPort.equals(other.getBrokerPort()))) {
				return false;
			}
			if ((deviceHardwareId == null)
					|| (!deviceHardwareId.equals(other.getDeviceHardwareId()))) {
				return false;
			}
			return true;
		} else {
			return super.equals(o);
		}
	}
}