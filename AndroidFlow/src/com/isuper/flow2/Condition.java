package com.isuper.flow2;

/**
 * 条件判断
 * 
 * @author ISuper
 *
 */
public abstract class Condition extends FlowCell {
	private FlowCell yesToFlow;
	private FlowCell noToFlow;

	public FlowCell getYesToFlow() {
		return yesToFlow;
	}

	public Flow setYesFlowEnd(){
		if(getFlow()!=null){
			this.yesToFlow = getFlow().getEndCell();
		}
		return getFlow();
	}
	public Flow setNoFlowEnd(){
		if(getFlow()!=null){
			this.noToFlow = getFlow().getEndCell();
		}
		return getFlow();
	}
	public <T extends FlowCell>T setYesToFlow(T yesToFlow) {
		if(yesToFlow.getFlow()==null){
			yesToFlow.setFlow(getFlow());
		}
		this.yesToFlow = yesToFlow;
		return yesToFlow;
	}

	public FlowCell getNoToFlow() {
		return noToFlow;
	}

	public <T extends FlowCell>T setNoToFlow(T noToFlow) {
		if(noToFlow.getFlow()==null){
			noToFlow.setFlow(getFlow());
		}
		this.noToFlow = noToFlow;
		return noToFlow;
	}

	public final void continueNextFlow(boolean result){
		if(result){
			yesToFlow.run();
		}else{
			noToFlow.run();
		}
	}
	public final void continueYesFlow() {
		yesToFlow.run();
	}

	public final void continueNoFlow() {
		noToFlow.run();
	}

}
