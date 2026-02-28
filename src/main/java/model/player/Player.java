package model.player;

import model.card.NumberCard;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private List<NumberCard> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<NumberCard> getHand() {
        return hand;
    }

    public void addCard(NumberCard card) {
        hand.add(card);
    }

    public void removeCard(NumberCard card) {
        hand.remove(card);
    }
}