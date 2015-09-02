package com.mike;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*带标签的特征向量*/
public class TagVector {

	public static final int TagNum = 6; // 标签数目
	public static final int vecNum = 72; //特征维度

	double[] vector; // 向量特征值
	String[] tagVal; // 标签值
	
	double[] orivector; //原始向量特征

	/* 构造方法 */
	public TagVector(String s) {

		vector = new double[vecNum];
		tagVal = new String[TagNum];
		/* 正则匹配 */
		String r = null;
		Matcher m = null;
		int index;
		//r = "[0-9]+";
		r = "[0-9.-]+";
		m = Pattern.compile(r).matcher(s);

//		 for (int i = 0; i < 40; i++)
//		 m.find(); // 跳过前40个信息增益维

		index = 0;
		while (m.find()) {
			vector[index++] = Double.parseDouble(m.group()); // 匹配特征向量值
			//System.out.println(vector[index-1]);
		}
		//System.exit(0);
		r = "[yn]";
		m = Pattern.compile(r).matcher(s);
		index = 0;
		while (m.find()) {
			tagVal[index++] = m.group(); // 匹配标签值
		}
	}
}
