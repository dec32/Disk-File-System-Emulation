package core;

public class Util {
	
	//��ȡĿ¼�еĵ�num��Ŀ¼��
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
		//0 �� 1 �鱣�� FAT ��2 �鱣���Ŀ¼���������ã�ֱ�Ӵ� 3 �鿪ʼ����
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
		//��Ŀ¼�����ڵĿ����������һ�ݸ���
		disk.read(blockNum);
		byte [] block = new byte[64];
		System.arraycopy(disk.getReader(), 0, block, 0, 64);
		//��Ŀ¼��д�뵽������
		System.arraycopy(di.getBytes(), 0, block, itemNum*8, 8);
		//�Ѹ���д��
		disk.write(blockNum, block);
	}
	
	public static void writeFat(int blockNum,int value, Disk disk) {
		disk.read(blockNum/64);
		byte [] block = new byte[64];
		System.arraycopy(disk.getReader(), 0, block, 0, 64);
		block[blockNum % 64] = (byte)value;
		disk.write(blockNum/64, block);
	}
	
	//��Ҫ���ܣ���reader�е����ݸ��Ƶ��ڴ��н��в���
	public static void copyBlock(byte[] src, byte[] dest) {
		System.arraycopy(src, 0, dest, 0, 64);
	}
	
	// ���·������һЩ�ַ�������
	
	//����"f1/f2/name.type", ����name.type
	public static String getFullName(String pathname) {
		if(pathname.equals("/")) {
			return "";
		}
		String[] names = pathname.split("/");
		return names[names.length-1];		
	}
	//����"f1/f2/name.type", ����"name"
	public static String getName(String pathname) {
		String fullName = getFullName(pathname);
		return fullName.split("\\.")[0];
	}
	//����"f1/f2/name.type", ����"type"
	public static String getType(String pathname) {
		String fullName = getFullName(pathname);
		return fullName.split("\\.")[1];
	}
	//����"f1/f2/f3", ����"f3"
	public static String getFolderName(String pathname) {
		return getFullName(pathname);//��ʵ���� fullname
	}
	
	
}
