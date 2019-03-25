package com.ldw.music.shake;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class ShakeDetector implements SensorEventListener {
	
	private SensorManager mSensorManager;
	private OnShakeListener mOnShakeListener;
	private float lowX, lowY, lowZ;
	private final float FILTERING_VALAUE = 0.1f;

	private boolean isDetector;

	public ShakeDetector(Context context) {

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			float x = event.values[SensorManager.DATA_X];
			float y = event.values[SensorManager.DATA_Y];
			float z = event.values[SensorManager.DATA_Z];

			lowX = x * FILTERING_VALAUE + lowX * (1.0f - FILTERING_VALAUE);
			lowY = y * FILTERING_VALAUE + lowY * (1.0f - FILTERING_VALAUE);
			lowZ = z * FILTERING_VALAUE + lowZ * (1.0f - FILTERING_VALAUE);

			float highX = x - lowX;
			float highY = y - lowY;
			float highZ = z - lowZ;
			
			if(highX >= 10 || highY >= 10 || highZ >= 10) {
				mOnShakeListener.onShake();
				stop();
			}
			
//			System.out.println(highX+"----"+highY+"----"+highZ);
//
		}
		
//		float newX = Math.abs(event.values[SensorManager.DATA_X]);
//		float newY = Math.abs(event.values[SensorManager.DATA_Y]);
//		float newZ = Math.abs(event.values[SensorManager.DATA_Z]);
//
//		if (newX >= 14 || newY >= 14 || newZ >= 14) {
//			//Log.e("zz", "if shaking then act it！");
//			mOnShakeListener.onShake();
//		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}


	public void start() {
		if(isDetector) {
			return;
		}
		if (mSensorManager == null) {
			throw new UnsupportedOperationException();
		}


		boolean success = mSensorManager
				.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_NORMAL);

		if (!success) {
			throw new UnsupportedOperationException();
		}
		isDetector = true;
	}


	public void stop() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this); 
		}
		isDetector = false;
	}
	
	public boolean isDetector() {
		return isDetector;
	}

	public interface OnShakeListener {

		void onShake();
	}

	public void setOnShakeListener(OnShakeListener mOnShakeListener) {
		this.mOnShakeListener = mOnShakeListener;
	}

}
