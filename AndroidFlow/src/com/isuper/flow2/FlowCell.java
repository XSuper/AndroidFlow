package com.isuper.flow2;



public abstract class FlowCell {
	public Object tag;
	private Flow flow;//所属的流程
	private String alias;//别名
	public Flow getFlow(){
		return flow;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setFlow(Flow flow){
		this.flow = flow;
	}
	public final void run() {
		//TODO 通知改变 
		if(flow!=null){
			flow.flowChange(FlowCell.this);
			flow.getFlowHandler().post(new Runnable() {
				@Override
				public void run() {
					excute();
					
				}
			});
		}else{
			excute();
		}
	}
	protected abstract void excute();
}
