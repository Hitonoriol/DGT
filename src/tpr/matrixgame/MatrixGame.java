package tpr.matrixgame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import hitonoriol.matrix.Matrix;
import tpr.TPR;

public class MatrixGame {
	private Matrix matrix;

	private Number tv, bv;

	public MatrixGame(Matrix matrix) {
		this.matrix = matrix;
		evaluatePrice();
	}

	// Нахождение решения игры -- если нет в чистых стратегиях, решаем в смешанных
	public void solve() {
		TPR.print("Исходная матрица игры:");
		dumpMatrix();

		while (findDominatingRows() || findDominatingCols())
			;

		evaluatePrice();
		if (!findSaddlePoint())
			findMixedStrategySolution();
	}

	// Решение в смешанных стратегиях
	private void findMixedStrategySolution() {
		TPR.print("\nРешение в смешанных стратегиях:");
		if (matrix.getWidth() == 2 && matrix.getHeight() == 2) {
			double a = matrix.getDouble(0, 0), b = matrix.getDouble(0, 1);
			double c = matrix.getDouble(1, 0), d = matrix.getDouble(1, 1);
			boolean undetermined = ((a < b && a < c && d < c) || (a > b && a > c && d > c));

			TPR.print("[игра " + (undetermined ? "не " : "") + "строго детерминирована]");

			if (undetermined) {
				double p[] = new double[2], q[] = new double[2];
				double v;
				double denom = (a + d) - (b + c);
				p[0] = (d - c) / denom;
				p[1] = (a - b) / denom;

				q[0] = (d - b) / denom;
				q[1] = (a - c) / denom;

				v = (a * d - b * c) / denom;

				TPR.print("p* = (" + p[0] + ", " + p[1] + ")");
				TPR.print("q* = (" + q[0] + ", " + q[1] + ")");
				TPR.print("V = " + v);

				boolean pg = Matrix.create(matrix -> matrix.addRow(ArrayUtils.toObject(p)))
						.multiply(matrix)
						.applyPredicate(Matrix.create(matrix -> matrix.addRow(v, v)), Matrix.greaterOrEqual);

				boolean gq = matrix
						.multiply(Matrix.create(matrix -> matrix.addRow(q[0]).addRow(q[1])))
						.applyPredicate(Matrix.create(matrix -> matrix.addRow(v).addRow(v)), Matrix.smaller);

				TPR.print("p*G >= (v, v) -> " + optimalStratMsg("p*", pg));
				TPR.print("Gq* < (v, v)^T -> " + optimalStratMsg("q*", gq));
			}
		}
	}

	private String optimalStratMsg(String stratName, boolean result) {
		return (result ? "выполняется, стратегия " + stratName + " оптимальна" : "не выполняется");
	}

	// Упрощение по доминирующим столбцам
	private boolean findDominatingCols() {
		List<Point> dominatedCols = new ArrayList<>();

		MutableInt j = new MutableInt(0), i = new MutableInt(0);
		for (; j.intValue() < matrix.getWidth(); j.increment()) {
			List<Integer> dominates = IntStream
					.range(0, matrix.getWidth())
					.boxed()
					.collect(Collectors.toList());

			i.setValue(0);
			matrix.forEachInCol(j.intValue(), colVal -> {
				for (int kj = 0; kj < matrix.getWidth(); ++kj) {
					if (j.intValue() == kj)
						continue;

					int elem = matrix.get(i.intValue(), kj).intValue();
					if (dominates.contains(Integer.valueOf(kj)))
						if (colVal.intValue() > elem) {
							dominates.remove(Integer.valueOf(kj));
						}
				}
				i.increment();
			});
			dominates.forEach(dominatedCol -> dominatedCols.add(new Point(j.intValue(), dominatedCol)));
		}

		int sz = dominatedCols.size() - 1;
		MutableInt idx = new MutableInt(0);
		Point altPair = new Point();
		MutableBoolean hasDominating = new MutableBoolean(false);
		IntStream
				.rangeClosed(0, sz)
				.mapToObj(k -> dominatedCols.get(k))
				.forEach(domPair -> {
					int dominating = domPair.x, dominated = domPair.y;
					if (dominating == dominated) {
						idx.decrement();
						return;
					}
					altPair.setLocation(dominated, dominating);
					if (dominatedCols.contains(altPair)
							&& dominatedCols.indexOf(altPair) > idx.intValue()
							|| !matrix.hasCol(dominated)) {
						idx.decrement();
						return;
					}

					TPR.out("Столбец " + (dominating + 1) + " доминирует столбец " + (dominated + 1) + "\n");
					TPR.drawArrow(15);
					TPR.out();
					matrix.delCol(dominated);
					dumpMatrix();
					hasDominating.setValue(true);
					idx.decrement();
				});
		return hasDominating.booleanValue();
	}

	// Упрощение по доминирующим строкам
	private boolean findDominatingRows() {
		List<Point> dominatedRows = new ArrayList<>();

		MutableInt i = new MutableInt(0), j = new MutableInt(0);
		for (; i.intValue() < matrix.getHeight(); i.increment()) {
			List<Integer> dominates = IntStream
					.range(0, matrix.getHeight())
					.boxed()
					.collect(Collectors.toList());

			j.setValue(0);
			matrix.forEachInRow(i.intValue(), rowVal -> {
				for (int ki = 0; ki < matrix.getHeight(); ++ki) {
					if (i.intValue() == ki)
						continue;

					int elem = matrix.get(ki, j.intValue()).intValue();
					if (dominates.contains(Integer.valueOf(ki)))
						if (rowVal.intValue() < elem) {
							dominates.remove(Integer.valueOf(ki));
						}
				}
				j.increment();
			});
			dominates.forEach(dominatedRow -> dominatedRows.add(new Point(i.intValue(), dominatedRow)));
		}

		int sz = dominatedRows.size() - 1;
		MutableInt idx = new MutableInt(sz);
		Point altPair = new Point();
		MutableBoolean hasDominating = new MutableBoolean(false);
		IntStream
				.rangeClosed(0, sz)
				.mapToObj(k -> dominatedRows.get(sz - k))
				.forEach(domPair -> {
					int dominating = domPair.x, dominated = domPair.y;
					if (dominating == dominated) {
						idx.decrement();
						return;
					}
					altPair.setLocation(dominated, dominating);
					if (dominatedRows.contains(altPair)
							&& dominatedRows.indexOf(altPair) < idx.intValue()) {
						idx.decrement();
						return;
					}

					TPR.out("Строка " + (dominating + 1) + " доминирует строку " + (dominated + 1) + "\n");
					TPR.drawArrow(15);
					TPR.out();
					matrix.delRow(dominated);
					dumpMatrix();
					hasDominating.setValue(true);
					idx.decrement();
				});
		return hasDominating.booleanValue();
	}

	// Нахождение верхней и нижней цен игры
	private void evaluatePrice() {
		Set<Double> maxMin = new HashSet<>(), minMax = new HashSet<>();

		for (int j = 0; j < matrix.getWidth(); ++j)
			minMax.add(matrix.pickFromCol(Matrix.maxPredicate, j).doubleValue());
		bv = Collections.min(minMax);

		for (int i = 0; i < matrix.getHeight(); ++i)
			maxMin.add(matrix.pickFromRow(Matrix.minPredicate, i).doubleValue());
		tv = Collections.max(maxMin);
	}

	// Существует ли минимум одна седловая точка (нижняя цена == верхней)
	public boolean hasSaddlePoint() {
		return tv.equals(bv);
	}

	// Нахождение всех седловых точек
	private boolean findSaddlePoint() {
		TPR.print("Верхняя цена: " + tv);
		TPR.print("Нижняя цена: " + bv + "\n");

		if (!hasSaddlePoint()) {
			TPR.print(tv + " < " + bv + " => Решения в чистых стратегиях нет");
			return false;
		}

		final int mi = matrix.getHeight(), mj = matrix.getWidth();

		for (int i = 0; i < mi; i++) {
			Number minVal = matrix.get(i, 0);
			int col = 0, row = 0;
			for (int j = 1; j < mj; j++) {
				if (minVal.floatValue() > matrix.get(i, j).floatValue()) {
					minVal = matrix.get(i, j);
					row = i;
					col = j;
				}
			}

			Number maxVal = matrix.get(0, col);
			for (int k = 1; k < mi; k++)
				if (maxVal.floatValue() < matrix.get(k, col).floatValue())
					maxVal = matrix.get(k, col);

			if (maxVal == minVal)
				TPR.print(minVal + " = " + maxVal + " => Седловая точка: (" + (row + 1) + ", " + (col + 1) + ") = "
						+ minVal);
		}

		return true;
	}

	public void dumpMatrix() {
		matrix.dump("A", "B");
	}
}
