package core;

public class DirItem {
	
	private byte[] bytes;//一个目录项占8个byte
	
	public DirItem() {
		this.bytes = new byte[8];
	}
	public DirItem(byte[] bytes) {
		this.bytes = bytes;		
	}
	
	//在磁盘中根目录是没有目录项的。在这里我们捏造一个，方便各种操作
	public static DirItem createRootDirItem() {
		//8表示这是一个目录的目录项，2表示了起始盘块号
		return new DirItem(new byte[] {0, 0, 0, ' ', ' ', 8, 2, 0});		
	}
		
	// getters
	
	public String getName() {
		//把目录项的前3个byte转成字符串
		String name = new String();
		for (int i = 0; i < 3; i++) {
			if(this.bytes[i]!=0) {
				name+=(char)bytes[i];	
			}
		}
		return name;
	}
	
	public String getType(){
		//把目录项的第3、第4个byte转成字符串
		String type = new String();
		for (int i = 3; i < 5; i++) {
			if(bytes[i]!=0) {
				type+=(char)bytes[i];
			}
		}
		return type;
	}
	
	public String getFullName() {
		if(this.isDir()) {
			return this.getName();
		}
		return this.getName()+"."+this.getType();
	}
	
	public int getBlockNum() {
		return this.bytes[6];
	}
	
	public boolean isDir() {
		//5号byte保存目录项的属性
		int property = bytes[5];
		//属性的3号二进制位表示这个目录项指向文件夹还是文件（1为文件夹）
		property>>=3;//左移3位
		if(property % 2 == 1) {//为奇数，说明最低位为1，是文件夹
			return true;
		}
		return false;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	//setters 
	
	public void setName(String name) {		
		//倒着把字符串中的字符一个个复制到byte数组中
		for (int i = 0; i < name.length(); i++) {
			bytes[2-i] = (byte)name.charAt(name.length()-1-i);
		}
	}

	public void setType(String type) {
		for (int i = 0; i < type.length(); i++) {
			bytes[4-i] = (byte)type.charAt(type.length()-1-i);
		}
	}
	
	private void setProperty(int property) {
		/*
		 * 目录项的第五个字节为属性, 属性是一个八位二进制数
		 * 第0位标识该文件/目录是否只读
		 * 第1位标识该文件/目录是否为系统文件/目录
		 * 第2位标识该文件/目录是否为普通文件/目录
		 * 第3为标识该项是否为为目录
		 * 其余位全部留空
		 * 了解上面的信息以后, 你才能理解接下来的 magical numbers 
		 */
		bytes[5] = (byte)property;
	}
	
	public void setProperty(boolean ro, boolean sys, boolean dir) {
		int property = 0;
		if(ro) {
			property+=1;
		}
		
		if(sys) {
			property+=2;
		}else {
			property+=4;
		}
		
		if(dir) {
			property+=8;
		}	
		setProperty(property);
	}
	
	public void setBlockNum(int blockNum) {
		bytes[6] = (byte)blockNum;
	}
	
	public void setSize(int size) {
		bytes[7] = (byte)size;
	}
	
	public int getSize() {
		return (int)bytes[7];
	}
	
	public boolean isRo() {
		//第五个字节的最低位表示是否只读
		if( bytes[5] % 2 ==1) {
			return true;
		}
		return false;
	}
	
	public boolean isSys() {
		//第五个字节的次低位表示是否为系统文件
		if( bytes[5] / 2 % 2 ==1) {
			return true;
		}
		return false;
	}
}
