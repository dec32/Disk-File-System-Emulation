package core;

import java.util.ArrayList;

/*
 * 命令的格式为:
 * command -opt0 -opt1 ... arg0 arg1 ...
 * command 为命令的类型, 比如 dir, openfile 之类
 * opt 为选项, 用1个字符表示
 * arg 为参数, 为字符串
 */

public class CommandLine {
	String command;
	String opts = new String();
	String[] args;
	
	public CommandLine(String str) {
		ArrayList<String> argList = new ArrayList<String>();
		//把字符串用空格分割, 便会得到命令, 各个选项和各个参数(这里先不考虑参数(比如路径)带空格的情况)
		String[] splited = str.split(" ");
		//字符串的第一个单词就是命令
		command = splited[0];
		//以"-"开头的为选项, 其他的为参数
		for (int i = 1; i < splited.length; i++) {
			if(splited[i].startsWith("-")) {
				opts+=splited[i].charAt(1);//charAt(0)为"-", charAt(1)为选项本身
			}else {
				argList.add(splited[i]);
			}
		}
		//把讨厌的ArrayList转成数组
		int size = argList.size();
		args = (String[])argList.toArray(new String[size]);
	}

	//getters & setters
	public String getCommand() {
		return command;
	}

	public String getOpts() {
		return opts;
	}

	public String[] getArgs() {
		return args;
	}
	
	
}
