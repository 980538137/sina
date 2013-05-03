package cn.edu.nuc.weibo.loadimg;

import java.lang.Thread.State;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
    private ImageManager mImageManager = null;
    private CallbackManager mCallbackManager = null;
    private BlockingQueue<String> mUrlQueue = null;
    private DownloadThread mDownloadThread = null;
    private static final String EXTRA_IMG_URL = "extra_img_url";
    private static final String EXTRA_IMG = "extra_img";

    public AsyncImageLoader(Context context) {
        mImageManager = new ImageManager(context);
        mCallbackManager = new CallbackManager();
        mUrlQueue = new ArrayBlockingQueue<String>(50);
        mDownloadThread = new DownloadThread();
    }

    /**
     * 获取图片
     * 
     * @param urlStr
     * @param imageCallback
     * @return
     */
    public Bitmap get(String urlStr, ImageCallback imageCallback) {
        if (mImageManager.contains(urlStr)) {
            return mImageManager.getFromCache(urlStr);
        } else {
            mCallbackManager.put(urlStr, imageCallback);
            startDownloadThread(urlStr);
        }
        return null;
    }

    /**
     * 开始下载线程
     * 
     * @param urlStr
     */
    private void startDownloadThread(String urlStr) {
        if (!mUrlQueue.contains(urlStr)) {
            try {
                mUrlQueue.put(urlStr);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        State mState = mDownloadThread.getState();
        if (mState == State.NEW) {
            mDownloadThread.start();
        } else if (mState == State.TERMINATED) {
            mDownloadThread = new DownloadThread();
            mDownloadThread.start();
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Bundle mbundle = msg.getData();
            String urlStr = (String) mbundle.getSerializable(EXTRA_IMG_URL);
            Bitmap bitmap = mbundle.getParcelable(EXTRA_IMG);
            mCallbackManager.callback(urlStr, bitmap);
        };
    };

    /**
     * 下载图片线程
     * 
     * @author songguoxing
     * 
     */
    private class DownloadThread extends Thread {
        private boolean flag = true;

        @Override
        public void run() {
            try {
                while (flag) {
                    String urlStr = mUrlQueue.poll();
                    if (urlStr == null) {
                        break;
                    }
                    Bitmap bitmap = mImageManager.safeGet(urlStr);
                    Message msg = mHandler.obtainMessage();
                    Bundle mBundle = msg.getData();
                    mBundle.putSerializable(EXTRA_IMG_URL, urlStr);
                    mBundle.putParcelable(EXTRA_IMG, bitmap);
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                flag = false;
            }

        }
    }

}
