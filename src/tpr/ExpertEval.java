package tpr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

public class ExpertEval {
	private Integer r[][];
	private int sz;

	public ExpertEval(Integer[][] m) {
		this.r = new Integer[m.length][m.length];
		sz = m.length;
		createRankMatrix(m);
	}

	private void createRankMatrix(Integer[][] m) {
		int exps = m.length;

		for (int j = 0; j < exps; ++j)
			for (int i = 1; i < exps + 1; ++i)
				r[j][i - 1] = ArrayUtils.indexOf(m[j], i) + 1;
	}

	public Integer[][] getRankMatrix() {
		return r;
	}

	private void arrange(Float[] arr, Map<Float, String> xMap, Comparator<Float> comp) {
		Arrays.sort(arr, comp);
		Set<String> xSet = new HashSet<>();
		String curX;
		for (int i = 0; i < sz; ++i) {
			if (xSet.add(curX = xMap.get(arr[i])))
				TPR.out(curX);
			if (i + 1 < sz) {
				if (!arr[i + 1].equals(arr[i]))
					TPR.out(">");
			} else
				TPR.out("");
		}
	}

	private void addToXMap(Map<Float, String> xMap, Float[] arr, int i) {
		String xName = "x" + (i + 1);
		if (xMap.containsKey(arr[i]))
			xName = xMap.get(arr[i]) + "~" + xName;
		xMap.put(arr[i], xName);
	}

	public double directRanking() {
		Float k[] = new Float[sz];
		Map<Float, String> xMap = new HashMap<>();

		float kTmp;
		for (int i = 0; i < sz; ++i) {
			kTmp = 0;
			for (int j = 0; j < sz; ++j) {
				kTmp += r[j][i];
			}
			k[i] = kTmp / (float) sz;
			addToXMap(xMap, k, i);
		}

		TPR.out();
		TPR.dump(k, 5, "k");
		TPR.out();

		arrange(k, xMap, Comparator.naturalOrder());

		double mk = ((double) sz + 1d) / 2d;
		double dMax = (Math.pow(sz, 2d) - 1d) / 12d;

		double dk = 0;
		for (int i = 0; i < sz; ++i) {
			dk += Math.pow(k[i] - mk, 2);
		}
		dk *= 1 / (double) sz;

		double w = dk / dMax;
		return w;
	}

	public double pairedComparison() {
		List<Integer[][]> conList = new ArrayList<>();
		for (int e = 0; e < sz; ++e) {
			Integer[][] conM = new Integer[sz][sz];
			for (int i = 0; i < sz; ++i)
				for (int j = 0; j < sz; ++j) {
					int xA = r[e][i], xB = r[e][j];
					if (xA == xB)
						conM[j][i] = 0;
					else if (xA > xB)
						conM[j][i] = 1;
					else
						conM[j][i] = -1;
				}
			conList.add(conM);
		}

		TPR.print("Контрастные матрицы");
		int k = 0;
		for (Integer[][] con : conList) {
			++k;
			TPR.print("E" + k);
			TPR.dump2d(con, "x", "x");
			TPR.out();
		}

		Double q[][] = new Double[sz][sz];
		double qCoef = 1d / (double) sz;
		for (int i = 0; i < sz; ++i)
			for (int j = 0; j < sz; ++j) {
				double conSum = 0;
				for (Integer[][] con : conList)
					conSum += con[i][j];
				q[i][j] = qCoef * conSum;
			}

		TPR.print("Матрица средних преимуществ Q_");
		TPR.dump2d(q, 5, "x", "x");

		Float q_[] = new Float[sz];
		float q_coef = 1f / (float) sz;
		for (int i = 0; i < sz; ++i) {
			double q_sum = 0;
			for (int j = 0; j < sz; ++j)
				q_sum += q[i][j];
			q_[i] = (float) (q_coef * q_sum);
		}

		TPR.out();
		TPR.dump(q_, 7, "q_");
		TPR.out();

		Map<Float, String> xMap = new HashMap<>();
		for (int i = 0; i < sz; ++i)
			addToXMap(xMap, q_, i);

		arrange(q_, xMap, Comparator.reverseOrder());
		TPR.out();

		double w = 0;
		for (int i = 0; i < sz; ++i)
			for (int j = 0; j < sz; ++j)
				w += Math.pow(q[i][j], 2);
		w *= 1d / (sz * (sz - 1));
		return w;
	}

	public static void expertRankingTest() {
		Integer m[][] = {
				{ 3, 2, 1, 4 }, // x3>x2>x1>x4
				{ 1, 3, 2, 4 }, // ...
				{ 4, 1, 3, 2 },
				{ 2, 4, 3, 1 }
		};
		ExpertEval experts = new ExpertEval(m);

		TPR.print("Матрица суждений экспертов:");
		TPR.dump2d(experts.getRankMatrix(), "E", "x");

		TPR.print("\nМетод непосредственного ранжирования:");
		double w = experts.directRanking();
		TPR.out();
		TPR.print("\nУровень согласованности экспертов W = " + w);

		TPR.print("\nМетод попарных сравнений:");
		w = experts.pairedComparison();
		TPR.print("\nУровень согласованности экспертов W = " + w);
	}
}
