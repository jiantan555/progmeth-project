package model.engine;

import model.card.NumberCard;
import model.operator.Operator;
import java.util.List;

public class ExpressionEvaluator {
    // คำนวณจากซ้ายไปขวา (ไม่มีวงเล็บ)
    public static double evaluate(List<NumberCard> cards, List<Operator> operators) {
        if (cards.isEmpty()) return 0;
        double result = cards.get(0).getNumber();
        for (int i = 0; i < operators.size(); i++) {
            if (i + 1 < cards.size()) {
                double nextValue = cards.get(i + 1).getNumber();
                // Polymorphism ทำงานที่นี่
                result = operators.get(i).apply(result, nextValue);
            }
        }
        return result;
    }
}