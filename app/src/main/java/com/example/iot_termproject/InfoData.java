package com.example.iot_termproject;

public class InfoData {

    int circleX;
    int circleY;
    int radius;

    /**
     *  원을 캔버스에 그리기 위해 필요한 데이터
     * @param circleX - 현재 원이 위치하고 있는 x좌표값
     * @param circleY - 현재 원이 위치하고 있는 y좌표값
     * @param radius - 현재 원의 반지름값 ( 원 크기를 결정 )
     *
     */
    public InfoData(int circleX, int circleY, int radius) {
        this.circleX = circleX;
        this.circleY = circleY;
        this.radius = radius;
    }

    public int getCircleX() {
        return circleX;
    }

    public void setCircleX(int circleX) {
        this.circleX = circleX;
    }

    public int getCircleY() {
        return circleY;
    }

    public void setCircleY(int circleY) {
        this.circleY = circleY;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

}
