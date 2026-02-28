package model.engine;

import model.card.CardColor;
import model.card.NumberCard;
import model.player.Player;
import model.operator.Operator;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private Player player1;
    private Player player2;
    private Player currentPlayer;

    private NumberCard targetCard;
    private List<Object> expressionList;

    // กฎแบบยืดหยุ่น (สามารถเปิด/ปิด ได้จาก UI)
    public boolean ruleAllowPlusMinusOne = false;
    public boolean ruleAllowSameParity = false;

    // โหมดการคำนวณ (false = ซ้ายไปขวา, true = ตามหลักคณิตศาสตร์ คูณ/หาร ก่อน)
    public boolean useMathematicalOrder = false;

    public GameEngine(Player p1, Player p2, int initialCardsCount) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        this.expressionList = new ArrayList<>();

        // แจกไพ่ตามจำนวนที่ตั้งค่าไว้
        for (int i = 0; i < initialCardsCount; i++) {
            p1.addCard(generateRandomCard());
            p2.addCard(generateRandomCard());
        }
        this.targetCard = generateRandomCard();
    }

    public NumberCard generateRandomCard() {
        CardColor color;
        if (Math.random() < 0.20) {
            color = CardColor.WILD;
        } else {
            CardColor[] normalColors = {CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW};
            color = normalColors[(int) (Math.random() * normalColors.length)];
        }
        int num = (int) (Math.random() * 9) + 1;
        return new NumberCard(color, num);
    }

    public Player getCurrentPlayer() { return currentPlayer; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public NumberCard getTargetCard() { return targetCard; }
    public List<Object> getExpressionList() { return expressionList; }

    public void setTargetCard(NumberCard newTarget) {
        this.targetCard = newTarget;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    // เมธอดช่วยตรวจสอบกฎทั้งหมด
    private boolean checkFlexibleMatch(NumberCard playCard, NumberCard target) {
        // 1. กฎมาตรฐาน: สีตรงกัน หรือ เลขตรงกัน (หรือเป็นสีรุ้ง) -> แมตช์ 100%
        if (playCard.match(target)) return true;

        // 2. กฎยืดหยุ่น: บังคับว่า "สีต้องตรงกัน" (หรือเป็นสีรุ้ง) ถึงจะอนุญาตให้เช็คความยืดหยุ่นของตัวเลข
        boolean isColorMatch = (playCard.getColor() == target.getColor()) ||
                (playCard.getColor() == CardColor.WILD) ||
                (target.getColor() == CardColor.WILD);

        if (isColorMatch) {
            // กฎยืดหยุ่น: เลขใกล้เคียง +/- 1
            if (ruleAllowPlusMinusOne) {
                if (Math.abs(playCard.getNumber() - target.getNumber()) <= 1) return true;
            }

            // กฎยืดหยุ่น: เลขคู่-คี่ เหมือนกัน
            if (ruleAllowSameParity) {
                if (playCard.getNumber() % 2 == target.getNumber() % 2) return true;
            }
        }

        return false;
    }

    public boolean canPlayCard(NumberCard card) {
        if (expressionList.size() >= 19) return false;

        if (expressionList.isEmpty()) {
            return checkFlexibleMatch(card, targetCard);
        }

        Object lastItem = expressionList.get(expressionList.size() - 1);
        if (lastItem instanceof Operator) {
            NumberCard previousCard = (NumberCard) expressionList.get(expressionList.size() - 2);
            return checkFlexibleMatch(card, previousCard);
        }

        return false;
    }

    // เช็คว่ากดวางเครื่องหมายปกติได้ไหม (ต้องต่อจากไพ่ตัวเลข)
    public boolean canPlayOperator() {
        if (expressionList.isEmpty()) return false;
        if (expressionList.size() >= 19) return false;

        Object lastItem = expressionList.get(expressionList.size() - 1);
        return lastItem instanceof NumberCard;
    }

    // เช็คว่าสามารถ "เปลี่ยน" เครื่องหมายได้ไหม (กรณีตัวสุดท้ายเป็นเครื่องหมายอยู่แล้ว)
    public boolean canReplaceOperator() {
        if (expressionList.isEmpty()) return false;
        return expressionList.get(expressionList.size() - 1) instanceof Operator;
    }

    // สลับเครื่องหมายตัวล่าสุดเป็นอันใหม่
    public void replaceLastOperator(Operator op) {
        if (canReplaceOperator()) {
            expressionList.set(expressionList.size() - 1, op);
        }
    }

    public double calculateCurrentResult() {
        if (expressionList.isEmpty()) return 0;

        if (!useMathematicalOrder) {
            // โหมด: คำนวณจากซ้ายไปขวา (Left-to-Right)
            double result = ((NumberCard) expressionList.get(0)).getNumber();

            for (int i = 1; i < expressionList.size(); i += 2) {
                if (i + 1 < expressionList.size()) {
                    Operator op = (Operator) expressionList.get(i);
                    NumberCard nextCard = (NumberCard) expressionList.get(i + 1);
                    result = op.apply(result, nextCard.getNumber());
                }
            }
            return result;

        } else {
            // โหมด: คำนวณตามหลักคณิตศาสตร์ (BODMAS/PEMDAS ทำคูณ/หารก่อน)
            List<Double> values = new ArrayList<>();
            List<Operator> ops = new ArrayList<>();

            // ดึงค่าไพ่และเครื่องหมายแยกกัน
            for (Object item : expressionList) {
                if (item instanceof NumberCard) {
                    values.add((double) ((NumberCard) item).getNumber());
                } else if (item instanceof Operator) {
                    ops.add((Operator) item);
                }
            }

            // ป้องกันกรณีที่สมการยังพิมพ์ไม่เสร็จ ให้ตัดเครื่องหมายสุดท้ายออกตอนพรีวิวผลลัพธ์
            if (ops.size() >= values.size() && !ops.isEmpty()) {
                ops.remove(ops.size() - 1);
            }

            // รอบที่ 1: จัดการคูณ (×) และ หาร (÷)
            for (int i = 0; i < ops.size(); i++) {
                Operator op = ops.get(i);
                String sym = op.getSymbol();
                if (sym.equals("×") || sym.equals("÷")) {
                    double left = values.get(i);
                    double right = values.get(i + 1);
                    double res = op.apply(left, right);

                    values.set(i, res); // นำผลลัพธ์ใส่แทนที่ตัวตั้ง
                    values.remove(i + 1); // ลบตัวคูณ/หารออก
                    ops.remove(i); // ลบเครื่องหมายออก
                    i--; // ถอย index กลับมาหนึ่งช่องเผื่อเครื่องหมายถัดไปเป็นการคูณ/หารติดกัน
                }
            }

            // รอบที่ 2: จัดการบวก (+) และ ลบ (-)
            for (int i = 0; i < ops.size(); i++) {
                Operator op = ops.get(i);
                double left = values.get(i);
                double right = values.get(i + 1);
                double res = op.apply(left, right);

                values.set(i, res);
                values.remove(i + 1);
                ops.remove(i);
                i--;
            }

            return values.isEmpty() ? 0 : values.get(0);
        }
    }

    public boolean submitExpression() {
        if (expressionList.isEmpty()) return false;
        if (expressionList.get(expressionList.size() - 1) instanceof Operator) return false;

        double result = calculateCurrentResult();
        if (result == targetCard.getNumber()) {
            targetCard = (NumberCard) expressionList.get(expressionList.size() - 1);
            expressionList.clear();
            switchTurn();
            return true;
        }
        return false;
    }

    public void undoFromIndex(int index) {
        for (int i = expressionList.size() - 1; i >= index; i--) {
            Object item = expressionList.remove(i);
            if (item instanceof NumberCard) {
                currentPlayer.addCard((NumberCard) item);
            }
        }
    }

    public void timeOut() {
        undoFromIndex(0);
        currentPlayer.addCard(generateRandomCard());
        switchTurn();
    }
}