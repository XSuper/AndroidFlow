# AndroidFlow


AndroidFlow 是一个简单的流程框架，定义好流程单元后，可以通过代码或者字符串来控制流程执行逻辑。

整个流程中包含四种流程单元

  **start** ：标识流程开始，不用自行编写
  **end**：  标识流程结束，不用自行编写
  **Operation**： 简单的执行流程，不包含判断，如打印一条日志
  **Condition**： 判断流程，包含yes和no两种判断，不同结果执行不同分支

流程编写 Operation

```java
public class LogCell extends Operation{

	String log;

	public LogCell(String log) {
		this.log = log;
	}
	@Override
	protected void excute() {
		Log.d("LOGCELL", log);
    //必须调用continueNextFlow()确保流程向下执行
		continueNextFlow();
		Log.d("LOGCELL", "LOGCELL  结束");
	}

}
```

如果是无条件判断的流程，直接继承自Operation，添加构造方法，实现`excute`方法在流程执行完成后务必调用`continueNextFlow()`方法确保流程向下进行。

编写 Condition
Condition有两种状态，在这里用Dialog演示
```java
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

```
同样很简单，但是必须任何情况都能继续接下来的流程，yes流程调用continueYesFlow() no流程调用continueNoFlow()
使用方式

纯代码控制流程
```java
Flow flow = new Flow();
LogCell logCell = new LogCell("this is logCell");
ToastCell toastCell = new ToastCell(this, "ToastCell");
DialogCell dialogCell = new DialogCell(this, "this is dialogCell");

flow.setStartCell(logCell)
.setNextFlow(dialogCell)
.setYesToFlow(toastCell)
.setNextFlowEnd()
.setNoCellIsEnd(dialogCell).start();


```

字符串定义流程

```java

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

			Flow.setGlobalCellParam("context", this);
			Flow.setGlobalCellParam("logValue", "yes会toast No 会直接结束");
			Flow.createWithString(flowStr).start();

			Flow flow = new Flow();
//			cell参数不是全局
//			flow.makeWithString(flowStr);
//			flow.setCellParam("context", this);
//			flow.setCellParam("logValue", "yes会toast No 会直接结束");
//			flow.makeWithString(flowStr);

```
每个流程可以分为三个部分

1. 流程类别绑定
2. 流程实例化
3. 流程执行流程

类别绑定
```java
//字符串方式定义
Dialog::com.isuper.androidflow.mflow.DialogCell
//用::分割别名和类名
```
```java
//全局绑定
Flow.bindGlobalCell("Dialog", DialogCell.class);
//单个流程绑定
Flow flow = new Flow();
flow.bindCell("Dialog", DialogCell.class);
flow.bindCell("Dialog","com.isuper.androidflow.mflow.DialogCell");

```
实例化

```java
//别名和定义语句用=> 分开，=>前的Dialog用于流程[]扩号里面的Dialog 是定义语句的别名，每一个：后面都是一个参数，实例化的时候会根据参数类型和参数个数选择构造方法进行实例化。
Dialog=>[Dialog:{context}:是否进行下去]

//代码定义
Flow flow = new Flow();
LogCell logCell = new LogCell("hahaha");
//设置别名，别名用于流程确定
flow.putCell("Log", logCell);

```

在上面字符串中应该注意到了{context}，流程定义支持设置值也可以直接写字符串，设置值可以是任意类型
设置值的方法
```java
//设置全局值
	Flow.setGlobalCellParam("context", MainActivity.this);
//单个流程设置值
  flow.setCellParam("context", MainActivity.this);

```


执行流程定义
```java

//字符串定义流程   用->连接，必须以start开始，流程以end 结束，判断流程必须给出(yes)和(no)不同流程
String flow =
				  "start->Log->Dialog\n"
				+ "Dialog(yes)->Toast->end\n"
				+ "Dialog(no)->end\n";


//代码设置流程，  必须通过代码实例化的流程单元
flow.setStartCell(logCell)
.setNextFlow(dialogCell)
.setYesToFlow(toastCell)
.setNextFlowEnd()
.setNoCellIsEnd(dialogCell).start();

//支持链式
开始的时候使用setStartCell(logCell) 标识启动的流程单元
在任何一个环节结束 都必须调用结束标志setNextFlowEnd()，setNoCellIsEnd(dialogCell)，setYesCellIsEnd(dialogCell)

```

流程启动
```java
flow.start();
```

监听
```java
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
				Log.d("", "addOnFlowCompleteListener  ------");
			}
		});
```
