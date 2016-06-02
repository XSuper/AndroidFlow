package com.isuper.flow2;

import java.util.HashMap;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 超时帮助类
 * @author ISuper
 *
 */
public class TimeOutHelper {
	private HandlerThread _handThread;
	private Handler _handler;
	private HashMap<String, Runnable> runnableMap ;
	private static TimeOutHelper timeOutHelper;
	private TimeOutHelper() {
		runnableMap = new HashMap<String, Runnable>();
		_handThread = new HandlerThread("TimeOutHelper");
		_handThread.start();
		_handler = new Handler(_handThread.getLooper());
	}
	public static TimeOutHelper getInstance(){
		if(timeOutHelper==null){
			timeOutHelper = new TimeOutHelper();
		}
		return timeOutHelper;
	}
	public void addTimeOutRunnable(Runnable runnable,long timeout){
		_handler.postDelayed(runnable, timeout);
	}
	public void addTimeOutRunnable(Runnable runnable,long timeout,String key){
		if(key!=null&&!"".equals(key)){
			runnableMap.put(key, runnable);
		}
		_handler.postDelayed(runnable, timeout);
	}
	public void removeTimeOutRunnable(String key){
		Runnable runnable = runnableMap.get(key);
		if(runnable!=null){
			removeTimeOutRunnable(runnable);
		}
	}
	public void removeTimeOutRunnable(Runnable runnable){
		_handler.removeCallbacks(runnable);
	}
	

}
