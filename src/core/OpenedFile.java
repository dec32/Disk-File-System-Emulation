package core;

public class OpenedFile {
	//private int dnum;//������̿��
	//private int bnum;//��������̿��ڵڼ����ֽ�
	private String pathname = "";//�����ļ�����·����
	private char attribute = ' '; //�ļ�����r/w
	private int number = 0;//�ļ���ʼ�̿��
	private int length = 0;//�ļ����ȣ��ļ�ռ���ֽ���
	private int flag = 0;//�������ͣ�0��ʾ����ʽ�򿪣�1��ʾд��ʽ��
	/*
	 * ���鲻Ҫ����Ϊ flag, ��������ΪoperationType, type, ot֮���
	 * ������һ���������� boolean ro ������, true ��ʾֻ��, false ��ʾ�ɶ���д
	 * �����Ļ�ֻҪ���� isRo()������ getter ����֪������Ѵ򿪵��ļ����Ƿ�ֻ��
	 */
	private int []read = new int[2]; //read[0]������dnum,read[1]������bnum
	private int []write = new int[2];//read[0]������dnum,read[1]������bnum

	public OpenedFile(String pathname, char attribute, int number, int length, int flag, int[] read, int[] write) {
		super();
		this.pathname = pathname;
		this.attribute = attribute;
		this.number = number;
		this.length = length;
		this.flag = flag;
		this.read = read;
		this.write = write;
	}



	public OpenedFile() {
		super();
		read[0]  = 0;  read[1]  = 0;
		write[0] = 0;  write[1] = 0;
		// TODO Auto-generated constructor stub
	}



	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	public char getAttribute() {
		return attribute;
	}

	public void setAttribute(char attribute) {
		this.attribute = attribute;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int[] getRead() {
		return read;
	}

	public void setRead(int[] read) {
		this.read = read;
	}

	public int[] getWrite() {
		return write;
	}

	public void setWrite(int[] write) {
		this.write = write;
	}


}
