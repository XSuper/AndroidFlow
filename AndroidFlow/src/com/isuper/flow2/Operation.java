package com.isuper.flow2;

public abstract class Operation extends FlowCell{
	private FlowCell nextFlow;
	
	public FlowCell getNextFlow() {
		return nextFlow;
	}
	public <T extends FlowCell>T setNextFlow(T flow) {
		if(flow.getFlow()==null){
			flow.setFlow(getFlow());
		}
		this.nextFlow = flow;
		return flow;
	}
	public Flow setNextFlowEnd(){
		if(getFlow()!=null){
			this.nextFlow = getFlow().getEndCell();
		}
		return getFlow();
	}
	public final void continueNextFlow(){
		nextFlow.run();
	}
	
	
}
