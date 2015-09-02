package com.mike;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class ExperimentMT2 extends Thread {

	public static final int FOLDNUM = 9;

	public static void setThreshold(int foldnum) throws InterruptedException, IOException { //算阈值
		// TODO Auto-generated method stub
		Threshold th0 = new Threshold(0,foldnum);
		Threshold th1 = new Threshold(1,foldnum);
		Threshold th2 = new Threshold(2,foldnum);
		Threshold th3 = new Threshold(3,foldnum);
		th0.start();
		th1.start();
		th2.start();
		th3.start();
		th0.join(); //等待线程结束
		th1.join();
		th2.join();
		th3.join();
		
		for(int i=0;i<4;i++){
			for(int j = 0; j < TagVector.TagNum; j++){
				ppa[j] += pa[i][j];
				aa[j] += a[i][j];
				ppr[j] += pr[i][j];
				rr[j] += r[i][j];
			}
		}
		
		FileWriter writer = new FileWriter("data/threshold.txt");
		writer.write(Arrays.toString(ppa));
		writer.write(Arrays.toString(ppr));
		writer.write(Arrays.toString(aa));
		writer.write(Arrays.toString(rr));
		
		for (int j = 0; j < TagVector.TagNum; j++) {
			if (aa[j] != 0)
				ppa[j] = ppa[j] / aa[j];
			if (rr[j] != 0)
				ppr[j] = ppr[j] / rr[j];
		}
		
		writer.write(Arrays.toString(ppa));
		writer.write(Arrays.toString(ppr));
		
		System.out.println("ppa: "+Arrays.toString(ppa));
		System.out.println("ppr: "+Arrays.toString(ppr));

		for (int i = 0; i < P.length; i++)
			P[i] = (ppa[i] + ppr[i]) / 2;
		
		writer.write(Arrays.toString(P));
		writer.flush();
		writer.close();
		
		System.out.println("P: "+Arrays.toString(P));
		System.out.println("setThreshold end!");
	}
	
	//最终测试
	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		int foldnum = FOLDNUM;
		
		setThreshold(foldnum);
		
		ExperimentMT2 emt0 = new ExperimentMT2(0,foldnum);
		ExperimentMT2 emt1 = new ExperimentMT2(1,foldnum);
		ExperimentMT2 emt2 = new ExperimentMT2(2,foldnum);
		ExperimentMT2 emt3 = new ExperimentMT2(3,foldnum);
		emt0.start();
		emt1.start();
		emt2.start();
		emt3.start();
		emt0.join(); //等待线程结束
		emt1.join();
		emt2.join();
		emt3.join();
				
		for(int i=0;i<4;i++){
			for(int j = 0; j < TagVector.TagNum; j++){
				AA[j] += A[i][j];
				BB[j] += B[i][j];
				CC[j] += C[i][j];
				DD[j] += nn[i][j];
			}
		}
		
		System.out.println("AA: "+Arrays.toString(AA));
		System.out.println("BB: "+Arrays.toString(BB));
		System.out.println("CC: "+Arrays.toString(CC));
		System.out.println("DD: "+Arrays.toString(DD));
		
		FileWriter writer = new FileWriter("data/final.txt");
		writer.write(Arrays.toString(AA));
		writer.write(Arrays.toString(BB));
		writer.write(Arrays.toString(CC));
		writer.write(Arrays.toString(DD));
		
		double[] P = new double[5];
		double[] R = new double[5];
		double[] F = new double[5];
		for(int i=0;i<P.length;i++){
			P[i] = (double)AA[i] / (AA[i]+BB[i]);
			R[i] = (double)AA[i] / (AA[i]+CC[i]);
			F[i] = P[i]*R[i]*2 / (P[i]+R[i]);
		}
		System.out.println("P: "+ Arrays.toString(P)+" " + avg(P));
		System.out.println("R: "+ Arrays.toString(R)+" " + avg(R));
		System.out.println("F: "+ Arrays.toString(F)+" " + avg(F));
		
		writer.write(Arrays.toString(P)+" " + avg(P));
		writer.write(Arrays.toString(R)+" " + avg(R));
		writer.write(Arrays.toString(F)+" " + avg(F));
		writer.flush();
		writer.close();
		
		System.out.println("main end!");
	}

	public static double avg(double[] d){
		double sum=0;
		for(int i=0;i<d.length;i++){
			sum += d[i];
		}
		return sum/d.length;
	}
	
	public int no;
	public int foldNum;
	public static double[][] pa = new double[4][5];
	public static double[][] a = new double[4][5];
	public static double[][] pr = new double[4][5];
	public static double[][] r = new double[4][5];
	public static double[] ppa = new double[5];
	public static double[] aa = new double[5];
	public static double[] ppr = new double[5];
	public static double[] rr = new double[5];
	public static final int step = 100;
	public static final int toEnd = 100;
	
	public static double[] P = new double[5]; // 最终阈值
	public static int[][] A = new int[5][5]; // 分类分出的正确数目
	public static int[][] B = new int[5][5]; // 分类分出的错误数目
	public static int[][] C = new int[5][5]; // 属此类但分类器没有分出的数目
	public static int[][] nn = new int[5][5]; // 非此类并且判断出来的数目
	public static int[] AA = new int[5];
	public static int[] BB = new int[5];
	public static int[] CC = new int[5];
	public static int[] DD = new int[5];
	
	public ExperimentMT2(int No,int foldnum){
		this.no = No;
		this.foldNum = foldnum;
	}
	
	//最终测试
	public void run() {
		try {
			switch (no) {
			case 0:
				begin(0, 50, 0, this.foldNum);
				break;
			case 1:
				begin(50, 50, 1, this.foldNum);
				break;
			case 2:
				begin(100, 50, 2, this.foldNum);
				break;
			case 3:
				begin(150, 50, 3, this.foldNum);
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Thread-" + no + " end!");
	}

	public void begin(int start, int num, int no, int foldnum) throws IOException {
		double[] tvp = null;
		String[] val = new String[5];
		RandomWalk rw = new RandomWalk(foldnum);
		int index = 0; // 序号索引
		for (int k = start; k < start + num; k++) {
			System.out.println("\n$" + no + '-' + k + '-' + index++); // 序号
			tvp = rw.StartWith(rw.testTagVec.get(k));
			for (int i = 0; i < tvp.length; i++) {
				System.out.print(rw.testTagVec.get(k).tagVal[i]); // 标注值
				if (tvp[i] > P[i]) {
					System.out.print("y ");
					val[i] = "y";
				} else {
					System.out.print("n ");
					val[i] = "n";
				}
			}
			System.out.println();
			for (int m = 0; m < val.length; m++) {
				if (rw.testTagVec.get(k).tagVal[m].equals("y")) {
					if (val[m].equals("y"))
						A[no][m]++;
					else
						C[no][m]++;
				} else {
					if (val[m].equals("y"))
						B[no][m]++;
					else
						nn[no][m]++;
				}
			}
		}
		System.out.println("A: "+Arrays.toString(A[no]));
		System.out.println("B: "+Arrays.toString(B[no]));
		System.out.println("C: "+Arrays.toString(C[no]));
		System.out.println("nn: "+Arrays.toString(nn[no]));
	}
}
