package core;

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
//			System.out.print(s+" ");
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

		} else if (command.equals("rd")) {

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
				break;
			}
		}
		updateCurDir();//��Ϊ�������ڵ�ǰĿ¼�½���һ���ļ�, ����ˢ��һ��
		return true;
	}

	public boolean openFile(String pathname, String opts) { // �����flag��ʾ�������ͣ�flag������Դ���Ѵ��ļ������
		
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
		DirItem di;
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
				System.out.println("����·��������");
				return false;
			} else {

				System.out.println("����·������");
			}
		} else {
			// ���ṩ�������·��, ��Ŀ¼���ǵ�ǰ�򿪵�Ŀ¼
			flag1 = true;
			superDirItem = curDirItem;
		}
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);

		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// �Ӹ�Ŀ¼��ȡ����i��Ŀ¼��
			if (di.getName().equals(filename) && di.getType().equals(type) && !di.isDir()) {
				flag = true;
				System.out.println("���·���ļ�����");
				bytes = di.getBytes();
				break;

			}
		}

		if (!flag){
			System.out.println("�ļ������ڣ�������");
			//return false;
		}


		/// �����ж����ļ��ľ���·��/���·���Ƿ��ڴ��̴���
		/// �������ж��ڴ��̴��ڵ��ļ��Ƿ����Ѵ��ļ�����


        if(flag1 && flag){
        	//��Ҫ�޸�pathname,��ֹ�ٴδ�
			String temp = "/".concat(pathname);

			System.out.println("temp:  "+temp);

			pathname = curPath.concat(temp);
			System.out.println("�޸ĺ��pathname: "+pathname);
        }
		boolean is_open = false;

		if (flag) {/// �ڴ����д��ڸ��ļ�
			int property = bytes[5];
			// byte[] bytes = di.getBytes();
			if (property % 2 == 1 && !opts.contains("r")) { // ˵����Ҫдֻ���ļ�
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

				if (!is_open) { // �ļ���û�д򿪣���Ҫ��ӵ��Ѵ��ļ�����,��Ҫ��

					OpenedFile newFile = new OpenedFile(); // ��Ҫ�����ļ���Ϣ��¼��newFile��
					// ������Ҫ��item����Ϣ���Ƶ�newFile��ȥ
					newFile.setPathname(pathname);
					if (opts.contains("r"))//�ļ�ֻ��
						newFile.setFlag(0);
					else
						newFile.setFlag(1);
					byte temp = bytes[5];
					char attribute = (char) temp;

					newFile.setAttribute(attribute);
					int number = bytes[6];
					newFile.setNumber(number);
					int[] read1 = new int[2];
					read1[0] = number;
					read1[1] = 0;
					newFile.setRead(read1);
					/// ע�����������������������������
					/// ��һ�δ�����֮ǰû�м�¼����ʼ��Ϊ0��Ŀ¼���¼�����̿����������ļ�ռ���ֽ�����
					/// ͬ��read[0]��ʼ��Ϊ��ʼ�̿飬read[1]��ʼ��Ϊ0
					/// write[0]��ʼ��Ϊ0��write[1]��ʼ��Ϊ1

					openedFileList.add(newFile);

				}
				// ˵���ļ��Ѿ���
				System.out.println("file opened");
				return true;
			}

		}
		return false;

	}

	public void readFile(String pathname, int length) {

	}

	public void writeFile(String pathname, String content) {

	}

	public boolean closeFile(String pathname) {

		// *1�ȿ��ļ��Ƿ����Ѵ��ļ�����
		// *2�Ѿ��򿪣����򿪷�ʽflag,
		// *3���flag==1���޸�Ŀ¼������ļ�����ɾ����Ӧ��
		if (pathname.charAt(0) != '/') {
			String temp = "/".concat(pathname);

			System.out.println("temp:  "+temp);

			pathname = curPath.concat(temp);
			System.out.println("�޸ĺ��pathname: "+pathname);
		}



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
			System.out.println("�ļ���û�д򿪣�����ر�");
			return false;
		}
		// �Ѿ��򿪣�
		if (flag == 0) {
			System.out.println("�ļ�û�о���д����");
		}
		// flag==1,�ļ������޸ģ���Ҫ�޸�Ŀ¼�׷���ļ���������#��
		// ������������������
		// ע�⣺�ļ���������#��׷����Ҫ�������write����ʵ��ʱ��д������Ͳ�

		if (flag == 1) { // �ļ�����д������������ҵ��ļ���ռ�����̿���

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
			// System.out.println("here");
			item.setSize(length);
		}

		// ���ļ����Ѵ��ļ�����ɾ��
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {
				// System.out.println("�ļ��Ѵ�");
				System.out.println("δɾǰ����" + openedFileList.size());
				openedFileList.remove(of);
				System.out.println("ɾ���󳤶�" + openedFileList.size());
				break;
			}
		}
		System.out.println("�ļ��ر�");
		return true;

		/// �����Ѵ��ļ�����ɾ����Ӧ��

	}

	public void deleteFile(String pathname) {

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

	public void rd() {

	}
	
	private void updateCurDir() {
		disk.read(curDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);
//		disk.read(curDirItem.getBlockNum());
	}

	public byte[] getCurDir() {
		return curDir;
	}
	
	public String getCurPath() {
		return curPath;
	}
		
	//getters & setters
	
}
