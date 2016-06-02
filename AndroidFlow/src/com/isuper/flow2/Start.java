package com.isuper.flow2;

import android.util.Log;

/**
 * 标识流程起点
 * @author ISuper
 *
 */
public final class Start extends Operation{

	public Start(Flow flow) {
		setFlow(flow);
	}
	@Override
	protected void excute() {
		Log.d("flow", "开始执行流程");
		continueNextFlow();
	}
		
}
