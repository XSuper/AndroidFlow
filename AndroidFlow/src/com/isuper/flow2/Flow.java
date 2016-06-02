package com.isuper.flow2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.HandlerThread;

public class Flow {
	public interface OnFlowCompleteListener {
		public void onEnd();
	}

	public interface OnFlowChangeListener {
		public void onChange(FlowCell cell);
	}

	private Handler flowHandler;
	private Start start;// 开始流程
	private End end;// 结束流程
	private FlowCell current;
	private static HandlerThread thread;
	// 自定义流程
	private HashMap<String, FlowCell> flows = new HashMap<String, FlowCell>();

	private HashMap<String, Class> flowcell_class = new HashMap<String, Class>();
	private HashMap<String, Object> param_value = new HashMap<String, Object>();

	//流程改变
	private List<OnFlowChangeListener> onChangeListeners = new ArrayList<Flow.OnFlowChangeListener>();
	//流程完成
	private List<OnFlowCompleteListener> onCompleteListeners = new ArrayList<Flow.OnFlowCompleteListener>();

	// 全局的 action 动作和类的对应关系
	private static HashMap<String, Class> global_cell_class = new HashMap<String, Class>();
	// 全局的 action参数
	private static HashMap<String, Object> global_cellParam_value = new HashMap<String, Object>();

	public static void bindGlobalCell(String cell, Class cellClass) {
		global_cell_class.put(cell, cellClass);
	}

	public static void unBindGlobalCell(String cell) {
		global_cell_class.remove(cell);
	}

	public static void setGlobalCellParam(String param, Object value) {
		global_cellParam_value.put(param, value);
	}

	public static void removeGlobalCellParam(String cell) {
		global_cellParam_value.remove(cell);
	}

	/**
	 * 添加流程改变监听
	 * 
	 * @param changeListener
	 */
	public void addOnFlowChangeListener(OnFlowChangeListener changeListener) {
		onChangeListeners.add(changeListener);
	}

	protected End getEndCell() {
		return end;
	}

	/**
	 * 添加流程结束监听
	 * 
	 * @param completeListener
	 */
	public void addOnFlowCompleteListener(
			OnFlowCompleteListener completeListener) {
		onCompleteListeners.add(completeListener);
	}

	public Flow() {
		// 初始化 流程运行线程
		if (thread == null || !thread.isAlive()) {
			thread = new HandlerThread("flow-hanlder-thread");
			thread.start();
		}
		// 初始化开始和结束流程
		start = new Start(this);
		end = new End(this);
		flows.put("start", start);
		flows.put("end", end);
	}

	public FlowCell getCurrentCell() {
		return current;
	}

	public Handler getFlowHandler() {
		return flowHandler;
	}

	public void start() {
		if (start.getNextFlow() != null) {
			flowHandler = new Handler(thread.getLooper());
			start.run();
		}
	}

	/**
	 * 将cell名称与对应的类名绑定
	 * 
	 * @param action
	 * @param className
	 * @throws ClassNotFoundException
	 */
	public void bindCell(String cell, String className) {
		Class cellClass = getClassByName(className);
		if (cellClass != null)
			flowcell_class.put(cell, cellClass);
	}

	public void bindCell(String cell, Class cellClass) {
		flowcell_class.put(cell, cellClass);
	}

	/**
	 * 解绑
	 * 
	 * @param action
	 */
	public void unBindCell(String cell) {
		flowcell_class.remove(cell);
	}

	/**
	 * 绑定参数
	 * 
	 * @param actionParam
	 * @param value
	 */
	public void setCellParam(String cellparam, Object value) {
		param_value.put(cellparam, value);
	}

	public void removeCellParam(String cellparam) {
		param_value.remove(cellparam);
	}

	/**
	 * 静态方法  根据文件生成流程
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws FlowException
	 * @throws ClassNotFoundException
	 */
	public static Flow createWithFile(File file) throws IOException,
			FlowException, ClassNotFoundException {
		Flow flow = new Flow();
		Reader in = new FileReader(file);
		flow.createWithReader(in);
		return flow;
	}
	/**
	 * 静态生成流程方法 
	 * @param flowStr
	 * @return
	 * @throws IOException
	 * @throws FlowException
	 * @throws ClassNotFoundException
	 */
	public static Flow createWithString(String flowStr) throws IOException,
			FlowException, ClassNotFoundException {
		Flow flow = new Flow();
		StringReader in = new StringReader(flowStr);
		flow.createWithReader(in);
		return flow;
	}

	/**
	 *  根据文件 定义 类  cell实例化  流程路径
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws FlowException
	 * @throws ClassNotFoundException
	 */
	public Flow makeWithFile(File file) throws IOException, FlowException,
			ClassNotFoundException {
		Reader in = new FileReader(file);
		return createWithReader(in);
	}

	/**
	 *  根据字符串 定义 类  cell实例化  流程路径
	 * @param flowStr
	 * @return
	 * @throws IOException
	 * @throws FlowException
	 * @throws ClassNotFoundException
	 */
	public Flow makeWithString(String flowStr) throws IOException,
			FlowException, ClassNotFoundException {
		StringReader in = new StringReader(flowStr);
		return createWithReader(in);
	}

	public Flow putCell(String alias, FlowCell cell) {
		cell.setAlias(alias);
		cell.setFlow(this);
		flows.put(alias, cell);
		return this;
	}

	public <T extends FlowCell> T setStartCell(T cell) {
		cell.setFlow(this);
		start.setNextFlow(cell);
		return cell;
	}

	public <T extends FlowCell> T setNextCell(Operation operation, T nextCell) {
		operation.setFlow(this);
		nextCell.setFlow(this);
		operation.setNextFlow(nextCell);
		return nextCell;
	}

	public <T extends FlowCell> T setYesCell(Condition condition, T yesCell) {
		condition.setFlow(this);
		yesCell.setFlow(this);
		condition.setYesToFlow(yesCell);
		return yesCell;
	}

	public <T extends FlowCell> T setNoCell(Condition condition, T noCell) {
		condition.setFlow(this);
		noCell.setFlow(this);
		condition.setNoToFlow(noCell);
		return noCell;
	}

	public Flow setNoCellIsEnd(Condition condition) {
		condition.setFlow(this);
		condition.setNoToFlow(end);
		return this;
	}

	public Flow setYesCellIsEnd(Condition condition) {
		condition.setFlow(this);
		condition.setYesToFlow(end);
		return this;
	}

	public Flow setNextCellIsEnd(Operation operation) {
		operation.setFlow(this);
		operation.setNextFlow(end);
		return this;

	}

	/**
	 * 根据字符串绑定cellName 和类的关系
	 * 
	 * @param value
	 * @return
	 * @throws IOException
	 * @throws FlowException
	 */
	public Flow bindFlowCellClassWhitString(String value) throws IOException,
			FlowException {
		StringReader in = new StringReader(value);
		BufferedReader reader = new BufferedReader(in);
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.contains("::")) {
				bindClass(line);
			}
		}
		reader.close();
		in.close();
		return this;
	}

	/**
	 * 用字符串设置流程路径
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws FlowException
	 */
	public Flow setFlowPathWhitString(String path) throws IOException,
			FlowException {
		StringReader in = new StringReader(path);
		BufferedReader reader = new BufferedReader(in);
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.contains("->")) {
				interpretationFlowLine(line);
			}
		}
		reader.close();
		in.close();
		return this;
	}

	private Flow createWithReader(Reader in) throws IOException, FlowException,
			ClassNotFoundException {
		BufferedReader reader = new BufferedReader(in);
		String line = null;
		while ((line = reader.readLine()) != null) {
			//类说明
			if (line.contains("::")) {
				bindClass(line);
			}
			// 定义语句
			else if (line.contains("=>")) {
				createFlowCell(line);
			}
			// 流程语句
			else if (line.contains("->")) {
				interpretationFlowLine(line);
			}
		}
		reader.close();
		in.close();
		return this;
	}

	// 绑定cell类
	private void bindClass(String str) throws FlowException {
		String[] split = str.split("::");
		if (split.length == 2) {
			bindCell(split[0].trim(), split[1].trim());
		} else {
			throw new FlowException("Bind class line [" + str + "] is wrong!");
		}
	}

	// 解释 一行流程
	private void interpretationFlowLine(String str) throws FlowException {
		String[] split = str.split("->");
		for (int i = split.length - 1; i >= 1; i--) {
			String key = split[i - 1].trim();
			// 避免condition 条件影响 如：Condition(yes)
			FlowCell flowCell = flows.get(key.replaceAll("\\(.*\\)", ""));
			String keyBehind = split[i].trim();
			FlowCell flowCellBehind = flows.get(keyBehind);

			if (flowCellBehind == null || flowCell == null) {
				throw new FlowException(key + " or " + keyBehind
						+ " hava not define");
			}
			if (flowCell instanceof Start) {
				((Start) flowCell).setNextFlow(flowCellBehind);
			} else if (flowCell instanceof Condition) {
				// 判断条件yes
				if (key.contains("(yes)")) {
					((Condition) flowCell).setYesToFlow(flowCellBehind);
				} else {
					((Condition) flowCell).setNoToFlow(flowCellBehind);
				}

			} else if (flowCell instanceof Operation) {
				((Operation) flowCell).setNextFlow(flowCellBehind);

			} else if (flowCell instanceof End) {
				// 异常 ，end 后不能再有流程单元
				throw new FlowException("flowcell can not behand end");
			}
		}

	}

	// 根据创建语句创建流程
	private void createFlowCell(String str) throws FlowException,
			ClassNotFoundException {
		String[] split = str.split("=>");
		if (split.length != 2) {
			throw new FlowException(
					"the define sentence is wrong ,the must be only one \"=>\"");
		}
		FlowCell mfc = null;// 流程单元
		String alias = split[0].trim();// 别名
		String defineLine = split[1].trim();
		if (!defineLine.startsWith("[") || !defineLine.endsWith("]")) {
			throw new FlowException(
					"the define sentence is wrong ,the must between [ ]");
		}
		String paramstr = defineLine.substring(1, defineLine.length() - 1);

		String[] params = paramstr.split(":");
		int length = params.length;
		// 获取类型名称
		Class cellClass = getClasseForCell(params[0]);
		// 没有注册 action 与类名的关系
		if (cellClass == null) {
			throw new FlowException("FLOWEXCEPTION: the " + params[0]
					+ " have to bind className");
		}
		Object ma = null;
		// 有参 实例化 提取真实参数
		Object[] newParams = new Object[length - 1];
		for (int i = 1; i < length; i++) {
			Matcher matcher = Pattern.compile("\\{(\\w+)\\}")
					.matcher(params[i]);
			// 如果是{*}格式 则查找对象
			if (matcher.find()) {
				String actionParam = matcher.group(1);
				Object paramValue = getValueForCellParam(actionParam);
				// 找不到值
				if (paramValue == null) {
					throw new FlowException(
							"FLOWEXCEPTION: please add the value of "
									+ actionParam);
				} else {
					// 参数只有{} 类型为 绑定值类型
					if (params[i].equals(matcher.group(0))) {
						newParams[i - 1] = paramValue;
					}
					// 参数包含其他字符如： name{*} 类型为字符串
					else {
						newParams[i - 1] = params[i].replace(matcher.group(0),
								paramValue.toString());
					}
				}
			} else {
				newParams[i - 1] = params[i];
			}
		}
		ma = newInstance(cellClass, newParams);
		// 正确注册类别
		if (ma == null) {
			throw new FlowException("FLOWEXCEPTION:" + cellClass.getName()
					+ "实例化失败");
		} else if (ma instanceof FlowCell) {
			mfc = (FlowCell) ma;
			putCell(alias, mfc);
		} else {
			throw new FlowException("FLOWEXCEPTION: " + cellClass.getName()
					+ " must extends [FlowCell]");
		}
	}

	/**
	 * 流程改变的调用
	 * @param cell
	 */
	protected void flowChange(final FlowCell cell) {
		current = cell;
		if (cell instanceof Start) {

		} else if (cell instanceof End) {
			for (int i = 0; i < onCompleteListeners.size(); i++) {
				OnFlowCompleteListener onFlowCompleteListener = onCompleteListeners
						.get(i);
				if(onFlowCompleteListener!=null){
					onFlowCompleteListener.onEnd();
				}
			}
		} else {
			for (int i = 0; i < onChangeListeners.size(); i++) {
				OnFlowChangeListener onFlowChangeListener = onChangeListeners
						.get(i);
				if(onFlowChangeListener!=null)
				onFlowChangeListener.onChange(cell);
			}
		}
	}

	/**
	 * 根据参数名称 得到参数
	 * 
	 * @param cellParam
	 * @return
	 */
	private Object getValueForCellParam(String cellParam) {
		if (param_value.containsKey(cellParam)) {
			return param_value.get(cellParam);
		}
		return global_cellParam_value.get(cellParam);
	}

	/**
	 * 找到Cell对应的类
	 * 
	 * @param string
	 * @return
	 */
	private Class getClasseForCell(String string) {
		if (flowcell_class.containsKey(string)) {
			return flowcell_class.get(string);
		}
		return global_cell_class.get(string);
	}

	/**
	 * 通过 类名称找类
	 * 
	 * @param className
	 * @return
	 */
	public Class getClassByName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 实例化
	 * 
	 * @param className
	 * @param args
	 * @return
	 * @throws ClassNotFoundException
	 * @throws FlowException
	 */
	private static Object newInstance(Class newoneClass, Object[] args)
			throws FlowException {
		String className = newoneClass.getName();
		if (args == null || args.length == 0) {
			try {
				return newoneClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new FlowException(className
						+ "实例化失败：InstantiationException");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new FlowException(className
						+ "实例化失败：IllegalAccessException");
			}
		}
		Class[] argsClass = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}
		Constructor cons = null;
		try {
			// 先用当前类进行查询
			cons = newoneClass.getConstructor(argsClass);
		} catch (NoSuchMethodException ep) {

			cons = null;
			// 获得所有构造方法
			Constructor[] constructors = newoneClass.getConstructors();
			for (int i = 0; i < constructors.length; i++) {
				Constructor consTemp = constructors[i];
				Class[] parameterTypes = consTemp.getParameterTypes();
				int paramCount = parameterTypes.length;
				// 筛选出 参数个数相同的方法
				if (paramCount == argsClass.length) {
					boolean find = true;
					for (int j = 0; j < paramCount; j++) {
						// 比较每一个传入参数类型 是否是构造函数参数的子类
						if (!parameterTypes[j].isAssignableFrom(argsClass[j])) {
							// 都符合 就用这个方法实例化，有一个不符合就继续查找
							find = false;
							break;
						}
					}
					if (find) {
						cons = constructors[i];
						break;
					} else {
						continue;
					}

				}
			}
		}
		try {
			return cons != null ? cons.newInstance(args) : null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new FlowException(className + "实例化失败：InstantiationException");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new FlowException(className + "实例化失败：IllegalAccessException");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new FlowException(className
					+ "实例化失败：IllegalArgumentException");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new FlowException(className
					+ "实例化失败：InvocationTargetException");
		}
	}

}
