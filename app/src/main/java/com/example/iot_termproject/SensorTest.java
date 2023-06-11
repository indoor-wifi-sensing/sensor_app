package com.example.iot_termproject;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class SensorTest extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;

    private ImageView compassImage;
    private TextView headingText;

    private float currentDegree = 0f;
    private float[] rotationMatrix = new float[9];
    private float[] remappedRotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    private float[] gyroscopeValues = new float[3];
    private float[] accelerometerValues = new float[3];

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;

    // 0에 북쪽 90에 서쪽 180에 남쪽 270에 동쪽으로 0~360까지
    private float targetDegree = 90f;  // 목표 방위각 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        compassImage = findViewById(R.id.compass_image);
        headingText = findViewById(R.id.heading_text);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            // 원하는 축으로 회전 행렬을 변경
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix);

            SensorManager.getOrientation(remappedRotationMatrix, orientationAngles);

            float azimuthInRadians = orientationAngles[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

            float azimuthInDegreesWithOffset = -azimuthInDegrees - targetDegree;  // 목표 방위각을 적용하여 회전

            // 방위각이 음수인 경우에 대한 처리
            if (azimuthInDegreesWithOffset < 0) {
                azimuthInDegreesWithOffset += 360f;
            }

            RotateAnimation rotateAnimation = new RotateAnimation(
                    currentDegree,
                    azimuthInDegreesWithOffset,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);

            compassImage.startAnimation(rotateAnimation);
            currentDegree = azimuthInDegreesWithOffset;  // 현재 각도를 업데이트

            String heading = "현재 방향: " + Math.round(currentDegree) + "도";
            headingText.setText(heading);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                gyroscopeValues[0] += axisX * dT;
                gyroscopeValues[1] += axisY * dT;
                gyroscopeValues[2] += axisZ * dT;

                float rotationZ = -gyroscopeValues[2];

                currentDegree += Math.toDegrees(rotationZ);

                RotateAnimation rotateAnimation = new RotateAnimation(
                        (float) (currentDegree - Math.toDegrees(rotationZ)),
                        currentDegree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(250);
                rotateAnimation.setFillAfter(true);

                compassImage.startAnimation(rotateAnimation);

                String heading = "현재 방향: " + Math.round(currentDegree) + "도";
                headingText.setText(heading);
            }
            timestamp = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerValues, 0, event.values.length);

            float[] gravity = new float[3];
            final float alpha = 0.8f;

            // 중력 센서 데이터를 필터링하여 추출
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // 가속도 센서 데이터에서 중력을 제거하여 나머지 움직임을 추출
            float[] linearAcceleration = new float[3];
            linearAcceleration[0] = event.values[0] - gravity[0];
            linearAcceleration[1] = event.values[1] - gravity[1];
            linearAcceleration[2] = event.values[2] - gravity[2];

            // 나머지 움직임을 활용하여 회전 행렬 계산
            float[] rotation = new float[9];
            SensorManager.getRotationMatrixFromVector(rotation, linearAcceleration);

            // 회전 행렬을 적용하여 방향 계산
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotation, orientation);

            float azimuthInRadians = orientation[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

            float azimuthInDegreesWithOffset = -azimuthInDegrees - targetDegree;  // 목표 방위각을 적용하여 회전

            // 방위각이 음수인 경우에 대한 처리
            if (azimuthInDegreesWithOffset < 0) {
                azimuthInDegreesWithOffset += 360f;
            }

            RotateAnimation rotateAnimation = new RotateAnimation(
                    currentDegree,
                    azimuthInDegreesWithOffset,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);

            compassImage.startAnimation(rotateAnimation);
            currentDegree = azimuthInDegreesWithOffset;  // 현재 각도를 업데이트

            String heading = "현재 방향: " + Math.round(currentDegree) + "도";
            headingText.setText(heading);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서 정확도 변경 시 호출되는 콜백 메서드
    }
}
