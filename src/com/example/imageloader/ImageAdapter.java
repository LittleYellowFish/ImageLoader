package com.example.imageloader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter implements OnScrollListener {

	private List<String> mUrlList;
	private LayoutInflater mInflater;
	private boolean mIsGridViewIdle = true;
	private boolean mCanGetBitmapFromNetWork = false;
	private ImageLoader mImageLoader;
	private int reqWidth = 500;
	private int reqHeight = 500;

	public ImageAdapter(Context context, List<String> mUrlList) {
		super();
		if (mUrlList == null) {
			this.mUrlList = new ArrayList<>();
		}
		this.mUrlList = mUrlList;
		mInflater = LayoutInflater.from(context);

		mImageLoader = ImageLoader.build(context);
	}

	public void setReqWidth(int reqWidth) {
		this.reqWidth = reqWidth;
	}

	public void setReqHeight(int reqHeight) {
		this.reqHeight = reqHeight;
	}

	public void setmCanGetBitmapFromNetWork(boolean mCanGetBitmapFromNetWork) {
		this.mCanGetBitmapFromNetWork = mCanGetBitmapFromNetWork;
	}

	@Override
	public int getCount() {
		return mUrlList.size();
	}

	@Override
	public Object getItem(int position) {
		return mUrlList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		ImageView imageView = viewHolder.imageView;
		final String tag = (String) imageView.getTag();
		final String uri = (String) getItem(position);
		if (!uri.equals(tag)) {
			imageView.setImageResource(R.drawable.ic_launcher);
		}
		if (mIsGridViewIdle && mCanGetBitmapFromNetWork) {
			imageView.setTag(uri);
			mImageLoader.bindBitmap(uri, imageView, reqWidth, reqHeight);
		}

		Log.d("ImageAdapter", "getView()");
		return convertView;
	}

	public static class ViewHolder {
		private ImageView imageView;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mIsGridViewIdle = true;
			notifyDataSetChanged();
		} else {
			mIsGridViewIdle = false;
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}
}
