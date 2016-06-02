package com.isuper.androidflow;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.isuper.androidflow.mflow.DialogCell;
import com.isuper.androidflow.mflow.LogCell;
import com.isuper.androidflow.mflow.ToastCell;
import com.isuper.flow2.Flow;
import com.isuper.flow2.Flow.OnFlowChangeListener;
import com.isuper.flow2.Flow.OnFlowCompleteListener;
import com.isuper.flow2.FlowCell;
import com.isuper.flow2.FlowException;

public class MainActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.flow1).setOnClickListener(this);
		findViewById(R.id.flow2).setOnClickListener(this);
		findViewById(R.id.flow3).setOnClickListener(this);
		findViewById(R.id.flow4).setOnClickListener(this);
	}
	
	
	private void testFlow4() {
		Log.d("", "开始流程");
		Flow.bindGlobalCell("Dialog", DialogCell.class);
		Flow.bindGlobalCell("Toast", ToastCell.class);
		Flow.bindGlobalCell("Log", LogCell.class);
		String flowStr =
				"dialog=>[Dialog:{context}:是否进行下去]\n"
				+ "toast=>[Toast:{context}:hello baily u a so cute]\n"
				+ "log=>[Log:{log}]\n" 
				+ "start->log->dialog\n"
				+ "dialog(yes)->toast->end\n" 
				+ "dialog(no)->end\n";

		Flow flow = new Flow();
		flow.bindCell("Toast", ToastCell.class);
		flow.addOnFlowChangeListener(new OnFlowChangeListener() {

			@Override
			public void onChange(FlowCell cell) {
				Toast.makeText(MainActivity.this, "当前cell" + cell.getAlias(),
						1000).show();
			}
		});
		flow.addOnFlowCompleteListener(new OnFlowCompleteListener() {

			@Override
			public void onEnd() {
				Log.e("", "addOnFlowCompleteListener  ------");
			}
		});
		Flow.setGlobalCellParam("context", this);
		flow.setCellParam("log", "yes会toast No 会直接结束");
		try {
			flow.makeWithString(flowStr).start();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlowException e) {
			e.printStackTrace();
		}

	}
	private void createFlowWithCode() {
		Flow flow = new Flow();
		LogCell logCell = new LogCell("hahaha");
		ToastCell toastCell = new ToastCell(this, "tosssss");
		DialogCell dialogCell = new DialogCell(this, "this is dialog");

		flow.setStartCell(logCell)
		.setNextFlow(dialogCell)
		.setYesToFlow(toastCell)
		.setNextFlowEnd()
		.setNoCellIsEnd(dialogCell).start();

	}

	private void testFlow3() {
		Flow flow = new Flow();
		LogCell logCell = new LogCell("hahaha");
		ToastCell toastCell = new ToastCell(this, "tosssss");
		DialogCell dialogCell = new DialogCell(this, "this is dialog");
		flow.putCell("Dialog", dialogCell);
		flow.putCell("Log", logCell);
		flow.putCell("Toast", toastCell);
		String path = 
				  "start->Log->Dialog\n"
				+ "Dialog(yes)->Toast->end\n"
				+ "Dialog(no)->end\n";
		try {
			flow.setFlowPathWhitString(path).start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlowException e) {
			e.printStackTrace();
		}
	}

	private void createFlowWithString() {
		String flowStr =
				"Dialog::com.isuper.androidflow.mflow.DialogCell\n"
				+ "Toast::com.isuper.androidflow.mflow.ToastCell\n"
				+ "Log::com.isuper.androidflow.mflow.LogCell\n"
				+ "Dialog=>[Dialog:{context}:是否进行下去]\n"
				+ "Toast=>[Toast:{context}:hello u a so cute]\n"
				+ "Log=>[Log:{logValue}]\n" 
				+ "start->Log->Dialog\n"
				+ "Dialog(yes)->Toast->end\n" 
				+ "Dialog(no)->end\n";
		try {
			Flow.setGlobalCellParam("context", this);
			Flow.setGlobalCellParam("logValue", "yes会toast No 会直接结束");
			Flow.createWithString(flowStr).start();
			
			Flow flow = new Flow();
//			cell参数不是全局
//			flow.makeWithString(flowStr);
//			flow.setCellParam("context", this);
//			flow.setCellParam("logValue", "yes会toast No 会直接结束");
//			flow.makeWithString(flowStr);
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlowException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.flow1:
			createFlowWithString();
			break;
		case R.id.flow2:
			createFlowWithCode();
			break;
		case R.id.flow3:
			testFlow3();
			break;
		case R.id.flow4:
			testFlow4();
			break;
		default:
			break;
		}
		
	}

	
}
