package com.example.imageloader;

import java.io.FileDescriptor;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class ImageResizer {

	/**
	 * ������ԴidΪresId��ͼƬ
	 * 
	 * @param res
	 * @param resId
	 *            ��Դid
	 * @param reqWidth
	 *            ���ź�Ŀ��
	 * @param reqHeight
	 *            ���ź�ĸ߶�
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
	 * ��ͼƬ�ļ���������
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
	 * ��ͼƬ�ļ���������
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
	 * ����inSampleSize
	 * 
	 * @param options
	 * @param reqWidth
	 *            ���ź�Ŀ��
	 * @param reqHeight
	 *            ���ź�ĸ߶�
	 * @return
	 */
	private static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// ����inSampleSizeֱ�����ź�Ŀ�߶�С��ָ���Ŀ��
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
		//��ȡ������С
		int wRatio = (int)Math.ceil(options.outWidth/reqWidth);
		int hRatio = (int)Math.ceil(options.outHeight/reqHeight);
		//�������ָ����С������С��Ӧ�ı���
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
