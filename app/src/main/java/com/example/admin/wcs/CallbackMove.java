package com.example.admin.wcs;

public interface CallbackMove {
    void MoveUpdate(float x, float y, float degree);
    void MoveUpdate();
    void MoveStorage(float x, float y, float degree);
    float MoveGetX();
    float MoveGetY();
    float MoveGetDegree();
}