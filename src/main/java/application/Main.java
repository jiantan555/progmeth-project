package application;

import model.card.NumberCard;
import model.engine.GameEngine;
import model.operator.*;
import model.player.Player;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private GameEngine engine;

    // ระบบเวลาและสถานะเกม
    private Timeline turnTimer;
    private Timeline gameTimer;
    private int maxTurnSeconds = 20;
    private int turnSecondsRemaining = 20;
    private int totalGameSeconds = 180;
    private int initialCardsCount = 5;
    private boolean isAutoSkipping = false;
    private boolean isGameOver = false;

    // UI Elements
    private FlowPane p1HandPanel, p2HandPanel;
    private VBox leftPanel, rightPanel;
    private HBox expressionPanel;
    private Label targetCardLabel, resultLabel, turnTimerLabel, totalTimerLabel, turnLabel;
    private Button btnSubmit, btnSkip, btnChangeTarget, btnRestart;

    // Operator Buttons Arrays
    private List<Button> p1Operators = new ArrayList<>();
    private List<Button> p2Operators = new ArrayList<>();

    // Settings Elements
    private CheckBox chkPlusMinus;
    private CheckBox chkParity;
    private CheckBox chkMathOrder;
    private ComboBox<Integer> timeComboBox;
    private ComboBox<Integer> turnTimeComboBox;
    private ComboBox<Integer> initialCardsComboBox;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f4f4f4;");

        // --- ส่วนการตั้งค่าและปุ่มด้านบน (Top Settings Bar) แบ่งเป็น 2 แถว ---
        VBox topSettingsContainer = new VBox(10);
        topSettingsContainer.setAlignment(Pos.CENTER);
        topSettingsContainer.setPadding(new Insets(10));
        topSettingsContainer.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbb; -fx-border-radius: 5px;");

        HBox topRow1 = new HBox(15);
        topRow1.setAlignment(Pos.CENTER);

        btnRestart = new Button("Restart Game / เล่นใหม่");
        btnRestart.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRestart.setOnAction(e -> handleRestart());

        Label timeConfigLabel = new Label("เวลาเกม (นาที):");
        timeComboBox = new ComboBox<>();
        timeComboBox.getItems().addAll(1, 3, 5, 10);
        timeComboBox.setValue(3);

        Label turnTimeLabel = new Label("เวลาเทิร์น (วิ):");
        turnTimeComboBox = new ComboBox<>();
        turnTimeComboBox.getItems().addAll(20, 30, 45, 60);
        turnTimeComboBox.setValue(20);
        turnTimeComboBox.setOnAction(e -> {
            maxTurnSeconds = turnTimeComboBox.getValue();
            if (!isGameOver && !isAutoSkipping) {
                turnSecondsRemaining = maxTurnSeconds;
                turnTimerLabel.setText("Turn Time: " + turnSecondsRemaining + "s");
            }
        });

        Label initCardsLabel = new Label("เริ่มแจกไพ่ (ใบ):");
        initialCardsComboBox = new ComboBox<>();
        initialCardsComboBox.getItems().addAll(5, 7, 10, 15);
        initialCardsComboBox.setValue(5);
        initialCardsComboBox.setOnAction(e -> {
            initialCardsCount = initialCardsComboBox.getValue();
        });

        topRow1.getChildren().addAll(btnRestart, timeConfigLabel, timeComboBox, turnTimeLabel, turnTimeComboBox, initCardsLabel, initialCardsComboBox);

        HBox topRow2 = new HBox(20);
        topRow2.setAlignment(Pos.CENTER);

        chkPlusMinus = new CheckBox("อนุญาต +/- 1 (ถ้าสีเดียวกัน)");
        chkPlusMinus.setOnAction(e -> {
            if(engine != null) {
                engine.ruleAllowPlusMinusOne = chkPlusMinus.isSelected();
                updateUI();
            }
        });

        chkParity = new CheckBox("อนุญาต คู่/คี่ เหมือนกัน (ถ้าสีเดียวกัน)");
        chkParity.setOnAction(e -> {
            if(engine != null) {
                engine.ruleAllowSameParity = chkParity.isSelected();
                updateUI();
            }
        });

        chkMathOrder = new CheckBox("ใช้หลักคณิตศาสตร์ (ทำ x, ÷ ก่อน)");
        chkMathOrder.setOnAction(e -> {
            if(engine != null) {
                engine.useMathematicalOrder = chkMathOrder.isSelected();
                updateUI();
            }
        });

        totalTimerLabel = new Label("Total Game Time: 03:00");
        totalTimerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalTimerLabel.setTextFill(javafx.scene.paint.Color.DARKBLUE);

        topRow2.getChildren().addAll(chkPlusMinus, chkParity, chkMathOrder, totalTimerLabel);
        topSettingsContainer.getChildren().addAll(topRow1, topRow2);
        root.setTop(topSettingsContainer);

        // โหลด Engine ไว้ใช้เตรียมสร้าง UI ชั่วคราว
        Player p1 = new Player("Player 1 (Left)");
        Player p2 = new Player("Player 2 (Right)");
        engine = new GameEngine(p1, p2, initialCardsCount);

        // --- ส่วนผู้เล่นฝั่งซ้าย ---
        leftPanel = new VBox(10);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPrefWidth(250);
        leftPanel.setStyle("-fx-background-color: #e6f7ff; -fx-padding: 10px; -fx-border-color: #ccc;");
        Label l1 = new Label("Player 1 Hand");
        l1.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // แถบปุ่มเครื่องหมายสำหรับ P1
        HBox p1OpBox = new HBox(5);
        p1OpBox.setAlignment(Pos.CENTER);
        Operator[] ops1 = {new AddOperator(), new SubtractOperator(), new MultiplyOperator(), new DivideOperator()};
        for(Operator op : ops1) {
            // ส่งหมายเลขผู้เล่น (1) ไปแทนการส่ง Object Player เพื่อแก้บั๊กข้อมูลเก่าตกค้าง
            Button b = createOperatorButton(op, 1);
            p1Operators.add(b);
            p1OpBox.getChildren().add(b);
        }

        p1HandPanel = new FlowPane(5, 5);
        ScrollPane s1 = new ScrollPane(p1HandPanel);
        s1.setFitToWidth(true);
        s1.setPrefHeight(400);
        leftPanel.getChildren().addAll(l1, p1OpBox, s1);

        // --- ส่วนผู้เล่นฝั่งขวา ---
        rightPanel = new VBox(10);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPrefWidth(250);
        rightPanel.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 10px; -fx-border-color: #ccc;");
        Label l2 = new Label("Player 2 Hand");
        l2.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // แถบปุ่มเครื่องหมายสำหรับ P2
        HBox p2OpBox = new HBox(5);
        p2OpBox.setAlignment(Pos.CENTER);
        Operator[] ops2 = {new AddOperator(), new SubtractOperator(), new MultiplyOperator(), new DivideOperator()};
        for(Operator op : ops2) {
            // ส่งหมายเลขผู้เล่น (2) ไปแทน
            Button b = createOperatorButton(op, 2);
            p2Operators.add(b);
            p2OpBox.getChildren().add(b);
        }

        p2HandPanel = new FlowPane(5, 5);
        ScrollPane s2 = new ScrollPane(p2HandPanel);
        s2.setFitToWidth(true);
        s2.setPrefHeight(400);
        rightPanel.getChildren().addAll(l2, p2OpBox, s2);

        // --- ส่วนตรงกลาง ---
        VBox centerPanel = new VBox(20);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setPadding(new Insets(20));

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        turnTimerLabel = new Label("Turn Time: " + maxTurnSeconds + "s");
        turnTimerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        turnTimerLabel.setTextFill(javafx.scene.paint.Color.RED);

        Label targetTitle = new Label("Current Target Card");
        targetCardLabel = new Label();
        targetCardLabel.setPrefSize(100, 140);
        targetCardLabel.setAlignment(Pos.CENTER);

        btnSkip = new Button("Skip Turn (Draw 1)");
        btnSkip.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSkip.setOnAction(e -> handleSkip());

        btnChangeTarget = new Button("Change Center Card");
        btnChangeTarget.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-weight: bold;");
        btnChangeTarget.setOnAction(e -> handleChangeTarget());

        HBox actionBox = new HBox(10, btnSkip, btnChangeTarget);
        actionBox.setAlignment(Pos.CENTER);

        Label expTitle = new Label("Expression Area (Click to Undo)");
        expressionPanel = new HBox(5);
        expressionPanel.setAlignment(Pos.CENTER);
        expressionPanel.setMinHeight(150);
        expressionPanel.setStyle("-fx-border-color: #888; -fx-background-color: #fff; -fx-padding: 10px;");
        ScrollPane expScroll = new ScrollPane(expressionPanel);
        expScroll.setFitToHeight(true);

        resultLabel = new Label("Current Result: 0");
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        btnSubmit = new Button("SUBMIT !");
        btnSubmit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        btnSubmit.setPrefSize(150, 50);
        btnSubmit.setOnAction(e -> handleSubmit());

        centerPanel.getChildren().addAll(
                turnLabel, turnTimerLabel,
                targetTitle, targetCardLabel, actionBox,
                expTitle, expScroll,
                resultLabel, btnSubmit
        );

        root.setLeft(leftPanel);
        root.setRight(rightPanel);
        root.setCenter(centerPanel);

        // เริ่มเกมครั้งแรก (สร้างผู้เล่นตัวจริง)
        handleRestart();

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Math UNO Duel - Full Version");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleRestart() {
        if (turnTimer != null) turnTimer.stop();
        if (gameTimer != null) gameTimer.stop();

        isGameOver = false;
        isAutoSkipping = false;

        Player p1 = new Player("Player 1 (Left)");
        Player p2 = new Player("Player 2 (Right)");
        engine = new GameEngine(p1, p2, initialCardsCount);

        engine.ruleAllowPlusMinusOne = chkPlusMinus.isSelected();
        engine.ruleAllowSameParity = chkParity.isSelected();
        engine.useMathematicalOrder = chkMathOrder.isSelected();

        maxTurnSeconds = turnTimeComboBox.getValue();
        turnSecondsRemaining = maxTurnSeconds;
        totalGameSeconds = timeComboBox.getValue() * 60;

        turnLabel.setTextFill(javafx.scene.paint.Color.BLACK);
        turnTimerLabel.setText("Turn Time: " + maxTurnSeconds + "s");
        updateTotalTimeLabel();

        btnSubmit.setDisable(false);
        btnSkip.setDisable(false);
        btnChangeTarget.setDisable(false);

        setupTimers();
        updateUI();
    }

    private void setupTimers() {
        turnTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!isAutoSkipping && !isGameOver) {
                turnSecondsRemaining--;
                turnTimerLabel.setText("Turn Time: " + turnSecondsRemaining + "s");
                if (turnSecondsRemaining <= 0) {
                    handleSkip();
                }
            }
        }));
        turnTimer.setCycleCount(Timeline.INDEFINITE);
        turnTimer.play();

        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!isGameOver) {
                totalGameSeconds--;
                updateTotalTimeLabel();

                if (totalGameSeconds <= 0) {
                    handleTimeOutGameOver();
                }
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void updateTotalTimeLabel() {
        int minutes = totalGameSeconds / 60;
        int seconds = totalGameSeconds % 60;
        totalTimerLabel.setText(String.format("Total Game Time: %02d:%02d", minutes, seconds));
    }

    private void handleTimeOutGameOver() {
        isGameOver = true;
        turnTimer.stop();
        gameTimer.stop();

        int p1Cards = engine.getPlayer1().getHand().size();
        int p2Cards = engine.getPlayer2().getHand().size();

        String winnerMessage;
        if (p1Cards < p2Cards) {
            winnerMessage = engine.getPlayer1().getName() + " WINS (Fewer Cards)!";
        } else if (p2Cards < p1Cards) {
            winnerMessage = engine.getPlayer2().getName() + " WINS (Fewer Cards)!";
        } else {
            winnerMessage = "IT'S A TIE! (Equal Cards)";
        }

        lockGameWithWinner(winnerMessage);
    }

    private void handleSkip() {
        if (isAutoSkipping || isGameOver) return;
        engine.timeOut();
        turnSecondsRemaining = maxTurnSeconds;
        turnTimerLabel.setText("Turn Time: " + maxTurnSeconds + "s");
        updateUI();
    }

    private void handleChangeTarget() {
        if (isAutoSkipping || isGameOver) return;
        engine.setTargetCard(engine.generateRandomCard());
        engine.undoFromIndex(0);
        turnSecondsRemaining = maxTurnSeconds;
        turnTimerLabel.setText("Turn Time: " + maxTurnSeconds + "s");
        updateUI();
    }

    private Button createCardButton(NumberCard card, Player owner) {
        Button btn = new Button(card.getDisplayText());
        btn.setStyle(card.getStyle());
        btn.setPrefSize(60, 90);

        btn.setOnAction(e -> {
            if (!isGameOver && engine.getCurrentPlayer() == owner && engine.canPlayCard(card) && !isAutoSkipping) {
                owner.removeCard(card);
                engine.getExpressionList().add(card);
                updateUI();
            }
        });
        return btn;
    }

    // อัปเดตเมธอดนี้ให้รับหมายเลขผู้เล่น (1 หรือ 2) แทนการรับ Object ผู้เล่นโดยตรง
    private Button createOperatorButton(Operator op, int playerNum) {
        Button btn = new Button(op.getSymbol());
        btn.setPrefSize(50, 50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setOnAction(e -> {
            // ดึงผู้เล่นตัวจริงจาก Engine รอบปัจจุบันมาตรวจสอบ
            Player owner = (playerNum == 1) ? engine.getPlayer1() : engine.getPlayer2();

            if (isGameOver || isAutoSkipping || engine.getCurrentPlayer() != owner) return;

            if (engine.canPlayOperator()) {
                engine.getExpressionList().add(op);
                updateUI();
            } else if (engine.canReplaceOperator()) {
                engine.replaceLastOperator(op);
                updateUI();
            }
        });
        return btn;
    }

    private void updateUI() {
        if (isAutoSkipping || isGameOver) return;

        NumberCard target = engine.getTargetCard();
        targetCardLabel.setText(target.getDisplayText());
        targetCardLabel.setStyle(target.getStyle() + " -fx-font-size: 36px;");

        boolean isP1Turn = engine.getCurrentPlayer() == engine.getPlayer1();

        if (isP1Turn) {
            turnLabel.setText("TURN: " + engine.getPlayer1().getName());
            leftPanel.setDisable(false);
            rightPanel.setDisable(true);
        } else {
            turnLabel.setText("TURN: " + engine.getPlayer2().getName());
            leftPanel.setDisable(true);
            rightPanel.setDisable(false);
        }

        // --- อัปเดตการแสดงผลปุ่มเครื่องหมาย (เปิดให้กดถ้าลงต่อได้ หรือ สลับได้) ---
        boolean canOpOrReplace = engine.canPlayOperator() || engine.canReplaceOperator();
        for (Button b : p1Operators) {
            b.setDisable(!isP1Turn || !canOpOrReplace);
        }
        for (Button b : p2Operators) {
            b.setDisable(isP1Turn || !canOpOrReplace);
        }

        // จัดเรียงไพ่ P1
        p1HandPanel.getChildren().clear();
        List<Button> p1Playable = new ArrayList<>();
        List<Button> p1Unplayable = new ArrayList<>();
        boolean p1CanPlayAny = false;

        for (NumberCard c : engine.getPlayer1().getHand()) {
            Button btn = createCardButton(c, engine.getPlayer1());
            if (isP1Turn) {
                if (engine.canPlayCard(c)) {
                    p1Playable.add(btn);
                    p1CanPlayAny = true;
                } else {
                    btn.setOpacity(0.4);
                    btn.setDisable(true);
                    p1Unplayable.add(btn);
                }
            } else {
                btn.setDisable(true);
                p1Playable.add(btn);
            }
        }
        p1HandPanel.getChildren().addAll(p1Playable);
        p1HandPanel.getChildren().addAll(p1Unplayable);

        // จัดเรียงไพ่ P2
        p2HandPanel.getChildren().clear();
        List<Button> p2Playable = new ArrayList<>();
        List<Button> p2Unplayable = new ArrayList<>();
        boolean p2CanPlayAny = false;

        for (NumberCard c : engine.getPlayer2().getHand()) {
            Button btn = createCardButton(c, engine.getPlayer2());
            if (!isP1Turn) {
                if (engine.canPlayCard(c)) {
                    p2Playable.add(btn);
                    p2CanPlayAny = true;
                } else {
                    btn.setOpacity(0.4);
                    btn.setDisable(true);
                    p2Unplayable.add(btn);
                }
            } else {
                btn.setDisable(true);
                p2Playable.add(btn);
            }
        }
        p2HandPanel.getChildren().addAll(p2Playable);
        p2HandPanel.getChildren().addAll(p2Unplayable);

        // ตรวจสอบ Auto-Skip
        if (engine.getExpressionList().isEmpty()) {
            boolean currentPlayerCanPlay = isP1Turn ? p1CanPlayAny : p2CanPlayAny;
            if (!currentPlayerCanPlay) {
                isAutoSkipping = true;
                turnLabel.setText("No playable cards! Auto-skipping...");
                btnSkip.setDisable(true);
                btnChangeTarget.setDisable(true);

                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(e -> {
                    isAutoSkipping = false;
                    btnSkip.setDisable(false);
                    btnChangeTarget.setDisable(false);
                    handleSkip();
                });
                pause.play();
                return;
            }
        }

        // วาดสมการตรงกลางใหม่ (พร้อมระบบ Undo)
        expressionPanel.getChildren().clear();
        for (int i = 0; i < engine.getExpressionList().size(); i++) {
            Object item = engine.getExpressionList().get(i);
            Button itemBtn = new Button();
            final int index = i;

            if (item instanceof NumberCard) {
                NumberCard c = (NumberCard) item;
                itemBtn.setText(c.getDisplayText());
                itemBtn.setStyle(c.getStyle());
                itemBtn.setPrefSize(60, 90);
            } else if (item instanceof Operator) {
                Operator op = (Operator) item;
                itemBtn.setText(op.getSymbol());
                itemBtn.setStyle("-fx-background-color: #ddd; -fx-font-size: 20px;");
                itemBtn.setPrefSize(50, 50);
            }

            itemBtn.setOnAction(e -> {
                if (!isAutoSkipping && !isGameOver) {
                    engine.undoFromIndex(index);
                    updateUI();
                }
            });

            expressionPanel.getChildren().add(itemBtn);
        }

        double res = engine.calculateCurrentResult();
        if (res == (long) res) {
            resultLabel.setText("Current Result: " + (long) res);
        } else {
            resultLabel.setText(String.format("Current Result: %.2f", res));
        }
    }

    private void handleSubmit() {
        if (isAutoSkipping || isGameOver) return;

        if (engine.submitExpression()) {
            turnSecondsRemaining = maxTurnSeconds;
            turnTimerLabel.setText("Turn Time: " + maxTurnSeconds + "s");
            updateUI();

            if (engine.getPlayer1().getHand().isEmpty()) {
                lockGameWithWinner(engine.getPlayer1().getName() + " WINS (Out of Cards)!");
            } else if (engine.getPlayer2().getHand().isEmpty()) {
                lockGameWithWinner(engine.getPlayer2().getName() + " WINS (Out of Cards)!");
            }
        }
    }

    private void lockGameWithWinner(String message) {
        isGameOver = true;
        turnTimer.stop();
        gameTimer.stop();

        turnLabel.setText(message);
        turnLabel.setTextFill(javafx.scene.paint.Color.GREEN);

        leftPanel.setDisable(true);
        rightPanel.setDisable(true);
        btnSubmit.setDisable(true);
        btnSkip.setDisable(true);
        btnChangeTarget.setDisable(true);

        for (Button b : p1Operators) b.setDisable(true);
        for (Button b : p2Operators) b.setDisable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}