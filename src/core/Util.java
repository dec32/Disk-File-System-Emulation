package core;

public class Util {
	
	//读取目录中的第num个目录项
	public static DirItem getDirItemAt(byte[] curDir,int num) {
		byte[] bytes = new byte[8];
		System.arraycopy(curDir, num*8, bytes, 0, 8);
		return new DirItem(bytes);
	}
	
	public static void setDirItemAt(byte[] writer, int num, DirItem di) {
		System.arraycopy(di.getBytes(), 0, writer, num*8, 8);
	}
	
	public static int findAvailableBlock(Disk d) {
		d.read(0);
		//0 和 1 块保存 FAT 表，2 块保存根目录，均不可用，直接从 3 块开始查找
		for (int i = 3; i < d.getReader().length; i++) {
			if(d.getReader()[i] == 0) {
				return i;
			}
		}
		
		d.read(1);
		for (int i = 0; i < d.getReader().length; i++) {
			if(d.getReader()[i] == 0) {
				return 64 + i;
			}
		}
		return -1;
	}
	
	public static void writeDirItem(DirItem di, int blockNum, int itemNum, Disk disk) {
		//把目录项所在的块读出，建立一份副本
		disk.read(blockNum);
		byte [] block = new byte[64];
		System.arraycopy(disk.getReader(), 0, block, 0, 64);
		//把目录项写入到副本中
		System.arraycopy(di.getBytes(), 0, block, itemNum*8, 8);
		//把副本写回
		disk.write(blockNum, block);
	}
	
	public static void writeFat(int blockNum,int value, Disk disk) {
		disk.read(blockNum/64);
		byte [] block = new byte[64];
		System.arraycopy(disk.getReader(), 0, block, 0, 64);
		block[blockNum % 64] = (byte)value;
		disk.write(blockNum/64, block);
	}
	
	//主要功能：把reader中的内容复制到内存中进行操作
	public static void copyBlock(byte[] src, byte[] dest) {
		System.arraycopy(src, 0, dest, 0, 64);
	}
	
	// 针对路径名的一些字符串操作
	
	//对于"f1/f2/name.type", 返回name.type
	public static String getFullName(String pathname) {
		if(pathname.equals("/")) {
			return "";
		}
		String[] names = pathname.split("/");
		return names[names.length-1];		
	}
	//对于"f1/f2/name.type", 返回"name"
	public static String getName(String pathname) {
		String fullName = getFullName(pathname);
		return fullName.split("\\.")[0];
	}
	//对于"f1/f2/name.type", 返回"type"
	public static String getType(String pathname) {
		String fullName = getFullName(pathname);
		return fullName.split("\\.")[1];
	}
	//对于"f1/f2/f3", 返回"f3"
	public static String getFolderName(String pathname) {
		return getFullName(pathname);//其实就是 fullname
	}
	
	
}
