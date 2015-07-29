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
package com.sitewhere.android;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;

import com.sitewhere.android.messaging.IFromSiteWhere;
import com.sitewhere.android.messaging.ISiteWhereMessaging;
import com.sitewhere.android.messaging.IToSiteWhere;
import com.sitewhere.android.messaging.SiteWhereMessagingException;

/**
 * Base class for acitivities that require SiteWhere support.
 * 
 * @author Derek
 */
public abstract class SiteWhereActivity extends Activity {

	/** Tag for logging */
	public static final String TAG = "SiteWhereActivity";

	/** Proxy to send messages */
	protected IToSiteWhere sitewhere;

	/** Handles responses from SiteWhere */
	protected SiteWhereResponseProcessor responseProcessor = new SiteWhereResponseProcessor();

	/** Indicates if bound to service */
	protected boolean bound = false;

	/**
	 * Get class used to create a local service instance.
	 * 
	 * @return
	 */
	protected abstract Class<? extends Service> getServiceClass();

	/**
	 * Gets the configuration data passed to the {@link Intent} for the service.
	 * 
	 * @return
	 */
	protected abstract Parcelable getServiceConfiguration();

	/**
	 * Called after connection to underlying messaging service is complete.
	 */
	protected abstract void onConnectedToSiteWhere();

	/**
	 * Called when a custom command payload is received.
	 */
	protected abstract void onReceivedCustomCommand(byte[] payload);

	/**
	 * Called when a custom command payload is received.
	 */
	protected abstract void onReceivedSystemCommand(byte[] payload);

	/**
	 * Called when connection to SiteWhere is disconnected.
	 */
	protected abstract void onDisconnectedFromSiteWhere();

	/**
	 * Create a connection to SiteWhere.
	 */
	protected void connectToSiteWhere() {
		connectToSiteWhereLocal();
	}

	/**
	 * Creates a connection to SiteWhere
	 */
	protected void connectToSiteWhereLocal() {
		if (!bound) {
			Intent intent = new Intent(this, getServiceClass());
			intent.putExtra(ISiteWhereMessaging.EXTRA_CONFIGURATION, getServiceConfiguration());
			startService(intent);
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	/**
	 * Attempts to connect to a remote {@link Service} using an {@link Intent} that will match based on a
	 * filter.
	 */
	protected void connectToSiteWhereRemote() {
		if (!bound) {
			Intent intent = new Intent(ISiteWhereMessaging.MESSAGING_SERVICE);
			intent.putExtra(ISiteWhereMessaging.EXTRA_CONFIGURATION, getServiceConfiguration());
			startService(intent);
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	/**
	 * Indicates whether SiteWhere connection is established.
	 * 
	 * @return
	 */
	protected boolean isConnectedToSiteWhere() {
		return bound;
	}

	/**
	 * Send command to SiteWhere.
	 */
	protected void sendCommand(byte[] payload) throws SiteWhereMessagingException {
		try {
			sitewhere.send(payload);
		} catch (RemoteException e) {
			throw new SiteWhereMessagingException("Unable to send command.", e);
		}
	}

	/**
	 * Disconnect from the underlying messaging service.
	 */
	protected void disconnectFromSiteWhere() {
		if ((serviceConnection != null) && (bound)) {
			if (sitewhere != null) {
				try {
					sitewhere.unregister(responseProcessor);
					Log.d(TAG, "No longer registered with SiteWhere messaging service.");
				} catch (RemoteException e) {
					Log.e(TAG, "Unable to unregister from response processor.", e);
				}
			}
			unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	/**
	 * Gets the unique id for a device.
	 * 
	 * @return
	 */
	public String getUniqueDeviceId() {
		String id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		if (id == null) {
			throw new RuntimeException(
					"Running in context that does not have a unique id. Override getUniqueDeviceId() in subclass.");
		}
		return id;
	}

	/** Handles connection to message service */
	protected ServiceConnection serviceConnection = new ServiceConnection() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName,
		 * android.os.IBinder)
		 */
		public void onServiceConnected(ComponentName className, IBinder service) {
			sitewhere = IToSiteWhere.Stub.asInterface(service);
			try {
				sitewhere.register(responseProcessor);
				bound = true;
				Log.d(TAG, "Registered with SiteWhere messaging service.");
			} catch (RemoteException e) {
				Log.e(TAG, "Unable to register with SiteWhere messaging service.");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		public void onServiceDisconnected(ComponentName className) {
			bound = false;
			try {
				sitewhere.unregister(responseProcessor);
			} catch (RemoteException e) {
				Log.e(TAG, "Unable to disconnect from messaging.");
			}
			sitewhere = null;
			responseProcessor = null;
			onDisconnectedFromSiteWhere();
			Log.d(TAG, "Disconnected from service.");
		}
	};

	/** Handles responses sent from the message service */
	protected class SiteWhereResponseProcessor extends IFromSiteWhere.Stub {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sitewhere.android.messaging.IFromSiteWhere#connected()
		 */
		@Override
		public void connected() throws RemoteException {
			onConnectedToSiteWhere();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sitewhere.android.messaging.IFromSiteWhere#receivedCustomCommand(byte[])
		 */
		@Override
		public void receivedCustomCommand(byte[] payload) throws RemoteException {
			onReceivedCustomCommand(payload);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sitewhere.android.messaging.IFromSiteWhere#receivedSystemCommand(byte[])
		 */
		@Override
		public void receivedSystemCommand(byte[] payload) throws RemoteException {
			onReceivedSystemCommand(payload);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sitewhere.android.messaging.IFromSiteWhere#disconnected()
		 */
		@Override
		public void disconnected() throws RemoteException {
			onDisconnectedFromSiteWhere();
		}
	}
}
