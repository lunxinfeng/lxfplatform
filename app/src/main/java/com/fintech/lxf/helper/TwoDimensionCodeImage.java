package com.fintech.lxf.helper;

import android.graphics.Bitmap;

import jp.sourceforge.qrcode.data.QRCodeImage;

public class TwoDimensionCodeImage implements QRCodeImage {

    Bitmap bitmap;

    public TwoDimensionCodeImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public int getPixel(int x, int y) {
        return bitmap.getPixel(x, y);
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

}

