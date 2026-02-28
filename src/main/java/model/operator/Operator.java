package model.operator;

// Interface สำหรับเครื่องหมายทางคณิตศาสตร์ (Strategy Pattern)
public interface Operator {
    double apply(double a, double b);
    String getSymbol();
}