package model.card;

// Enum สำหรับสีของการ์ด
public enum CardColor {
    RED, BLUE, GREEN, YELLOW, WILD;

    // สุ่มสีสำหรับการจั่วไพ่
    public static CardColor getRandomColor() {
        return values()[(int) (Math.random() * values().length)];
    }
}