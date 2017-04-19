package com.example.imageloader;

import java.io.FileDescriptor;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class ImageResizer {

	/**
	 * 缩放资源id为resId的图片
	 * 
	 * @param res
	 * @param resId
	 *            资源id
	 * @param reqWidth
	 *            缩放后的宽度
	 * @param reqHeight
	 *            缩放后的高度
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * 对图片文件进行缩放
	 * @param fd
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd,null, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;

		try {
			return BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (Exception e) {
			Log.e("ImageResizer", "decodeFileDescriptor error:"+e);
		}
		return null;
	}
	/**
	 * 对图片文件进行缩放
	 * @param path
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		options.inSampleSize = calculateInSampleSizeFromFile(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeFile(path, options);
	}

	/**
	 * 计算inSampleSize
	 * 
	 * @param options
	 * @param reqWidth
	 *            缩放后的宽度
	 * @param reqHeight
	 *            缩放后的高度
	 * @return
	 */
	private static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// 计算inSampleSize直到缩放后的宽高都小于指定的宽高
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}

		}

		System.out.println(inSampleSize);
		return inSampleSize;
	}

	/**
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private static int calculateInSampleSizeFromFile(Options options, int reqWidth, int reqHeight) {
		//获取比例大小
		int wRatio = (int)Math.ceil(options.outWidth/reqWidth);
		int hRatio = (int)Math.ceil(options.outHeight/reqHeight);
		//如果超出指定大小，则缩小相应的比例
		if(wRatio > 1 && hRatio > 1){
			if(wRatio > hRatio){
				options.inSampleSize = wRatio;
			}else{
				options.inSampleSize = hRatio;
			}
		}
		System.out.println(options.inSampleSize);
		return options.inSampleSize;
	}
}
