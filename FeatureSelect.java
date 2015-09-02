package com.mike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureSelect {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new FeatureSelect().FeatureWeight();
		//new FeatureSelect(0);
	}

	ArrayList<Integer> listab; //用于计算联合熵
	ArrayList<Integer> lista; //特征值
	ArrayList<Integer> listb; //标签
	//ArrayList<Double> igs;
	//ArrayList<Double> igz;
	static final int MIN = -10000; //用于计算H(AB)时的特殊设定值
	static final String FILE = "data/result_2460_d.txt";
	
	public void process(){
		init(FILE, 50, 1);
		System.out.println(lista);
		System.out.println(listb);
		System.out.println(listab);
		double ha = HX(lista);
		double hb = HX(listb);
		double hab = HXY(listab);
		double ig = IGBA(lista,listb,listab);
		System.out.println(ha);
		System.out.println(hb);
		System.out.println(hab);
		System.out.println(ig);
	}
	
	public void FeatureWeight(){
		ArrayList<Double> igs = new ArrayList<Double>();
		for(int i=0;i<TagVector.vecNum;i++){
			double sum = 0;
			for(int j=0;j<TagVector.TagNum;j++){
				init(FILE, i, j);
				double ig = IGBA(lista, listb, listab); //算IG
				sum += ig; //算IGS
			}
			igs.add(sum);
		}
		ArrayList<Double> igz = IGS2IGZ(igs); //变换成IGZ
		for(int i=0;i<igz.size();i++){
			System.out.println(Math.abs(igz.get(i)) + ",");
		}
	}
	
	public FeatureSelect(){}
	
	public FeatureSelect(int param){
		ArrayList<Double> igs = new ArrayList<Double>();
		for(int i=0;i<TagVector.vecNum;i++){
			double sum = 0;
			for(int j=0;j<TagVector.TagNum;j++){
				init(FILE, i, j);
				double ig = IGBA(lista, listb, listab); //算IG
				sum += ig; //算IGS
			}
			igs.add(sum);
		}
		ArrayList<Double> igz = IGS2IGZ(igs); //变换成IGZ
		System.out.println(igz);
		double del = delta(igz);
		System.out.println("阈值：" + del);
		for(int i=0;i<igz.size();i++){
			if(Math.abs(igz.get(i))<del){
				System.out.println(i);
			}
		}
	}
	
	// 载入数据
	private void init(String filePath, int fea, int lab) {
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("file not exist");
			return;
		}
		lista = new ArrayList<Integer>();
		listb = new ArrayList<Integer>();
		listab = new ArrayList<Integer>();
		BufferedReader br = null;
		String str = "";
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((str = br.readLine()) != null) { // 全部读取
				String r = null;
				Matcher m = null;
				int val;
				r = "[0-9]+";
				m = Pattern.compile(r).matcher(str);
				for (int i = 0; i <= fea; i++) {
					m.find();
				}
				//m.find();
				val = Integer.parseInt(m.group());
				lista.add(val); // 载入特征值
				r = "[yn]";
				m = Pattern.compile(r).matcher(str);
				for (int i = 0; i <= lab; i++)
					m.find();
				// 载入标签，y和n分别用1和0代替
				if (m.group().equals("y")) {
					listb.add(1);
					listab.add(val);
				} else {
					listb.add(0);
					if (val == 0)
						listab.add(MIN);
					else
						listab.add(-val);
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 集合A的信息熵
	private double HX(ArrayList<Integer> A) {
		if (A.size() <= 1) {
			System.out.println("HX err");
			System.exit(0);
		}// 警戒：规模至少为2
		Collections.sort(A);// 从小到大排序
		double f = Collections.frequency(A, A.get(0));
		double p = f / A.size();
		double sum = p * Math.log(p); // 先处理第一个概率
		for (int i = 1; i < A.size(); i++) {
			if (A.get(i) != A.get(i - 1)) { // 每当第一次遇到不同元素，处理其概率
				f = Collections.frequency(A, A.get(i));
				p = f / A.size();
				sum += p * Math.log(p);
			}
		}
		return -sum;
	}
	
	// 集合A与B的联合熵
	// 这里的参数AB是特殊处理的，其中数值符号为正表示包含某标签，符号为负表示不包含，0表示数值为0且包含，MIN表示数值为0且不包含
	private double HXY(ArrayList<Integer> AB){
		if (AB.size() <= 1) {
			System.out.println("HXY err");
			System.exit(0);
		}// 警戒：规模至少为2
		Collections.sort(AB);// 从小到大排序
		double f = Collections.frequency(AB, AB.get(0));
		double p = f / AB.size();
		double sum = p * Math.log(p); // 先处理第一个概率
		for (int i = 1; i < AB.size(); i++) {
			if (AB.get(i) != AB.get(i - 1)) { // 每当第一次遇到不同元素，处理其概率
				f = Collections.frequency(AB, AB.get(i));
				p = f / AB.size();
				sum += p * Math.log(p);
			}
		}
		return -sum;
	}

	// 信息增益并归一化
	private double IGBA(ArrayList<Integer> A,ArrayList<Integer> B,ArrayList<Integer> AB){
		double ig = HX(A) + HX(B) - HXY(AB);
		return 2 * (ig / (HX(A) + HX(B)));
	}
	
	// 数值变换
	private ArrayList<Double> IGS2IGZ(ArrayList<Double> igs){
		if(igs == null){
			System.out.println("igs err");
			System.exit(0);
		}
		double sum = 0;
		double sum2 = 0;
		for(int i=0;i<igs.size();i++){
			sum += igs.get(i);
			sum2 += igs.get(i) * igs.get(i);
		}
		double avg = sum / igs.size();
		double sgm = Math.sqrt((sum2 - (sum * sum)/igs.size())/igs.size());
		ArrayList<Double> igz = new ArrayList<Double>();
		for(int i=0;i<igs.size();i++){
			igz.add((igs.get(i) - avg)/sgm);
		}
		return igz;
	}
	
	// 阈值
	private double delta(ArrayList<Double> igz){
		if(igz == null){
			System.out.println("igz err");
			System.exit(0);
		}
		double sum = 0;
		for(int i=0;i<igz.size();i++){
			sum += Math.abs(igz.get(i));
		}
		return sum / igz.size();
	}
}
