package com.isuper.androidflow.mflow;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.isuper.flow2.Operation;

public class ToastCell extends Operation {
	private Context context;
	private String toast;

	public ToastCell(Context ctx, String toast) {
		this.context = ctx;
		this.toast = toast;
	}

	@Override
	protected void excute() {
		String name =  Thread.currentThread().getName();
		Log.d("name", name);
		Toast.makeText(context, toast, 2000).show();
		continueNextFlow();
	}

}
