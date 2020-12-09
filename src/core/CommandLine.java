package core;

import java.util.ArrayList;

/*
 * ����ĸ�ʽΪ:
 * command -opt0 -opt1 ... arg0 arg1 ...
 * command Ϊ���������, ���� dir, openfile ֮��
 * opt Ϊѡ��, ��1���ַ���ʾ
 * arg Ϊ����, Ϊ�ַ���
 */

public class CommandLine {
	String command;
	String opts = new String();
	String[] args;
	
	public CommandLine(String str) {
		ArrayList<String> argList = new ArrayList<String>();
		//���ַ����ÿո�ָ�, ���õ�����, ����ѡ��͸�������(�����Ȳ����ǲ���(����·��)���ո�����)
		String[] splited = str.split(" ");
		//�ַ����ĵ�һ�����ʾ�������
		command = splited[0];
		if(command.equals("write")) {
			//write�����һ����Ϊ��write�����ڶ�����Ϊ·���������ȫ��Ϊ�������֣�Ϊд������
			argList.add(splited[1]);//·��
			String content = "";
			for (int i = 2; i < splited.length; i++) {
				content += splited[i];
				if(i!=splited.length-1) {
					content+=" ";
				}
			}
			argList.add(content);
		}else {
			//��"-"��ͷ��Ϊѡ��, ������Ϊ����
			for (int i = 1; i < splited.length; i++) {
				if(splited[i].startsWith("-")) {
					opts+=splited[i].charAt(1);//charAt(0)Ϊ"-", charAt(1)Ϊѡ���
				}else {
					argList.add(splited[i]);
				}
			}
		}

		//�������ArrayListת������
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
