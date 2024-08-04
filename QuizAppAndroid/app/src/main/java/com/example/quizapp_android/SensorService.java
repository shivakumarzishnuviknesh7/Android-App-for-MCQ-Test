package com.example.quizapp_android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class SensorService {

    private static volatile SensorService instance;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor lightSensor;
    private LightSensorListener lightSensorListener;
    private AccelerometerListener accelerometerListener;
    private QuestionsActivity questionsActivity; // Reference to QuestionsActivity for callback

    private SensorService(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public static SensorService getInstance(Context context) {
        if (instance == null) {
            synchronized (SensorService.class) {
                if (instance == null) {
                    instance = new SensorService(context);
                }
            }
        }
        return instance;
    }

    public void registerAccelerometerListener(QuestionsActivity activity) {
        this.questionsActivity = activity;
        if (accelerometer != null) {
            accelerometerListener = new AccelerometerListener();
            sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(activity, "Accelerometer not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void unregisterAccelerometerListener() {
        if (accelerometerListener != null) {
            sensorManager.unregisterListener(accelerometerListener);
            accelerometerListener = null;
            questionsActivity = null; // Release reference to activity
        }
    }

    public void registerLightSensorListener(Context context) {
        if (lightSensor != null) {
            lightSensorListener = new LightSensorListener(context);
            sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(context, "Light sensor not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void unregisterLightSensorListener() {
        if (lightSensorListener != null) {
            sensorManager.unregisterListener(lightSensorListener);
            lightSensorListener = null;
        }
    }

    private class AccelerometerListener implements SensorEventListener {

        private long lastShakeTime;
        private static final int SHAKE_THRESHOLD = 800;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > SHAKE_THRESHOLD) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
                    if (acceleration > 4) { // Adjust this threshold as needed
                        // Shake detected, notify QuestionsActivity
                        if (questionsActivity != null) {
                            questionsActivity.onShakeDetected(x); // Pass x-axis acceleration
                        }
                        lastShakeTime = currentTime;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private static class LightSensorListener implements SensorEventListener {

        private final Context context;

        LightSensorListener(Context context) {
            this.context = context;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float ambientLight = event.values[0];
                adjustScreenBrightness(ambientLight);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not used
        }

        private void adjustScreenBrightness(float ambientLight) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(context)) {
                        // Normalize the ambient light value to a brightness level (0-255)
                        int brightness = (int) (ambientLight / 10000 * 255);
                        brightness = Math.max(0, Math.min(brightness, 255));

                        // Set the system brightness level
                        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                    } else {
                        Toast.makeText(context, "Cannot write settings. Please enable permission.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e("LightSensorListener", "Error adjusting screen brightness", e);
            }
        }
    }
}