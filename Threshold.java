package com.mike;

import java.io.IOException;
import java.util.Arrays;

public class Threshold extends Thread {

	int no;
	int foldnum;

	public Threshold(int no, int foldnum) {
		this.no = no;
		this.foldnum = foldnum;
	}

	public void run() {
		try {
			switch (no) {
			case 0:
				CalculateThreshold(10, 10, no);
				break;
			case 1:
				CalculateThreshold(20, 10, no);
				break;
			case 2:
				CalculateThreshold(30, 10, no);
				break;
			case 3:
				CalculateThreshold(40, 10, no);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Thread-" + no + " end!");
	}

	public void CalculateThreshold(int start, int num, int no) throws IOException {
		double[] tmp = null;
		for (int i = start; i < start + num; i++) {
			System.out.println("--" + i + "--");
			RandomWalk rw = new RandomWalk(this.foldnum);
			//System.out.println(Arrays.toString(rw.testTagVec.get(i).vector));
			tmp = rw.StartWith(rw.testTagVec.get(i));
			//System.out.println(Arrays.toString(tmp));
			for (int j = 0; j < TagVector.TagNum; j++) {
				if (rw.testTagVec.get(i).tagVal[j].equals("y")) {
					ExperimentMT2.pa[no][j] += tmp[j];
					ExperimentMT2.a[no][j]++;
				} else {
					ExperimentMT2.pr[no][j] += tmp[j];
					ExperimentMT2.r[no][j]++;
				}
			}
		}
	}
}
