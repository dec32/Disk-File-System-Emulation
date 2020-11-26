package gui;

import java.io.OutputStream;
import java.io.PrintStream;

import javafx.scene.control.TextArea;

public class TextAreaPrintStream extends PrintStream{
	
	private TextArea ta;
	
	public TextAreaPrintStream(OutputStream ops,TextArea ta) {
		/*
		 * System.out ��һ�� PrintStream, ����� print �������ն������ write ����
		 * ��PrintStream�Ĺ��췽����,һ��Ҫ�ṩһ�� OutputStream,
		 * ��ΪPrintStream �� write ��������ֽ�д����� OutputStream��
		 * �������ǲ�ϣ����Щ�ֽ��ܵ���ĵط�, ����������д write ����, ����Щ�ֽ�ת���ַ���, Ȼ��׷�ӵ� TextArea����
		 * �����ⲻ��ζ�����ǿ��Բ��ṩ OutputStream, ��Ϊ PrintStream ��û�� PrintStream() ������췽��
		 * �������ǻ�����Ҫ���ⲿ�ṩһ��û���κ������ OutputStream, Ȼ����� super(ops), ��ɳ�ʼ��
		 */
		super(ops);
		this.ta = ta;
	}
	
	
	/*
	 * ��дwrite ����, ���ֽ����� buf ת���ַ���, ׷�ӵ�TextArea����
	 */
    @Override
    public void write(byte[] buf, int off, int len){
        String str = new String(buf, off, len); 
        ta.appendText(str);
    }
}
