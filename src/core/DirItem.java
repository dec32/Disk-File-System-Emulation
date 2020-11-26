package core;

public class DirItem {
	
	private byte[] bytes;//һ��Ŀ¼��ռ8��byte
	
	public DirItem() {
		this.bytes = new byte[8];
	}
	public DirItem(byte[] bytes) {
		this.bytes = bytes;		
	}
	
	//�ڴ����и�Ŀ¼��û��Ŀ¼��ġ���������������һ����������ֲ���
	public static DirItem createRootDirItem() {
		//8��ʾ����һ��Ŀ¼��Ŀ¼�2��ʾ����ʼ�̿��
		return new DirItem(new byte[] {0, 0, 0, ' ', ' ', 8, 2, 0});		
	}
		
	// getters
	
	public String getName() {
		//��Ŀ¼���ǰ3��byteת���ַ���
		String name = new String();
		for (int i = 0; i < 3; i++) {
			if(this.bytes[i]!=0) {
				name+=(char)bytes[i];	
			}
		}
		return name;
	}
	
	public String getType(){
		//��Ŀ¼��ĵ�3����4��byteת���ַ���
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
		//5��byte����Ŀ¼�������
		int property = bytes[5];
		//���Ե�3�Ŷ�����λ��ʾ���Ŀ¼��ָ���ļ��л����ļ���1Ϊ�ļ��У�
		property>>=3;//����3λ
		if(property % 2 == 1) {//Ϊ������˵�����λΪ1�����ļ���
			return true;
		}
		return false;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	//setters 
	
	public void setName(String name) {		
		//���Ű��ַ����е��ַ�һ�������Ƶ�byte������
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
		 * Ŀ¼��ĵ�����ֽ�Ϊ����, ������һ����λ��������
		 * ��0λ��ʶ���ļ�/Ŀ¼�Ƿ�ֻ��
		 * ��1λ��ʶ���ļ�/Ŀ¼�Ƿ�Ϊϵͳ�ļ�/Ŀ¼
		 * ��2λ��ʶ���ļ�/Ŀ¼�Ƿ�Ϊ��ͨ�ļ�/Ŀ¼
		 * ��3Ϊ��ʶ�����Ƿ�ΪΪĿ¼
		 * ����λȫ������
		 * �˽��������Ϣ�Ժ�, ��������������� magical numbers 
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
		//������ֽڵ����λ��ʾ�Ƿ�ֻ��
		if( bytes[5] % 2 ==1) {
			return true;
		}
		return false;
	}
	
	public boolean isSys() {
		//������ֽڵĴε�λ��ʾ�Ƿ�Ϊϵͳ�ļ�
		if( bytes[5] / 2 % 2 ==1) {
			return true;
		}
		return false;
	}
}
