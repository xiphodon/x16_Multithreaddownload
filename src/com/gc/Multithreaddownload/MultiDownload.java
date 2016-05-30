package com.gc.Multithreaddownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MultiDownload {

	/**
	 * @param args
	 */
	static int threadCount = 3;
	static int finishedThread = 0;
	//确定下载地址
	static String path = "http://192.168.1.101:8080/PowerWord.exe";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			URL url = new URL(path);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			
			if(conn.getResponseCode() == 200){
				//拿到请求的文件资源的长度
				int length = conn.getContentLength();
				
				File file = new File(MultiDownload.getFileName(path));
				//生成临时文件
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				//设置临时文件大小
				raf.setLength(length);
				raf.close();
				int size = length / threadCount;
				//计算出每个线程应该下载多少字节
				for(int i=0;i<threadCount;i++){
					//计算线程开始和结束的位置
					int startIndex = i * size ;
					int endIndex = (i + 1) * size - 1;
					
					//如果是最后一个线程，结束位置写死
					if(i == threadCount - 1){
						endIndex = length - 1; 
					}
					System.out.println("线程" + i + "的区间是：" + startIndex + "~" + endIndex);
					new DownloadThread(startIndex, endIndex, i).start();

				}
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
	
	 public static String getFileName(String path){
	    	int index = path.lastIndexOf("/");
	    	return path.substring(index + 1);
	    }
	
}


class DownloadThread extends Thread{
	int startIndex;
	int endIndex;
	int threadId;
	
	public DownloadThread(){
		
	}
	
	
	
	public DownloadThread(int startIndex, int endIndex, int threadId) {
		super();
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.threadId = threadId;
	}



	public void run() {
		try {
			File progressFile = new File(threadId + ".txt");
			//判断进度临时文件是否存在
			if(progressFile.exists()){
				FileInputStream fis = new FileInputStream(progressFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				//读取上一次文件的进度，计算出新的开始位置
				startIndex += Integer.parseInt(br.readLine());
				fis.close();
			}
			
			URL url = new URL(MultiDownload.path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			
			//设置本次Http请求的请求的数据区间
			conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
			
			//请求部分数据的请求成功的响应码是206
			if(conn.getResponseCode() == 206){
				InputStream is = conn.getInputStream();
				byte[] b = new byte[1024];
				int len = 0;
				int total = 0;
				//拿到临时文件的输出流
				File file = new File(MultiDownload.getFileName(MultiDownload.path));
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				//把文件的写入位置移动至startIndex
				raf.seek(startIndex);
				while((len = is.read(b)) != -1){
					//每次读取流里数据之后，同步把数据写入临时文件
					raf.write(b, 0, len);
					total += len;
				}
				
				//生成一个专门用来记录下载进度的临时文件
				RandomAccessFile progressRaf = new RandomAccessFile(progressFile, "rwd");
				//每次读取流里的数据后，同步把当前进程下载的总进度写进进度文件中
				progressRaf.write((total + "").getBytes());
				progressRaf.close();
				
				System.out.println("线程" + threadId + "下载完毕");
				raf.close();
				
				MultiDownload.finishedThread++;
				
				if(MultiDownload.finishedThread == MultiDownload.threadCount){
					for(int i=0;i<MultiDownload.threadCount;i++){
						File f = new File(i + ".txt");
						f.delete();
					}
					MultiDownload.finishedThread = 0;
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

