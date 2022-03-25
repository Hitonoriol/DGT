package tpr;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import hitonoriol.matrix.Matrix;

public class TPR {
	public static void main(String[] args) {
		ExpertRiskEval riskEval = new ExpertRiskEval(
				Matrix.create(matrix -> matrix
						.addRow(-1, 4, 5, 9)
						.addRow(3, 8, 4, -3)
						.addRow(4, 6, -6, 2)));

		Matrix q = Matrix.create(matrix -> matrix.addRow(0.1, 0.5, 0.2, 0.2));

		print("* Матрица решений:");
		riskEval.dumpMatrix();
		q.dump(null, "q");

		print("\n\n* BL(MM) Критерий:");
		riskEval.blMMCriterion(q);

		print("\n\n* BL(S) Критерий:");
		riskEval.blSCriterion(q);

		print("\n\n* Критерий произведений:");
		riskEval.mulCriterion();
	}

	private static NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
	static {
		numberFormatter.setMinimumFractionDigits(0);
		numberFormatter.setRoundingMode(RoundingMode.HALF_DOWN);
	}

	public static String round(Number num, int n) {
		numberFormatter.setMaximumFractionDigits(n);
		return numberFormatter.format(num.doubleValue());
	}

	public static void out(String str) {
		System.out.print(str);
	}

	public static void out(String str, int width) {
		out(setWidth(str, width));
	}

	public static void print(String str) {
		System.out.println(str);
	}

	public static void print() {
		print("");
	}

	public static void printf(String str, Object... args) {
		System.out.println(String.format(str, args));
	}

	public static String str(int val) {
		return Integer.toString(val);
	}

	public static void out() {
		out("\n");
	}

	public static void outf(String str, Object... args) {
		out(String.format(str, args));
	}

	public static void drawArrow(int width) {
		out("||", width);
		out();
		out("\\/", width);
		out();
	}

	public static <T> void dump2d(T[][] arr, int width, String rowName, String colName) {
		int ysz = arr.length, xsz = arr[0].length;

		out(setWidth("", width));
		for (int i = 0; i < xsz; ++i)
			out(colName + (i + 1), width);
		out();

		for (int i = 0; i < ysz; ++i) {
			out(rowName + (i + 1), width);
			for (int j = 0; j < xsz; ++j)
				out(arr[i][j] + "", width);
			out();
		}
	}

	public static void dump2d(Number[][] arr, String rowName, String colName) {
		dump2d(arr, 6, rowName, colName);
	}

	public static void dump2d(Number[][] arr) {
		dump2d(arr, "", "");
	}

	public static void dump(Number[] arr, int width, String elemName) {
		for (int i = 0; i < arr.length; ++i)
			out(elemName + (i + 1), width);
		out();

		for (int i = 0; i < arr.length; ++i)
			out(round(arr[i], 1) + "", width);
		out();
	}

	public static String setWidth(String string, int length) {
		return String.format("%1$" + length + "s", string);
	}
}
