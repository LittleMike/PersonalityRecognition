package com.mike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adaboost {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//Adaboost.process(); //训练
		Adaboost.process2(); //测试
	}
	
	private static void process() throws IOException{
		
		//Adaboost ab = new Adaboost(TRAIN,null); //训练第一个分类器
		
		LoadWeight(); //加载上次更新的权值
		Adaboost ab = new Adaboost(TRAIN,updateWeight); //迭代训练分类器
		
		//ab.ReadData(TRAIN);
		ab.ReadDataFor2Files(TRAIN);
		
		int[] sampleData = ab.GetSampleData();
		double err = 1;
		int[] CTD = null;
		double[] Threshold = new double[TagVector.TagNum];
		ArrayList<Integer> correctList = null;
		while(err>0.5){
			correctList = new ArrayList<Integer>();
			CTD = ab.GetClassiferTrainData();
			err = ab.ExportAndCalculate(CTD,ab.GetThresholdData(null),sampleData,Threshold,correctList);
		}
		
		//log:CTD,阈值,最终误差率,分类正确的元组,检测数据
		System.out.println("CTD: " + Arrays.toString(CTD));
		System.out.println("Threshold: " + Arrays.toString(Threshold));
		System.out.println("err: " + err);
		System.out.println("list: " + correctList);
		System.out.println("sampleData: " + Arrays.toString(sampleData));
		
		//更新元组的权重
		ab.UpdateWeight(correctList, err);
		System.out.println(Arrays.toString(ab.weight));
		//将更新的权重写入文件
		FileWriter weightWriter = new FileWriter("data/updateWeight.txt");
		weightWriter.write(Arrays.toString(ab.weight));
		weightWriter.flush();
		weightWriter.close();
		//将CTD,阈值,最终误差率写入文件
		FileWriter paraWriter = new FileWriter("data/para.txt");
		paraWriter.write(Arrays.toString(CTD)); paraWriter.write("\r\n");
		paraWriter.write(Arrays.toString(Threshold)); paraWriter.write("\r\n");
		paraWriter.write(Double.toString(err)); paraWriter.write("\r\n");
		paraWriter.flush();
		paraWriter.close();
	}

	private static void process2() throws IOException{
		Adaboost ab = new Adaboost(TEST,null);
		
		//ab.ReadData(TEST);
		ab.ReadDataFor2Files(TEST);
		
		double[][] thresholds = new double[NumberOfClassifer][TagVector.TagNum];
		double[] errs = new double[NumberOfClassifer];
		RandomWalk[] rwset = ab.GetClassiferSet(thresholds, errs);
		for(int i=0;i<NumberOfClassifer;i++){ //得到分类器投票权值
			ab.classiferWeight[i] = Math.log((1-errs[i])/errs[i]);
		}
		
		String[] res = new String[TagVector.TagNum];
		int[] R = new int[TagVector.TagNum];
		int[] W = new int[TagVector.TagNum];
		int[] yy = new int[TagVector.TagNum];
		int[] yn = new int[TagVector.TagNum];
		int[] ny = new int[TagVector.TagNum];
		int[] nn = new int[TagVector.TagNum];
		for(int i=0;i<ab.testData.size();i++){ //针对每一个测试元组
			double[][] weightedResult = new double[TagVector.TagNum][2]; //正反两个统计
			for(int j=0;j<NumberOfClassifer;j++){
				String[] s = ab.Predict(rwset[j], thresholds[j], ab.testData.get(i)); //预测
				for(int k=0;k<s.length;k++){ //累计权值
					if(s[k].equals("y"))
						weightedResult[k][0] += ab.classiferWeight[j];
					else
						weightedResult[k][1] += ab.classiferWeight[j];
				}
			}
			for(int j=0;j<res.length;j++){
				if(weightedResult[j][0]>=weightedResult[j][1])
					res[j] = "y";
				else
					res[j] = "n";
			}
			//log
			System.out.println(Arrays.toString(res));
			System.out.println(Arrays.toString(ab.testData.get(i).tagVal));
			
			//统计一下准确率...
			for(int j=0;j<TagVector.TagNum;j++){
				if(res[j].equals(ab.testData.get(i).tagVal[j])){
					R[j]++;
				}else
					W[j]++;
			}
			//统计指标值
			for(int j=0;j<TagVector.TagNum;j++){
				if(ab.testData.get(i).tagVal[j].equals("y") && res[j].equals("y"))
					yy[j]++;
				if(ab.testData.get(i).tagVal[j].equals("y") && res[j].equals("n"))
					yn[j]++;
				if(ab.testData.get(i).tagVal[j].equals("n") && res[j].equals("y"))
					ny[j]++;
				if(ab.testData.get(i).tagVal[j].equals("n") && res[j].equals("n"))
					nn[j]++;
			}
		}
		System.out.println(Arrays.toString(R));
		System.out.println(Arrays.toString(W));
		for(int i=0;i<TagVector.TagNum;i++)
			System.out.println((double)R[i]/(R[i]+W[i]));
		System.out.println("yy: " + Arrays.toString(yy));
		System.out.println("yn: " + Arrays.toString(yn));
		System.out.println("ny: " + Arrays.toString(ny));
		System.out.println("nn: " + Arrays.toString(nn));
		
		double[] Pre = new double[TagVector.TagNum];
		double[] Re = new double[TagVector.TagNum];
		double[] Fval = new double[TagVector.TagNum];
		double[] sum = new double[3]; //3个指标
		//计算指标值
		for(int i=0;i<TagVector.TagNum;i++){
			Pre[i] = (double)yy[i] / (yy[i] + ny[i]);
			Re[i] = (double)yy[i] / (yy[i] + yn[i]);
			Fval[i] = Pre[i] * Re[i] * 2 /(Pre[i]+Re[i]);
			sum[0] += Pre[i]; sum[1] += Re[i]; sum[2] += Fval[i];
		}
		System.out.println("Pre: " + Arrays.toString(Pre));
		System.out.println("Re: " + Arrays.toString(Re));
		System.out.println("Fval: " + Arrays.toString(Fval));
		System.out.println("avgPre: " + sum[0]/TagVector.TagNum + "; avgRe: " + sum[1]/TagVector.TagNum + "; avgFval: " + sum[2]/TagVector.TagNum);
	}
	
	ArrayList<TagVector> data1; //分类器的训练数据
	ArrayList<TagVector> thresholdData; //阈值训练数据
	ArrayList<TagVector> data2; //计算误差率用的数据
	static final int NumOfData1 = 150; //训练数据量
	static final int NumOfThresholdData = 90; //阈值训练数据量
	static final int NumOfData2 = 150; //计算误差率用的数据量
	static final int N = NumOfData1+NumOfThresholdData+NumOfData2; //训练数据总数
	static final int NumOfTestData = 200; //测试数据量
	static final String filepath = "data/result_2460_d_n.txt";
	static final String trainFilePath = "data/emotions/emotions-train_m_n.txt";
	static final String testFilePath = "data/emotions/emotions-test_m_n.txt";
	ArrayList<TagVector> testData; //测试数据
	double[] weight; //计算误差率用的数据权值
	double[] classiferWeight; //分类器投票权重
	static final boolean TRAIN = true;
	static final boolean TEST = false;
	static final int NumberOfClassifer = 1;
	static final int ThresholdNum = 9; //取1/ThresholdNum的data1中数据用于计算阈值
	static final int ERR = 1; //误分类为1
	static final int IncorrectNum = 2; //单个元组错误分类个数下限(一共TagVector.TagNum个)
	static final String updateWeightfilepath = "data/updateWeight.txt";
	static double[] updateWeight = new double[NumOfData2]; //存放更新后的权重,用于加载
	static final String parafilepath = "data/totalparas.txt"; //所有参数汇总,手工完成
	
	public Adaboost(boolean aim,double[] w){
		if(aim == TRAIN){ //训练时的相关初始化工作
			data1 = new ArrayList<TagVector>();
			thresholdData = new ArrayList<TagVector>();
			data2 = new ArrayList<TagVector>();
			weight = new double[NumOfData2];
			if(w==null){
				for(int i=0;i<weight.length;i++){
					weight[i] = 1.0/NumOfData2;
				} //初始化权值
			}else{
				for(int i=0;i<weight.length;i++){
					weight[i] = w[i];
				} //初始化权值
			}
			
		}else{
			data1 = new ArrayList<TagVector>();
			testData = new ArrayList<TagVector>();
			classiferWeight = new double[NumberOfClassifer];
			//其它工作
		}
		
		
	}
	
	//轮盘赌算法,n为规模,P[]为概率数组,P为null,表示非加权随机数,否则为加权随机数
	private int RWS(int n, double P[]) {
		if (P != null) {
			double sum = 0;
			double r = Math.random(); // 产生0-1随机数
			for (int i = 0; i < n; i++) {
				sum = sum + P[i];
				if (r <= sum)
					return i;
			}
			return -1; // P[]出错
		} else {
			return (int) (n * Math.random()); // 产生0-n随机数
		}
	}
	
	//读取文件中的数据,isTrain为true,表示读取训练数据,否则读取测试数据
	private void ReadData(boolean isTrain) throws IOException{
		if(isTrain){
			if(data1 == null || data2 == null || thresholdData == null){
				System.out.println("data err.");
				System.exit(0);
			}
		}else{
			if(data1 == null || testData == null){
				System.out.println("Data err.");
				System.exit(0);
			}
		}
		/* 加载训练集向量文件 */
		File file = new File(filepath);
		if (!file.exists()) {
			System.out.println("文件不存在");
			System.exit(0);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String str = "";
		if(isTrain){ //读取训练数据
			for(int i=0;i<NumOfData1;i++){
				str = br.readLine();
				data1.add(new TagVector(str));
			} 
			for(int i=0;i<NumOfThresholdData;i++){
				str = br.readLine();
				thresholdData.add(new TagVector(str));
			}
			for(int i=0;i<NumOfData2;i++){
				str = br.readLine();
				data2.add(new TagVector(str));
			}
		}else{
			for(int i=0;i<NumOfData1;i++){
				str = br.readLine();
				data1.add(new TagVector(str));
			} 
			for(int i=0;i<NumOfThresholdData+NumOfData2;i++){str = br.readLine();}
			for(int i=0;i<NumOfTestData;i++){
				str = br.readLine();
				testData.add(new TagVector(str));
			} // 读取测试数据
		}
		br.close();
	}
	
	//分别从训练集和测试集读取数据
	private void ReadDataFor2Files(boolean isTrain) throws IOException{
		if(isTrain){
			if(data1 == null || data2 == null || thresholdData == null){
				System.out.println("data err.");
				System.exit(0);
			}
		}else{
			if(data1 == null || testData == null){
				System.out.println("Data err.");
				System.exit(0);
			}
		}
		if(isTrain){ //读取训练数据
			/* 加载训练集向量文件 */
			File file = new File(trainFilePath);
			if (!file.exists()) {
				System.out.println("文件不存在");
				System.exit(0);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String str = "";
			for(int i=0;i<NumOfData1;i++){
				str = br.readLine();
				data1.add(new TagVector(str));
			} 
			
			//-----------------------
//			TagVector tv = new TagVector(str);
//			System.out.println("----------");
//			System.out.println(Arrays.toString(tv.vector));
//			System.out.println(Arrays.toString(tv.tagVal));
//			System.exit(0);
			
			for(int i=0;i<NumOfThresholdData;i++){
				str = br.readLine();
				thresholdData.add(new TagVector(str));
			}
			for(int i=0;i<NumOfData2;i++){
				str = br.readLine();
				data2.add(new TagVector(str));
			}
			br.close();
		}else{
			File file = new File(trainFilePath);
			if (!file.exists()) {
				System.out.println("文件不存在");
				System.exit(0);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String str = "";
			for(int i=0;i<NumOfData1;i++){
				str = br.readLine();
				data1.add(new TagVector(str));
			} 
			file = new File(testFilePath);
			if (!file.exists()) {
				System.out.println("文件不存在");
				System.exit(0);
			}
			br.close();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			str = "";
			for(int i=0;i<NumOfTestData;i++){
				str = br.readLine();
				testData.add(new TagVector(str));
			} // 读取测试数据
			br.close();
		}
	}
	
	//获取用于计算误差率的数据,带权有放回,一个分类器合格与否就用它来检验
	private int[] GetSampleData(){
		int[] sampleData = new int[NumOfData2]; //用于计算误差率的检验数据,实际存放的是data2中编号
		for(int i=0;i<NumOfData2;i++){
			sampleData[i] = RWS(NumOfData2,weight);
		}
		return sampleData;
	}
	
	//获取分类器加载数据,无权有放回,如果误差率不满足条件,会更换数据
	private int[] GetClassiferTrainData(){
		int[] classiferTrainData = new int[NumOfData1]; //分类器需加载的数据,实际存放的是data1中编号
		for(int i=0;i<NumOfData1;i++){
			classiferTrainData[i] = RWS(NumOfData1,null);
		}
		return classiferTrainData;
	}
	
	//获取用于计算阈值的数据,无权无放回,从阈值数据中取
	private int[] GetThresholdData(int[] CTD){
		int num = NumOfThresholdData/ThresholdNum;
		int[] thresholdData = new int[num]; //用于计算阈值的数据，实际存放的是阈值数据中编号
		ArrayList<Integer> ali = new ArrayList<Integer>();
		while(ali.size()<num){
			//int tmp = CTD[RWS(NumOfThresholdData,null)];
			int tmp = RWS(NumOfThresholdData,null);
			if(!ali.contains(tmp)) //保证不重复
				ali.add(tmp);
		}
		for(int i=0;i<num;i++){
			thresholdData[i] = ali.get(i);
		}
		return thresholdData;
	}
	
	//导出rw模型并计算误差率,参数分别为,训练数据索引,阈值数据索引,检测数据索引,导出的阈值,导出的正确分类元组索引
	private double ExportAndCalculate(int[] CTDind,int[] TDind,int[] SDind,double[] threshold,ArrayList<Integer> list){
		RandomWalk rw = new RandomWalk(CTDind, data1); //构造rw
		
		//检测rw中图是否联通
		if(!rw.amgtest.isConnect()) {
			System.out.println("--------------------------------------------------------------");
			return 1.0;
		}//如果图不连通,重新来过
		rw.amgtest = null; //检测通过,也无需再使用
		//检测元组是否包含所有类别
		int[] flag = new int[TagVector.TagNum];
		int i=0;
		for(;i<rw.tagVec.size();i++){
			int k=0;
			for(;k<TagVector.TagNum;k++){
				if(flag[k]!=100) break; //只要存在一个不包含,继续查找
			}
			if(k==TagVector.TagNum) break; //所有类别都包含,跳过检测
			for(int j=0;j<TagVector.TagNum;j++){
				if(rw.tagVec.get(i).tagVal[j].equals("y")){
					flag[j] = 100; //找到一个包含的类别,设置100
				}
			}
		}
		if(i==rw.tagVec.size()){
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			return 1.0;
		}
		
		//计算阈值
		double[] tmp = null;
		double[] pa = new double[TagVector.TagNum];
		double[] pr = new double[TagVector.TagNum];
		int[] a = new int[TagVector.TagNum];
		int[] r = new int[TagVector.TagNum];
		for(i=0;i<TDind.length;i++){
			
			System.out.println(TDind[i]);
			
			
			tmp = rw.StartWith(thresholdData.get(TDind[i])); //原来为data1,已改为data2,又改为thresholdData
			for (int j = 0; j < TagVector.TagNum; j++) {
				if (thresholdData.get(TDind[i]).tagVal[j].equals("y")) { //原来为data1,已改为data2,,又改为thresholdData
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
		for (i = 0; i < threshold.length; i++){
			if(pa[i]!=0 && pr[i]!=0) //有可能因为数据量少造成某个为0的情况
				threshold[i] = (pa[i] + pr[i]) / 2;
			else
				threshold[i] = pa[i]==0?pr[i]:pa[i];
		}
		//阈值计算完成
		
		//使用检测数据来计算误差率
		double errs = 0;
		int sum = 0;
		for(i=0;i<SDind.length;i++){
			sum = 0; //复位
			tmp = rw.StartWith(data2.get(SDind[i]));
			for(int j=0;j<TagVector.TagNum;j++){ //大于或等于阈值认为y
				if(data2.get(SDind[i]).tagVal[j].equals("y")){
					if(tmp[j]<threshold[j]){
						sum++;
					}
				}else{
					if(tmp[j]>=threshold[j]){
						sum++;
					}
				}
			}
			if(sum > IncorrectNum){ //高于错误个数下限,认为该元组分类错误
				errs += weight[SDind[i]] * ERR;
			}else{ //保存分类正确的元组
				if(!list.contains(SDind[i])){
					list.add(SDind[i]);
				}
			}
		}
		return errs;
	}
	
	//更新权重
	private void UpdateWeight(ArrayList<Integer> CL, double errs) {
		for (int i = 0; i < CL.size(); i++) { // 改变权重
			weight[CL.get(i)] = weight[CL.get(i)] * errs / (1 - errs);
		}
		double sum = 0;
		for (int i = 0; i < weight.length; i++) { // 计算新权重之和
			sum += weight[i];
		}
		for (int i = 0; i < weight.length; i++) { // 规范化
			weight[i] = weight[i] * 1 / sum;
		}
	}
	
	//从文件加载权重
	private static void LoadWeight() throws IOException{
		File file = new File(updateWeightfilepath);
		if (!file.exists()) {
			System.out.println("文件不存在");
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String str = br.readLine();
		String r = "[0-9.E-]+";
		Matcher m = Pattern.compile(r).matcher(str);
		int index = 0;
		while (m.find()){
			updateWeight[index++] = Double.parseDouble(m.group()); //匹配
		}
		br.close();
	}
	
	//加载文件得到复合模型
	private RandomWalk[] GetClassiferSet(double[][] thresholds,double[] errs) throws IOException{
		RandomWalk[] rwset = new RandomWalk[NumberOfClassifer];
		File file = new File(parafilepath);
		if (!file.exists()) {
			System.out.println("文件不存在");
			return null;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		for(int i=0;i<NumberOfClassifer;i++){
			//读第一行
			String str = br.readLine();
			String r = "[0-9]+";
			Matcher m = Pattern.compile(r).matcher(str);
			int[] CTD = new int[NumOfData1];
			int index = 0;
			while (m.find()){
				CTD[index++] = Integer.parseInt(m.group()); //匹配
			}
			
			//System.out.println(Arrays.toString(CTD));
			//System.exit(0);
			
			rwset[i] = new RandomWalk(CTD, data1); //构建rw
			//读第二行
			str = br.readLine();
			r = "[0-9.]+";
			m = Pattern.compile(r).matcher(str);
			index=0;
			while(m.find()){ //读阈值
				thresholds[i][index++] = Double.parseDouble(m.group());
			}
			//读第三行
			str = br.readLine();
			r = "[0-9.]+";
			m = Pattern.compile(r).matcher(str);
			m.find();
			errs[i] = Double.parseDouble(m.group()); //读误差率
		}
		br.close();
		return rwset;
	}
	
	//用复合模型对单个元组进行预测
/* 	static final double alpha0 = 0.0000005;//不动
	static final double alpha1 = 0.000003;//0.000002
	static final double alpha2 = -0.000007;//不动
	static final double alpha3 = 0;//不动
	static final double alpha4 = -0.000005;//-0.000004 */
	private String[] Predict(RandomWalk rw,double[] threshold,TagVector tv){
		double[] P = new double[TagVector.TagNum];
		P = rw.StartWith(tv);
		
		//System.out.println(Arrays.toString(P));
		//System.out.println(Arrays.toString(threshold));
		
		String[] result = new String[TagVector.TagNum];
		for(int i=0;i<TagVector.TagNum;i++){
			if(P[i]>=threshold[i]){
				result[i] = "y";
			}else{
				result[i] = "n";
			}
		}
		return result;
	}
}
