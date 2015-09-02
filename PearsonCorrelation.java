package com.mike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class PearsonCorrelation {

	public ArrayList<TagVector> tagVec = null;

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		PearsonCorrelation pc = new PearsonCorrelation();
		pc.LoadFile("data/result_2460.txt");
		pc.Normalized();
		//double[] d = new double[67];
		ArrayList<Bingram> b = new ArrayList<Bingram>();
		double[] a = new double[67];
		for(int t=0;t<5;t++){
			for (int i = 0; i < 67; i++){
				//System.out.println(i + ": " + pc.Calculate(i, 0));
				//d[i] = pc.Calculate(i, 0);
				a[i] += Math.abs(pc.Calculate(i, t));
				
			}
		}
		for(int i=0;i<67;i++)
			b.add(new Bingram(a[i]/5, i));
		
		//Arrays.sort(d);
		Collections.sort(b, new Comparator<Bingram>() {

			@Override
			public int compare(Bingram o1, Bingram o2) {
				// TODO Auto-generated method stub
				return Double.compare(Math.abs(o2.value), Math.abs(o1.value));
			}
		});
		//System.out.println(Arrays.toString(d));
		for (int i = 0; i < 67; i++){
			System.out.println(b.get(i).num+": "+b.get(i).value);
		}
	}

	public void LoadFile(String filepath) throws IOException{
		tagVec = new ArrayList<TagVector>(); // 为训练集申请新的内存空间
		/* 加载训练集向量文件 */
		File file = new File(filepath);
		if (!file.exists()) {
			System.out.println("文件不存在");
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String str = "";
		while ((str = br.readLine()) != null) { // 全部读取
			tagVec.add(new TagVector(str));
		}
		br.close();
	}
	
	public double Calculate(int vno, int tno) {
		if (tagVec == null)
			return -1;
		double[] X = new double[tagVec.size()];
		double[] Y = new double[tagVec.size()];
		for (int i = 0; i < tagVec.size(); i++) {
			X[i] = tagVec.get(i).vector[vno];
			Y[i] = tagVec.get(i).tagVal[tno].equals("y") ? 1 : 0;
		}
		//System.out.println(Arrays.toString(X));
		//System.out.println(Arrays.toString(Y));

		double sgmX = 0, sgmY = 0, sgmXS = 0, sgmYS = 0, sgmXY = 0;
		int n = tagVec.size();
		for (int i = 0; i < n; i++) {
			sgmX += X[i];
			sgmY += Y[i];
			sgmXS += X[i] * X[i];
			sgmYS += Y[i] * Y[i];
			sgmXY += X[i] * Y[i];
		}
		double fz = sgmXY - (sgmX * sgmY) / n;
		double fm = Math.sqrt((sgmXS - (sgmX * sgmX) / n)
				* (sgmYS - (sgmY * sgmY) / n));
		return fz / fm;
	}
	
	//归一化
	private void Normalized() {
		// TODO Auto-generated method stub
		double[] store = new double[tagVec.size()];
		double min = -1, max = -1;
		for (int i = 0; i < TagVector.vecNum; i++) {
			for (int j = 0; j < tagVec.size(); j++)
				store[j] = tagVec.get(j).vector[i];
			Arrays.sort(store);
			// System.out.println(Arrays.toString(store));
			min = store[0];
			max = store[tagVec.size() - 1];
			for (int j = 0; j < tagVec.size(); j++)
				if ((max - min) != 0)
					tagVec.get(j).vector[i] = (tagVec.get(j).vector[i] - min) / (max - min);
				else
					tagVec.get(j).vector[i] = 0;
		}
	}
	
}

class Bingram{
	double value;
	int num;
	public Bingram(double v,int n){
		this.value = v;
		this.num = n;
	}
}
