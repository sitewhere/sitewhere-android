package com.sitewhere.android.mqtt.preferences;

import com.sitewhere.android.preferences.IConnectivityPreferences;

/**
 * Provides constants for connectivity preferences.
 * 
 * @author Derek
 */
public interface IMqttConnectivityPreferences extends IConnectivityPreferences {

	/** Preference for base URI used to access SiteWhere APIs */
	public static final String PREF_SITEWHERE_MQTT_BROKER_URI = "sw_mqtt_uri";
}