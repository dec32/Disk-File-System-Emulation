package core;

import java.util.ArrayList;


public class Core {

	private Disk disk;
	private DirItem curDirItem = new DirItem();// 保存当前打开的目录的目录项
	private byte[] curDir = new byte[64];// 保存当前打开的目录
	private String curPath = "/";
	private ArrayList<OpenedFile> openedFileList = new ArrayList<OpenedFile>();//已打开文件表

	public Core() {
		disk = new Disk();
		disk.createDisk("D:/test.dsk");

		// 使用绝对路径的命令
		dir("/");
		md("/123");
		md("/123/456");
		createFile("/123/456/abc.ef", "");//空串的意思是, 不选择任何选项(既不是系统文件, 也不是只读)
		openFile("/123/456/abc.ef","");
		writeFile("/123/456/abc.ef", "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ");
		closeFile("/123/456/abc.ef");
		dir("/123");
		// 使用相对路径的命令
		dir("456");
		md("789");
		createFile("ghi.jk", "");
		// 转到根目录
		dir("/");
	}

	public void execute(String str) {

		/*
		 * 示范:
		 * dir /123 (转到 /123 下)
		 * dir 456 (转到 456 下)
		 * create kkk.kk (创建一个普通文件)
		 * create -s kkk.kk (创建一个系统文件)
		 * create -r -s kkk.kk(创建一个只读的系统文件)
		 * open -r kkk.kk(以只读方式打开文件, -r 代表"read only")
		 * open kkk.kk(打开文件, 可以写也可以读)
		 */

		// 提供字符串形式的一行命令, 新建一个 CommandLine 对象, 构造方法会解析选项和参数
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
			openFile(args[0], opts);// args0 为路径, 命令中唯一的参数, 下同
		} else if (command.equals("close")) {
			closeFile(args[0]);
		} else if (command.equals("dir")) {
			dir(args[0]);// dir 命令只有一个参数, 路径
		} else if (command.equals("write")) {
			writeFile(args[0],args[1]);
		} else if (command.equals("read")) {
			readFile(args[0], Integer.valueOf(args[1]));
		} else if (command.equals("type")) {
			typeFile(args[0]);
		} else if (command.equals("change")) {
			change(args[0],opts);
		} else if (command.equals("create")) {
			createFile(args[0], opts);// create 命令只有一个参数, 路径. 选项会有-r和-s
		} else if (command.equals("md")) {
			md(args[0]);// md 命令只有一个参数, 路径
		} else if (command.equals("delete")) {
			deleteFile(args[0]);
		} else if (command.equals("rd")) {
			rd(args[0]);
		} else {
			System.out.println("Syntax error.");
		}

	}



	// 返回给定目录或文件的目录项(只能处理绝对路径(以'/'开头))
	private DirItem findDirItem(String pathname) {
		String[] names = pathname.split("/"); // 拆解路径, 形如"/a/b/c"的字符串, 会被拆解为"", "a", "b", "c"(含空字符串)
		String fullname = Util.getFullName(pathname);// 目标文件的完整名字
		byte[] curDir = new byte[64];
		// 从根目录開始查询
		DirItem di = DirItem.createRootDirItem();

		disk.read(di.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);// 首先读入根目录的内容

		// 根据每一级的文件夹名(或文件名), 一层一层往下搜索, names[0] 为空字符串, 所以直接从names[1] 开始遍历
		for (int i = 1; i < names.length; i++) {
			// 遍历当前目录的8个目录项，j为项指针
			boolean found = false;
			for (int j = 0; j < 8; j++) {
				di = Util.getDirItemAt(curDir, j);// 从reader中取出第j个目录项
				if (di.getFullName().equals(names[i])) {// 目录项和路径指定的目录同名, 转到此目录下
					// 注: 如果di指向的是一个文件, 那么文件的第一个块会被写到curDir里面, 不过不影响程序执行
					disk.read(di.getBlockNum());
					Util.copyBlock(disk.getReader(), curDir);
					found = true;
					break;
				}
			}
			if (!found) {
//				System.out.println("findDirItem没有找到该文件");
				return null;// 找不到文件夹，终止方法
			}
		}
		return di;
	}

	// 返回父目录的目录项(只能处理绝对路径(以'/'开头))
	private DirItem findSuperDirItem(String pathname) {
		String[] names = pathname.split("/"); // 拆解路径, 形如"/a/b/c"的字符串, 会被拆解为"", "a", "b", "c"(含空字符串)
		byte[] curDir = new byte[64];
		// 从根目录開始查询
		DirItem di = DirItem.createRootDirItem();

		disk.read(di.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);// 首先读入根目录的内容

		// 针对每一级目录的循环, names[0] 为空字符串, 所以直接从names[1] 开始遍历
		for (int i = 1; i < names.length - 1; i++) {
			// 遍历当前目录的8个目录项，j为项指针
			boolean found = false;
			for (int j = 0; j < 8; j++) {
				di = Util.getDirItemAt(curDir, j);// 从reader中取出第j个目录项
				if (di.getName().equals(names[i]) && di.isDir()) {// 如果目录项为文件夹，且和路径指定的文件夹同名，则转到这个文件夹
					disk.read(di.getBlockNum());
					Util.copyBlock(disk.getReader(), curDir);
					found = true;
					break;
				}
			}
			if (!found) {
				return null;// 找不到文件夹，终止方法
			}
		}
		return di;
	}

	//更新当前目录
	private void updateCurDir() {
		disk.read(curDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);
	}
	
	//读取fat
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

	//写fat
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
	
	//把指定块的内容擦除
	private void wipeBlock(int blockNum) {
		byte[] emptyBlock= new byte[64];
		disk.write(blockNum, emptyBlock);
	}

	public boolean createFile(String pathname, String opts) {
		// opts 中可能有的选项有 -r 和 -s, 前者表示只读, 后者表示系统
		String filename = Util.getName(pathname);
		String type = Util.getType(pathname);
		DirItem superDirItem;// 父目录项
		byte[] superDir = new byte[64];// 父目录

		if (pathname.charAt(0) == '/') {
			// 若提供的是绝对路径, 则需要查找父目录
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
				return false;
			}
			// else System.out.println("绝对路径存在");
		} else {
			// 若提供的是相对路径, 则父目录就是当前打开的目录
			superDirItem = curDirItem;
		}

		// 把父目录的内容读取到缓存中, 并复制出来
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);

		// 查找父目录中有没有重名文件
		DirItem di;
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// 从父目录中取出第i个目录项
			if (di.getName().equals(filename) && di.getType().equals(type) && !di.isDir()) {
				System.out.println("存在重名文件。");
				return false;// 有重名文件，终止方法
			}
		}

		// 寻找一个空位把新文件的目录项放进去
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// 从reader中取出第i个目录项
			if (di.getName().equals("")) {// 第i个位置为空，空位置的文件名为空字符串

				// 建立新文件的目录项
				di.setName(filename);// 设置名字
				di.setType(type);// 设置类型

				// 设置属性
				boolean ro = false;
				boolean sys = false;
				if(opts.contains("r")) {
					ro = true;
				}
				if(opts.contains("s")) {
					sys = true;
				}
				di.setProperty(ro, sys, false);// 最后一个false表示这是个一个文件而不是一个目录

				int availableBlock;
				availableBlock = Util.findAvailableBlock(disk);// 找到一个空闲块
				di.setBlockNum(availableBlock);// 设置要占用的磁盘块
				di.setSize(1);// 设置文件大小，初始为1块

				Util.writeDirItem(di, superDirItem.getBlockNum(), i, disk);// 把目录项写到找到的空位之中
				Util.writeFat(availableBlock, 255, disk);// 更新FAT
				//往文件的第一块里面写一个“#”
				byte[] block = new byte[64];
				block[0] = '#';
				disk.write(availableBlock, block);
				break;
			}
		}
		updateCurDir();//因为可能是在当前目录新建了一个文件, 所以刷新一下
		return true;
	}

	public boolean openFile(String pathname, String opts) { //pathname-文件名,opts-操作类型(读/写)
		/*
		 * 传进来的pathname可能是相对路径, 也可能是绝对路径
		 * 解决的办法是, 如果传进来的是相对路径, 我就把他转换成绝对路径
		 * 绝对路径 = 当前路径+"/"+相对路径
		 */

		// *1检查文件是否存在,若不存在，则打开失败
		// *2存在，检查打开方式flag是读还是写，和文件类型是否符合
		// *3填写已打开文件表，若文件原本已经在已打开文件表，不需要重复填写

		boolean flag = false;

		// 查找父目录中有没有该文件
		DirItem di = null;
		byte[] bytes = new byte[8];
		boolean flag1 = false;

		String filename = Util.getName(pathname);
		String type = Util.getType(pathname);
		DirItem superDirItem;// 父目录项
		byte[] superDir = new byte[64];// 父目录
		if (pathname.charAt(0) == '/') {
			// 若提供的是绝对路径, 则需要查找父目录
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
//				System.out.println("父目录不存在");
				return false;
			}

		}else {
			// 若提供的是相对路径, 则父目录就是当前打开的目录
			flag1 = true;
			superDirItem = curDirItem;
		}
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);


		//如果父目录存在,需要判断父目录下是否存在该子文件
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// 从父目录中取出第i个目录项
			if (di.getName().equals(filename) && di.getType().equals(type) && !di.isDir()) {
				flag = true;
//				System.out.println("父目录下子文件存在");
				bytes = di.getBytes();  //获取该子文件的目录项
				break;

			}
		}

		if (!flag){
			System.out.println("文件不存在！"); //由于父目录都不存在,更不可能存在父目录相对应的文件了
			return false;
		}


		/// 以上判断了文件的绝对路径/相对路径是否在磁盘存在
		/// 接下来判断在磁盘存在的文件是否在已打开文件表中


        if(flag1 && flag){
        	//需要修改相对路径pathname,转为绝对路径,防止再次打开
			String temp = "/".concat(pathname);

//			System.out.println("temp:  "+temp);

			pathname = curPath.concat(temp);
//			System.out.println("修改后的pathname: "+pathname);
        }
		boolean is_open = false;


		//
		if (flag) {/// 在磁盘中存在该文件
			int property = bytes[5]; //获取DirItem目录项的第5个字节，即属性字节
			// byte[] bytes = di.getBytes();
			if (property % 2 == 1 && !opts.contains("r")) { // 最后1位是1,该文件是只读文件;操作类型不是r  :说明要写只读文件
				System.out.println("写只读文件，文件打开错误！");
				return false;
			} else {

				// 判断是否在已打开文件表中存在
				for (OpenedFile of : openedFileList) {
					if (of.getPathname().equals(pathname)) {
						System.out.println("文件在之前已打开");
						is_open = true;
						return true;

					}
				}


				//如果文件没有存在已打开文件表，下面建立新OpenedFile对象，将该信息存入已打开文件表
				//
				if (!is_open) {

					OpenedFile newFile = new OpenedFile(); // 将打开文件信息记录在OpenFiled中

					// 下面将item的信息复制到newFile中去
					newFile.setPathname(pathname);
					if (opts.contains("r"))//文件只读
						newFile.setFlag(0);
					else
						newFile.setFlag(1);

					byte temp = bytes[5];  //bytes[5]存文放着文件的属性信息
					char attribute = (char) temp;
                    newFile.setAttribute(attribute);

					int number =di.getBlockNum();
					newFile.setNumber(number);
					int[] read1 = new int[2];
					read1[0] = number;
					read1[1] = 0;
					newFile.setRead(read1);

					//修改write[]
					int blockNums[] = getBlockNums(di);
					int []write1 = new int[2];
					write1[0] = blockNums[blockNums.length-1];//写指针的块地址指向该文件的最后一个块
					byte[] block = disk.read(write1[0]);      //读出文件的最后一块,找到文件末尾地址
					for(int i = 0; i<64; i++){
						if(block[i] == '#'){
							write1[1] = i;
							break;
						}
					}
//					System.out.println("dnum: "+write1[0]);
//					System.out.println("bnum: "+write1[1]);
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

//		 1.查找已打开文件表中是否存在该文件；如果不存在，则打开后再读；
//	      2.然后检查是否是以读方式打开文件，如果是以写方式打开文件，则不允许读；
//	      3.最后从已打开文件表中读出读指针，从这个位置上读出所需要长度，若所需长度没有读完已
//	     经遇到文件结束符，就终止操作。实验中用“#”表示文件结束。

		if (pathname.charAt(0) != '/') {
			String temp = "/".concat(pathname);

//			System.out.println("temp:  " + temp);

			pathname = getCurPath().concat(temp);
//			System.out.println("修改后的pathname: " + pathname);
		}

		DirItem item = findDirItem(pathname);
		boolean isopen = false;

		int flag = 0;
		OpenedFile op = null;
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				isopen = true;
				flag = of.getFlag();
				op = of;
				break;
			}
		}
		if (!isopen) {
			String opt = "r";
			if (!openFile(pathname, opt)) {
				System.out.println("文件不存在，读取文件失败");
				return;
			}
			;
			flag = 0;
		}
		// 已经打开：
		else if (flag == 1) {
			System.out.println("文件类型为写，不允许读");
			return;
		}
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				op = of;
				break;
			}
		}
		/* 这里写第三步 */
		byte[] fat = readFat();

		int dnum = op.getRead()[0];
		int bnum = op.getRead()[1];// 确定两个读指针

		// 把文件需要读的那一块读出来

		byte[] block = disk.read(dnum);

		for (int i = 0; i < length; i++) {
			if (bnum < 64) {// 当前块还没读完
				char curChar = (char) (int) block[bnum];
				if (curChar != '#') {
					System.out.print(curChar);
					bnum++;
				} else {
					return;
				}

			} else {
				// 寻找文件的下一块
				dnum = fat[dnum];
				if (dnum == -1) {
					return;
				}
				block = disk.read(dnum);
				bnum = 0;
				char curChar = (char) (int) block[bnum];
				if (curChar != '#') {
					System.out.print(curChar);
					bnum++;
				} else {
					return;
				}

			}
		}
		System.out.println();
		// 更新文件的两个读指针dnum和bnum
		int[] read = new int[2];
		read[0] = dnum;
		read[1] = bnum;
		op.setRead(read);

	}

	public void writeFile(String pathname, String content) {
//		1.查找已打开文件表中是否存在该文件
//      不存在则打开后再写；如果存在，还要检查是否以写方式打开文件；如果不是写方式打开文件，不
//		能写；最后从已打开文件表中读出写指针，从这个位置上写入缓冲中的数据。
//		写文件有两种情况，一种情况是建立文件后的写入，这种写比较简单，一边写一边申请
//		空间即可完成；另一种情况是文件打开后的写入，这个比较复杂，存在着文件中间修改的问
//		题。实验中，第二种情况只要求完成从文件末尾向后追加的功能。

		//把路径转成绝对路径
		if(!pathname.startsWith("/")) {
			pathname = toAbsPath(pathname);
		}

		//判断文件是否打开
		boolean open = false;
		OpenedFile ofToWrite=null;
		int flag = 0;//0为只读，1为读写
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				open = true;
				flag = of.getFlag();
				ofToWrite=of;
				break;
			}
		}
		if (!open) {
//			if(!openFile(pathname,"")){
//				System.out.println("文件不存在，写文件失败");
//				return;
//			}
//			flag=1;
			System.out.println("文件未打开");
			return;
		}
		// 已经打开：
		else if (flag == 0) {
			System.out.println("文件类型为读，不允许写操作");
			return;
		}

		for (OpenedFile of: openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				ofToWrite=of;
				break;
			}
		}


		int dnum = ofToWrite.getWrite()[0];// 写指针的第0个元素表示块地址
		int bnum = ofToWrite.getWrite()[1];// 写指针的第1个元素表示块内地址
		int nxtDnum;
		int cp = 0;// contentPointer
		// 把文件的最后一块读出来
		byte[] block = disk.read(dnum);

		for (cp = 0; cp < content.length(); cp++) {
		
			block[bnum] = (byte) (int) content.charAt(cp);//不判断指针有没有越界，直接写字节
			bnum++;//指针自增，然后再判断有没有越界
			if(bnum == 64) {
				disk.write(dnum,block);//指针越界，说明已经写满了一个块，把这个块写回磁盘
				nxtDnum = Util.findAvailableBlock(disk);// 找到一个新的空闲块，并更新FAT
				writeFat(dnum, nxtDnum);
				writeFat(nxtDnum, -1);
				dnum = nxtDnum;
				block = disk.read(dnum);// 把新申请的块读出来
				bnum = 0;//写指针重新回到0
			}
		}

		disk.write(dnum,block);//循环结束后，还会有最后一个块没写回磁盘，所以补上

		int[] newWriter= {dnum,bnum};
		ofToWrite.setWrite(newWriter);//更新写指针
		//更新文件的大小
		int len=ofToWrite.getLength();
		len+=content.length();
		ofToWrite.setLength(len);
	}

	/*
	 * TODO: 关闭文件时要更新文件大小
	 */
	public boolean closeFile(String pathname) {

		// *1先看文件是否在已打开文件表中
		// *2已经打开，检查打开方式flag,
		// *3如果flag==1，修改目录项，从已文件表中删除对应项

		if (pathname.charAt(0) != '/') {
			String temp = "/".concat(pathname);
//			System.out.println("temp:  "+temp);
			pathname = curPath.concat(temp);
//			System.out.println("修改后的pathname: "+pathname);
		}
//
		DirItem item = findDirItem(pathname);
		boolean isopen = false;

		int flag = 0;
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				isopen = true;
				flag = of.getFlag();
				break;
			}
		}
		if (!isopen) {
			System.out.println("文件之前没有打开，无需关闭");
			return false;
		}

//		if (flag == 0) {
////			System.out.println("文件没有经过写操作"); //直接将文件信息从已打开目录表删除就行
//		}

		// flag==1,文件经过修改，需要1.修改目录项--即文件总长度，2.追加文件结束符‘#’
		if (flag == 1) { // 文件经过写操作，需遍历找到文件所占用总盘块数，修改总盘快数

			// 以下函数遍历找到文件所占用盘块数
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


			///// 在这里追加文件结束符‘#’
			//write[0]--第几块   write[1]--第几个字
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

			//更新目录项里记录的文件大小
			DirItem diToUpdate = findDirItem(pathname);
			diToUpdate.setSize(of.getLength()/64 + 1);
			DirItem superDirItem = findSuperDirItem(pathname);
			byte[] superDir = disk.read(superDirItem.getBlockNum());
				for(int k = 0; k < 8 ; k++){
				DirItem di = Util.getDirItemAt(superDir,k);
				if(di.getFullName().equals(diToUpdate.getFullName())){
					//把父目录中的目录项销毁
					for(int j = 0; j < 8; j++){
						superDir[k*8+j] = diToUpdate.getBytes()[j];
					}
					//把父目录写回磁盘
					disk.write(findSuperDirItem(pathname).getBlockNum(),superDir);
					//再把父目录读回来
					updateCurDir();
					break;
				}
			}

		}

		// 将文件从已打开文件表中删除
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {
				// System.out.println("文件已打开");
//				System.out.println("未删前长度" + openedFileList.size());
				openedFileList.remove(of);
//				System.out.println("删除后长度" + openedFileList.size());
				break;
			}
		}
		System.out.println("文件关闭");
		return true;



	}


	public void deleteFile(String pathname) {
		if(!pathname.startsWith("/")){
			pathname = toAbsPath(pathname);//如果传进来的路径是相对路径，那么把他转成绝对路径
		}
//		System.out.println("deleting file: "+pathname);
		//判断文件是否打开
		boolean opened = false;	
		for(OpenedFile op:openedFileList){
			if(op.getPathname().equals(pathname)){
				opened = true;
			}
		}
		if(opened){
			System.out.println("文件已打开");
			return;
		}
		//获取文件的块号列表
		int[] blockNums = getBlockNums(findDirItem(pathname));
		//之后把文件占用的所有块全部解除占用（把FAT表中对应的位置全部修改为0，表示未被占用）
		for(int blockNum:blockNums){
//			System.out.println("blockNum: "+blockNum);
			writeFat(blockNum,0);
		}
		//再在父目录中销毁掉目录项
		DirItem superDirItem = findSuperDirItem(pathname);
		byte[] superDir = disk.read(superDirItem.getBlockNum());
		String name = findDirItem(pathname).getFullName();//获取要删除的文件的名字
		//找到要销毁的目录项是父目录中的第几个
		int numOfDirItemToDestroy = 0;
		for(int i = 0; i < 8 ;i++){
			if(Util.getDirItemAt(superDir,i).getFullName().equals(name)){
				numOfDirItemToDestroy = i;
//				System.out.println("numOfDirItemToDestroy: "+numOfDirItemToDestroy);
				break;
			}
		}
		//开始销毁
		for(int i = 0; i < 8; i++){
			superDir[numOfDirItemToDestroy*8+i] = 0;
		}
		//把父目录写回磁盘
		disk.write(superDirItem.getBlockNum(),superDir);
		//再把父目录重新读回来
//		curDir = disk.read(curDirItem.getBlockNum());
		updateCurDir();
	}

	public void typeFile(String pathname) {
//		显示文件内容首先要找到该文件的目录登记项，如果文件不存在，指令执行失败；如果
//		存在，查看文件是否打开，打开则不能显示文件内容；若没有打开，从目录中取出文件的起
//		始盘块号，一块一块显示文件内容。
		
		boolean flag = false;
		// 查找父目录中有没有该文件
		DirItem di = null;
		byte[] bytes = new byte[8];
		boolean flag1 = false;

		String filename = Util.getName(pathname);
		String type = Util.getType(pathname);
		DirItem superDirItem;// 父目录项
		byte[] superDir = new byte[64];// 父目录
		if (pathname.charAt(0) == '/') {
			// 若提供的是绝对路径, 则需要查找父目录
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
//				System.out.println("父目录不存在");
				return;
			}

		} else {
			// 若提供的是相对路径, 则父目录就是当前打开的目录
			flag1 = true;
			superDirItem = curDirItem;
		}
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);

		// 如果父目录存在,需要判断父目录下是否存在该子文件
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);// 从父目录中取出第i个目录项
			if (di.getName().equals(filename) && di.getType().equals(type) && !di.isDir()) {
				flag = true;
				bytes = di.getBytes(); // 获取该子文件的目录项
				break;

			}
		}

		if (!flag) {
			System.out.println("文件不存在！"); // 由于父目录都不存在,更不可能存在父目录相对应的文件了
			return;
		}

		if (flag1 && flag) {
			// 需要修改相对路径pathname,转为绝对路径,防止再次打开
			String temp = "/".concat(pathname);
			pathname = curPath.concat(temp);

		}
		// 判断文件是否打开
		boolean open = false;
		OpenedFile ofToWrite = null;
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				open = true;
				break;
			}
		}
		if (open) {
			System.out.println("文件已打开，操作失败");
			return;
		}
		// 获取文件的块号列表
		int[] blockNums = getBlockNums(findDirItem(pathname));

		byte[] fat = readFat();

		// 把文件一块一块地读出来
		for (int n = 0; n < blockNums.length; n++) {
			int BlockNum = blockNums[n];
			byte[] block = disk.read(BlockNum);
//			System.out.println("文件第" + n + "块内容为:");
			for (int i = 0; i < 64; i++) {
				char curChar = (char) (int) block[i];
				if (curChar != '#') {
					System.out.print(curChar);
				} else {
					break;
				}
			}
		}
		System.out.println("");
	
	}

	public void change(String pathname,String opts) {
//		首先查找该文件，如果不存在，结束；如果存在，检查文件是否打开，
//		打开不能改变属性；没有打开，根据要求改变目录项中属性值。
		pathname=toAbsPath(pathname);
		if(findDirItem(pathname)==null) {
		              
		    System.out.println("文件不存在，无法改变属性，操作失败");
		}
		
		// 判断文件是否打开
		boolean open = false;
		OpenedFile ofToWrite = null;
		for (OpenedFile of : openedFileList) {
			if (of.getPathname().equals(pathname)) {// 已经打开
				open = true;
				break;
			}
		}
		if (open) {
			System.out.println("文件已打开，不能修改属性，操作失败");
			return;
		}
		
		boolean ro=false;
		boolean sys=false;
		if(opts.contains("r")) {
			ro=true;
		}
		if(opts.contains("s")) {
		 sys=true; 
		}
		DirItem fileToModify=findDirItem(pathname);
	    fileToModify.setProperty(ro, sys, fileToModify.isDir());
	    
	    DirItem superDirItem = findSuperDirItem(pathname);
	    byte[] superDir = disk.read(superDirItem.getBlockNum());
	    for (int i = 0; i < 8; i++) {
	    	DirItem di = Util.getDirItemAt(superDir, i);
	    	if(di.getFullName().equals(fileToModify.getFullName())) {
	    		for (int j = 0; j < 8; j++) {
					superDir[i*8+j] = fileToModify.getBytes()[j];
				}
	    		break;
	    	}
		}
	    disk.write(superDirItem.getBlockNum(),superDir);
	    updateCurDir();
	
	}

	public boolean md(String pathname) {
		String folderName;
		DirItem superDirItem;
		byte[] superDir = new byte[64];

		int availableBlock;
		DirItem di;

		if (pathname.charAt(0) == '/') {
			// 用绝对路径创建新目录, 则需要通过路径寻找父目录
			superDirItem = findSuperDirItem(pathname);
			if (superDirItem == null) {
				return false;
			}
		} else {
			// 用相对路径创建新目录, 则当前打开的目录就是父目录
			superDirItem = curDirItem;
		}

		// 把父目录的内容读取到缓存中, 并复制出来
		disk.read(superDirItem.getBlockNum());
		Util.copyBlock(disk.getReader(), superDir);

		// 父目录存在,判断其中是否有重名文件夹
		folderName = Util.getFolderName(pathname);
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(superDir, i);
			if (di.getName().equals(folderName) && di.isDir()) {
				return false;// 发现重名文件夹, 终止方法
			}
		}

		// 不存在重名文件夹, 则寻找一个空位置放入目录项
		for (int i = 0; i < 8; i++) {
			di = Util.getDirItemAt(disk.getReader(), i);
			if (di.getName().equals("")) {
				// 新建一个目录项, 写入新文件夹的信息
				di = new DirItem();
				di.setName(folderName);
				di.setType("  ");// 文件夹的类型用两个空格填充

				di.setProperty(false, false, true);// 设置属性, 非 read-only, 非系统文件, 为目录
				availableBlock = Util.findAvailableBlock(disk);// 设置盘块号
				di.setBlockNum(availableBlock);
				wipeBlock(availableBlock);//把分配到的盘块擦除
				di.setSize(0);// 文件夹的大小统一设置为0

				Util.writeDirItem(di, superDirItem.getBlockNum(), i, disk);// 把目录项写入硬盘中
				Util.writeFat(availableBlock, 255, disk);// 更新FAT
				updateCurDir();//因为可能是在当前目录新建了一个文件夹, 所以刷新一下
				return true;

			}
		}
		return false;// 找不到空位置，建立失败
	}

	public boolean dir(String pathname) {

		DirItem di = new DirItem();
		String name = Util.getFolderName(pathname);

		if (pathname.charAt(0) == '/') {
			// 用绝对路径访问目录, 直接用findDirItem方法找到相应的目录项
			di = findDirItem(pathname);
			if (di == null) {
				System.out.println("找不到路径。");
				return false;
			}
			//更新当前路径
			curPath = pathname;

		} else {
			// 用相对路径访问目录, 则需要把当前打开的目录当作父目录, 在父目录中寻找要打开的文件夹
			byte[] superDir = curDir;
			boolean found = false;
			for (int i = 0; i < 8; i++) {
				di = Util.getDirItemAt(superDir, i);// 从父目录中取出第i个目录项
				if (di.getName().equals(name) && di.isDir()) {
					found = true;
					break;
				}
			}
			if(!found) {//在当前目录找不到给定的目录, 返回 false
				System.out.println("找不到路径。");
				return false;
			}

			//进入下一层目录时, 在当前路径的最后加一个"/", 然后加上下层目录的名字
			if(!curPath.equals("/")) {
				curPath+="/";
			}
			curPath+=pathname;
		}

		// 以下三行是打开一个目录的完整操作(把目录的内容和目录项全部转移到内存里)
		disk.read(di.getBlockNum());
		Util.copyBlock(disk.getReader(), curDir);
		curDirItem = di;

		// 输出当前目录下有什么内容
		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> dirNames = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			// 把当前目录的8个目录项全部取出来检查一遍
			// 为空的丢弃, 为文件的名字加到fileNames中, 为目录的名字加到dirNames中
			di = Util.getDirItemAt(curDir, i);// di到这里已经没用了, 直接拿来遍历
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
			//输出目录名
			System.out.print(s);
			//补足空格
			for (int i = 0; i < 20-s.length(); i++) {
				System.out.print(" ");
			}
			System.out.println("[目录]");
		}
		for (String s : fileNames) {
			//输出文件名
			System.out.print(s);
			//补足空格
			for (int i = 0; i < 20-s.length(); i++) {
				System.out.print(" ");
			}
			System.out.println("[文件]");
		}
		return true;
	}

	public void rd(String pathname) {
		if(pathname.equals("/")){
			System.out.println("无法删除根目录");
			return;
		}
		if(!pathname.startsWith("/")){
			pathname = toAbsPath(pathname);
		}
		DirItem diToRemove = findDirItem(pathname);
		if(diToRemove == null){
			System.out.println("目录不存在");
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
				//把父目录中的目录项销毁
				for(int j = 0; j < 8; j++){
					superDir[i*8+j] = 0;
				}
				//把父目录写回磁盘
				disk.write(findSuperDirItem(pathname).getBlockNum(),superDir);
				//再把父目录读回来
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
