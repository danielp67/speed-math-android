package com.example.speedMath.ui.memory;

public class Card {
    public enum CardType { OPERATION, RESULT }

    private String content;      // texte affiché
    private CardType type;
    private int index;           // OPERATION ou RESULT
    private boolean isMatched;   // déjà trouvé
    private boolean isFaceUp;    // visible ou cachée

    public Card(String content, CardType type, int index) {
        this.content = content;
        this.type = type;
        this.index = index;
        this.isMatched = false;
        this.isFaceUp = false;
    }

    public String getContent() { return content; }
    public CardType getType() { return type; }
    public boolean isMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }
    public boolean isFaceUp() { return isFaceUp; }
    public void setFaceUp(boolean faceUp) { isFaceUp = faceUp; }
    public int getIndex() { return index; }

    public void setBackGroundColor(int color) {


    }
}
