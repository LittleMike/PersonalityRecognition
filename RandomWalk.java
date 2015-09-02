package com.mike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class RandomWalk {

	public static void main(String[] args) throws IOException { // log
		
		RandomWalk rw = new RandomWalk();

	}

	/* 常量 */
	public static final int XFold = 10; // 10折
	public static final double ALPHA = 0.15; // 跳转发生概率
	public static final double PRCIOUS = 1E-8; // 迭代误差值
	private static final String filepath = "data/emotions/emotions-test_m.txt";
	/* 全局变量 */
	public ArrayList<TagVector> tagVec = null; // 训练集的带标签特征向量列表
	public ArrayList<TagVector> testTagVec = null; // 测试集的带标签特征向量列表
	public AdjMatrixGraph amg = null; // 训练集结点与测试向量结点构成的图
	public AdjMatrixGraph amgtest = null; //检测训练集结点图的连通性使用
	public double[][] MatrixM = null; // 邻接矩阵M,|D|+1维
	public double[] VecD = null; // 跳转分布向量d,|D|+1维
	public double[] PT = null; // 阈值向量
	public TagVector singleTV = null; // 从训练集中抽出的测试向量
	static final double NormalizedMIN = 0.1;
	static final double NormalizedMAX = 1.0;
	
/* 	public static final double[] WMM = { 0.0256439, 0.024172411, 0.023472627, 0.031349101,
			0.027831088, 0.026729964, 0.029758456, 0.030173804, 0.024390252, 0.031015555,
			0.024319726, 0.023628038, 0.026437412, 0.020592461, 0.031258179, 0.026989469,
			0.025666903, 0.02, // 人工指定
			0.021254167, 0.029400552, 0.023264335, 0.02085859, 0.02, 0.02, 0.02, 0.02, 0.02 // 最后五个均为人工指定
	}; */

	public static final double[] FSWeight = {
		0.1552211077003809,
		0.2597730188446128,
		0.42876098007350444,
		0.24191485776208785,
		0.795180262929627,
		0.4816399761117563,
		0.568549497853115,
		1.0750681422926853,
		1.5126905121580985,
		0.19491774830940062,
		0.04843822405412431,
		1.0077356538808309,
		0.13802261913437247,
		0.5866992766940418,
		1.1830827509726125,
		0.7177070062248517,
		0.486522925891346,
		0.4339591653742837,
		1.6125503610243885,
		0.02403528112272505,
		0.10208005185580792,
		1.6518289806500386,
		1.6629409082085178,
		1.6934623877058559,
		1.1711773518469237,
		0.2728923274165018,
		0.43901200359458037,
		1.1365288658696522,
		0.3545818581892749,
		0.8245835685874042,
		1.088929538320033,
		1.7181814811373812,
		0.02788890184731038,
		1.7262624113329108,
		1.6971161102454286,
		0.11351987847884656,
		0.09803318451740237,
		0.7192018868112237,
		1.563804852661612,
		1.6023040013412508,
		1.0623828778260507,
		1.0623807208325093,
		1.0623828366498327,
		1.0623824650773952,
		1.0623809781576388,
		1.0623829361188655,
		1.0623844991216638,
		0.799595239850481,
		0.49488159481886923,
		1.0623849361365785,
		1.0623816103922443,
		1.0623829619722926,
		1.062382272282507,
		0.5832640675497046,
		1.0623827603406082,
		1.0623815661285467,
		1.0549154622721433,
		1.0623829424906106,
		0.633122919140661,
		1.0623826164453058,
		1.0623824989595425,
		1.2581820754904092,
		1.0623828019105188,
		1.0590278331284169,
		1.0623836503465443,
		1.061898966223006,
		1.0623813087120983,
	};

	public RandomWalk() throws IOException{
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
		/* 加载训练集向量文件 END */

		Normalized(); // 数据归一化
		
		FileWriter writer = new FileWriter("data/Normalized.txt");
		for(int i=0;i<tagVec.size();i++){
			writer.write(Arrays.toString(tagVec.get(i).vector));
			writer.write(Arrays.toString(tagVec.get(i).tagVal));
			writer.write("\r\n");
		}
		writer.flush();
		writer.close();
	}
	
	//搭配adaboost算法设计的rw构造函数
	public RandomWalk(int[] ind,ArrayList<TagVector> data){
		tagVec = new ArrayList<TagVector>();
		for(int i=0;i<ind.length;i++){ //加载数据
			tagVec.add(data.get(ind[i]));
		}
		
//		System.out.println(Arrays.toString(data.get(20).vector));
//		System.out.println(Arrays.toString(data.get(20).tagVal));
//		System.out.println(Arrays.toString(tagVec.get(20).vector));
//		System.out.println(Arrays.toString(tagVec.get(20).tagVal));
//		System.exit(0);
		
		this.RemoveIsolatedVertex(); // 去除孤立顶点
		
		/* 加权无向图 */
		amg = new AdjMatrixGraph(tagVec.size() + 1); // 0表示测试向量顶点,其它训练向量顶点
		amgtest = new AdjMatrixGraph(tagVec.size());

		/* 图中插入边 */
		for (int i = 0; i < tagVec.size(); i++) {
			for (int j = 0; j < tagVec.size(); j++) {
				
				//System.out.print(Arrays.toString(tagVec.get(i).vector));
				//System.out.print(Arrays.toString(tagVec.get(j).vector));
				
				double dist = WDist(tagVec.get(i), tagVec.get(j)); // 计算边权值
				/* 双向插边,第0个位置保留给测试向量 */
				amg.insertEdge(i + 1, j + 1, dist);
				amg.insertEdge(j + 1, i + 1, dist);
				
				amgtest.insertEdge(i, j, dist);
				amgtest.insertEdge(j, i, dist);
			}
		}
		
		//amg.isConnect();
		//amgtest.isConnect();
		//System.exit(0);
		
		MatrixM = new double[tagVec.size() + 1][tagVec.size() + 1]; // 构造M矩阵

		VecD = new double[tagVec.size() + 1]; // 构造d
		for (int i = 0; i < VecD.length; i++)
			VecD[i] = 1.0 / VecD.length;
	}
	
	/**
	 * 构造方法
	 */
	public RandomWalk(int n) throws IOException { // 参数n:交叉验证中针对测试集进行分组,从0开始到XFold-1

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
		/* 加载训练集向量文件 END */

		Normalized(); // 数据归一化
		
//		for (int i=0;i<tagVec.size();i++) {
//		//System.out.print(Arrays.toString(tv.vector));
//		//System.out.println(Arrays.toString(tv.tagVal));
//			if(test(tagVec.get(i)))
//				//System.out.println(i);
//				tagVec.remove(i);
//	}
		
		XFoldCV(n); // 10折交叉验证预处理,经过此步,tagVec和testTagVec被处理

		this.RemoveIsolatedVertex(); // 去除孤立顶点,只针对训练集.测试集只做最后验证使用,不需要进行处理
		/* 此时,tagVec和testTagVec都已经处理完毕 */


		
		/* 加权无向图 */
		amg = new AdjMatrixGraph(tagVec.size() + 1); // 0表示测试向量顶点,其它训练向量顶点

		/* 图中插入边 */
		for (int i = 0; i < tagVec.size(); i++) {
			for (int j = 0; j < tagVec.size(); j++) {
				
				//System.out.print(Arrays.toString(tagVec.get(i).vector));
				//System.out.print(Arrays.toString(tagVec.get(j).vector));
				
				double dist = WDist(tagVec.get(i), tagVec.get(j)); // 计算边权值
				/* 双向插边,第0个位置保留给测试向量 */
				amg.insertEdge(i + 1, j + 1, dist);
				amg.insertEdge(j + 1, i + 1, dist);
			}
		}

		// log
		// amg.print();

		MatrixM = new double[tagVec.size() + 1][tagVec.size() + 1]; // 构造M矩阵

		VecD = new double[tagVec.size() + 1]; // 构造d
		for (int i = 0; i < VecD.length; i++)
			VecD[i] = 1.0 / VecD.length;
	}

	// 归一化
	private void Normalized() {
		// TODO Auto-generated method stub
		double[] store = new double[tagVec.size()];
		Double min = null, max = null;
		for (int i = 0; i < TagVector.vecNum; i++) {
			for (int j = 0; j < tagVec.size(); j++)
				store[j] = tagVec.get(j).vector[i];
			Arrays.sort(store);
			// store存放特征值，通过排序得到最小值和最大值
			min = store[0];
			max = store[tagVec.size() - 1];
			for (int j = 0; j < tagVec.size(); j++)
				if ((max - min) != 0)
					tagVec.get(j).vector[i] = (tagVec.get(j).vector[i] - min) / (max - min) * (NormalizedMAX-NormalizedMIN) + NormalizedMIN;
				else
					tagVec.get(j).vector[i] = NormalizedMIN; // 相等置NormalizedMIN
		}
	}

	/**
	 * 构造方法,单独抽取某一个向量时使用
	 */
	public RandomWalk(int ind, Object otherPara) throws IOException { // 参数i:取出第i个向量作为测试向量
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

		singleTV = tagVec.remove(ind);
		//singleTV = tagVec.remove(1);
		//tagVec.remove(1);
//		for (int i = 0; i < Experimet.PTNum; i++)
//			tagVec.remove(2 * i + Experimet.offset);

		this.RemoveIsolatedVertex(); // 去除孤立顶点,只针对训练集.测试集只做最后验证使用,不需要进行处理
		/* 此时,tagVec和testTagVec都已经处理完毕 */

		/* 加权无向图 */
		amg = new AdjMatrixGraph(tagVec.size() + 1); // 0表示测试向量顶点,其它训练向量顶点

		/* 图中插入边 */
		for (int i = 0; i < tagVec.size(); i++) {
			for (int j = 0; j < tagVec.size(); j++) {
				double dist = WDist(tagVec.get(i), tagVec.get(j)); // 计算边权值
				/* 双向插边,第0个位置保留给测试向量 */
				amg.insertEdge(i + 1, j + 1, dist);
				amg.insertEdge(j + 1, i + 1, dist);
			}
		}

		// log
		// amg.print();

		MatrixM = new double[tagVec.size() + 1][tagVec.size() + 1]; // 构造M矩阵

		VecD = new double[tagVec.size() + 1]; // 构造d
		for (int i = 0; i < VecD.length; i++)
			VecD[i] = 1.0 / VecD.length;
	}

	/**
	 * 交叉验证预处理
	 * @throws IOException 
	 */
	private void XFoldCV(int n) throws IOException {
		if (n < 0) {
			System.out.println("XFoldCV():para err");
			return;
		} // 警戒
		if (tagVec == null) {
			System.out.println("XFoldCV():tagVec err");
			return;
		} // 警戒

		testTagVec = new ArrayList<TagVector>(); // 为测试集申请新的内存空间.因为经过预处理后,测试集应为全新的内容
		int baseNum = tagVec.size() / XFold; // 10折分割后的每份大小,相当于基数
		for (int i = n * baseNum; i < (n + 1) * baseNum; i++) { // 向量加入到测试集中
			testTagVec.add(tagVec.get(i));
		}
		for (int i = n * baseNum; i < (n + 1) * baseNum; i++) { // 从训练集中去除已经加入到测试集中的向量
			tagVec.remove(n * baseNum); // remove()操作后元素索引会变化,所以使用固定参数n*baseNum
		}
	}

	/**
	 * 移除孤立点(孤立点是指标签值全为'n'的向量构成的点,其不属于任何一类,故为孤立点.算法假定图连通,故需要移除孤立点)
	 * 即使已经这样做，在有放回抽样的情况下，仍然会有图不连通的可能，只能检测了
	 */
	private void RemoveIsolatedVertex() {
		// TODO Auto-generated method stub
		boolean isIso;
		int i;
		for (i = 0; i < tagVec.size(); i++) { // 扫描整个训练集数据,随着remove()操作的执行,size()也会变化,但检查过的向量不必重复检查
			isIso = true; // 假设是孤立点
			for (int j = 0; j < tagVec.get(i).tagVal.length; j++)
				// 检查所有的标签
				if (tagVec.get(i).tagVal[j].equals("y")) {
					isIso = false; // 表示存在y,不是孤立点
					break;
				}
			if (isIso == true) {
				tagVec.remove(i); // 是孤立点,移除
				i--; // remove()操作后会引起索引的变化,下次循环需要重新检查原来的索引i
			}
		}
	}

	/**
	 * 计算边权值(因为图结构对于默认情况都有处理,所以只需处理需要的情况)
	 */
	public double WDist(TagVector tv1, TagVector tv2) {

		if (tv1 == null || tv2 == null) {
			System.out.println("WDist() err, tv1 or tv2 is null.");
			System.exit(0);
		} else if (tv1 == tv2) // 同一个向量
			return 0;

		for (int i = 0; i < tv1.tagVal.length; i++)
			if (tv1.tagVal[i].equals("y") && tv2.tagVal[i].equals("y")) // 只要两向量有同类标签,就说明它们有边相连
				return Dist(tv1, tv2);
				//return Dist_WMM(tv1, tv2);
		return AdjMatrixGraph.MAX_WEIGHT; // 没有边的情况
	}

	/**
	 * 计算欧氏距离
	 */
	public double osDist(TagVector tv1, TagVector tv2) {

		double sum = 0;
		for (int i = 0; i < tv1.vector.length; i++)
			sum += (tv1.vector[i] - tv2.vector[i]) * (tv1.vector[i] - tv2.vector[i]);
		return Math.sqrt(sum);
	}
	
	// 我直接在这里面修改加权
	public double Dist(TagVector tv1, TagVector tv2) {
		double numerator = 0.0, denominator1 = 0.0, denominator2 = 0.0;
		double res;
		double v1,v2;
		int w = 1;
		// 使用余弦相似度
		for (int i = 0; i < tv1.vector.length; i++) {
//			if(i<40){ //前40维信息增益特征词个数的值进行加权放大
//				v1 = tv1.vector[i] * w;
//				v2 = tv2.vector[i] * w;
//			}else{
//				v1 = tv1.vector[i];
//				v2 = tv2.vector[i];
//			}
			v1 = tv1.vector[i] * w;
			v2 = tv2.vector[i] * w;
			//v1 = tv1.vector[i] * FSWeight[i];
			//v2 = tv2.vector[i] * FSWeight[i];
			numerator += v1 * v2;
			denominator1 += v1 * v1;
			denominator2 += v2 * v2;
		}
		if(denominator1 * denominator2==0){
			System.out.println("*****************");
			System.out.println(Arrays.toString(tv1.vector));
			System.out.println(Arrays.toString(tv2.vector));
		}
		res = numerator / (Math.sqrt(denominator1 * denominator2));
		//res = (Math.sqrt(denominator1 * denominator2)) / numerator;
		
		//log
		//System.out.print("$"+res);
		
		return (1-res)!=0?(1-res):PRCIOUS;
		//return (1-res)!=0?(1-res):0.01;
	}
	
	//利用权值
	public double Dist_WMM(TagVector tv1, TagVector tv2) {

		double sum = 0;
		for (int i = 0; i < tv1.vector.length; i++)
			sum += (tv1.vector[i] - tv2.vector[i]) * (tv1.vector[i] - tv2.vector[i]) * WMM[i] * 2; //带上权值
		return Math.sqrt(sum);
	}

	/**
	 * 构建Gk. tv是测试向量. k为类标识
	 */
	public AdjMatrixGraph ConstructGk(TagVector tv, int k) {
		if (tv == null || k < 0 || k >= TagVector.TagNum) {
			System.out.println("ConstructGk() err. tv or k err.");
			System.exit(0);
		}
		AdjMatrixGraph Gk = new AdjMatrixGraph(amg);
		for (int i = 0; i < tagVec.size(); i++) { // 填充测试向量对应的边(权值)
			//System.out.print(tagVec.get(i).tagVal[k]);
			if (tagVec.get(i).tagVal[k].equals("y")) {
				Gk.insertEdge(0, i + 1, Dist(tv, tagVec.get(i)));
				Gk.insertEdge(i + 1, 0, Dist(tv, tagVec.get(i)));
				//Gk.insertEdge(0, i + 1, Dist_WMM(tv, tagVec.get(i)));
				//Gk.insertEdge(i + 1, 0, Dist_WMM(tv, tagVec.get(i)));
			}
		}

		//System.out.println();
		// log
		// Gk.print();
//		 if(!Gk.isConnect()){
//			 Gk.print();
//			 System.exit(0);
//		 }
		return Gk;
	}

	/**
	 * 邻接矩阵M,|D|+1维,第0维是测试向量,元素值为随机游走概率,归一化数值
	 */
	public void ConstructMatrixM(AdjMatrixGraph gk) {
		double sum1, sum2;
		for (int i = 0; i < MatrixM.length; i++) {
			sum1 = sum2 = 0;
			for (int j = 0; j < MatrixM.length; j++)
				if (j == i || gk.adjMatrix[i][j] == AdjMatrixGraph.MAX_WEIGHT)
					MatrixM[i][j] = 0; // 1)不会游走到自身. 2)两点之间没有边,游走概率为0
				else
					sum1 += gk.adjMatrix[i][j]; // 整列求和,'自身到自身'以及'没有边相连'的除外
			for (int j = 0; j < MatrixM.length; j++)
				if (j != i && gk.adjMatrix[i][j] != AdjMatrixGraph.MAX_WEIGHT) {
					if (gk.adjMatrix[i][j] != 0)
						MatrixM[i][j] = sum1 / gk.adjMatrix[i][j]; // 用'整列的和'除以'单个权值',因为权值越大,距离越远,概率越小
					else
						MatrixM[i][j] = 0; // 这里应该是多余了，因为除了对角线，距离没有为0的了
					sum2 += MatrixM[i][j]; // 变化后的值整列求和,用于归一化
				}
			// log
			// System.out.println("$"+Arrays.toString(MatrixM[i]));
			for (int j = 0; j < MatrixM.length; j++)
				MatrixM[i][j] = MatrixM[i][j] / sum2; // 归一化
			// log
			// System.out.println(Arrays.toString(MatrixM[i]));
		}
	}

	/**
	 * 入口
	 * 
	 * @param tv
	 * @return
	 */
	public double[] StartWith(TagVector tv) {
		// TODO Auto-generated method stub

		if (tv == null)
			return null;

		double[] p = new double[TagVector.TagNum];
		double[] cp;
		double[] pp = PriorProb(tv);
		// log
		// System.out.println("PP: "+Arrays.toString(pp));
		AdjMatrixGraph gk = null;
		for (int i = 0; i < TagVector.TagNum; i++) {
			gk = ConstructGk(tv, i);
			// log
			// System.out.println("gk----");
			// gk.print();
			// System.out.println("M----");
			System.out.print(i);
			ConstructMatrixM(gk); // 先得到Gk,再由Gk得到M
			cp = CondProb(CalculateAvgSimAndU(ConstructPI(gk), i)); // 这里考虑是否用CalculateAvgSimAndU()?
			// System.out.println(Arrays.toString(cp));
			for (int j = 0; j < p.length; j++) {
				p[j] += cp[j] * pp[i];
			}
		}
		//System.out.println();
		//System.out.println("P: " + Arrays.toString(p));
		System.out.println("\n"+tagVec.indexOf(tv)+" P: " + Arrays.toString(p));
		// System.out.println("----------");
		return p;
	}

	/**
	 * 先验概率
	 */
	public double[] PriorProb(TagVector tv) {
		double sum, num, s1 = 0, s2 = 0;
		double[] pp = new double[TagVector.TagNum];
		for (int i = 0; i < TagVector.TagNum; i++) {
			sum = num = 0;
			for (int j = 0; j < tagVec.size(); j++) {
				if (tagVec.get(j).tagVal[i].equals("y")) {
					sum += Dist(tv, tagVec.get(j)); // 测试点与某一标签类所有点的距离求和
					//sum += Dist_WMM(tv, tagVec.get(j));
					num++; // 某一标签类所有点个数(不会为0)
				}
			}
			//System.out.println(sum);
			pp[i] = sum / num; // 针对某一标签类的平均距离
			s1 += pp[i]; // 求和,用于变换
		}
		//System.out.println(Arrays.toString(pp));
		for (int i = 0; i < TagVector.TagNum; i++) {
			pp[i] = s1 / pp[i]; // 与邻接矩阵M的变换过程相同
			s2 += pp[i]; // 求和用于归一化
		}
		for (int i = 0; i < TagVector.TagNum; i++)
			pp[i] = pp[i] / s2; // 归一化
		return pp;
	}

	/**
	 * 计算PI
	 * 
	 * @param gk
	 * @return
	 */
	public double[] ConstructPI(AdjMatrixGraph gk) {
		/* 构造S0 */
		double[] S0 = new double[tagVec.size() + 1]; // 随机游走初始向量S0

		// S0[0] = 1;
		// for(int i=0;i<tagVec.size();i++) //S0的另一种构造方式
		// S0[i+1] = 0;

		S0[0] = 0; // 初始状态时到自身的概率为0,也就是在开始时一定往外游走
		for (int i = 0; i < tagVec.size(); i++) {
			if (gk.adjMatrix[0][i + 1] == AdjMatrixGraph.MAX_WEIGHT)
				S0[i + 1] = 0; // 没有边相连,概率为0
			else
				S0[i + 1] = gk.adjMatrix[0][i + 1];
		}
		double sum1 = 0, sum2 = 0;
		for (int i = 0; i < S0.length; i++)
			sum1 += S0[i];
		for (int i = 0; i < S0.length; i++)
			if (S0[i] != 0) {
				S0[i] = sum1 / S0[i]; // 变换(同邻接矩阵M过程)
				sum2 += S0[i]; // 累加,用于归一化
			}
		for (int i = 0; i < S0.length; i++)
			S0[i] = S0[i] / sum2;

		// log
		// System.out.println("S0: "+Arrays.toString(S0));

		/* 迭代计算PI过程 */
		boolean isntPI = true;
		double tmp;
		double[] tmpA = new double[tagVec.size() + 1];
		// int n = 0;

		while (isntPI) {
			// n++;
			for (int i = 0; i < MatrixM.length; i++) {
				tmp = 0;
				for (int j = 0; j < MatrixM.length; j++) {
					tmp += MatrixM[j][i] * S0[j]; // i,j已交换
				}
				tmpA[i] = tmp * (1 - ALPHA) + ALPHA * VecD[i]; // 迭代计算PI,存放在S0中
			}
			// if (ArrayDist(S0, tmpA) < PRCIOUS){ // 迭代完成条件
			if (isEqual(S0, tmpA)) {
				isntPI = false;

				// log
				// System.out.println(Arrays.toString(S0));
				// System.out.println(Arrays.toString(tmpA));
			}

			for (int i = 0; i < tmpA.length; i++)
				S0[i] = tmpA[i];

			// log
			// System.out.println(Arrays.toString(S0));

			// try {
			// System.in.read();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		// log
		// System.out.println(Arrays.toString(S0));

		return S0;
	}

	/**
	 * 向量的欧氏距离
	 */
	public double ArrayDist(double[] a, double[] b) {
		int sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		return Math.sqrt(sum);
	}

	/**
	 * 向量各维度单独比较,用PRCIOUS衡量
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean isEqual(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++) {
			if (Math.abs(a[i] - b[i]) > PRCIOUS)
				return false;
		}
		return true;
	}

	/**
	 * 计算待测向量与每个标签的相似度,平均值方式
	 * 
	 * @param pi
	 * @return
	 */
	public double[] CalculateAvgSim(double[] pi) {
		double sum, num;
		double[] avgSim = new double[TagVector.TagNum];
		for (int i = 0; i < TagVector.TagNum; i++) {
			sum = num = 0;
			for (int j = 0; j < tagVec.size(); j++) {
				if (tagVec.get(j).tagVal[i].equals("y")) {
					sum += pi[j + 1]; // 因为pi是n+1维向量，也就是里面含有测试向量，因为假设了它属于某一类，在累加时是否需要加上它自己的概率？[在这里不加]
					num++;
				}
			}
			avgSim[i] = sum / num;
		}
		return avgSim;
	}

	public double[] CalculateAvgSimAndU(double[] pi, int k) {
		double sum, num;
		double[] avgSim = new double[TagVector.TagNum];
		for (int i = 0; i < TagVector.TagNum; i++) {
			sum = num = 0;
			for (int j = 0; j < tagVec.size(); j++) {
				if (tagVec.get(j).tagVal[i].equals("y")) {
					sum += pi[j + 1];
					num++;
				}
			}
			if (k == i) {
				sum += pi[0]; // 此处把测试向量概率也加上
				num++;
			}
			avgSim[i] = sum / num;
		}
		return avgSim;
	}

	/**
	 * 条件概率,结合CalculateAvgSim(),论文公式(17)
	 * 
	 * @param avgsim
	 * @return
	 */
	public double[] CondProb(double[] avgsim) {
		double sum = 0;
		double[] cp = new double[TagVector.TagNum];
		for (int i = 0; i < avgsim.length; i++) {
			sum += avgsim[i];
		}
		for (int i = 0; i < cp.length; i++) {
			cp[i] = avgsim[i] / sum;
		}
		return cp;
	}

	//debug
	public boolean test(TagVector tv){
		boolean isZero = true;
		for(int i=0;i<TagVector.vecNum;i++){
			if(tv.vector[i]!=0)
				isZero = false;
		}
		if(isZero){
			//System.out.println("zero!");
			//System.out.print(Arrays.toString(tv.vector));
			//System.out.println(Arrays.toString(tv.tagVal));
			return true;
		}
		return false;
	}
}
