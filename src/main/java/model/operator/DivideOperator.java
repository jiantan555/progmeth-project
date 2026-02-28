package model.operator;

public class DivideOperator implements Operator {
    @Override
    public double apply(double a, double b) {
        if (b == 0) {
            // ป้องกันการหารด้วยศูนย์
            return 0; // หรือจะ throw Exception ก็ได้ แต่ให้คืน 0 เพื่อไม่ให้เกมแครช
        }
        return a / b;
    }

    @Override
    public String getSymbol() {
        return "÷";
    }
}