package com.mike;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class ExperimentMT extends Thread {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void amain(String[] args) throws InterruptedException, IOException { //算阈值
		// TODO Auto-generated method stub
		ExperimentMT emt0 = new ExperimentMT(0);
		ExperimentMT emt1 = new ExperimentMT(1);
		ExperimentMT emt2 = new ExperimentMT(2);
		ExperimentMT emt3 = new ExperimentMT(3);
		emt0.start();
		emt1.start();
		emt2.start();
		emt3.start();
		emt0.join(); //等待线程结束
		emt1.join();
		emt2.join();
		emt3.join();
		
		//while(emt0.isAlive()||emt1.isAlive()||emt2.isAlive()||emt3.isAlive());
		
		for(int i=0;i<4;i++){
			for(int j = 0; j < TagVector.TagNum; j++){
				ppa[j] += pa[i][j];
				aa[j] += a[i][j];
				ppr[j] += pr[i][j];
				rr[j] += r[i][j];
			}
		}
		
		FileWriter writer = new FileWriter("data/threshold.txt");
		writer.write(Arrays.toString(ppa)+'\n');
		writer.write(Arrays.toString(ppr)+'\n');
		writer.write(Arrays.toString(aa)+'\n');
		writer.write(Arrays.toString(rr)+'\n');
		
		for (int j = 0; j < TagVector.TagNum; j++) {
			if (aa[j] != 0)
				ppa[j] = ppa[j] / aa[j];
			if (rr[j] != 0)
				ppr[j] = ppr[j] / rr[j];
		}
		
		writer.write(Arrays.toString(ppa)+'\n');
		writer.write(Arrays.toString(ppr)+'\n');
		writer.flush();
		writer.close();
		
		System.out.println(Arrays.toString(ppa));
		System.out.println(Arrays.toString(ppr));
		
		System.out.println("main end!");
	}
	
	//最终测试
	public static void main(String[] args) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		setThreshold();
		ExperimentMT emt0 = new ExperimentMT(0);
		ExperimentMT emt1 = new ExperimentMT(1);
		ExperimentMT emt2 = new ExperimentMT(2);
		ExperimentMT emt3 = new ExperimentMT(3);
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
		
		System.out.println(Arrays.toString(AA));
		System.out.println(Arrays.toString(BB));
		System.out.println(Arrays.toString(CC));
		System.out.println(Arrays.toString(DD));
		
		FileWriter writer = new FileWriter("data/final.txt");
		writer.write(Arrays.toString(AA)+'\n');
		writer.write(Arrays.toString(BB)+'\n');
		writer.write(Arrays.toString(CC)+'\n');
		writer.write(Arrays.toString(DD)+'\n');
		
		double[] P = new double[5];
		double[] R = new double[5];
		double[] F = new double[5];
		for(int i=0;i<P.length;i++){
			P[i] = (double)AA[i] / (AA[i]+BB[i]);
			R[i] = (double)AA[i] / (AA[i]+CC[i]);
			F[i] = P[i]*R[i]*2 / (P[i]+R[i]);
		}
		System.out.println(Arrays.toString(P)+" " + avg(P));
		System.out.println(Arrays.toString(R)+" " + avg(R));
		System.out.println(Arrays.toString(F)+" " + avg(F));
		
		writer.write(Arrays.toString(P)+" " + avg(P) + '\n');
		writer.write(Arrays.toString(R)+" " + avg(R) + '\n');
		writer.write(Arrays.toString(F)+" " + avg(F) + '\n');
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
	
	public static double[] P; // 最终阈值
	public static int[][] A = new int[5][5]; // 分类分出的正确数目
	public static int[][] B = new int[5][5]; // 分类分出的错误数目
	public static int[][] C = new int[5][5]; // 属此类但分类器没有分出的数目
	public static int[][] nn = new int[5][5]; // 非此类并且判断出来的数目
	public static int[] AA = new int[5];
	public static int[] BB = new int[5];
	public static int[] CC = new int[5];
	public static int[] DD = new int[5];
	
	public ExperimentMT(int No){
		this.no = No;
	}
	
	//算阈值
	public void arun() {
		// System.out.println("当前线程是：" + Thread.currentThread());
		try {
			switch (no) {
			case 0:
				CalculateThreshold(0, no);
				break;
			case 1:
				CalculateThreshold(step * 1, no);
				break;
			case 2:
				CalculateThreshold(step * 2, no);
				break;
			case 3:
				CalculateThreshold(step * 3, no);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Thread-"+no+" end!");
	}
	
	//最终测试
	public void run() {
		try {
			switch (no) {
			case 0:
				begin(0, 3, 0);
				break;
			case 1:
				begin(3, 3, 1);
				break;
			case 2:
				begin(6, 2, 2);
				break;
			case 3:
				begin(8, 2, 3);
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Thread-"+no+" end!");
	}
	
	public void CalculateThreshold(int start,int no) throws IOException {
		
		double[] tmp = null;

		int end = start + toEnd;
		for (int i = start; i < end; i++) {
			System.out.println("--" + i + "--");
			//RandomWalk rw = new RandomWalk(6 * i + offset, null);
			RandomWalk rw = new RandomWalk(6 * i, null);
			//RandomWalk rw = new RandomWalk(i, null);
			tmp = rw.StartWith(rw.singleTV);

			for (int j = 0; j < TagVector.TagNum; j++) {
				if (rw.singleTV.tagVal[j].equals("y")) {
					pa[no][j] += tmp[j];
					a[no][j]++;
				} else {
					pr[no][j] += tmp[j];
					r[no][j]++;
				}
			}
		}
	}
	
	//算出阈值后要填在这里
	public static void setThreshold() {
		P = new double[5];
		//包含
//		double[] pa = { 0.20234478702798536, 0.19752106318689056,
//				0.20170361438968773, 0.20304589893815655, 0.19633635808557368 };
//		double[] pr = { 0.2019953498969, 0.19706058636130416,
//				0.20134759935210014, 0.2026513981782526, 0.19595487607622272 };
		//不包含
		double[] pa = { 0.20216654189858912, 0.19729793556879263,
				0.20153398842201614, 0.20284880189236215, 0.19614053081493843 };
		double[] pr = { 0.20216723589192948, 0.19730837032658963,
				0.2015374286441737, 0.20285197108912467, 0.19614753597403908 };

		for (int i = 0; i < P.length; i++)
			P[i] = (pa[i] + pr[i]) / 2;
		 System.out.println(Arrays.toString(P));
	}
	
	public void begin(int start,int num,int no) throws IOException {
		double[] tvp = null;
		String[] val = new String[5];
		for (int k = start; k < start+num; k++) {
			RandomWalk rw = new RandomWalk(k);
			// int j = 0;
			int index = 0;
			for (int j = 0; j < rw.testTagVec.size(); j++) { // 单组全部循环一遍
				// 序号
				System.out.println("\n$"+no+'-'+k+'-'+index++);
				tvp = rw.StartWith(rw.testTagVec.get(j));
				for (int i = 0; i < tvp.length; i++) { // 各标签跑一遍
					// 标注值
					System.out.print(rw.testTagVec.get(j).tagVal[i]);
					// 预测值
					if (tvp[i] > P[i]) {
						System.out.print("y ");
						val[i] = "y";
					} else {
						System.out.print("n ");
						val[i] = "n";
					}
				}
				//System.out.println();
				for (int m = 0; m < val.length; m++) {
					if (rw.testTagVec.get(j).tagVal[m].equals("y")) {
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
		}
		System.out.println(Arrays.toString(A[no]));
		System.out.println(Arrays.toString(B[no]));
		System.out.println(Arrays.toString(C[no]));
		System.out.println(Arrays.toString(nn[no]));
	}
}
