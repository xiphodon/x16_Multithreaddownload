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
	//ȷ�����ص�ַ
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
				//�õ�������ļ���Դ�ĳ���
				int length = conn.getContentLength();
				
				File file = new File(MultiDownload.getFileName(path));
				//������ʱ�ļ�
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				//������ʱ�ļ���С
				raf.setLength(length);
				raf.close();
				int size = length / threadCount;
				//�����ÿ���߳�Ӧ�����ض����ֽ�
				for(int i=0;i<threadCount;i++){
					//�����߳̿�ʼ�ͽ�����λ��
					int startIndex = i * size ;
					int endIndex = (i + 1) * size - 1;
					
					//��������һ���̣߳�����λ��д��
					if(i == threadCount - 1){
						endIndex = length - 1; 
					}
					System.out.println("�߳�" + i + "�������ǣ�" + startIndex + "~" + endIndex);
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
			//�жϽ�����ʱ�ļ��Ƿ����
			if(progressFile.exists()){
				FileInputStream fis = new FileInputStream(progressFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				//��ȡ��һ���ļ��Ľ��ȣ�������µĿ�ʼλ��
				startIndex += Integer.parseInt(br.readLine());
				fis.close();
			}
			
			URL url = new URL(MultiDownload.path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			
			//���ñ���Http������������������
			conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
			
			//���󲿷����ݵ�����ɹ�����Ӧ����206
			if(conn.getResponseCode() == 206){
				InputStream is = conn.getInputStream();
				byte[] b = new byte[1024];
				int len = 0;
				int total = 0;
				//�õ���ʱ�ļ��������
				File file = new File(MultiDownload.getFileName(MultiDownload.path));
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				//���ļ���д��λ���ƶ���startIndex
				raf.seek(startIndex);
				while((len = is.read(b)) != -1){
					//ÿ�ζ�ȡ��������֮��ͬ��������д����ʱ�ļ�
					raf.write(b, 0, len);
					total += len;
				}
				
				//����һ��ר��������¼���ؽ��ȵ���ʱ�ļ�
				RandomAccessFile progressRaf = new RandomAccessFile(progressFile, "rwd");
				//ÿ�ζ�ȡ��������ݺ�ͬ���ѵ�ǰ�������ص��ܽ���д�������ļ���
				progressRaf.write((total + "").getBytes());
				progressRaf.close();
				
				System.out.println("�߳�" + threadId + "�������");
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

