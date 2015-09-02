package com.mike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Administrator 下一步：全体离散化，把数值写入文件，可以先让RW跑一下看看，再写特征选择代码
 */

public class Discretization {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Discretization().process();
	}

	private static final int ELENUMS = 2460;
	private static final int TAGNUMS = 5;
	private static final String FILE = "data/64.txt";

	private double[] elements;
	private String[][] classes;

	public void process() {
		double[] c = CPLXgetGoodCenters(FILE, 0);
		int val;
		for (int i = 0; i < elements.length; i++) {
			System.out.print(elements[i]);
			System.out.print(',');
		}
		System.out.println();
		for (int i = 0; i < elements.length; i++) {
			val = discretizeSingleValue(c, elements[i]);
			System.out.print(val);
			System.out.print(',');
		}

	}
	
	public void process0() {
		readData(FILE, 50);
		System.out.println("elements:" + Arrays.toString(elements));
		// System.out.println(Arrays.toString(classes[999]));
		Normalize();
		System.out.println("normalize:" + Arrays.toString(elements));
		double[] c = null;
		LocateCenters(26, c);
		// Arrays.sort(elements);
		// System.out.println(Arrays.toString(elements));
		System.out.println("centers:" + Arrays.toString(c));
		double[][] m = MembershipFunction(c);
		// for(int i=0;i<m.length;i++)
		// System.out.println("m:" + Arrays.toString(m[i]));
		double fe = FuzzyEntropy(m, c);
		System.out.println("fe:" + fe);
	}

	// 离散化数值，从1起始
	private int discretizeSingleValue(double[] centers, double value) {
		if (value < centers[0])
			return 1;
		else if (value > centers[centers.length - 1])
			return centers.length;
		for (int i = 0; i < centers.length; i++) {
			if (value == centers[i])
				return i + 1;
			else if (centers[i] < value && value < centers[i + 1]) {
				double half = (centers[i] + centers[i + 1]) / 2;
				if (value > half)
					return i + 1 + 1;
				else
					return i + 1;
			}
		}
		return 0;// 0表示错误数值
	}

	// CPL:复合函数
	private double[] CPLXgetGoodCenters(String file, int col) {
		double[] c;
		boolean notOK = true;
		int intvs = 0;
		readData(file, col);
		Normalize();
		for (int i = 1; notOK == true; i++) {
			c = new double[i];
			notOK = LocateCenters(i, c);
			intvs = i - 1;
			// System.out.println("centers:" + Arrays.toString(c));
		}
		c = new double[intvs];
		LocateCenters(intvs, c);
		System.out.println("centers:" + Arrays.toString(c));
		return c;
	}

	// 加载数据
	private void readData(String filePath, int col) {
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("file not exist");
			return;
		}
		elements = new double[ELENUMS];
		classes = new String[ELENUMS][TAGNUMS];
		BufferedReader br = null;
		String str = "";
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			int index1 = 0;
			while ((str = br.readLine()) != null) { // 全部读取
				String r = null;
				Matcher m = null;
				r = "[0-9]+";
				m = Pattern.compile(r).matcher(str);
				for (int i = 0; i < col; i++) {
					m.find();
				}
				m.find();
				elements[index1] = Integer.parseInt(m.group());
				r = "[yn]";
				m = Pattern.compile(r).matcher(str);
				int index2 = 0;
				while (m.find()) {
					classes[index1][index2++] = m.group();
				}
				index1++;
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 归一化
	private void Normalize() {
		double max = elements[0], min = elements[0];
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] > max)
				max = elements[i];
			if (elements[i] < min)
				min = elements[i];
		}
		if ((max - min) == 0 && max != 0) {
			for (int i = 0; i < elements.length; i++) {
				elements[i] = 1;
			}
			return;
		}
		for (int i = 0; i < elements.length; i++) {
			elements[i] = (elements[i] - min) / (max - min);
		}
		return;
	}

	// 确定聚类中心
	private boolean LocateCenters(int intervals, double[] centers) {
		// centers初始化
		// centers = new double[intervals];
		if (intervals != centers.length) {
			System.out.println("error");
			System.exit(0);
		}
		if (intervals == 1)
			centers[0] = 0;
		else
			for (int q = 0; q < intervals; q++) {
				centers[q] = ((q + 1) - 1) / (intervals - 1.0);
			}
		int[] classNum = new int[elements.length];
		double temp;
		double[] newCenters = new double[centers.length];
		boolean isDiff = true;
		boolean isEmpty;
		boolean isGood = false;

		while (isDiff) {
			isEmpty = false;
			for (int i = 0; i < elements.length; i++) {
				temp = 1;// 边界值
				classNum[i] = 0;// 初始化
				for (int q = 0; q < intervals; q++) {
					double val = Math.abs(elements[i] - centers[q]);
					if (val < temp) {
						temp = val;
						classNum[i] = q;
					}
				}
			}
			// 重新计算中心
			int[] N = new int[intervals];
			double sum;
			for (int q = 0; q < intervals; q++) {
				N[q] = 0;// 初始化
				sum = 0;
				for (int i = 0; i < elements.length; i++) {
					if (classNum[i] == q) {
						N[q]++;
						sum += elements[i];
					}
				}
				// System.out.println(N[q]);
				if (N[q] != 0)
					newCenters[q] = sum / N[q];
				else // 如果没有属于该区间的元素，区间中心保持不动，是否合理？
				{
					newCenters[q] = centers[q];
					System.out.println("q:" + q + " empty!");
					isEmpty = true;
				}
			}
			isDiff = false;
			for (int q = 0; q < intervals; q++) {
				if (newCenters[q] != centers[q]) {
					isDiff = true;
					for (int qq = 0; qq < intervals; qq++) {
						centers[qq] = newCenters[qq];
					}
					break;
				}
			}
			if (!isDiff) {
				if (!isEmpty) {
					System.out.println("stop");
					isGood = true;
				}
			}
			System.out.println(Arrays.toString(N));
		}
		return isGood;
	}

	// 隶属度函数
	private double[][] MembershipFunction(double[] centers) {
		double[][] mf = new double[elements.length][centers.length];
		if (centers.length == 1) {
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] <= centers[0]) {
					if (centers[0] == 0)
						mf[i][0] = 1;
					else
						mf[i][0] = (centers[0] + elements[i]) / (2 * centers[0]);
				} else {
					if (centers[0] == 1) {
						System.out.println("<error 1>");
						System.exit(0);
					} else
						mf[i][0] = (2 - elements[i] - centers[0]) / (2 * (1 - centers[0]));
				}
			}
			return mf;
		}
		for (int i = 0; i < centers.length; i++) {
			for (int j = 0; j < elements.length; j++) {
				switch (i) {
				case 0:
					if (elements[j] <= centers[0]) {
						if (centers[0] == 0)
							mf[j][0] = 1;
						else
							mf[j][0] = (centers[0] + elements[j]) / (2 * centers[0]);
					} else {
						if (centers[1] - centers[0] == 0) {
							System.out.println("<error 2>");
							System.exit(0);
						} else
							mf[j][0] = 1 - (elements[j] - centers[0]) / (centers[1] - centers[0]);
						if (mf[j][0] < 0)
							mf[j][0] = 0;
					}
					break;
				default:
					if (i == centers.length - 1) {
						if (elements[j] <= centers[i]) {
							if (centers[i] - centers[i - 1] == 0) {
								System.out.println("<error 3>");
								System.exit(0);
							} else
								mf[j][i] = 1 - (centers[i] - elements[j]) / (centers[i] - centers[i - 1]);
							if (mf[j][i] < 0)
								mf[j][i] = 0;
						} else {
							if (centers[i] == 1) {
								System.out.println("<error 4>");
								System.exit(0);
							} else
								mf[j][i] = (2 - elements[j] - centers[i]) / (2 * (1 - centers[i]));
						}
					} else {
						if (elements[j] <= centers[i]) {
							if (centers[i] - centers[i - 1] == 0) {
								System.out.println("<error 5>");
								System.exit(0);
							} else
								mf[j][i] = 1 - (centers[i] - elements[j]) / (centers[i] - centers[i - 1]);
							if (mf[j][i] < 0)
								mf[j][i] = 0;
						} else {
							if (centers[i + 1] - centers[i] == 0) {
								System.out.println("<error 6>");
								System.exit(0);
							} else
								mf[j][i] = 1 - (elements[j] - centers[i]) / (centers[i + 1] - centers[i]);
							if (mf[j][i] < 0)
								mf[j][i] = 0;
						}
					}
				}
			}
		}
		return mf;
	}

	// 模糊熵
	private double FuzzyEntropy(double[][] mf, double[] c) {
		double f, b;
		double sum;
		double sumtotal;
		double D;
		double fe;
		double mfe;
		double ife;
		double totalfe;

		totalfe = 0;
		for (int x = 0; x < c.length; x++) {// 针对每个区间
			ife = 0;
			// 定边界
			if (c.length == 1) {// 只有一个区间的情况
				b = 0;
				f = 1;
			} else {
				if (x == 0) {
					b = 0;
					f = (c[0] + c[1]) / 2;
				} else if (x == (c.length - 1)) {
					b = (c[c.length - 1] + c[c.length - 2]) / 2;
					f = 1;
				} else {
					b = (c[x] + c[x - 1]) / 2;
					f = (c[x] + c[x + 1]) / 2;
				}
			}

			for (int i = 0; i < c.length; i++) {// 针对每一个模糊集
				mfe = 0;
				for (int j = 0; j < classes[0].length; j++) {// 针对每一类
					sum = 0;
					sumtotal = 0;
					for (int k = 0; k < elements.length; k++) {// 处理每一个元素
						if (f == 1) {
							if (b <= elements[k] && elements[k] <= f) {
								if (classes[k][j].equals("y")) {
									sum += mf[k][i];
								}
								sumtotal += mf[k][i];
							}
						} else {
							if (b <= elements[k] && elements[k] < f) {
								if (classes[k][j].equals("y")) {
									sum += mf[k][i];
								}
								sumtotal += mf[k][i];
							}
						}
					}
					if (sumtotal == 0)
						D = 0;
					else
						D = sum / sumtotal;
					if (D == 0)
						fe = 0;
					else
						fe = -D * Math.log(D);
					mfe += fe;
				}
				ife += mfe;
			}
			totalfe += ife;
		}
		return totalfe;
	}

}// END class Discretization
