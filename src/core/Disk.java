package core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Disk {
	//�����СΪ64B��ǡ�ÿ�������һ����
	private byte[] writer = new byte[64];
	private byte[] reader = new byte[64];
	private File f;
	
	public Disk() {

	}
	
	public Disk(String pathname) {
		this();
		this.f = new File(pathname);
	}
	
	//�½�һ��ģ���ļ�
	public void createDisk(String pathname) {		
		try {
			f = new File(pathname);
			f.createNewFile();
					
			//��һ��64B���ֽ������ʾһ��ȫ0��
			byte[] emptyBlock = new byte[64];
			for (int i = 0; i < emptyBlock.length; i++) {
				emptyBlock[i] = 0;		
			}
			
			//������128���飬д128��
			FileOutputStream fos = new FileOutputStream(f);
			for (int i = 0; i < 128; i++) {
				fos.write(emptyBlock);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	// ��writer�е�����д������ģ���ļ��Ķ�Ӧλ��
	public void write(int blockNum) {
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(f, "rw");
			raf.seek(blockNum*64);
			raf.write(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(int blockNum, byte[] bytes) {
		for (int i = 0; i < writer.length; i++) {
			writer[i] = bytes[i];
		}
		this.write(blockNum);
	}
	
	// �Ѵ���ģ���ļ��ж�Ӧλ�õ����ݶ���reader��
	public byte[] read(int blockNum) {
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(f, "r");
			raf.seek(blockNum*64);
			raf.read(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reader;
		
//		//��reader�е��������������
//		System.out.print("content of reader: ");
//		for (int i = 0; i < reader.length; i++) {
//			System.out.print(reader[i] + " ");
//		}
//		System.out.println("");
	}

	public byte[] getReader() {
		return reader;
	}

	public byte[] getWriter() {
		return writer;
	}
	
}
