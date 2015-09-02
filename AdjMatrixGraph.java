package com.mike;

public class AdjMatrixGraph {

	public static final int MAX_WEIGHT = Integer.MAX_VALUE; // 表示顶点之间无边相连(用整型在判等时要比double型安全一些?)
	public double[][] adjMatrix; // 图的邻接矩阵,权值用double型
	public int size; // 图的规模

	/**
	 * 构造具有n个独立顶点的图
	 */
	public AdjMatrixGraph(int n) { // n为顶点数目
		size = n;
		adjMatrix = new double[n][n]; // 为邻接矩阵申请内存空间
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				adjMatrix[i][j] = (i == j) ? 0 : MAX_WEIGHT; // 权值初始化,对角线上为0,其它都为无穷大
	}

	/**
	 * 拷贝构造方法
	 */
	public AdjMatrixGraph(AdjMatrixGraph G) {
		size = G.size;
		adjMatrix = new double[G.size][G.size];
		for (int i = 0; i < G.size; i++)
			for (int j = 0; j < G.size; j++)
				adjMatrix[i][j] = G.adjMatrix[i][j];
	}

	/**
	 * 插入边,3参数,i和j是索引,单向,插入一条权值为weight的边<vi,vj>,若该边已有,则不插入
	 */
	public boolean insertEdge(int i, int j, double weight) {
		if (i != j && adjMatrix[i][j] == MAX_WEIGHT) { // 先判断该边两个顶点是否存在,是否为同一顶点,边是否存在
			adjMatrix[i][j] = weight; // 添加权值
			return true;
		}
		return false;
	}

	/**
	 * 定制输出格式
	 */
	public void print() {
		String str = "邻接矩阵：\n";
		System.out.print(str);
		for (int i = 0; i < size; i++) {
			str = "";
			for (int j = 0; j < size; j++) {
				if (adjMatrix[i][j] == MAX_WEIGHT)
					str += "    ∞"; // 最大值(边不存在)的时候的显示方式
				else
					str += "    " + adjMatrix[i][j]; // 每一个顶点到其他顶点的权值
			}
			str += "\n";
			System.out.print(str);
		}
	}

	/**
	 * 图的连通性
	 */
	public boolean isConnect() {
		int n = this.size;
		boolean[] visited = new boolean[n];
		// 记录不能一次深度优先遍历通过的数目
		// 全部顶点作为出发点开始遍历，如果全部都不能一次遍历通过（notConnectNum == n），说明该图不连通。
		int notConnectNum = 0;
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				visited[i] = false;
			}
			this.DFS(j, visited);
			for (int k = 0; k < n; k++) {
				if (visited[k] == false) {
					notConnectNum++;
					break;// 一旦有没有被遍历到的顶点（说明该顶点不属于该连通分量），跳出循环
				}
			}
		}
		if (notConnectNum == n) {
			System.out.println("此图是不连通的");
			return false;
		} else {
			System.out.println("此图是连通的");
			return true;
		}
	}

	/**
	 * 参数1:遍历起始点的编号.参数2:记录各个顶点是否被访问过
	 */
	public void DFS(int v, boolean[] visited2) {
		boolean[] visited = visited2;
		visited[v] = true;
		for (int w = this.getFirstNeighbor(v); w >= 0; w = this.getNextNeighbor(v, w)) {
			if (!visited[w]) {
				visited[w] = true;
				DFS(w, visited);
			}
		}
	}

	/**
	 * 返回顶点序号v的第一个邻接顶点的序号
	 * 
	 * @param v
	 * @return 若不存在第一个邻接顶点,则返回-1
	 */
	public int getFirstNeighbor(int v) {
		return getNextNeighbor(v, -1);
	}

	/**
	 * 返回v在w后的下一个邻接顶点
	 * 
	 * @param v
	 * @param w
	 * @return 若不存在则返回-1
	 */
	public int getNextNeighbor(int v, int w) {
		if (v >= 0 && v < size && w >= -1 && w < size // 对v，w的范围限定
				&& v != w)
			for (int j = w + 1; j < size; j++)
				/* w=-1时，j从0开始寻找下一个邻接顶点 */
				if (adjMatrix[v][j] > 0 && adjMatrix[v][j] < MAX_WEIGHT) // 遍历和v相关的点，得到下一个点
					return j;
		return -1;
	}
}
