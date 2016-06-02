package com.isuper.androidflow.mflow;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

import com.isuper.flow2.Condition;

public class DialogCell extends Condition {

	private Context context;
	private String message;
	public DialogCell(Context ctx, String message) {
		this.context = ctx;
		this.message = message;
	}

	@Override
	protected void excute() {
		AlertDialog.Builder builder = new Builder(context);
		
		builder.setMessage(message);
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				continueYesFlow();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				continueNoFlow();
			}
		});
		
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				continueNoFlow();
			}
		});
		builder.create().show();
	}
}
