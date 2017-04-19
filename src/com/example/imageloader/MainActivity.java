package com.example.imageloader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends Activity {

	private boolean mIsWifi = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		GridView gridView = (GridView) findViewById(R.id.gridview);

		final ImageAdapter adapter = new ImageAdapter(this, getUrlList());
		gridView.setAdapter(adapter);
		gridView.setOnScrollListener(adapter);

		if (mIsWifi) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("初次使用会从网络下载大概5m的图片，确认要下载吗？");
			builder.setTitle("注意");
			builder.setPositiveButton("是", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					adapter.setmCanGetBitmapFromNetWork(true);
					adapter.notifyDataSetChanged();
				}
			});
			builder.setNegativeButton("否", null);
			builder.show();
		}
	}

	private List<String> getUrlList() {
		List<String> urls = new ArrayList<>();
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596677131&di=879095fbcbe6b7b94b8516f0fd1b6cb1&imgtype=0&src=http%3A%2F%2Fimg3.duitang.com%2Fuploads%2Fitem%2F201510%2F10%2F20151010211325_ZdA4R.jpeg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1493191411&di=951d7ed4424471319039852ab82e0d45&imgtype=jpg&er=1&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F17%2F14%2F25%2F43Y58PICfJB_1024.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691718&di=1d69d04337be7212de38f810d52e394f&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2015%2Fxu%2F04%2F17%2F2%2F7.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691717&di=789b5ff8d900e67c8cfac6f2f113b2d1&imgtype=0&src=http%3A%2F%2Fimg.tuku.cn%2Ffile_big%2F201502%2F0e93d8ab02314174a933b5f00438d357.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691717&di=52c3272c7bc376c9bc13ecc10dcc6b5d&imgtype=0&src=http%3A%2F%2Fimg.tuku.cn%2Ffile_big%2F201503%2Fd8905515d1c046aeba51025f0ea842f0.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691717&di=43e3fc3c88d48cffd04f7a5eac98fcfc&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Fallimg%2F150819%2F14-150QZ9194K27.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691717&di=cad58a78a2915404da4a64b259bea885&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2013%2Fmxy%2F10%2F12%2F2%2F4.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691716&di=1b47ef291ce63ecb57bd7afe8f8cce14&imgtype=0&src=http%3A%2F%2Fimg05.tooopen.com%2Fimages%2F20150105%2Fsy_78543795524.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691713&di=573e8937516c993de6ac500d9d1bb6da&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2013%2Fmxy%2F10%2F12%2F1%2F15.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691713&di=2ed7ee352d176591cada1042570a5e3d&imgtype=0&src=http%3A%2F%2Fpic33.nipic.com%2F20131007%2F13644136_140929204101_2.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691712&di=0a1f669028e6330b884ecfdf75dc284e&imgtype=0&src=http%3A%2F%2Fimg3.duitang.com%2Fuploads%2Fitem%2F201510%2F10%2F20151010211325_ZdA4R.jpeg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691711&di=0e4055145e72dbd748513831354788c6&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F10%2F32%2F14%2F72bOOOPIC7a.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691712&di=38f9a5bcac3a02903dd9908c4ca2ce40&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2012%2F1210%2Fczs%2F02%2Fqihuanfengjing%2520%25284%2529.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691710&di=735162a55dac771cc6ecb75c18d9f873&imgtype=0&src=http%3A%2F%2Fsc.jb51.net%2Fuploads%2Fallimg%2F150629%2F14-15062911450U38.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691705&di=c4bfef31d8faedda99ce1a7167b4c149&imgtype=0&src=http%3A%2F%2Fpic38.nipic.com%2F20140228%2F3822951_135521683000_2.jpg");
		urls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492596691705&di=2ebb7e0a92eb78c043a1e337746d6912&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F11%2F85%2F40%2F36458PICzCe.jpg");
		return urls;
	}

}
