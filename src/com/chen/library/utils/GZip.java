package com.chen.library.utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip工具类，包含了压缩与解压缩字节以及文件的操作
 * 
 * @author chenxx
 *
 */
public class GZip {

	private final static int DEF_BUF_SIZE = 1024;
	public static final String EXT = ".gz";

	/**
	 * GZip压缩的核心方法
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public static void compress(InputStream is, OutputStream os) throws IOException {
		GZIPOutputStream gos = null;
		gos = new GZIPOutputStream(os);
		int len;
		byte[] buf = new byte[DEF_BUF_SIZE];
		while ((len = is.read(buf)) != -1) {
			gos.write(buf, 0, len);
		}
		gos.finish();
		gos.flush();
	}

	/**
	 * 压缩byte
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(byte[] bytes) throws IOException {
		InputStream is = null;
		is = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		compress(is, bos);
		byte[] res = bos.toByteArray();
		bos.close();
		is.close();
		return res;
	}

	/**
	 * 压缩文件
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static File compress(String path) throws IOException {
		return compress(new File(path));
	}

	/**
	 * 压缩文件
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static File compress(String path, boolean deleteSrcFile) throws IOException {
		return compress(new File(path), deleteSrcFile);
	}

	/**
	 * 压缩文件
	 * 
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static File compress(File src) throws IOException {
		return compress(src, false);
	}

	/**
	 * 压缩文件
	 * 
	 * @param src
	 * @param deleteSrcFile
	 * @return
	 * @throws IOException
	 */
	public static File compress(File src, boolean deleteSrcFile) throws IOException {
		File resFile = null;
		FileInputStream fis = new FileInputStream(src);
		FileOutputStream fos = new FileOutputStream(resFile = new File(src.getPath() + EXT));
		compress(fis, fos);
		fos.close();
		fis.close();
		if (deleteSrcFile)
			src.delete();
		return resFile;
	}

	/**
	 * 解压缩的核心方法
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public static void decompress(InputStream is, OutputStream os) throws IOException {
		GZIPInputStream gis = new GZIPInputStream(is);
		byte[] buf = new byte[DEF_BUF_SIZE];
		int len;
		while ((len = gis.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		os.flush();
	}

	/**
	 * 解压缩byte
	 * 
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static byte[] decompress(byte[] src) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(src);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		decompress(bis, bos);
		byte[] res = bos.toByteArray();
		bos.close();
		bis.close();
		return res;
	}

	/**
	 * 解压缩文件
	 * 
	 * @param src
	 * @param deleteSrcFile
	 * @return
	 * @throws IOException
	 */
	public static File decompress(File src, boolean deleteSrcFile) throws IOException {
		File resFile = null;
		FileInputStream fis = new FileInputStream(src);
		FileOutputStream fos = new FileOutputStream(resFile = new File(src.getPath().replace(EXT, "")));
		decompress(fis, fos);
		fos.close();
		fis.close();
		if (deleteSrcFile)
			src.delete();
		return resFile;
	}

	/**
	 * 解压缩文件
	 * 
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static File decompress(File src) throws IOException {
		return decompress(src, false);
	}

	/**
	 * 解压缩文件
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static File decompress(String path) throws IOException {
		return decompress(new File(path));
	}

	/**
	 * 解压缩文件
	 * 
	 * @param path
	 * @param deleteSrcFile
	 * @return
	 * @throws IOException
	 */
	public static File decompress(String path, boolean deleteSrcFile) throws IOException {
		return decompress(new File(path), deleteSrcFile);
	}

	public static void main(String[] args) {
		// 压缩以及解压缩字节数组
		// try {
		// System.out.println(new String(decompress(compress("http://weibo.com/mygroups?gid=3413803515921645&wvr=6&leftnav=1#_rnd1437185786060".getBytes()))));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// 压缩以及解压缩文件
		// try {
		// compress("D:\\cj.php.jpg",true);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// try {
		// decompress("D:\\cj.php.jpg.gz",true);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
