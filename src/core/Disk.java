package core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Disk {
	//缓存大小为64B，恰好可以容下一个块
	private byte[] writer = new byte[64];
	private byte[] reader = new byte[64];
	private File f;
	
	public Disk() {

	}
	
	public Disk(String pathname) {
		this();
		this.f = new File(pathname);
	}
	
	//新建一个模拟文件
	public void createDisk(String pathname) {		
		try {
			f = new File(pathname);
			f.createNewFile();
					
			//用一个64B的字节数组表示一个全0块
			byte[] emptyBlock = new byte[64];
			for (int i = 0; i < emptyBlock.length; i++) {
				emptyBlock[i] = 0;		
			}
			
			//磁盘有128个块，写128次
			FileOutputStream fos = new FileOutputStream(f);
			for (int i = 0; i < 128; i++) {
				fos.write(emptyBlock);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	// 把writer中的内容写到磁盘模拟文件的对应位置
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
	
	// 把磁盘模拟文件中对应位置的内容读到reader中
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
		byte[] block = new byte[64];
		System.arraycopy(reader, 0, block, 0, 64);
		return block;
		
//		//把reader中的内容输出，测试
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
