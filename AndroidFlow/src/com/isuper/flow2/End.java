package com.isuper.flow2;

import android.util.Log;



public final class End extends Operation{
	public End(Flow flow) {
		setFlow(flow);
	}

	@Override
	protected void excute() {
		Log.d("  ","流程执行完成");
	}
}
