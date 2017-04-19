package com.example.imageloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public class ImageLoader {
	private static final String TAG = ImageLoader.class.getSimpleName();
	public static final int MESSAGE_POST_RESULT = 1;
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
	private static final long KEEP_ALIVE = 10L;

	private static final int TAG_KEY_URI = R.id.imageloder_uri;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100;
	private static final int IO_BUFFER_SIZE = 1024 * 8;
	private static final int DISK_CACHE_INDEX = 0;
	private boolean mIsDiskLruCacheCreated = false;

	/**
	 * 线程制造工厂
	 */
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {

			return new Thread(r, "imageloder#" + mCount.getAndIncrement());
		}
	};

	/**
	 * 自定义线程池
	 */
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
			KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

	private Handler mMainHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			LoaderResult result = (LoaderResult) msg.obj;
			ImageView imageView = result.imageView;
			String uri = (String) imageView.getTag(TAG_KEY_URI);
			if (uri.equals(result.uri)) {
				imageView.setImageBitmap(result.bitmap);
			} else {
				Log.w(TAG, "see image bitmap ,but rul has changed,ignored!");
			}
		};
	};

	private Context mContext;
	private ImageResizer mImageResizer = new ImageResizer();
	private LruCache<String, Bitmap> mMemoryCache;
	private DiskLruCache mDiskLruCache;

	private ImageLoader(Context context) {
		mContext = context.getApplicationContext();
		// 当前进程最大内存
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// 计算缓存大小
				return value.getRowBytes() * value.getHeight() / 1024;
			}
		};

		File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
		if (!diskCacheDir.exists()) {
			diskCacheDir.mkdirs();
		}
		/**
		 * 判断当前设备剩余的存储空间是否足够
		 */
		if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
			try {
				mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建ImageLoader单例
	 * 
	 * @param context
	 * @return
	 */
	public static ImageLoader build(Context context) {
		return new ImageLoader(context);
	}

	/**
	 * 添加到内存
	 * 
	 * @param key
	 * @param bitmap
	 */
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 从内存中取出
	 * 
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * 异步加载和显示图片
	 * 
	 * @param uri
	 * @param imageView
	 */
	public void bindBitmap(final String uri, final ImageView imageView) {
		bindBitmap(uri, imageView, 0, 0);
	}

	public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight) {
		imageView.setTag(TAG_KEY_URI, uri);
		Bitmap bitmap = loadBitmapFromMemCache(uri);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return;
		}

		Runnable loadBitmapTask = new Runnable() {

			@Override
			public void run() {
				Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
				if (bitmap != null) {
					LoaderResult result = new LoaderResult(imageView, uri, bitmap);
					mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
				}
			}
		};

		THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
	}

	protected Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
		Bitmap bitmap = loadBitmapFromMemCache(uri);
		if (bitmap != null) {
			Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
			return bitmap;
		}
		try {
			bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
			if (bitmap != null) {
				Log.d(TAG, "loadBitmapFromDiskCache,url:" + uri);
				return bitmap;
			}

			bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
			Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (bitmap == null && !mIsDiskLruCacheCreated) {
			Log.w(TAG, "encounter error,DiskChache is not created.");
			bitmap = downloadBitmapFromUrl(uri);
		}
		return bitmap;
	}

	/**
	 * 从网络下载图片不缓存
	 * 
	 * @param uri
	 * @return
	 */
	private Bitmap downloadBitmapFromUrl(String uri) {
		Bitmap bitmap = null;
		HttpURLConnection urlConnection = null;
		BufferedInputStream in = null;

		try {
			URL url = new URL(uri);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			bitmap = BitmapFactory.decodeStream(in);

		} catch (IOException e) {
			Log.e(TAG, "Error in downloadBitmap:" + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}

			MyUtils.close(in);
		}

		return bitmap;
	}

	/**
	 * 从网络下载图片并缓存
	 * 
	 * @param uri
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 * @throws IOException
	 */
	private Bitmap loadBitmapFromHttp(String uri, int reqWidth, int reqHeight) throws IOException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new RuntimeException("can not visit network from UI Thread");
		}
		if (mDiskLruCache == null) {
			return null;
		}
		String key = hashKeyFromUrl(uri);
		DiskLruCache.Editor editor = mDiskLruCache.edit(key);
		if (editor != null) {
			OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
			if (downloadUrlToStream(uri, outputStream)) {
				editor.commit();
			} else {
				editor.abort();
			}
			mDiskLruCache.flush();
		}

		return loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
	}

	private boolean downloadUrlToStream(String uri, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			final URL url = new URL(uri);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}

			return true;
		} catch (IOException e) {
			Log.e(TAG, "downloadBitmap failed." + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			MyUtils.close(out);
			MyUtils.close(in);
		}

		return false;
	}

	/**
	 * 从磁盘获取缓存并压缩存入内存当中
	 * 
	 * @param uri
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 * @throws IOException
	 */
	private Bitmap loadBitmapFromDiskCache(String uri, int reqWidth, int reqHeight) throws IOException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			Log.w(TAG, "loadBitmap from UI Thread,it's not recommended!");
		}
		if (mDiskLruCache == null) {
			return null;
		}

		Bitmap bitmap = null;
		String key = hashKeyFromUrl(uri);
		DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
		if (snapshot != null) {
			FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
			FileDescriptor fileDescriptor = fileInputStream.getFD();
			bitmap = mImageResizer.decodeSampledBitmapFromDescriptor(fileDescriptor, reqWidth, reqHeight);
			if (bitmap != null) {
				addBitmapToMemoryCache(key, bitmap);
			}
		}

		return bitmap;
	}

	/**
	 * 向内存中添加
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap loadBitmapFromMemCache(String url) {
		final String key = hashKeyFromUrl(url);
		Bitmap bitmap = getBitmapFromMemoryCache(key);
		return bitmap;
	}

	/**
	 * 对url进行md5加密
	 * 
	 * @param url
	 * @return
	 */
	private String hashKeyFromUrl(String url) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(url.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());

		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(url.hashCode());
		}
		return cacheKey;
	}

	/**
	 * 字节数组转化为16进制的字符串
	 * 
	 * @param bytes
	 * @return
	 */
	private String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * 获取设备的缓存路径
	 * 
	 * @param context
	 * @param uniqueName
	 * @return
	 */
	public File getDiskCacheDir(Context context, String uniqueName) {
		boolean externalStorageAvaliable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		final String cachePath;
		if (externalStorageAvaliable) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * sd卡剩余空间
	 * 
	 * @param path
	 * @return
	 */
	@TargetApi(VERSION_CODES.GINGERBREAD)
	private long getUsableSpace(File path) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * bitmap包装类
	 * 
	 * @author yl
	 *
	 */
	private static class LoaderResult {
		private ImageView imageView;
		private String uri;
		private Bitmap bitmap;

		public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
			super();
			this.imageView = imageView;
			this.uri = uri;
			this.bitmap = bitmap;
		}

	}
}
