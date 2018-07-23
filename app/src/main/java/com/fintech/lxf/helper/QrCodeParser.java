package com.fintech.lxf.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.IOException;
import java.util.Hashtable;

import jp.sourceforge.qrcode.QRCodeDecoder;

public class QrCodeParser {

    public static String decodeImg(String path) throws IOException {

        Bitmap scanBitmap;
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小

        int sampleSize = (int) (options.outHeight / (float) 200);

        if (sampleSize <= 0)
            sampleSize = 1;

        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        if (scanBitmap == null) {
            return null;
        }

        if (scanBitmap.getWidth() % 2 != 0 ){
            scanBitmap = Bitmap.createBitmap(scanBitmap,0,0,scanBitmap.getWidth() -1 ,scanBitmap.getHeight());
            Log.d("QrCodeParser", "decodeImg: width为奇数");
        }
        if (scanBitmap.getHeight() % 2 != 0 ){
            scanBitmap = Bitmap.createBitmap(scanBitmap,0,0,scanBitmap.getWidth() ,scanBitmap.getHeight() - 1);
            Log.d("QrCodeParser", "decodeImg: height为奇数");
        }

        LuminanceSource source = new PlanarYUVLuminanceSource(
                rgb2YCbCr420(scanBitmap), scanBitmap.getWidth(),
                scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(),
                scanBitmap.getHeight(), false);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(binaryBitmap).getText();
        } catch (Exception e) {
            e.printStackTrace();
            return decoderQRCode(path);
        }

    }


    /**
     * RGB图片转YUV420数据
     * 宽、高不能为奇数
     *
     * @return
     */
    public static byte[] rgb2YCbCr420(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int len = width * height;
        //yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //屏蔽ARGB的透明度值
                int rgb = pixels[i * width + j] & 0x00FFFFFF;
                //像素的颜色顺序为bgr，移位运算。
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                //套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                //调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                //赋值
                yuv[i * width + j] = (byte) y;
                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;// #
                yuv[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;// #
            }
        }
        return yuv;
    }

    public static String decoderQRCode(String imgPath) throws IOException {
        // QRCode 二维码图片的文件
        Bitmap scanBitmap = BitmapFactory.decodeFile(imgPath);
        QRCodeDecoder decoder = new QRCodeDecoder();
        return new String(decoder.decode(new TwoDimensionCodeImage(scanBitmap)), "utf-8");
    }

}
