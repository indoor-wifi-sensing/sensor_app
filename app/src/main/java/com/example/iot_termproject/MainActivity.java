package com.example.iot_termproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DrawTouchView mView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mLastAccelerometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mView = new DrawTouchView(this);
        setContentView(mView);

        // SensorManager 초기화
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 가속도계 센서 초기화
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 지자기 센서 초기화
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // SensorEventListener 초기화
        sensorEventListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                // 지자기 센서 값이 변경될 때 호출되는 메서드
//                float magX = event.values[0];
//                float magY = event.values[1];
//                float magZ = event.values[2];
//                float a = (magX * magY) + (magY * magZ) + (magZ * magX);
//                double magnitude;
//                if(a > 0) {
//                    magnitude = Math.sqrt(a);
//                } else {
//                    magnitude = -Math.sqrt(-a);
//                }
//
//                float azimuth = event.values[0];  // 방위각 값
//                android.util.Log.e("test", "X " + Float.toString(magX));
//                android.util.Log.e("test", "Y " + Float.toString(magY));
//                android.util.Log.e("test", "Z " + Float.toString(magZ));
//                android.util.Log.e("test", "magnitude " + Double.toString(magnitude));
//
//                // 지자기 센서 값을 전달하여 이미지 회전
//                mView.rotateImage((float) magnitude);
//            }
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor == accelerometer) {
                    System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                    mLastAccelerometerSet = true;
                } else if(event.sensor == magnetometer) {
                    System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                    mLastMagnetometerSet = true;
                }
                if(mLastAccelerometerSet && mLastMagnetometerSet) {
                    SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                    float azimuthinDegress  = (int) ( Math.toDegrees( SensorManager.getOrientation( mR, mOrientation)[0] ) + 360 ) % 360;
                    android.util.Log.e("test", "Z " + Float.toString(azimuthinDegress));
                    azimuthinDegress = 360 - azimuthinDegress;
                    mView.rotateImage((float) azimuthinDegress);
//                    RotateAnimation ra = new RotateAnimation(
//                            mCurrentDegree,
//                            -azimuthinDegress,
//                            Animation.RELATIVE_TO_SELF, 0.5f,
//                            Animation.RELATIVE_TO_SELF, 0.5f
//                    );
//                    ra.setDuration(250);
//                    ra.setFillAfter(true);
//                    mPointer.startAnimation(ra);
//                    mCurrentDegree = -azimuthinDegress;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // 센서의 정확도가 변경될 때 호출되는 메서드
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.startRotation();

        // 가속도계 센서 등록
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // 지자기 센서 등록
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.stopRotation();

        // 지자기 센서 해제
        sensorManager.unregisterListener(sensorEventListener);
    }


    public class DrawTouchView extends View {
        private static final int ROTATION_DELAY = 1000; // 1초
        private static final float DEGREE_PER_FRAME = 1.0f;

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Bitmap image;
        private float canvasX, canvasY;
        private float centerX, centerY;
        private Matrix matrix;
        private boolean rotating = false;
        private Handler rotationHandler;
        private String distanceText = "10.2m"; // 추가된 변수: 텍스트

        public DrawTouchView(Context context) {
            super(context);
            init();
        }

        public DrawTouchView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DrawTouchView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        private void init() {
            // 이미지 로드
            image = BitmapFactory.decodeResource(getResources(), R.drawable.downarrow);

            // 회전을 위한 Matrix 객체 생성
            matrix = new Matrix();

            rotationHandler = new Handler();
        }

        public void startRotation() {
            rotating = true;
            rotateImage(0);
        }

        public void stopRotation() {
            rotating = false;
            rotationHandler.removeCallbacksAndMessages(null);
        }

        public void rotateImage(float rotation) {
            if (rotating) {
//                float rotation = (float) Math.toDegrees(Math.atan2(x, y));
                rotation += 180;
                matrix.setRotate(rotation, image.getWidth() / 2.0f, image.getHeight() / 2.0f);
                invalidate();
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            // 이미지 중심 좌표 계산
            centerX = w / 2.0f;
            centerY = h / 2.0f;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            centerX = canvas.getWidth() / 2.0f - image.getWidth() / 2.0f;
            centerY = canvas.getHeight() / 2.0f - image.getHeight() / 2.0f;
            matrix.postTranslate(centerX, centerY);
            canvas.drawBitmap(image, matrix, paint);

            // 텍스트 그리기
            float textSize = 40; // 텍스트 크기 설정
            float textX = (canvas.getWidth() - paint.measureText(distanceText)) / 2.0f; // 가운데 정렬을 위한 X 좌표 계산
            float textY = textSize * 2; // 상단에 위치할 Y 좌표 설정

            paint.setTextSize(textSize);
            canvas.drawText(distanceText, textX, textY, paint);
        }
    }

}





//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Button btn = findViewById(R.id.btn);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, SensorTest.class));
//            }
//        });
//    }
//
//}