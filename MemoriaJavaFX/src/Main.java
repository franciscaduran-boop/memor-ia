// src/Main.java
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main extends Application {

    private final int FILAS = 4;
    private final int COLUMNAS = 4;
    private Card primeraSeleccion = null;
    private boolean bloqueado = false;
    private int paresEncontrados = 0;

    private Label puntosLabel;
    private Label tiempoLabel;
    private int puntos = 0;
    private int segundos = 0;
    private Timeline reloj;

    @Override
    public void start(Stage stage) {
        // Top bar (similar a la imagen)
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(20);
        topBar.setStyle("-fx-background-color: #cceaff;");
        Label acerca = new Label("Acerca de");
        Label ranking = new Label("Ranking");
        Label valoranos = new Label("¡Valóranos!");
        Label tema = new Label("Tema");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox loginBox = new HBox(new Label("Iniciar sesión"), new Label("  "), new Label("Registrate"));
        loginBox.setSpacing(10);
        topBar.getChildren().addAll(acerca, ranking, valoranos, tema, espacio, loginBox);

        // Logo
        VBox logoBox = new VBox();
        Label logo1 = new Label("Memor-IA");
        logo1.setFont(Font.font(32));
        logo1.setTextFill(Color.web("#20377a"));
        Label subtitulo = new Label("Juegos interactivos");
        subtitulo.setFont(Font.font(14));
        subtitulo.setTextFill(Color.web("#20377a"));
        logoBox.setAlignment(Pos.CENTER);
        logoBox.getChildren().addAll(logo1, subtitulo);

        // Menu principal (Modos / Diseños / Dificultad)
        HBox menuMain = new HBox();
        menuMain.setAlignment(Pos.CENTER);
        menuMain.setPadding(new Insets(10));
        menuMain.setSpacing(40);
        menuMain.setStyle("-fx-background-color: #073d68; -fx-background-radius: 30;");
        Button modosBtn = new Button("Modos");
        Button disenosBtn = new Button("Diseños");
        Button dificultadBtn = new Button("Dificultad");
        estilosBotonMenu(modosBtn);
        estilosBotonMenu(disenosBtn);
        estilosBotonMenu(dificultadBtn);
        menuMain.getChildren().addAll(modosBtn, disenosBtn, dificultadBtn);

        // Info (tiempo/puntos)
        HBox infoBar = new HBox();
        infoBar.setPadding(new Insets(10));
        infoBar.setSpacing(20);
        infoBar.setAlignment(Pos.CENTER);
        tiempoLabel = new Label("TIEMPO: 00:00");
        puntosLabel = new Label("PUNTOS: 0");
        tiempoLabel.setFont(Font.font(16));
        puntosLabel.setFont(Font.font(16));
        infoBar.getChildren().addAll(tiempoLabel, puntosLabel);

        // Título de tablero
        Label titulo = new Label("Para empezar selecciona un modo");
        titulo.setFont(Font.font(22));
        titulo.setTextFill(Color.web("#20377a"));
        titulo.setPadding(new Insets(10));
        titulo.setAlignment(Pos.CENTER);

        // Contenedor del tablero
        GridPane tablero = new GridPane();
        tablero.setHgap(12);
        tablero.setVgap(12);
        tablero.setAlignment(Pos.CENTER);
        tablero.setPadding(new Insets(10));

        // Crear cartas y añadir al tablero
        List<String> simbolos = generarSimbolos(FILAS * COLUMNAS / 2);
        List<String> pairList = new ArrayList<>(simbolos);
        pairList.addAll(simbolos);
        Collections.shuffle(pairList);

        int idx = 0;
        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                String simb = pairList.get(idx++);
                Card carta = new Card(simb);
                carta.setOnMouseClicked(ev -> {
                    if (ev.getButton() != MouseButton.PRIMARY) return;
                    manejarClickCarta(carta);
                });
                tablero.add(carta, c, r);
            }
        }

        // Layout principal
        VBox root = new VBox(topBar, logoBox, menuMain, titulo, infoBar, tablero);
        root.setSpacing(8);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #e8f4ff;");
        Scene scene = new Scene(root, 900, 700);

        // Iniciar reloj
        iniciarReloj();

        stage.setTitle("Memor-IA - Memoria");
        stage.setScene(scene);
        stage.show();
    }

    private void iniciarReloj() {
        segundos = 0;
        reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundos++;
            tiempoLabel.setText("TIEMPO: " + formatSegundos(segundos));
        }));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    private String formatSegundos(int s) {
        int mm = s / 60;
        int ss = s % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    private void manejarClickCarta(Card carta) {
        if (bloqueado) return;
        if (carta.isMatched() || carta.isFaceUp()) return;

        carta.flip();

        if (primeraSeleccion == null) {
            primeraSeleccion = carta;
            return;
        } else {
            // comparar
            if (primeraSeleccion.getValor().equals(carta.getValor())) {
                // acierto
                primeraSeleccion.setMatched(true);
                carta.setMatched(true);
                primeraSeleccion = null;
                paresEncontrados++;
                puntos += 10;
                puntosLabel.setText("PUNTOS: " + puntos);

                if (paresEncontrados == (FILAS * COLUMNAS) / 2) {
                    // juego terminado
                    reloj.stop();
                    tituloVictoria();
                }
            } else {
                // fallo: bloquear y desvoltear después de un tiempo
                bloqueado = true;
                PauseTransition wait = new PauseTransition(Duration.seconds(0.9));
                wait.setOnFinished(e -> {
                    primeraSeleccion.flip();
                    carta.flip();
                    primeraSeleccion = null;
                    bloqueado = false;
                });
                wait.play();
                puntos = Math.max(0, puntos - 2);
                puntosLabel.setText("PUNTOS: " + puntos);
            }
        }
    }

    private void tituloVictoria() {
        // Mensaje simple: actualizar el título de la ventana o hacer algo visible
        System.out.println("¡Ganaste! Puntos: " + puntos + " Tiempo: " + formatSegundos(segundos));
    }

    private void estilosBotonMenu(Button b) {
        b.setFont(Font.font(18));
        b.setTextFill(Color.WHITE);
        b.setStyle("-fx-background-color: transparent;");
    }

    private List<String> generarSimbolos(int cantidadParejas) {
        // Usamos emojis como "imágenes" simples para evitar recursos externos.
        String[] pool = {
                "🍎","🍋","🍇","🍓","🍉","🍒","🍑","🍍",
                "🐶","🐱","🐭","🐼","🦊","🐻","🐵","🦁",
                "⚽","🏀","🏈","🎾","🎲","🎯","🎮","🎹"
        };

        List<String> salida = new ArrayList<>();
        for (int i = 0; i < cantidadParejas; i++) {
            salida.add(pool[i % pool.length]);
        }
        return salida;
    }

    // Clase interna Card
    private class Card extends StackPane {
        private final String valor;
        private final Rectangle fondo;
        private final Label frente;
        private boolean faceUp = false;
        private boolean matched = false;

        Card(String valor) {
            this.valor = valor;
            setPrefSize(90, 90);

            fondo = new Rectangle(90, 90);
            fondo.setArcWidth(12);
            fondo.setArcHeight(12);
            fondo.setFill(Color.web("#ffffff"));
            fondo.setStroke(Color.web("#d0d0d0"));

            frente = new Label(valor);
            frente.setFont(Font.font(36));
            frente.setVisible(false);

            Label dorso = new Label(""); // dorso vacío o con ícono
            dorso.setFont(Font.font(24));
            dorso.setText(" "); // apariencia limpia

            setAlignment(Pos.CENTER);
            getChildren().addAll(fondo, dorso, frente);
        }

        String getValor() { return valor; }
        boolean isFaceUp() { return faceUp; }
        boolean isMatched() { return matched; }
        void setMatched(boolean m) {
            matched = m;
            if (m) {
                // destacar carta emparejada
                fondo.setFill(Color.web("#dff6e0"));
                fondo.setStroke(Color.web("#60a86b"));
            }
        }

        void flip() {
            ScaleTransition st1 = new ScaleTransition(Duration.millis(120), this);
            st1.setFromX(1);
            st1.setToX(0);
            st1.setOnFinished(e -> {
                // intercambiar vista
                faceUp = !faceUp;
                frente.setVisible(faceUp);
                // ocultar o mostrar dorso: el segundo hijo es dorso
                if (getChildren().size() >= 2) {
                    getChildren().get(1).setVisible(!faceUp);
                }
                ScaleTransition st2 = new ScaleTransition(Duration.millis(120), this);
                st2.setFromX(0);
                st2.setToX(1);
                st2.play();
            });
            st1.play();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
