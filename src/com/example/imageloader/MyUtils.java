package com.example.imageloader;

import java.io.Closeable;
import java.io.IOException;

public class MyUtils {
	/**
	 * 关闭当前流
	 * 
	 * @param closeable
	 */
	public static void close(Closeable closeable) {// 只需要把你想要关闭的流传入就可以关闭此流了
		if (null != closeable) {
			try {
				closeable.close();// 此接口只有一个关闭流的方法
			} catch (IOException e) {
				System.out.println("关闭流出错了,错误信息---->" + e);
			}
		}
	}
}
