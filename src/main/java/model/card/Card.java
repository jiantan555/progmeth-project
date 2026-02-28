package model.card;

// Abstract Class แสดงถึงแนวคิด Inheritance
public abstract class Card {
    protected CardColor color; // protected เพื่อให้คลาสลูกเข้าถึงได้

    public Card(CardColor color) {
        this.color = color;
    }

    public CardColor getColor() {
        return color;
    }

    // Abstract method บังคับให้คลาสลูกต้องมีกฎของตัวเอง (Polymorphism)
    public abstract boolean match(Card otherCard);

    public abstract String getDisplayText();
    public abstract String getStyle();
}