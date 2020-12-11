package core;

import java.awt.List;
import java.util.ArrayList;


public class Core {

	private Disk disk;
	private DirItem curDirItem = new DirItem();// ���浱ǰ�򿪵�Ŀ¼��Ŀ¼��
	private byte[] curDir = new byte[64];// ���浱ǰ�򿪵�Ŀ¼
	private String curPath = "/";
	private ArrayList<OpenedFile> openedFileList = new ArrayList<OpenedFile>();//�Ѵ��ļ���

	public Core() {
		disk = new Disk();
		disk.createDisk("D:/test.dsk");

		// ʹ�þ���·��������
		dir("/");
		md("/123");
		md("/123/456");
		createFile("/123/456/abc.ef", "");//�մ�����˼��, ��ѡ���κ�ѡ��(�Ȳ���ϵͳ�ļ�, Ҳ����ֻ��)
		dir("/123");
		// ʹ�����·��������
		dir("456");
		md("789");
		createFile("ghi.jk", "");
		// ת����Ŀ¼
		dir("/");
	}

	public void execute(String str) {

		/*
		 * ʾ��:
		 * dir /123 (ת�� /123 ��)
		 * dir 456 (ת�� 456 ��)
		 * create kkk.kk (����һ����ͨ�ļ�)
		 * create -s kkk.kk (����һ��ϵͳ�ļ�)
		 * create -r -s kkk.kk(����һ��ֻ����ϵͳ�ļ�)
		 * open -r kkk.kk(��ֻ����ʽ���ļ�, -r ����"read only")
		 * open kkk.kk(���ļ�, ����дҲ���Զ�)
		 */

		// �ṩ�ַ�����ʽ��һ������, �½�һ�� CommandLine ����, ���췽�������ѡ��Ͳ���
		CommandLine cl = new CommandLine(str);
		String command = cl.getCommand();
		String opts = cl.getOpts();
		String[] args = cl.getArgs();

//		System.out.println("command: "+command);
//		System.out.println("opts: "+opts);
//		System.out.print("args: ");
//		for(String s:args)
//		{
//			System.out.print(s+", ");
//		}
//		System.out.println();


		/*
		 * if-else nightmare, try to re-write this part
		 */
		if (command.equals("open")) {
			openFile(args[0], opts);// args0 Ϊ·��, ������Ψһ�Ĳ���, ��ͬ
		} else if (command.equals("close")) {
			closeFile(args[0]);
		} else if (command.equals("dir")) {
			dir(args[0]);// dir ����ֻ��һ������, ·��
		} else if (command.equals("write")) {
			writeFile(args[0],args[1]);
		} else if (command.equals("read")) {
			readFile(args[0], Integer.valueOf(args[1]));
		} else if (command.equals("type")) {
			typeFile(args[0]);
		} else if (command.equals("change")) {
			// i don't really know what the fuck is change
		} else if (command.equals("create")) {
			createFile(args[0], opts);// create ����ֻ��һ������, ·��. ѡ�����-r��-s
		} else if (command.equals("md")) {
			md(args[0]);// md ����ֻ��һ������, ·��
		} else if (command.equals("delete")) {
			deleteFile(args[0]);
		} else if (command.equals("rd")) {
			rd(args[0]);
		} else {
			System.out.println("Syntax error you dumbass");
		}

	}



	// ���ظ���Ŀ¼���ļ���Ŀ¼��(ֻ�ܴ������·��(��'/'��ͷ))
	private DirItem findDirItem(String pathname) {
		String[] names = pathname.split("/"); // ���·��, ����"/a/b/c"���ַ���, �ᱻ���Ϊ"", "a", "b", "c"(�����ַ���)
		String fullname = Util.getFullName(pathname);// Ŀ���ļ�����������
		byte[] curDir = new byte[64];
		// �Ӹ�Ŀ¼�_ʼ��ѯ
		DirItem di = DirItem.createRootDirItem();

		disk.read(di.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);// ���ȶ����Ŀ¼������

		// ����ÿһ�����ļ�����(���ļ���), һ��һ����������, names[0] Ϊ���ַ���, ����ֱ�Ӵ�names[1] ��ʼ����
		for (int i = 1; i < names.length; i++) {
			// ������ǰĿ¼��8��Ŀ¼�jΪ��ָ��
			boolean found = false;
			for (int j = 0; j < 8; j++) {
				di = Util.getDirItemAt(curDir, j);// ��reader��ȡ����j��Ŀ¼��
				if (di.getFullName().equals(names[i])) {// Ŀ¼���·��ָ����Ŀ¼ͬ��, ת����Ŀ¼��
					// ע: ���diָ�����һ���ļ�, ��ô�ļ��ĵ�һ����ᱻд��curDir����, ������Ӱ�����ִ��
					disk.read(di.getBlockNum());
					Util.copyBlock(disk.getReader(), curDir);
					found = true;
					break;
				}
			}
			if (!found) {
				System.out.println("findDirItemû���ҵ����ļ�");
				return null;// �Ҳ����ļ��У���ֹ����
			}
		}
		return di;
	}

	// ���ظ�Ŀ¼��Ŀ¼��(ֻ�ܴ������·��(��'/'��ͷ))
	private DirItem findSuperDirItem(String pathname) {
		String[] names = pathname.split("/"); // ���·��, ����"/a/b/c"���ַ���, �ᱻ���Ϊ"", "a", "b", "c"(�����ַ���)
		byte[] curDir = new byte[64];
		// �Ӹ�Ŀ¼�_ʼ��ѯ
		DirItem di = DirItem.createRootDirItem();

		disk.read(di.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);// ���ȶ����Ŀ¼������

		// ���ÿһ��Ŀ¼��ѭ��, names[0] Ϊ���ַ���, ����ֱ�Ӵ�names[1] ��ʼ����
		for (int i = 1; i < names.length - 1; i++) {
			// ������ǰĿ¼��8��Ŀ¼�jΪ��ָ��
			boolean found = false;
			for (int j = 0; j < 8; j++) {
				di = Util.getDirItemAt(curDir, j);// ��reader��ȡ����j��Ŀ¼��
				if (di.getName().equals(names[i]) && di.isDir()) {// ���Ŀ¼��Ϊ�ļ��У��Һ�·��ָ�����ļ���ͬ������ת������ļ���
					disk.read(di.getBlockNum());
					Util.copyBlock(disk.getReader(), curDir);
					found = true;
					break;
				}
			}
			if (!found) {
				return null;// �Ҳ����ļ��У���ֹ����
			}
		}
		return di;
	}

	//���µ�ǰĿ¼
	private void updateCurDir() {
		disk.read(curDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);
	}
	
	//��ȡfat
	private byte[] readFat() {
		byte[] fat = new byte[128];
		disk.read(0);
		for (int i = 0; i < 64; i++) {
			fat[i] = disk.getReader()[i];
		}
		disk.read(1);
		for (int i = 0; i < 64; i++) {
			fat[i+64] = disk.getReader()[i];
		}
		return fat;
	}

	//дfat
	private void writeFat(int blockNum,int value) {
		disk.read(blockNum/64);
		byte [] block = new byte[64];
		System.arraycopy(disk.getReader(), 0, block, 0, 64);
		block[blockNum % 64] = (byte)value;
		disk.write(blockNum/64, block);
	}

	private String toAbsPath(String relPath) {
		String absPath = "";
		if(curPath.equals("/")) {
			absPath = curPath + relPath;
		}else {
			absPath = curPath + "/" + relPath;
		}
		return absPath;
	}

	private int[] getBlockNums(DirItem di) {
		ArrayList<Integer> blockNumList = new ArrayList<Integer>();
		byte[] fat = readFat();
		blockNumList.add(di.getBlockNum());
		int nxtBlockNum;
		for(int i = 0 ;true ;i++) {
			nxtBlockNum = fat[blockNumList.get(i)];
			if(nxtBlockNum == -1) {
				break;
			}
			blockNumList.add(nxtBlockNum);
		}
		int[] blockNums = new int[blockNumList.size()];
		for (int i = 0; i < blockNums.length; i++) {
			blockNums[i] = blockNumList.get(i);
		}
		return blockNums;
	}
	
	//��ָ��������ݲ���
	private void wipeBlock(int blockNum) {
		byte[] emptyBlock= new byte[64];
		disk.write(blockNum, emptyBlock);
	}

	public boolean createFile(String pathname, String opts) {
		// opts �п����е�ѡ���� -r �� -s, ǰ�߱�ʾֻ��, ���߱�ʾϵͳ
		String filename = Util.getName(pathname);
		String type = Util.getType(pathname);
		DirItem superDirItem;// ��Ŀ¼��
		byte[] superDir = new byte[64];// ��Ŀ¼

		if (pathname.charAt(0) == '/') {
			// ���ṩ���Ǿ���·��, ����Ҫ���Ҹ�Ŀ¼
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
				return false;
			}
			// else System.out.println("����·������");
		} else {
			// ���ṩ�������·��, ��Ŀ¼���ǵ�ǰ�򿪵�Ŀ¼
			superDirItem = curDirItem;
		}

		// �Ѹ�Ŀ¼�����ݶ�ȡ��������, �����Ƴ���
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);

		// ���Ҹ�Ŀ¼����û�������ļ�
		DirItem di;
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// �Ӹ�Ŀ¼��ȡ����i��Ŀ¼��
			if (di.getName().equals(filename) && di.getType().equals(type) && !di.isDir()) {
				System.out.println("���������ļ���");
				return false;// �������ļ�����ֹ����
			}
		}

		// Ѱ��һ����λ�����ļ���Ŀ¼��Ž�ȥ
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// ��reader��ȡ����i��Ŀ¼��
			if (di.getName().equals("")) {// ��i��λ��Ϊ�գ���λ�õ��ļ���Ϊ���ַ���

				// �������ļ���Ŀ¼��
				di.setName(filename);// ��������
				di.setType(type);// ��������

				// ��������
				boolean ro = false;
				boolean sys = false;
				if(opts.contains("r")) {
					ro = true;
				}
				if(opts.contains("s")) {
					sys = true;
				}
				di.setProperty(ro, sys, false);// ���һ��false��ʾ���Ǹ�һ���ļ�������һ��Ŀ¼

				int availableBlock;
				availableBlock = Util.findAvailableBlock(disk);// �ҵ�һ�����п�
				di.setBlockNum(availableBlock);// ����Ҫռ�õĴ��̿�
				di.setSize(0);// �����ļ���С����ʼΪ0�ֽ�

				Util.writeDirItem(di, superDirItem.getBlockNum(), i, disk);// ��Ŀ¼��д���ҵ��Ŀ�λ֮��
				Util.writeFat(availableBlock, 255, disk);// ����FAT
				//���ļ��ĵ�һ������дһ����#��
				byte[] block = new byte[64];
				block[0] = '#';
				disk.write(availableBlock, block);
				break;
			}
		}
		updateCurDir();//��Ϊ�������ڵ�ǰĿ¼�½���һ���ļ�, ����ˢ��һ��
		return true;
	}

	public boolean openFile(String pathname, String opts) { //pathname-�ļ���,opts-��������(��/д)
		/*
		 * ��������pathname���������·��, Ҳ�����Ǿ���·��
		 * ����İ취��, ����������������·��, �ҾͰ���ת���ɾ���·��
		 * ����·�� = ��ǰ·��+"/"+���·��
		 */

		// *1����ļ��Ƿ����,�������ڣ����ʧ��
		// *2���ڣ����򿪷�ʽflag�Ƕ�����д�����ļ������Ƿ����
		// *3��д�Ѵ��ļ������ļ�ԭ���Ѿ����Ѵ��ļ�������Ҫ�ظ���д

		boolean flag = false;

		// ���Ҹ�Ŀ¼����û�и��ļ�
		DirItem di = null;
		byte[] bytes = new byte[8];
		boolean flag1 = false;

		String filename = Util.getName(pathname);
		String type = Util.getType(pathname);
		DirItem superDirItem;// ��Ŀ¼��
		byte[] superDir = new byte[64];// ��Ŀ¼
		if (pathname.charAt(0) == '/') {
			// ���ṩ���Ǿ���·��, ����Ҫ���Ҹ�Ŀ¼
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
//				System.out.println("��Ŀ¼������");
				return false;
			}

		}else {
			// ���ṩ�������·��, ��Ŀ¼���ǵ�ǰ�򿪵�Ŀ¼
			flag1 = true;
			superDirItem = curDirItem;
		}
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);


		//�����Ŀ¼����,��Ҫ�жϸ�Ŀ¼���Ƿ���ڸ����ļ�
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// �Ӹ�Ŀ¼��ȡ����i��Ŀ¼��
			if (di.getName().equals(filename) && di.getType().equals(type) && !di.isDir()) {
				flag = true;
//				System.out.println("��Ŀ¼�����ļ�����");
				bytes = di.getBytes();  //��ȡ�����ļ���Ŀ¼��
				break;

			}
		}

		if (!flag){
			System.out.println("�ļ������ڣ�"); //���ڸ�Ŀ¼��������,�������ܴ��ڸ�Ŀ¼���Ӧ���ļ���
			return false;
		}


		/// �����ж����ļ��ľ���·��/���·���Ƿ��ڴ��̴���
		/// �������ж��ڴ��̴��ڵ��ļ��Ƿ����Ѵ��ļ�����


        if(flag1 && flag){
        	//��Ҫ�޸����·��pathname,תΪ����·��,��ֹ�ٴδ�
			String temp = "/".concat(pathname);

//			System.out.println("temp:  "+temp);

			pathname = curPath.concat(temp);
//			System.out.println("�޸ĺ��pathname: "+pathname);
        }
		boolean is_open = false;


		//
		if (flag) {/// �ڴ����д��ڸ��ļ�
			int property = bytes[5]; //��ȡDirItemĿ¼��ĵ�5���ֽڣ��������ֽ�
			// byte[] bytes = di.getBytes();
			if (property % 2 == 1 && !opts.contains("r")) { // ���1λ��1,���ļ���ֻ���ļ�;�������Ͳ���r  :˵��Ҫдֻ���ļ�
				System.out.println("дֻ���ļ����ļ��򿪴���");
				return false;
			} else {

				// �ж��Ƿ����Ѵ��ļ����д���
				for (OpenedFile of : openedFileList) {
					if (of.getPathname().equals(pathname)) {
						System.out.println("�ļ���֮ǰ�Ѵ�");
						is_open = true;
						return true;

					}
				}


				//����ļ�û�д����Ѵ��ļ������潨����OpenedFile���󣬽�����Ϣ�����Ѵ��ļ���
				//
				if (!is_open) {

					OpenedFile newFile = new OpenedFile(); // �����ļ���Ϣ��¼��OpenFiled��

					// ���潫item����Ϣ���Ƶ�newFile��ȥ
					newFile.setPathname(pathname);
					if (opts.contains("r"))//�ļ�ֻ��
						newFile.setFlag(0);
					else
						newFile.setFlag(1);

					byte temp = bytes[5];  //bytes[5]���ķ����ļ���������Ϣ
					char attribute = (char) temp;
                    newFile.setAttribute(attribute);

					int number =di.getBlockNum();
					newFile.setNumber(number);
					int[] read1 = new int[2];
					read1[0] = number;
					read1[1] = 0;
					newFile.setRead(read1);

					//�޸�write[]
					int blockNums[] = getBlockNums(di);
					int []write1 = new int[2];
					write1[0] = blockNums[blockNums.length-1];//дָ��Ŀ��ַָ����ļ������һ����
					byte[] block = disk.read(write1[0]);      //�����ļ������һ��,�ҵ��ļ�ĩβ��ַ
					for(int i = 0; i<64; i++){
						if(block[i] == '#'){
							write1[1] = i;
							break;
						}
					}
					System.out.println("dnum: "+write1[0]);
					System.out.println("bnum: "+write1[1]);
					newFile.setWrite(write1);



					openedFileList.add(newFile);

				}

//				System.out.println("file opened");
				return true;
			}

		}
		return false;

	}

	public void readFile(String pathname, int length) {

//		 1.�����Ѵ��ļ������Ƿ���ڸ��ļ�����������ڣ���򿪺��ٶ���
//	      2.Ȼ�����Ƿ����Զ���ʽ���ļ����������д��ʽ���ļ������������
//	      3.�����Ѵ��ļ����ж�����ָ�룬�����λ���϶�������Ҫ���ȣ������賤��û�ж�����
//	     �������ļ�������������ֹ������ʵ�����á�#����ʾ�ļ�������

	if (pathname.charAt(0) != '/') {
		String temp = "/".concat(pathname);

		System.out.println("temp:  "+temp);

		pathname = getCurPath().concat(temp);
		System.out.println("�޸ĺ��pathname: "+pathname);
	}



	DirItem item = findDirItem(pathname);
	boolean isopen = false;

	int flag = 0;
	OpenedFile op=null;
	for (OpenedFile of: openedFileList) {
		if (of.getPathname().equals(pathname)) {// �Ѿ���
			isopen = true;
			flag = of.getFlag();
			op=of;
			break;
		}
	}
	if (!isopen) {
		String opt="r";
		if(!openFile(pathname,opt))  {System.out.println("�ļ������ڣ���ȡ�ļ�ʧ��");return;};
		flag=0;
	}
	// �Ѿ��򿪣�
	else if (flag == 1) {
		System.out.println("�ļ�����Ϊд���������");
		return;
	}
	for (OpenedFile of: openedFileList) {
		if (of.getPathname().equals(pathname)) {// �Ѿ���
			op=of;
			break;
		}
	}
	/*����д������*/
			 byte[] fat=readFat();

			 int dnum = op.getRead()[0];
			 int bnum = op.getRead()[1];//ȷ��������ָ��

			 //���ļ���Ҫ������һ�������

			 byte[] block = disk.read(dnum);

			 for(int i = 0;i< length;i++){
			  if(bnum < 64)
			  {//��ǰ�黹û����
			    char curChar=(char)(int)block[bnum];
				  if(curChar!='#')
			   {System.out.print(curChar);
			   bnum++;}
			   else { return;}

			  }
			  else
			  {
			   //Ѱ���ļ�����һ��
				dnum = fat[dnum];
			   if(dnum==-1) {return;}
			   bnum = 0;
			   char curChar=(char)(int)block[bnum];
				  if(curChar!='#')
			   {System.out.print(curChar);
			   bnum++;}
			   else { return;}


			  }
			                           }
			 //�����ļ���������ָ��dnum��bnum


	}

	public void writeFile(String pathname, String content) {
//		1.�����Ѵ��ļ������Ƿ���ڸ��ļ�
//      ��������򿪺���д��������ڣ���Ҫ����Ƿ���д��ʽ���ļ����������д��ʽ���ļ�����
//		��д�������Ѵ��ļ����ж���дָ�룬�����λ����д�뻺���е����ݡ�
//		д�ļ������������һ������ǽ����ļ����д�룬����д�Ƚϼ򵥣�һ��дһ������
//		�ռ伴����ɣ���һ��������ļ��򿪺��д�룬����Ƚϸ��ӣ��������ļ��м��޸ĵ���
//		�⡣ʵ���У��ڶ������ֻҪ����ɴ��ļ�ĩβ���׷�ӵĹ��ܡ�

		//��·��ת�ɾ���·��
		if(!pathname.startsWith("/")) {
			pathname = toAbsPath(pathname);
		}

		//�ж��ļ��Ƿ��
		boolean open = false;
		OpenedFile ofToWrite=null;
		int flag = 0;//0Ϊֻ����1Ϊ��д
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// �Ѿ���
				open = true;
				flag = of.getFlag();
				ofToWrite=of;
				break;
			}
		}
		if (!open) {
//			if(!openFile(pathname,"")){
//				System.out.println("�ļ������ڣ�д�ļ�ʧ��");
//				return;
//			}
//			flag=1;
			System.out.println("�ļ�δ��");
			return;
		}
		// �Ѿ��򿪣�
		else if (flag == 0) {
			System.out.println("�ļ�����Ϊ����������д����");
			return;
		}

		for (OpenedFile of: openedFileList) {
			if (of.getPathname().equals(pathname)) {// �Ѿ���
				ofToWrite=of;
				break;
			}
		}


		int dnum = ofToWrite.getWrite()[0];// дָ��ĵ�0��Ԫ�ر�ʾ���ַ
		int bnum = ofToWrite.getWrite()[1];// дָ��ĵ�1��Ԫ�ر�ʾ���ڵ�ַ
		int nxtDnum;
		int cp = 0;// contentPointer
		int sizeIncrement = 0;
		// ���ļ������һ�������
		byte[] block = disk.read(dnum);

		for (cp = 0; cp < content.length(); cp++) {
//			if (bnum < 64) {// �������ָ��С��64��˵����ǰ�黹û��
//				block[bnum] = (byte) (int) content.charAt(cp);
//				bnum++;
//			} else {// ��ǰ����������Ҫ�����µĴ��̿�
//				sizeIncrement++;
//				disk.write(dnum,block);// �Ȱ��Ѿ�д�õĿ�д�ش���
//				nxtDnum = Util.findAvailableBlock(disk);
//				writeFat(dnum, nxtDnum);
//				writeFat(nxtDnum, -1);// �ҵ�һ���µĿ��п飬������FAT
//				dnum = nxtDnum;
//				block = disk.read(dnum);// ��������Ŀ������
//				bnum = 0;
//				block[bnum] = (byte) (int) content.charAt(cp);
//				bnum++;
//			}
			
			block[bnum] = (byte) (int) content.charAt(cp);//���ж�ָ����û��Խ�磬ֱ��д�ֽ�
			bnum++;//ָ��������Ȼ�����ж���û��Խ��
			if(bnum == 64) {
				disk.write(dnum,block);//ָ��Խ�磬˵���Ѿ�д����һ���飬�������д�ش���
				nxtDnum = Util.findAvailableBlock(disk);// �ҵ�һ���µĿ��п飬������FAT
				writeFat(dnum, nxtDnum);
				writeFat(nxtDnum, -1);
				dnum = nxtDnum;
				block = disk.read(dnum);// ��������Ŀ������
				bnum = 0;//дָ�����»ص�0
			}
		}

		disk.write(dnum,block);//ѭ�������󣬻��������һ����ûд�ش��̣����Բ���


		int[] newWriter= {dnum,bnum};
		ofToWrite.setWrite(newWriter);//����дָ��
		//�����ļ��Ĵ�С����С�������Ѿ������sizeIncrement��
		int len=ofToWrite.getLength();
		len+=content.length();
		ofToWrite.setLength(len);





	}

	public boolean closeFile(String pathname) {

		// *1�ȿ��ļ��Ƿ����Ѵ��ļ�����
		// *2�Ѿ��򿪣����򿪷�ʽflag,
		// *3���flag==1���޸�Ŀ¼������ļ�����ɾ����Ӧ��

		if (pathname.charAt(0) != '/') {
			String temp = "/".concat(pathname);

//			System.out.println("temp:  "+temp);

			pathname = curPath.concat(temp);
//			System.out.println("�޸ĺ��pathname: "+pathname);
		}


//
		DirItem item = findDirItem(pathname);
		boolean isopen = false;

		int flag = 0;
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// �Ѿ���
				isopen = true;
				flag = of.getFlag();
				break;
			}
		}
		if (!isopen) {
			System.out.println("�ļ�֮ǰû�д򿪣�����ر�");
			return false;
		}

//		if (flag == 0) {
////			System.out.println("�ļ�û�о���д����"); //ֱ�ӽ��ļ���Ϣ���Ѵ�Ŀ¼��ɾ������
//		}


		// flag==1,�ļ������޸ģ���Ҫ1.�޸�Ŀ¼��--���ļ��ܳ��ȣ�2.׷���ļ���������#��
		if (flag == 1) { // �ļ�����д������������ҵ��ļ���ռ�����̿������޸����̿���

			// ���º��������ҵ��ļ���ռ���̿���
			byte[] readDisk = new byte[128];

			disk.read(0);
			System.arraycopy(disk.getReader(), 0, readDisk, 0, 64);
			disk.read(1);
			System.arraycopy(disk.getReader(), 0, readDisk, 64, 64);
			int startNumber = item.getBytes()[6];
			// System.out.println("startNumbe = " + startNumber);
			// System.out.println(" readDisk[startNumbe] = "+readDisk[startNumber] );
			int length = 0;

			while (readDisk[startNumber] != 255 && readDisk[startNumber] != -1) {
				length++;
				startNumber = readDisk[startNumber];
			}

			item.setSize(length);


			///// ������׷���ļ���������#��
			//write[0]--�ڼ���   write[1]--�ڼ�����
			OpenedFile of;
			int i=0;
			for(i=0;i<openedFileList.size();i++){
//				System.out.println(openedFileList.get(i).getPathname());
				if(openedFileList.get(i).getPathname().equals(pathname)){

//					System.out.println("i_equ : "+i);
					break;
				}
			}
//			System.out.println("i="+i);
			of = openedFileList.get(i);

			int []write = of.getWrite();
			byte[]block=disk.read(write[0]);
			block[write[1]]='#';
			disk.write(write[0],block);

			//////


		}

		// ���ļ����Ѵ��ļ�����ɾ��
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {
				// System.out.println("�ļ��Ѵ�");
//				System.out.println("δɾǰ����" + openedFileList.size());
				openedFileList.remove(of);
//				System.out.println("ɾ���󳤶�" + openedFileList.size());
				break;
			}
		}
		System.out.println("�ļ��ر�");
		return true;



	}


	public void deleteFile(String pathname) {
		if(!pathname.startsWith("/")){
			pathname = toAbsPath(pathname);//�����������·�������·������ô����ת�ɾ���·��
		}
		System.out.println("deleting file: "+pathname);
		//�ж��ļ��Ƿ��
		boolean opened = false;	
		for(OpenedFile op:openedFileList){
			if(op.getPathname().equals(pathname)){
				opened = true;
			}
		}
		if(opened){
			System.out.println("�ļ��Ѵ�");
			return;
		}
		//��ȡ�ļ��Ŀ���б�
		int[] blockNums = getBlockNums(findDirItem(pathname));
		//֮����ļ�ռ�õ����п�ȫ�����ռ�ã���FAT���ж�Ӧ��λ��ȫ���޸�Ϊ0����ʾδ��ռ�ã�
		for(int blockNum:blockNums){
//			System.out.println("blockNum: "+blockNum);
			writeFat(blockNum,0);
		}
		//���ڸ�Ŀ¼�����ٵ�Ŀ¼��
		DirItem superDirItem = findSuperDirItem(pathname);
		byte[] superDir = disk.read(superDirItem.getBlockNum());
		String name = findDirItem(pathname).getFullName();//��ȡҪɾ�����ļ�������
		//�ҵ�Ҫ���ٵ�Ŀ¼���Ǹ�Ŀ¼�еĵڼ���
		int numOfDirItemToDestroy = 0;
		for(int i = 0; i < 8 ;i++){
			if(Util.getDirItemAt(superDir,i).getFullName().equals(name)){
				numOfDirItemToDestroy = i;
//				System.out.println("numOfDirItemToDestroy: "+numOfDirItemToDestroy);
				break;
			}
		}
		//��ʼ����
		for(int i = 0; i < 8; i++){
			superDir[numOfDirItemToDestroy*8+i] = 0;
		}
		//�Ѹ�Ŀ¼д�ش���
		disk.write(superDirItem.getBlockNum(),superDir);
		//�ٰѸ�Ŀ¼���¶�����
//		curDir = disk.read(curDirItem.getBlockNum());
		updateCurDir();
	}

	public void typeFile(String pathname) {

	}

	public void change() {

	}

	public boolean md(String pathname) {
		String folderName;
		DirItem superDirItem;
		byte[] superDir = new byte[64];

		int availableBlock;
		DirItem di;

		if (pathname.charAt(0) == '/') {
			// �þ���·��������Ŀ¼, ����Ҫͨ��·��Ѱ�Ҹ�Ŀ¼
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
				return false;
			}
		} else {
			// �����·��������Ŀ¼, ��ǰ�򿪵�Ŀ¼���Ǹ�Ŀ¼
			superDirItem = curDirItem;
		}

		// �Ѹ�Ŀ¼�����ݶ�ȡ��������, �����Ƴ���
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);

		// ��Ŀ¼����,�ж������Ƿ��������ļ���
		folderName = Util.getFolderName(pathname);
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);
			if (di.getName().equals(folderName) && di.isDir()) {
				return false;// ���������ļ���, ��ֹ����
			}
		}

		// �����������ļ���, ��Ѱ��һ����λ�÷���Ŀ¼��
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(disk.getReader(), i);
			if (di.getName().equals("")) {
				// �½�һ��Ŀ¼��, д�����ļ��е���Ϣ
				di = new DirItem();
				di.setName(folderName);
				di.setType("  ");// �ļ��е������������ո����

				di.setProperty(false, false, true);// ��������, �� read-only, ��ϵͳ�ļ�, ΪĿ¼
				availableBlock = Util.findAvailableBlock(disk);// �����̿��
				di.setBlockNum(availableBlock);
				wipeBlock(availableBlock);//�ѷ��䵽���̿����
				di.setSize(0);// �ļ��еĴ�Сͳһ����Ϊ0

				Util.writeDirItem(di, superDirItem.getBlockNum(), i, disk);// ��Ŀ¼��д��Ӳ����
				Util.writeFat(availableBlock, 255, disk);// ����FAT
				updateCurDir();//��Ϊ�������ڵ�ǰĿ¼�½���һ���ļ���, ����ˢ��һ��
				return true;

			}
		}
		return false;// �Ҳ�����λ�ã�����ʧ��
	}

	public boolean dir(String pathname) {

		DirItem di = new DirItem();
		String name = Util.getFolderName(pathname);

		if (pathname.charAt(0) == '/') {
			// �þ���·������Ŀ¼, ֱ����findDirItem�����ҵ���Ӧ��Ŀ¼��
			di = findDirItem(pathname);
			if (di == null) {
				System.out.println("�Ҳ���·����");
				return false;
			}
			//���µ�ǰ·��
			curPath = pathname;

		} else {
			// �����·������Ŀ¼, ����Ҫ�ѵ�ǰ�򿪵�Ŀ¼������Ŀ¼, �ڸ�Ŀ¼��Ѱ��Ҫ�򿪵��ļ���
			byte[] superDir = curDir;
			boolean found = false;
			for (int i = 0; i < 8; i++) {
				di = Util.getDirItemAt(superDir, i);// �Ӹ�Ŀ¼��ȡ����i��Ŀ¼��
				if (di.getName().equals(name) && di.isDir()) {
					found = true;
					break;
				}
			}
			if(!found) {//�ڵ�ǰĿ¼�Ҳ���������Ŀ¼, ���� false
				System.out.println("�Ҳ���·����");
				return false;
			}

			//������һ��Ŀ¼ʱ, �ڵ�ǰ·��������һ��"/", Ȼ������²�Ŀ¼������
			if(!curPath.equals("/")) {
				curPath+="/";
			}
			curPath+=pathname;
		}

		// ���������Ǵ�һ��Ŀ¼����������(��Ŀ¼�����ݺ�Ŀ¼��ȫ��ת�Ƶ��ڴ���)
		disk.read(di.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);
		curDirItem = di;

		// �����ǰĿ¼����ʲô����
		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> dirNames = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			// �ѵ�ǰĿ¼��8��Ŀ¼��ȫ��ȡ�������һ��
			// Ϊ�յĶ���, Ϊ�ļ������ּӵ�fileNames��, ΪĿ¼�����ּӵ�dirNames��
			di = Util.getDirItemAt(curDir, i);// di�������Ѿ�û����, ֱ����������
			if (di.getFullName().equals(".")) {
				continue;
			}
			if (di.isDir()) {
				dirNames.add(di.getFullName());
			} else {
				fileNames.add(di.getFullName());
			}
		}

		for (String s : dirNames) {
			//���Ŀ¼��
			System.out.print(s);
			//����ո�
			for (int i = 0; i < 20-s.length(); i++) {
				System.out.print(" ");
			}
			System.out.println("[Ŀ¼]");
		}
		for (String s : fileNames) {
			//����ļ���
			System.out.print(s);
			//����ո�
			for (int i = 0; i < 20-s.length(); i++) {
				System.out.print(" ");
			}
			System.out.println("[�ļ�]");
		}
		return true;
	}

	public void rd(String pathname) {
		if(pathname.equals("/")){
			System.out.println("ɾ����Ŀ¼����ô���������");
			return;
		}
		if(!pathname.startsWith("/")){
			pathname = toAbsPath(pathname);
		}
		DirItem diToRemove = findDirItem(pathname);
		if(diToRemove == null){
			System.out.println("dir does not exit");
			return;
		}
		byte[] dirToRemove = disk.read(diToRemove.getBlockNum());
		for(int i = 0; i < 8 ; i++){
			DirItem di = Util.getDirItemAt(dirToRemove,i);
			if(di.getFullName().equals(".")) {
				continue;
			}
			if(!di.isDir()){
				deleteFile(pathname + "/"+di.getFullName());
			}else{
				rd(pathname +"/"+di.getFullName());
			}
		}
		DirItem superDirItem = findSuperDirItem(pathname);
		byte[] superDir = disk.read(superDirItem.getBlockNum());
			for(int i = 0; i < 8 ; i++){
			DirItem di = Util.getDirItemAt(superDir,i);
			if(di.getFullName().equals(diToRemove.getFullName())){
				//�Ѹ�Ŀ¼�е�Ŀ¼������
				for(int j = 0; j < 8; j++){
					superDir[i*8+j] = 0;
				}
				//�Ѹ�Ŀ¼д�ش���
				disk.write(findSuperDirItem(pathname).getBlockNum(),superDir);
				//�ٰѸ�Ŀ¼������
//				curDir = disk.read(curDirItem.getBlockNum());
				updateCurDir();
				break;
			}

		}
	}



	public byte[] getCurDir() {
		return curDir;
	}

	public String getCurPath() {
		return curPath;
	}

	//getters & setters

}
