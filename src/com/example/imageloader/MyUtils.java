package com.example.imageloader;

import java.io.Closeable;
import java.io.IOException;

public class MyUtils {
	/**
	 * �رյ�ǰ��
	 * 
	 * @param closeable
	 */
	public static void close(Closeable closeable) {// ֻ��Ҫ������Ҫ�رյ�������Ϳ��Թرմ�����
		if (null != closeable) {
			try {
				closeable.close();// �˽ӿ�ֻ��һ���ر����ķ���
			} catch (IOException e) {
				System.out.println("�ر���������,������Ϣ---->" + e);
			}
		}
	}
}
