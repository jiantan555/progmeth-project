package model.operator;

public class AddOperator implements Operator {
    @Override
    public double apply(double a, double b) {
        return a + b;
    }

    @Override
    public String getSymbol() {
        return "+";
    }
}