package model.card;

// คลาสการ์ดตัวเลข สืบทอดจาก Card
public class NumberCard extends Card {
    private final int number; // Encapsulation: ซ่อนข้อมูลตัวเลข

    public NumberCard(CardColor color, int number) {
        super(color);
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean match(Card otherCard) {
        if (otherCard instanceof NumberCard) {
            NumberCard target = (NumberCard) otherCard;
            // กฎ: สีตรงกัน หรือ เลขตรงกัน อย่างใดอย่างหนึ่ง (ถ้าเป็น WILD สามารถแทนสีอะไรก็ได้)
            boolean isColorMatch = (this.color == target.color) ||
                    (this.color == CardColor.WILD) ||
                    (target.color == CardColor.WILD);

            return isColorMatch || this.number == target.number;
        }
        return false;
    }

    @Override
    public String getDisplayText() {
        return String.valueOf(number);
    }

    // เมธอดสำหรับดึงสีไปใช้ทำ UI ใน JavaFX
    @Override
    public String getStyle() {
        String hexColor;
        String textColor = "black";
        switch (color) {
            case RED: hexColor = "#ff4d4d"; break;
            case BLUE: hexColor = "#4da6ff"; break;
            case GREEN: hexColor = "#4dff4d"; break;
            case YELLOW: hexColor = "#ffff4d"; break;
            case WILD:
                // ปรับให้ไพ่ไม่เจาะจงสี (WILD) กลายเป็นสีรุ้ง (Rainbow Gradient)
                hexColor = "linear-gradient(to bottom right, #ff0000, #ff7f00, #ffff00, #00ff00, #0000ff, #4b0082, #8b00ff)";
                textColor = "white"; // เปลี่ยนตัวอักษรเป็นสีขาวเพื่อให้มองเห็นชัดพร้อมเงา
                return "-fx-background-color: " + hexColor + "; -fx-text-fill: " + textColor + "; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0, 0, 1);";
            default: hexColor = "#ffffff";
        }
        return "-fx-background-color: " + hexColor + "; -fx-text-fill: " + textColor + "; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold; -fx-font-size: 16px;";
    }
}