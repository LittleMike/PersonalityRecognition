package com.mike;

import java.io.IOException;
import java.util.Arrays;

public class Experimet {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// CalculateThreshold();
		setThreshold();
		System.out.println(Arrays.toString(P));
		run();
	}

	public static final int PTNum = 400; // 抽取的用于计算阈值的测试向量数目
	//public static final int offset = 0; // 数据位置偏移
	public static double[] P; // 最终阈值
	public static int[] A = new int[5]; // 分类分出的正确数目
	public static int[] B = new int[5]; // 分类分出的错误数目
	public static int[] C = new int[5]; // 属此类但分类器没有分出的数目
	public static int[] nn = new int[5]; // 非此类并且判断出来的数目

	public static void CalculateThreshold() throws IOException {
		double[] pa = new double[5];
		double[] a = new double[5];
		double[] pr = new double[5];
		double[] r = new double[5];
		double[] tmp = null;

		for (int i = 0; i < PTNum; i++) {
			System.out.println("--" + i + "--");
			//RandomWalk rw = new RandomWalk(6 * i + offset, null);
			RandomWalk rw = new RandomWalk(6 * i, null);
			tmp = rw.StartWith(rw.singleTV);

			for (int j = 0; j < TagVector.TagNum; j++) {
				if (rw.singleTV.tagVal[j].equals("y")) {
					pa[j] += tmp[j];
					a[j]++;
				} else {
					pr[j] += tmp[j];
					r[j]++;
				}
			}
		}

		for (int j = 0; j < TagVector.TagNum; j++) {
			if (a[j] != 0)
				pa[j] = pa[j] / a[j];
			if (r[j] != 0)
				pr[j] = pr[j] / r[j];
		}
		System.out.println(Arrays.toString(pa));
		System.out.println(Arrays.toString(pr));
	}

	public static void setThreshold() {
		P = new double[5];
		//数据为阈值实验跑出的结果
//		double[] pa0 = { 0.20167900496490523, 0.19665787602859458, 0.20081602591035283,
//				0.20101637172484174, 0.19983098243755695 };
//		double[] pr0 = { 0.2016787632270661, 0.19665770470408056, 0.20081590998386153,
//				0.20101640500433388, 0.19983091954706586 };
//		double[] pa1 = { 0.2034431914337527, 0.1952969933696899, 0.20164926037215294,
//				0.20016077967674806, 0.19944993248828105 };
//		double[] pr1 = { 0.20344319927446583, 0.19529684534418815, 0.20164930197148007,
//				0.20016076908341965, 0.1994497086664644 };
//		double[] pa2 = { 0.20274781009850398, 0.19535650705021948, 0.20010711186516666,
//				0.20212468417585117, 0.19966399194992787 };
//		double[] pr2 = { 0.2027478284422312, 0.19535630146292032, 0.2001069714911553,
//				0.2021249202553196, 0.19966389154554393 };
//		double[] pa = new double[5];
//		double[] pr = new double[5];
//		for (int i = 0; i < P.length; i++) {
//			pa[i] = (pa0[i] + pa1[i] + pa2[i]) / 3;
//			pr[i] = (pr0[i] + pr1[i] + pr2[i]) / 3;
//		}
		double[] pa = { 0.2024755698352352, 0.19515703770306767, 0.2015306575127596,
				0.20097853207634128, 0.19985824460679721 };
		double[] pr = { 0.2024755013236494, 0.19515695179661166, 0.2015307044442238,
				0.2009785887113427, 0.1998582116329289 };
		for (int i = 0; i < P.length; i++)
			P[i] = (pa[i] + pr[i]) / 2;
		// System.out.println(Arrays.toString(P));
	}

	public static void run() throws IOException {
		int index = 0;
		double[] tvp = null;
		String[] val = new String[5];
		for (int k = 0; k < RandomWalk.XFold; k++) {
			RandomWalk rw = new RandomWalk(k);
			// int j = 0;
			for (int j = 0; j < rw.testTagVec.size(); j++) { // 单组全部循环一遍
				// 序号
				System.out.print(index++);
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
				System.out.println();
				for (int m = 0; m < val.length; m++) {
					if (rw.testTagVec.get(j).tagVal[m].equals("y")) {
						if (val[m].equals("y"))
							A[m]++;
						else
							C[m]++;
					} else {
						if (val[m].equals("y"))
							B[m]++;
						else
							nn[m]++;
					}
				}
			}
		}
		System.out.println(Arrays.toString(A));
		System.out.println(Arrays.toString(B));
		System.out.println(Arrays.toString(C));
		System.out.println(Arrays.toString(nn));
	}
}
