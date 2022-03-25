package tpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

import hitonoriol.ifelse.If;
import hitonoriol.matrix.Matrix;

public class ExpertRiskEval {
	private Matrix m;

	public ExpertRiskEval(Matrix m) {
		this.m = m;
	}

	public void minMaxCriterion() {
		int max = IntStream
				.range(0, m.getHeight())
				.map(i -> {
					int min = m.pickFromRow(Matrix.minPredicate, i).intValue();
					If.ifDo(i == 0, () -> TPR.out("min = { "));
					TPR.out(TPR.str(min) + " ");
					If.ifDo(i == m.getHeight() - 1, () -> TPR.out("}\n"));
					return min;
				})
				.max()
				.getAsInt();

		TPR.printf("Z_MM = %d", max);
		TPR.print("E_i0: i = {" +
				IntStream.range(0, m.getHeight())
						.filter(i -> m.rowHas(i, max))
						.map(i -> i + 1)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining())
				+ "}");
	}

	// .bayesianCriterion(Matrix.create(matrix -> matrix.addRow(0.1, 0.5, 0.2,
	// 0.2)));
	public void bayesianCriterion(Matrix q) {
		q.dump(null, "q");

		List<Double> er = IntStream
				.range(0, m.getHeight())
				.mapToDouble(i -> {
					MutableDouble sum = new MutableDouble(0);
					MutableInt j = new MutableInt(0);
					m.forEachInRow(i, n -> sum.add(n.doubleValue() * q.getDouble(0, j.getAndIncrement())));
					double e_r = sum.doubleValue();

					If.ifDo(i == 0, () -> TPR.out("sum[e_ij * q_i] = { "));
					TPR.out(e_r + " ");
					If.ifDo(i == m.getHeight() - 1, () -> TPR.out("}\n"));
					return e_r;
				})
				.boxed()
				.collect(Collectors.toList());
		double max = Collections.max(er);
		TPR.printf("Z_BL = %f", max);
		TPR.out("E_i0: i = { ");
		er.forEach(elem -> If.ifDo(elem == max, () -> TPR.out((er.indexOf(Double.valueOf(max)) + 1) + " ")));
		TPR.print("}");
	}

	public void savageCriterion() {
		Matrix a = new Matrix(m.getHeight(), m.getWidth());
		a.forEachElement(elem -> {
			int maxC = m.pickFromCol(Matrix.maxPredicate, elem.j).intValue();
			int mVal = m.get(elem.i, elem.j).intValue();
			a.set(elem.i, elem.j, maxC - mVal);
		});
		TPR.print("Матрица A:");
		a.dump("E", "F");

		int min = IntStream
				.range(0, m.getHeight())
				.map(i -> {
					int max = a.pickFromRow(Matrix.maxPredicate, i).intValue();
					If.ifDo(i == 0, () -> TPR.out("max = { "));
					TPR.out(TPR.str(max) + " ");
					If.ifDo(i == a.getHeight() - 1, () -> TPR.out("}\n"));
					return max;
				})
				.min()
				.getAsInt();

		TPR.printf("Z_S = %d", min);
		TPR.print("E_i0: i = {" +
				IntStream.range(0, m.getHeight())
						.filter(i -> m.rowHas(i, min))
						.map(i -> i + 1)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining())
				+ "}");
	}

	public void hurwitzCriterion(double c) {
		List<Double> eir = IntStream.range(0, m.getHeight())
				.mapToDouble(i -> c * m.pickFromRow(Matrix.minPredicate, i).doubleValue()
						+ (1 - c) * m.pickFromRow(Matrix.maxPredicate, i).doubleValue())
				.boxed()
				.collect(Collectors.toList());

		double max;
		TPR.printf("\nc = %f\n"
				+ "e_ir = %s\n"
				+ "Z_HW = %f\n"
				+ "E_i0: i = %d",
				c, Arrays.toString(eir.toArray()), max = Collections.max(eir),
				eir.indexOf(max) + 1);
	}

	public void hlCriterion(Matrix q, double v) {
		List<Double> eir = IntStream.range(0, m.getHeight())
				.mapToDouble(i -> {
					MutableDouble sum = new MutableDouble(0);
					MutableInt j = new MutableInt(0);
					m.forEachInRow(i, n -> sum.add(n.doubleValue() * q.getDouble(0, j.getAndIncrement())));
					sum.setValue(sum.doubleValue() * v + (1 - v) * m.pickFromRow(Matrix.minPredicate, i).doubleValue());
					return sum.doubleValue();
				})
				.boxed()
				.collect(Collectors.toList());

		double max;
		TPR.printf("\nv = %f\n"
				+ "e_ir = %s\n"
				+ "Z_HL = %f\n"
				+ "E_i0: i = %d",
				v, Arrays.toString(eir.toArray()), max = Collections.max(eir),
				eir.indexOf(max) + 1);
	}

	public void gCriterion(Matrix q) {
		List<Double> eir = IntStream.range(0, m.getHeight())
				.mapToDouble(i -> {
					List<Double> prod = new ArrayList<>();
					MutableInt k = new MutableInt(0);

					q.forEachInRow(0, qv -> prod.add(qv.doubleValue() * m.getDouble(i, k.getAndIncrement())));

					return Collections.min(prod);
				})
				.boxed()
				.collect(Collectors.toList());

		double max;
		TPR.printf("\ne_ir = %s\n"
				+ "Z_G = %f\n"
				+ "E_i0: i = %d",
				Arrays.toString(eir.toArray()), max = Collections.max(eir),
				eir.indexOf(max) + 1);
	}

	public void blMMCriterion(Matrix q) {
		int zMM = m.iStream()
				.map(i -> m.pickFromRow(Matrix.minPredicate, i).intValue())
				.max()
				.getAsInt();
		TPR.printf("Z_MM = %d", zMM);

		int i0 = m.iStream()
				.filter(i -> m.rowHas(i, zMM))
				.map(i -> i + 1)
				.findFirst().getAsInt();
		TPR.printf("i0 = %d\n", i0);

		List<Double> i1 = new ArrayList<>(), i2 = new ArrayList<>(), ex = new ArrayList<>();

		double e = m.iStream()
				.mapToDouble(i -> {
					MutableDouble sum = new MutableDouble(0);
					MutableInt j = new MutableInt(0);
					m.forEachInRow(i, n -> sum.add(n.doubleValue() * q.getDouble(0, j.getAndIncrement())));

					double vI1 = zMM - m.pickFromRow(Matrix.minPredicate, i).doubleValue();
					double vI2 = m.pickFromRow(Matrix.maxPredicate, i).doubleValue()
							- m.pickFromRow(Matrix.maxPredicate, i0 - 1).doubleValue();
					ex.add(sum.doubleValue());
					i1.add(vI1);
					i2.add(vI2);
					TPR.printf("E%d(x) = %f", i + 1, sum.doubleValue());
					TPR.printf("I1_%d = %f", i + 1, vI1);
					TPR.printf("I2_%d = %f\n", i + 1, vI2);

					return sum.doubleValue();
				})
				.max().getAsDouble();

		int ni0 = ex.indexOf(Collections.max(ex)) + 1;
		TPR.printf("E_i0: i0 = %d", ni0);
		TPR.printf("epsilon = %f", Collections.min(i1));
		TPR.printf("I1_%d <= %f", ni0, Collections.min(i1));
	}

	public void blSCriterion(Matrix q) {
		Matrix a = new Matrix(m.getHeight(), m.getWidth());
		a.forEachElement(elem -> {
			int maxC = m.pickFromCol(Matrix.maxPredicate, elem.j).intValue();
			int mVal = m.get(elem.i, elem.j).intValue();
			a.set(elem.i, elem.j, maxC - mVal);
		});

		int zS = m.iStream()
				.map(i -> a.pickFromRow(Matrix.maxPredicate, i).intValue())
				.min()
				.getAsInt();

		TPR.printf("Z_S = %d", zS);

		int i0 = m.iStream()
				.filter(i -> m.rowHas(i, zS))
				.map(i -> i + 1)
				.findFirst().getAsInt();
		TPR.printf("i0 = %d\n", i0);

		List<Double> i1 = new ArrayList<>(), i2 = new ArrayList<>(), ex = new ArrayList<>();

		double e = m.iStream()
				.mapToDouble(i -> {
					MutableDouble sum = new MutableDouble(0);
					MutableInt j = new MutableInt(0);
					m.forEachInRow(i, n -> sum.add(n.doubleValue() * q.getDouble(0, j.getAndIncrement())));

					double vI1 = m.pickFromRow(Matrix.maxPredicate, i).doubleValue() - zS;
					double vI2 = m.pickFromRow(Matrix.minPredicate, i0 - 1).doubleValue()
							- m.pickFromRow(Matrix.minPredicate, i).doubleValue();
					ex.add(sum.doubleValue());
					i1.add(vI1);
					i2.add(vI2);
					TPR.printf("E%d(x) = %f", i + 1, sum.doubleValue());
					TPR.printf("I1_%d = %f", i + 1, vI1);
					TPR.printf("I2_%d = %f\n", i + 1, vI2);

					return sum.doubleValue();
				})
				.max().getAsDouble();

		int ni0 = ex.indexOf(Collections.min(ex)) + 1;
		TPR.printf("E_i0: i0 = %d", ni0);
		TPR.printf("epsilon = %f", Collections.min(i1));
		TPR.printf("I2_%d >= %f", ni0, Collections.min(i1));
	}

	public void mulCriterion() {
		TPR.out("P = { ");
		List<Integer> p = new ArrayList<>();
		int pMax = m.iStream()
				.map(i -> {
					MutableInt mul = new MutableInt(1);
					m.forEachInRow(i,
							num -> If.ifDo(num.doubleValue() > 0, () -> mul.setValue(mul.intValue() * num.intValue())));
					TPR.out(mul.intValue() + " ");
					p.add(mul.intValue());
					return mul.intValue();
				})
				.max().getAsInt();
		TPR.print("}");
		TPR.printf("Z_p = %d", pMax);
		TPR.printf("E_i0: i0 = %d", p.indexOf(Collections.max(p)) + 1);
	}

	public void dumpMatrix() {
		m.dump("E", "F");
	}
}
