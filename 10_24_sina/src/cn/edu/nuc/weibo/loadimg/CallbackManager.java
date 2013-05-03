package cn.edu.nuc.weibo.loadimg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

public class CallbackManager {
	private ConcurrentHashMap<String, List<ImageCallback>> mCallbackMap = null;

	public CallbackManager() {
		mCallbackMap = new ConcurrentHashMap<String, List<ImageCallback>>();
	}

	public void put(String url, ImageCallback imageCallback) {
		if (!mCallbackMap.contains(url)) {
			mCallbackMap.put(url, new ArrayList<ImageCallback>());
		}
		mCallbackMap.get(url).add(imageCallback);
	}

	public void callback(String urlStr, Bitmap bitmap) {
		List<ImageCallback> mcallbacks = mCallbackMap.get(urlStr);
		if (mcallbacks == null) {
			return;
		}
		for (ImageCallback imageCallback : mcallbacks) {
			imageCallback.imageLoad(urlStr, bitmap);
		}
		mcallbacks.clear();
		mCallbackMap.remove(urlStr);
	}

}
