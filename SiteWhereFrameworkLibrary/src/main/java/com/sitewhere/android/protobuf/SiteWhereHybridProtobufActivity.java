package com.sitewhere.android.protobuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;

import com.sitewhere.spi.device.event.IDeviceEventOriginator;

/**
 * Extends {@link SiteWhereProtobufActivity} to automatically decode custom commands that were encoded using
 * the hybrid Java format. The hybid format uses protocol buffers for system commands and responses, but
 * encoded custom commands as serialized Java objects so methods can dynamically be called rather than relying
 * on compiled stubs from protocol buffers.
 * 
 * @author Derek
 */
public abstract class SiteWhereHybridProtobufActivity extends SiteWhereProtobufActivity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.android.protobuf.SiteWhereProtobufActivity#onReceivedCustomCommand(byte[])
	 */
	@Override
	protected void onReceivedCustomCommand(byte[] payload) {
		super.onReceivedCustomCommand(payload);

		try {
			ByteArrayInputStream encoded = new ByteArrayInputStream(payload);
			ObjectInputStream in = new ObjectInputStream(encoded);

			String commandName = (String) in.readObject();
			Object[] parameters = (Object[]) in.readObject();
			Object[] parametersWithOriginator = new Object[parameters.length + 1];
			Class<?>[] types = new Class[parameters.length];
			Class<?>[] typesWithOriginator = new Class[parameters.length + 1];
			int i = 0;
			for (Object parameter : parameters) {
				types[i] = parameter.getClass();
				typesWithOriginator[i] = types[i];
				parametersWithOriginator[i] = parameters[i];
				i++;
			}
			IDeviceEventOriginator originator = (IDeviceEventOriginator) in.readObject();
			typesWithOriginator[i] = IDeviceEventOriginator.class;
			parametersWithOriginator[i] = originator;

			Method method = null;
			try {
				method = getClass().getMethod(commandName, typesWithOriginator);
				method.invoke(this, parametersWithOriginator);
			} catch (NoSuchMethodException e) {
				method = getClass().getMethod(commandName, types);
				method.invoke(this, parameters);
			}
		} catch (StreamCorruptedException e) {
			Log.e(TAG, "Unable to decode command in hybrid mode.", e);
		} catch (IOException e) {
			Log.e(TAG, "Unable to read command in hybrid mode.", e);
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Unable to resolve parameter class.", e);
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "Unable to find method signature that matches command.", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "Not allowed to call method for command.", e);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Invalid argument for command.", e);
		} catch (InvocationTargetException e) {
			Log.e(TAG, "Unable to call method for command.", e);
		}
	}
}