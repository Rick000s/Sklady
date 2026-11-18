package com.example.sklady.gui;

import com.example.sklady.enums.eTypProhl;
import com.example.sklady.managers.AgendaSklady;
import com.example.sklady.models.Produkt;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StromOkno {

    private static final double ROZESTUP_X = 40;
    private static final double ROZESTUP_Y = 60;
    private static final double POLOMER = 17;

    private static final Color BARVA_UZEL = Color.web("#F1EFE8");
    private static final Color BARVA_CESTA = Color.web("#CECBF6");
    private static final Color BARVA_ODEBRAT = Color.web("#F5C4B3");
    private static final Color BARVA_NALEZENO = Color.web("#9FE1CB");

    private static class Uzel {
        String klic;
        String levy;
        String pravy;
        int hloubka;
        double x;
        double y;
    }

    private final AgendaSklady agenda;
    private final Runnable poZmene;

    private final Stage stage = new Stage();
    private final Pane platno = new Pane();
    private final ScrollPane scroll = new ScrollPane(new Group(platno));
    private final Label lblZoom = new Label();
    private double zoom = 1;
    private double sirkaObsahu = 900;
    private final TextField txtId = new TextField();
    private final Button btnProvest = new Button("Provést odebrání");
    private final Label lblInfo = new Label();
    private final Label lblZprava = new Label();
    private final Label lblPoradi = new Label();

    private final Map<String, Uzel> uzly = new HashMap<>();
    private final Map<String, Color> zvyrazneni = new HashMap<>();
    private String koren;
    private String kOdebrani;
    private Timeline animace;

    public StromOkno(AgendaSklady agenda, Runnable poZmene) {
        this.agenda = agenda;
        this.poZmene = poZmene;

        txtId.setPromptText("např. P005");
        txtId.setPrefWidth(110);
        txtId.setOnAction(e -> najdi());

        Button btnNajdi = new Button("Najdi");
        btnNajdi.setOnAction(e -> najdi());
        Button btnOdeber = new Button("Odeber");
        btnOdeber.setOnAction(e -> pripravOdebrani());
        btnProvest.setOnAction(e -> provedOdebrani());
        ukazProvest(false);

        Button btnHloubka = new Button("Do hloubky");
        btnHloubka.setOnAction(e -> projdi(eTypProhl.HLOUBKA));
        Button btnSirka = new Button("Do šířky");
        btnSirka.setOnAction(e -> projdi(eTypProhl.SIRKA));
        Button btnVybuduj = new Button("Vybuduj");
        btnVybuduj.setOnAction(e -> vybuduj());

        Button btnMensi = new Button("−");
        btnMensi.setOnAction(e -> nastavZoom(zoom / 1.2));
        Button btnVetsi = new Button("+");
        btnVetsi.setOnAction(e -> nastavZoom(zoom * 1.2));
        Button btnCely = new Button("Celý strom");
        btnCely.setOnAction(e -> nastavZoom(zoomNaSirku()));

        HBox ovladani = new HBox(8, new Label("ID:"), txtId, btnNajdi, btnOdeber, btnProvest,
                new Separator(Orientation.VERTICAL), btnHloubka, btnSirka, btnVybuduj,
                new Separator(Orientation.VERTICAL), btnMensi, lblZoom, btnVetsi, btnCely,
                new Separator(Orientation.VERTICAL), lblInfo);
        ovladani.setAlignment(Pos.CENTER_LEFT);
        ovladani.setPadding(new Insets(10));

        lblZprava.setWrapText(true);
        lblPoradi.setWrapText(true);
        lblPoradi.setFont(Font.font("Monospaced", 12));
        VBox spodek = new VBox(6, lblZprava, lblPoradi);
        spodek.setPadding(new Insets(10));

        BorderPane rozlozeni = new BorderPane();
        rozlozeni.setTop(ovladani);
        rozlozeni.setCenter(scroll);
        rozlozeni.setBottom(spodek);

        scroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                nastavZoom(e.getDeltaY() > 0 ? zoom * 1.1 : zoom / 1.1);
                e.consume();
            }
        });

        stage.setTitle("Strom");
        stage.setScene(new Scene(rozlozeni, 1150, 650));
        stage.setOnHidden(e -> zastavAnimaci());
        nastavZoom(1);
    }

    public void zobraz() {
        prekresli();
        stage.show();
        stage.toFront();
        Platform.runLater(this::prizpusobit);
    }

    private void nastavZoom(double novyZoom) {
        zoom = Math.max(0.2, Math.min(2, novyZoom));
        platno.setScaleX(zoom);
        platno.setScaleY(zoom);
        lblZoom.setText(Math.round(zoom * 100) + " %");
    }

    private double zoomNaSirku() {
        double viditelna = scroll.getViewportBounds().getWidth();
        if (viditelna <= 0) viditelna = 1100;
        return Math.min(1, (viditelna - 10) / sirkaObsahu);
    }

    private void prizpusobit() {
        nastavZoom(Math.max(0.6, zoomNaSirku()));
        Platform.runLater(this::vycentrujKoren);
    }

    private void vycentrujKoren() {
        Uzel u = koren == null ? null : uzly.get(koren);
        double viditelna = scroll.getViewportBounds().getWidth();
        double sirka = sirkaObsahu * zoom;
        if (u == null || sirka <= viditelna) {
            scroll.setHvalue(0);
            return;
        }
        double h = (u.x * zoom - viditelna / 2) / (sirka - viditelna);
        scroll.setHvalue(Math.max(0, Math.min(1, h)));
        scroll.setVvalue(0);
    }

    public boolean jeOtevrene() {
        return stage.isShowing();
    }

    public void prekresli() {
        nactiStrom();
        kresli();
        lblInfo.setText("uzlů: " + uzly.size() + " | výška: " + agenda.vyskaStromu());
    }

    private void nactiStrom() {
        uzly.clear();
        koren = null;

        agenda.projdiStrom((klic, levy, pravy, hloubka) -> {
            Uzel u = new Uzel();
            u.klic = klic;
            u.levy = levy;
            u.pravy = pravy;
            u.hloubka = hloubka;
            uzly.put(klic, u);
            if (koren == null) koren = klic;
        });

        List<String> serazene = new ArrayList<>(uzly.keySet());
        Collections.sort(serazene);
        for (int i = 0; i < serazene.size(); i++) {
            Uzel u = uzly.get(serazene.get(i));
            u.x = 30 + i * ROZESTUP_X;
            u.y = 35 + u.hloubka * ROZESTUP_Y;
        }
    }

    private void kresli() {
        platno.getChildren().clear();

        if (uzly.isEmpty()) {
            Text prazdny = new Text(30, 40, "Strom je prázdný (root = null)");
            platno.getChildren().add(prazdny);
            sirkaObsahu = 900;
            platno.setPrefSize(sirkaObsahu, 100);
            return;
        }

        double maxX = 0;
        double maxY = 0;

        for (Uzel u : uzly.values()) {
            for (String dite : new String[]{u.levy, u.pravy}) {
                if (dite == null) continue;
                Uzel d = uzly.get(dite);
                Line hrana = new Line(u.x, u.y, d.x, d.y);
                hrana.setStroke(Color.web("#888780"));
                platno.getChildren().add(hrana);
            }
        }

        for (Uzel u : uzly.values()) {
            Color barva = zvyrazneni.getOrDefault(u.klic, BARVA_UZEL);
            Circle kruh = new Circle(u.x, u.y, POLOMER, barva);
            kruh.setStroke(Color.web("#5F5E5A"));
            kruh.setStrokeWidth(zvyrazneni.containsKey(u.klic) ? 2 : 1);

            Text text = new Text(u.klic);
            text.setFont(Font.font(10));
            text.setX(u.x - text.getLayoutBounds().getWidth() / 2);
            text.setY(u.y + 4);
            text.setMouseTransparent(true);

            Produkt p = agenda.najdi(u.klic);
            if (p != null) {
                Tooltip.install(kruh, new Tooltip(p.getId() + " | " + p.getNazev() + " | " + p.getSklad()));
            }

            platno.getChildren().addAll(kruh, text);
            maxX = Math.max(maxX, u.x);
            maxY = Math.max(maxY, u.y);
        }

        sirkaObsahu = Math.max(900, maxX + 40);
        platno.setPrefSize(sirkaObsahu, maxY + 40);
    }

    private List<String> cesta(String id) {
        List<String> cesta = new ArrayList<>();
        String aktualni = koren;

        while (aktualni != null) {
            cesta.add(aktualni);
            int c = id.compareTo(aktualni);
            if (c == 0) break;
            if (c < 0) aktualni = uzly.get(aktualni).levy;
            else aktualni = uzly.get(aktualni).pravy;
        }
        return cesta;
    }

    private String nactiId() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            lblZprava.setText("Zadej ID.");
            return null;
        }
        return id;
    }

    private void najdi() {
        String id = nactiId();
        if (id == null) return;
        zastavAnimaci();
        zvyrazneni.clear();
        ukazProvest(false);

        List<String> cesta = cesta(id);
        for (String k : cesta) zvyrazneni.put(k, BARVA_CESTA);

        boolean nalezeno = !cesta.isEmpty() && cesta.get(cesta.size() - 1).equals(id);
        if (nalezeno) {
            zvyrazneni.put(id, BARVA_NALEZENO);
            lblZprava.setText("Nalezeno za " + cesta.size() + " kroků: " + String.join(" → ", cesta)
                    + ". " + popis(agenda.najdi(id)));
        } else {
            lblZprava.setText("Cesta " + (cesta.isEmpty() ? "(prázdný strom)" : String.join(" → ", cesta))
                    + " skončila na null: " + id + " ve stromu není, najdi vrací null.");
        }
        kresli();
    }

    private void pripravOdebrani() {
        String id = nactiId();
        if (id == null) return;
        zastavAnimaci();
        zvyrazneni.clear();
        ukazProvest(false);

        List<String> cesta = cesta(id);
        if (cesta.isEmpty() || !cesta.get(cesta.size() - 1).equals(id)) {
            lblZprava.setText("Krok 1: " + id + " nenalezeno, odeber vrací null.");
            kresli();
            return;
        }

        String rodic = cesta.size() > 1 ? cesta.get(cesta.size() - 2) : null;
        Uzel aktualni = uzly.get(id);
        zvyrazneni.put(id, BARVA_ODEBRAT);

        String text = "Krok 1: aktualni = " + id + ", rodic = " + (rodic == null ? "null (je to kořen)" : rodic) + ". ";

        if (aktualni.levy != null && aktualni.pravy != null) {
            String rodicNaslednika = id;
            String naslednik = aktualni.pravy;
            while (uzly.get(naslednik).levy != null) {
                rodicNaslednika = naslednik;
                naslednik = uzly.get(naslednik).levy;
            }
            zvyrazneni.put(naslednik, BARVA_NALEZENO);

            String diteNaslednika = uzly.get(naslednik).pravy;
            String strana = rodicNaslednika.equals(id) ? ".right" : ".left";
            text += "Dvě děti. Krok 2: jednou doprava (" + aktualni.pravy + "), pak doleva až na konec → naslednik = "
                    + naslednik + ". Jeho klíč a hodnota se zkopírují do aktualni, maže se starý " + naslednik
                    + " (jeho rodic = " + rodicNaslednika + "). Kroky 3–4: ";
            if (diteNaslednika != null) {
                zvyrazneni.put(diteNaslednika, BARVA_CESTA);
                text += "dite = " + diteNaslednika + ", " + rodicNaslednika + strana + " = " + diteNaslednika + ".";
            } else {
                text += "dite = null, " + rodicNaslednika + strana + " = null.";
            }
        } else {
            String dite = aktualni.levy != null ? aktualni.levy : aktualni.pravy;
            String kam;
            if (rodic == null) kam = "root";
            else kam = rodic + (id.equals(uzly.get(rodic).levy) ? ".left" : ".right");

            if (dite != null) {
                zvyrazneni.put(dite, BARVA_CESTA);
                text += "Jedno dítě. Krok 3: dite = " + dite + ". Krok 4: " + kam + " = " + dite + " (uzel se vystřihne).";
            } else {
                text += "Žádné dítě. Krok 3: dite = null. Krok 4: " + kam + " = null.";
            }
        }

        kOdebrani = id;
        ukazProvest(true);
        lblZprava.setText(text + " Klikni na „Provést odebrání“.");
        kresli();
    }

    private void provedOdebrani() {
        if (kOdebrani == null) return;
        String id = kOdebrani;
        kOdebrani = null;
        ukazProvest(false);
        zvyrazneni.clear();

        Produkt odebrany = agenda.odeber(id);
        poZmene.run();
        prekresli();

        if (odebrany == null) lblZprava.setText(id + " už ve stromu není.");
        else lblZprava.setText(id + " odebrán (" + odebrany.getNazev() + "). Uzlů: " + uzly.size()
                + ", výška: " + agenda.vyskaStromu() + ".");
    }

    private void projdi(eTypProhl typ) {
        zastavAnimaci();
        zvyrazneni.clear();
        ukazProvest(false);
        kresli();

        List<String> poradi = new ArrayList<>();
        Iterator<Produkt> iterator = agenda.vytvorIterator(typ);
        while (iterator.hasNext()) poradi.add(iterator.next().getId());

        if (typ == eTypProhl.HLOUBKA) {
            lblZprava.setText("Do hloubky, in-order (zásobník): vlevo → uzel → vpravo, proto vychází seřazeně podle ID.");
        } else {
            lblZprava.setText("Do šířky (fronta): po úrovních shora dolů, zleva doprava.");
        }
        if (poradi.isEmpty()) return;

        double krok = poradi.size() <= 60 ? 400 : 12000.0 / poradi.size();
        animace = new Timeline();
        for (int i = 0; i < poradi.size(); i++) {
            int index = i;
            animace.getKeyFrames().add(new KeyFrame(Duration.millis(krok * (i + 1)), e -> {
                zvyrazneni.clear();
                zvyrazneni.put(poradi.get(index), BARVA_NALEZENO);
                lblPoradi.setText(String.join(" ", poradi.subList(0, index + 1)));
                kresli();
            }));
        }
        animace.play();
    }

    private void vybuduj() {
        zastavAnimaci();
        zvyrazneni.clear();
        ukazProvest(false);

        int vyskaPred = agenda.vyskaStromu();
        agenda.vybuduj();
        poZmene.run();
        prekresli();
        Platform.runLater(this::prizpusobit);

        lblZprava.setText("Vybuduj: in-order do pole, zrus, pak se přes frontu vkládají středy intervalů. Výška "
                + vyskaPred + " → " + agenda.vyskaStromu() + ".");
    }

    private void zastavAnimaci() {
        if (animace != null) animace.stop();
        animace = null;
        lblPoradi.setText("");
    }

    private void ukazProvest(boolean ukaz) {
        btnProvest.setVisible(ukaz);
        btnProvest.setManaged(ukaz);
        if (!ukaz) kOdebrani = null;
    }

    private String popis(Produkt p) {
        if (p == null) return "";
        return p.getNazev() + " | " + p.getKategorie() + " | " + p.getSklad();
    }
}
