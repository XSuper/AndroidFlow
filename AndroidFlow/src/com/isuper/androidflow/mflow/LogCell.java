package com.isuper.androidflow.mflow;

import android.util.Log;

import com.isuper.flow2.Operation;

public class LogCell extends Operation{

	String log;
	
	public LogCell(String log) {
		this.log = log;
	}
	@Override
	protected void excute() {
		Log.d("LOGCELL", log);
		continueNextFlow();
		Log.d("LOGCELL", "LOGCELL  结束");
	}

}
