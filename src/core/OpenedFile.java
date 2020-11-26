package core;

public class OpenedFile {
	//private int dnum;//代表磁盘块号
	//private int bnum;//代表磁盘盘块内第几个字节
	private String pathname = "";//代表文件绝对路径名
	private char attribute = ' '; //文件属性r/w
	private int number = 0;//文件起始盘块号
	private int length = 0;//文件长度，文件占用字节数
	private int flag = 0;//操作类型，0表示读方式打开，1表示写方式打开
	/*
	 * 建议不要命名为 flag, 而是命名为operationType, type, ot之类的
	 * 或者用一个布尔变量 boolean ro 来代替, true 表示只读, false 表示可读可写
	 * 这样的话只要调用 isRo()这样的 getter 就能知道这个已打开的文件的是否只读
	 */
	private int []read = new int[2]; //read[0]用来放dnum,read[1]用来放bnum
	private int []write = new int[2];//read[0]用来放dnum,read[1]用来放bnum

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
