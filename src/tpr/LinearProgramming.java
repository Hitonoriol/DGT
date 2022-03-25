package tpr;

public class LinearProgramming {
	private static final double EPSILON = 1.0E-10;
	private double[][] a;
	private int m;
	private int n;

	private int[] basis;

	public static void main(String[] args) {
		TPR.print("* Пример 1");
		double[][] A = {
				{ 2, -5, 1, 0, 0 },
				{ 3, 4, 0, 1, 0 },
				{ -2, 3, 0, 0, 1 }
		};
		double[] c = { 2, -5, 0, 0, 0 };
		double[] b = { -10, 12, 9 };
		TPR.print("Матрица ограничений");
		test(A, b, c);
	}

	private static void test(double[][] A, double[] b, double[] c) {
		LinearProgramming lp;
		try {
			lp = new LinearProgramming(A, b, c);
		} catch (ArithmeticException e) {
			System.out.println(e);
			return;
		}

		TPR.print("Z(X) = " + lp.value());

		double[] x = lp.primal();
		TPR.out("x* = { ");
		for (int i = 0; i < x.length; i++)
			TPR.out(x[i] + " ");
		TPR.out("}");
	}

	public LinearProgramming(double[][] A, double[] b, double[] c) {
		m = b.length;
		n = c.length;
		for (int i = 0; i < m; i++)
			if (!(b[i] >= 0))
				throw new IllegalArgumentException("Свободные члены должны быть неотрицательными");

		a = new double[m + 1][n + m + 1];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				a[i][j] = A[i][j];
		for (int i = 0; i < m; i++)
			a[i][n + i] = 1.0;
		for (int j = 0; j < n; j++)
			a[m][j] = c[j];
		for (int i = 0; i < m; i++)
			a[i][m + n] = b[i];

		basis = new int[m];
		for (int i = 0; i < m; i++)
			basis[i] = n + i;

		solve();
	}

	private void solve() {
		while (true) {
			show();
			TPR.print();
			int q = bland();
			if (q == -1) {
				TPR.print("План оптимален");
				break;
			}

			TPR.print("Разрешающий столбец " + (q + 1));

			int p = minRatioRule(q);
			if (p == -1)
				throw new ArithmeticException();

			TPR.print("Разрешающая строка " + (p + 1));

			pivot(p, q);

			basis[p] = q;
		}
	}

	private int bland() {
		for (int j = 0; j < m + n; j++)
			if (a[m][j] > 0)
				return j;
		return -1;
	}

	private int minRatioRule(int q) {
		int p = -1;
		for (int i = 0; i < m; i++) {
			if (a[i][q] <= EPSILON)
				continue;
			else if (p == -1)
				p = i;
			else if ((a[i][m + n] / a[i][q]) < (a[p][m + n] / a[p][q]))
				p = i;
		}
		return p;
	}

	private void pivot(int p, int q) {
		for (int i = 0; i <= m; i++)
			for (int j = 0; j <= m + n; j++)
				if (i != p && j != q)
					a[i][j] -= a[p][j] * a[i][q] / a[p][q];

		for (int i = 0; i <= m; i++)
			if (i != p)
				a[i][q] = 0.0;

		for (int j = 0; j <= m + n; j++)
			if (j != q)
				a[p][j] /= a[p][q];
		a[p][q] = 1.0;
	}

	public double value() {
		return -a[m][m + n];
	}

	public double[] primal() {
		double[] x = new double[n];
		for (int i = 0; i < m; i++)
			if (basis[i] < n)
				x[basis[i]] = a[i][m + n];
		return x;
	}

	private void show() {
		TPR.out("b", 7);
		for (int j = 0; j <= m + n - 1; j++)
			TPR.out("y" + (j + 1), 8);
		TPR.print();

		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= m + n; j++) {
				if (j == 0)
					TPR.outf("%7.2f ", a[i][m + n]);
				else
					TPR.outf("%7.2f ", a[i][j - 1]);
			}
			TPR.print();
		}
		TPR.print("Z(X) = " + value());
		for (int i = 0; i < m; i++)
			if (basis[i] < n)
				TPR.print("y_" + (basis[i] + 1) + " = " + a[i][m + n]);
		TPR.print();
	}
}
