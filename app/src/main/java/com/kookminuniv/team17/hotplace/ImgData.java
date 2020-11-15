package com.kookminuniv.team17.hotplace;

import android.graphics.Bitmap;

public class ImgData {
    private Bitmap img;

    public ImgData(Bitmap img){
        this.img = img;
    }

    public void setImg(Bitmap img){
        this.img = img;
    }

    public Bitmap getImg(){
        return img;
    }
}
