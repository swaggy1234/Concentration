
import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents a card
class Card {
  int rank;
  String suit;
  boolean faceUp;
  Posn pos;

  //the constructor
  Card(int rank, String suit, Posn pos) {
    this.rank = rank;
    this.suit = suit;
    this.faceUp = false;
    this.pos = pos;
  }

  //EFFECT: Flips the card to the given boolean
  void flip(boolean faceUp) {
    this.faceUp = faceUp;
  }

  //returns if a card is clicked or not
  boolean clicked(Posn mouse) {
    return mouse.x > pos.x - 35
        && mouse.x < pos.x + 35
        && mouse.y < pos.y + 50
        && mouse.y > pos.y - 50;
  }

  //returns if two cards are "mathches"
  boolean match(Card other) {
    boolean suitMatch = false;
    if (other.suit.equals("♣")) {
      suitMatch = this.suit.equals("♠");

    } 
    else if (other.suit.equals("♦")) {
      suitMatch = this.suit.equals("♥");

    } 
    else if (other.suit.equals("♥")) {
      suitMatch = this.suit.equals("♦");

    } 
    else if (other.suit.equals("♠")) {
      suitMatch = this.suit.equals("♣");

    }
    return (this.rank == other.rank) && suitMatch;
  }

  //draws this card
  void draw(WorldScene scene) {
    if (faceUp) {
      this.drawNormal(scene);
    } else {
      this.drawFlipped(scene);
    }
  }

  //Draws the card flipped over on its back
  void drawFlipped(WorldScene scene) {
    scene.placeImageXY(new RectangleImage(70, 100, OutlineMode.SOLID, Color.GRAY), pos.x, pos.y);
  }

  //draws a card facing the screen
  void drawNormal(WorldScene scene) {
    WorldImage base = new RectangleImage(70, 100, OutlineMode.OUTLINE, Color.black);
    String rankS = toRankS(this.rank);
    WorldImage numberImage = new TextImage(rankS, 15, Color.black);
    WorldImage rotated = new RotateImage(numberImage, 180);

    //changes the suit color if its a hearts or diamond card
    Color suitColor = Color.black;
    if (suit.equals("♥") || suit.equals("♦")) {
      suitColor = Color.red;
    }

    WorldImage suitImage = new TextImage(suit, 24, suitColor);
    WorldImage cardWithNumber1 = new OverlayOffsetImage(numberImage, 25, 35, base);
    WorldImage cardWithNumber2 = new OverlayOffsetImage(rotated, -25, -35, cardWithNumber1);
    WorldImage finished = new OverlayImage(suitImage, cardWithNumber2);

    scene.placeImageXY(finished, pos.x, pos.y);
  }

  //transfers a face card's rank from an int to a letter
  String toRankS(int rank) {
    if (rank == 1) {
      return "A";
    } else if (rank == 11) { 
      return "J";
    } else if (rank == 12) { 
      return "Q";
    } else if (rank == 13) { 
      return "K";
    } else {
      return String.valueOf(rank);
    }
  }
}

//represents the concentration game
class ConcentrationGame extends World {
  ArrayList<Card> base; 
  ArrayList<Card> deck;
  Random r;
  int score = 26;

  //the constructor
  ConcentrationGame(Random r) {
    this.base = new ArrayList<Card>();
    this.r = r;
    createBase();
    shuffle();
    this.deck = new ArrayList<Card>(base);
  }

  //creates a standard base deck : all suits and values of ranks
  void createBase() {
    for (int i = 1; i <= 4; i += 1) {
      for (int j = 1; j <= 13; j += 1) {
        String suit;
        if (i == 1) {
          suit = "♣";
        } else if (i == 2) {
          suit = "♦";
        } else if (i == 3) {
          suit = "♥";
        } else if (i == 4) {
          suit = "♠";
        } else {
          suit = "Error";
        }

        int x = 132 + ((j - 1) * 85); 
        int y = 140 + ((i - 1) * 115);
        base.add(new Card(j, suit, new Posn(x, y)));
      }
    }
  }

  //randomizes the order of these cards in the rows
  void shuffle() {
    for (int i = base.size() - 1; i > 0; i--) {
      int j = r.nextInt(i + 1);

      Card temp = base.get(i);
      Card temp2 = base.get(j);
      int x = temp.pos.x;
      int y = temp.pos.y;
      temp.pos = temp2.pos;
      temp2.pos = new Posn(x, y);
      base.set(i, temp2);
      base.set(j, temp);
    }
  }

  //resets the deck by copying by base
  void reset() {
    shuffle();
    for (Card c : base) {
      c.flip(false);
    }
    this.deck = new ArrayList<Card>(base);
  }

  //flips the card that is clicked on 
  void clickCard(Posn pos) {
    for (Card c : deck) {
      if (c.clicked(pos)) {
        c.flip(true);
      }
    }
  }

  //finds the flipped cards
  ArrayList<Card> findFlipped() {
    ArrayList<Card> list = new ArrayList<Card>();
    for (Card c : deck) {
      if (c.faceUp) {
        list.add(c);
      }
    }
    return list;
  }

  //draws the world scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(1280, 720);

    for (Card c : deck) {
      c.draw(scene);
    }

    scene.placeImageXY(new TextImage("Score: " + String.valueOf(score), 30, Color.black), 640, 600);

    return scene;
  }

  //registers mouse clicks of big bang
  public void onMouseClicked(Posn pos) {
    ArrayList<Card> flipped = findFlipped();
    if (flipped.size() == 2) {
      flipped.get(0).flip(false);
      flipped.get(1).flip(false);
    }
    clickCard(pos);
    if (flipped.size() == 2) {
      if (flipped.get(0).match(flipped.get(1))) {
        deck.remove(deck.indexOf(flipped.get(0)));
        deck.remove(deck.indexOf(flipped.get(1)));
      } 
    } 
  }

  //on tick for big bang
  public void onTick() {
    int counter = 0;
    for (Card c : deck) {
      counter += 1;
    }
    this.score = counter / 2;
    if (counter == 0) {
      this.endOfWorld("You Win");
    }

  }

  //end scene for big bang
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(1280, 720);
    scene.placeImageXY(new TextImage("You Win!", 60, Color.black), 640, 480);
    return scene;
  }

  //registers a key event for big bang
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      reset();
    }
  }
}

//examples and tests for the concentration game
class ExampelesConcentration {

  ConcentrationGame w;
  Card card1;
  Card card2;
  Card card3;
  Card card4;
  Card cardmatchw4;
  WorldScene scene1;

  void initData() {
    w = new ConcentrationGame(new Random(14));
    card1 = new Card(12, "♣", new Posn(0,0));
    card2 = new Card(3, "♥", new Posn(0,0));
    card3 = new Card(4, "♠", new Posn(0,0));
    card4 = new Card(11, "♦", new Posn(0,0));
    cardmatchw4 = new Card(11, "♥", new Posn(0,0));
    scene1 = new WorldScene(1280, 720);
  }

  //checker test for flip()
  void testFlip(Tester t) {
    initData();
    t.checkExpect(card1.faceUp, false);
    card1.flip(true);
    t.checkExpect(card1.faceUp, true);
    card1.flip(false);
    t.checkExpect(card1.faceUp, false);
  }

  //checker test for clicked()
  void testClicked(Tester t) {
    initData();
    t.checkExpect(card1.clicked(new Posn(10, 10)), true);
    t.checkExpect(card1.clicked(new Posn(200, 300)), false);
  }

  //checker test for match()
  void testMatch(Tester t) {
    initData();
    t.checkExpect(card4.match(cardmatchw4), true);
    t.checkExpect(card4.match(card1), false);
  }

  //checker test for drawFlipped()
  void testDrawFlipped(Tester t) {
    initData();
    card1.drawFlipped(scene1);
    WorldScene scene2 = new WorldScene(1280, 720);
    scene2.placeImageXY(new RectangleImage(70, 100, OutlineMode.SOLID, Color.GRAY), 0, 0);
    t.checkExpect(scene1, scene2);
  }

  //checker test for drawNormal()
  void testDrawNormal(Tester t) {
    initData();
    card1.drawNormal(scene1);
    WorldScene scene2 = new WorldScene(1280, 720);
    scene2.placeImageXY(new OverlayImage(new TextImage("♣", 24, Color.BLACK), 
        new OverlayOffsetImage(new RotateImage(
            new TextImage(card1.toRankS(12), 15, Color.black), 180),
            -25, -35, new OverlayOffsetImage(new TextImage(card1.toRankS(12), 
                15, Color.black), 25, 35, 
                new RectangleImage(70, 100, OutlineMode.OUTLINE, Color.black)))), 0, 0);
    t.checkExpect(scene1, scene2);
  }

  //checker test for draw()
  void testDraw(Tester t) {
    initData();
    card1.draw(scene1);
    WorldScene scene2 = new WorldScene(1280, 720);
    card1.drawFlipped(scene2);
    t.checkExpect(scene1, scene2);
    initData();
    card2.flip(true);
    card2.draw(scene1);
    WorldScene scene3 = new WorldScene(1280, 720);
    card2.drawNormal(scene3);
    t.checkExpect(scene1, scene3);
  }

  //checker test for toRankS(int)
  void testToRankS(Tester t) {
    initData();
    t.checkExpect(card3.toRankS(card3.rank), "4");
    t.checkExpect(card4.toRankS(card4.rank), "J");
  }

  //checker test for createBase()
  void testCreateBase(Tester t) {
    initData();
    ArrayList<Card> exdeck = new ArrayList<Card>();
    for (int i = 1; i <= 4; i += 1) {
      for (int j = 1; j <= 13; j += 1) {
        String suit;
        if (i == 1) {
          suit = "♣";
        } else if (i == 2) {
          suit = "♦";
        } else if (i == 3) {
          suit = "♥";
        } else if (i == 4) {
          suit = "♠";
        } else {
          suit = "Error";
        }

        int x = 132 + ((j - 1) * 85); 
        int y = 140 + ((i - 1) * 115);
        exdeck.add(new Card(j, suit, new Posn(x, y)));
      }
    }
    w.base = new ArrayList<Card>();
    w.createBase();
    t.checkExpect(w.base, exdeck);
  }

  //checker test for createBase()
  void testShuffle(Tester t) {
    initData();
    ConcentrationGame game1 = new ConcentrationGame(new Random(14));
    game1.shuffle();
    ConcentrationGame game2 = new ConcentrationGame(new Random(14));
    game2.shuffle();
    t.checkExpect(game1, game2);
  }

  //checker test for reset()
  void testReset(Tester t) {
    initData();
    w.onMouseClicked(new Posn(200, 200));
    w.reset();
    t.checkExpect(w.deck, w.base);
  }

  //checker test for clickCard()
  void testClickCard(Tester t) {
    initData();
    ConcentrationGame exgame = new ConcentrationGame(new Random(14));
    Card first = exgame.deck.get(0);
    first.flip(true);
    ConcentrationGame exgame2 = new ConcentrationGame(new Random(14));
    Card first2 = exgame2.deck.get(0);
    exgame2.clickCard(first2.pos);
    t.checkExpect(first.faceUp, first2.faceUp);
  }

  //checker test for findFlipped()
  void testFindFlipped(Tester t) {
    initData();
    ConcentrationGame exgame = new ConcentrationGame(new Random(14));
    Card fourth = exgame.deck.get(4);
    fourth.flip(true);
    ArrayList<Card> exlist = new ArrayList<Card>();
    exlist.add(fourth);
    t.checkExpect(exgame.findFlipped(), exlist);
  }

  //tests the makeScene() method for ConcentrationGame
  void testmakeScene(Tester t) {
    initData();
    ConcentrationGame exgame = new ConcentrationGame(new Random(14));
    ConcentrationGame exgame2 = new ConcentrationGame(new Random(14));
    t.checkExpect(exgame.makeScene(), exgame2.makeScene());
  }

  //tests the onMouseClicked(Posn) method for ConcentrationGame
  void testOnMouseClicked(Tester t) {
    initData();
    ConcentrationGame exgame = new ConcentrationGame(new Random(14));
    ConcentrationGame exgame2 = new ConcentrationGame(new Random(14));
    exgame.onMouseClicked(exgame.deck.get(0).pos);
    exgame2.deck.get(0).flip(true);
    t.checkExpect(exgame2, exgame);
  }

  //tests the onTick() method for ConcentrationGame
  void testOnTick(Tester t) {
    initData();
    w.onTick();
    t.checkExpect(w.score, 26);
  }

  //tests the lastScene(String) method for ConcentrationGame
  void testLastScene(Tester t) {
    initData();
    w.deck = new ArrayList<Card>();
    w.onTick();
    WorldScene finalscene = new WorldScene(1280, 720);
    finalscene.placeImageXY(new TextImage("You Win!", 60, Color.black), 640, 480);
    t.checkExpect(w.lastScene("asdf"), finalscene);
  }

  //tests the onKeyEvent(String) method for ConcentrationGame
  void testOnKeyEvent(Tester t) {
    initData();
    w.deck.get(25).flip(true);
    w.onKeyEvent("r");
    t.checkExpect(w.deck.get(25).faceUp, false);
    
    initData();
    w.deck.get(25).flip(true);
    w.onKeyEvent("k");
    t.checkExpect(w.deck.get(25).faceUp, true);
  }

  //bigbang, starts the game
  void testGame(Tester t) {
    initData();
    w.bigBang(1280, 720, 0.1);
  }

}