package com.sitewhere.android.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sitewhere.android.messaging.SiteWhereMessagingException;
import com.sitewhere.android.protobuf.SiteWhereProtobufActivity;

/**
 * Fragment that implements example application.
 * 
 * @author Derek
 */
public class ExampleFragment extends Fragment implements LocationListener {

	/** Tag used for logging */
	private static final String TAG = "ExampleFragment";

	/** Interval at which locations are sent */
	private static final int SEND_INTERVAL_IN_SECONDS = 5;

	/** Manages location updates */
	protected LocationManager locationManager;

	/** Last reported location */
	private Location lastLocation;

	/** Used to schedule a recurring report to SiteWhere for location */
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
	 * android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.example_app, container, false);
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		scheduler.scheduleAtFixedRate(new LocationReporter(), SEND_INTERVAL_IN_SECONDS,
				SEND_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);

		Button sendLocation = (Button) getActivity().findViewById(R.id.main_send_location);
		sendLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onSendCurrentLocation();
			}
		});
	}

	/**
	 * Sends current location to SiteWhere.
	 */
	protected void onSendCurrentLocation() {
		if (lastLocation != null) {
			SiteWhereProtobufActivity sw = (SiteWhereProtobufActivity) getActivity();
			try {
				sw.sendLocation(sw.getUniqueDeviceId(), null, lastLocation.getLatitude(),
						lastLocation.getLongitude(), lastLocation.getAltitude());
				sw.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(getActivity(), "Sent Location to SiteWhere",
								Toast.LENGTH_SHORT).show();
					}
				});
			} catch (SiteWhereMessagingException e) {
				Log.e(TAG, "Unable to send location to SiteWhere.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		this.lastLocation = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Location provider (" + provider + ") disabled.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Location provider (" + provider + ") enabled.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int,
	 * android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
		}
	}

	/**
	 * Runs location reporting in a separate thread.
	 * 
	 * @author Derek
	 */
	private class LocationReporter implements Runnable {

		@Override
		public void run() {
			onSendCurrentLocation();
		}
	}
}