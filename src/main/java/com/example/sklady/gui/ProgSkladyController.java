package com.example.sklady.gui;

import com.example.sklady.enums.eTypProhl;
import com.example.sklady.enums.enumPozice;
import com.example.sklady.enums.enumSklad;
import com.example.sklady.managers.AgendaSklady;
import com.example.sklady.models.Produkt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Iterator;
import java.util.Optional;

public class ProgSkladyController {

    @FXML
    private TableView<Produkt> tableProducts;
    @FXML
    private TableColumn<Produkt, String> colId;
    @FXML
    private TableColumn<Produkt, String> colName;
    @FXML
    private TableColumn<Produkt, String> colCategory;
    @FXML
    private TableColumn<Produkt, Float> colPrice;
    @FXML
    private TableColumn<Produkt, Integer> colQuantity;
    @FXML
    private TableColumn<Produkt, enumSklad> colSklad;

    @FXML
    private ComboBox<enumSklad> comboSklady;
    @FXML
    private Label lblAveragePrice;
    @FXML
    private TextField txtLimit;
    @FXML
    private Button btnMove;
    @FXML
    private TextField txtPocetGeneruj;
    @FXML
    private TextField txtHledaneId;
    @FXML
    private TextField txtIdOd;
    @FXML
    private TextField txtIdDo;
    @FXML
    private Label lblDetail;
    @FXML
    private Label lblStav;

    private final AgendaSklady agenda = new AgendaSklady();
    private final ObservableList<Produkt> productList = FXCollections.observableArrayList();

    private Runnable posledniZobrazeni = this::zobrazVse;
    private String popisZobrazeni = "Všechny sklady";
    private StromOkno stromOkno;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nazev"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("kategorie"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("cena"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("mnozstvi"));
        colSklad.setCellValueFactory(new PropertyValueFactory<>("sklad"));

        tableProducts.setItems(productList);
        comboSklady.getItems().addAll(enumSklad.values());

        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnMove.setDisable(newVal == null);
            if (newVal != null) {
                lblDetail.setText(popisProduktu(newVal));
            }
        });

        obnov(null);
    }

    private void zobrazVse() {
        productList.setAll(agenda.zobrazProduktyList(null));
        popisZobrazeni = "Všechny sklady";
    }

    private void zobrazSklad(enumSklad sklad) {
        productList.setAll(agenda.zobrazProduktyList(sklad));
        popisZobrazeni = "Sklad " + sklad;
    }

    private void zobrazPodLimitem(enumSklad sklad, int limit) {
        productList.setAll(agenda.zobrazProduktyPodLimitem(sklad, limit));
        popisZobrazeni = "Množství pod " + limit + (sklad == null ? " (všechny sklady)" : " (" + sklad + ")");
    }

    private void zobrazStrom(eTypProhl typ) {
        productList.clear();
        Iterator<Produkt> iterator = agenda.vytvorIterator(typ);
        while (iterator.hasNext()) {
            productList.add(iterator.next());
        }
        popisZobrazeni = typ == eTypProhl.HLOUBKA ? "Strom do hloubky (in-order)" : "Strom do šířky (level-order)";
    }

    private void zobrazRozsah(String idOd, String idDo) {
        productList.setAll(agenda.najdiVRozsahu(idOd, idDo));
        popisZobrazeni = "Rozsah " + idOd + " až " + idDo;
    }

    private void nastavZobrazeni(Runnable zobrazeni, String zprava) {
        posledniZobrazeni = zobrazeni;
        obnov(zprava);
    }

    private void obnov(String zprava) {
        posledniZobrazeni.run();
        String text = popisZobrazeni
                + " | zobrazeno: " + productList.size()
                + " | celkem produktů: " + agenda.getPocet()
                + " | výška stromu: " + agenda.vyskaStromu();
        if (zprava != null) {
            text = text + " | " + zprava;
        }
        lblStav.setText(text);

        if (stromOkno != null && stromOkno.jeOtevrene()) {
            stromOkno.prekresli();
        }
    }

    private String popisProduktu(Produkt p) {
        return "Produkt: " + p.getId()
                + " | " + p.getNazev()
                + " | " + p.getKategorie()
                + " | cena: " + p.getCena()
                + " | množství: " + p.getMnozstvi()
                + " | sklad: " + p.getSklad();
    }

    @FXML
    protected void onLoadDataClick() {
        File soubor = new File("sklady.csv");
        if (!soubor.exists()) {
            soubor = new File("src/main/java/com/example/sklady/sklady.csv");
        }
        if (!soubor.exists()) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Vyberte soubor sklady.csv");
            soubor = chooser.showOpenDialog(tableProducts.getScene().getWindow());
            if (soubor == null) return;
        }

        int nacteno = agenda.importData(soubor.getPath());
        enumSklad prvni = enumSklad.values()[0];
        comboSklady.setValue(prvni);
        nastavZobrazeni(() -> zobrazSklad(prvni), "načteno z CSV: " + nacteno);
    }

    @FXML
    protected void onGenerujClick() {
        int kolik;
        try {
            kolik = Integer.parseInt(txtPocetGeneruj.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Chyba", "Zadejte počet produktů jako celé číslo!");
            return;
        }
        if (kolik <= 0) {
            showAlert("Chyba", "Počet produktů musí být kladný!");
            return;
        }

        int vygenerovano = agenda.generuj(kolik);
        obnov("vygenerováno: " + vygenerovano);
    }

    @FXML
    protected void onSaveClick() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Uložit produkty do souboru");
        chooser.setInitialFileName("sklady_vystup.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File soubor = chooser.showSaveDialog(tableProducts.getScene().getWindow());
        if (soubor == null) return;

        int ulozeno = agenda.ulozData(soubor.getPath());
        if (ulozeno < 0) {
            showAlert("Chyba", "Soubor se nepodařilo uložit.");
        } else {
            obnov("uloženo do souboru: " + ulozeno);
        }
    }

    @FXML
    protected void onSkladSelected() {
        enumSklad sklad = comboSklady.getValue();
        if (sklad != null) {
            nastavZobrazeni(() -> zobrazSklad(sklad), null);
        }
    }

    @FXML
    protected void onShowAllClick() {
        comboSklady.setValue(null);
        nastavZobrazeni(this::zobrazVse, null);
    }

    @FXML
    protected void onAveragePriceClick() {
        if (productList.isEmpty()) {
            lblAveragePrice.setText("Cena: 0.00");
            return;
        }

        float sum = 0;
        for (Produkt p : productList) {
            sum += p.getCena();
        }
        float avg = sum / productList.size();
        lblAveragePrice.setText(String.format("Cena: %.2f", avg));
    }

    @FXML
    protected void onFilterLimitClick() {
        try {
            int limit = Integer.parseInt(txtLimit.getText().trim());
            enumSklad sklad = comboSklady.getValue();
            nastavZobrazeni(() -> zobrazPodLimitem(sklad, limit), null);
        } catch (NumberFormatException e) {
            showAlert("Chyba", "Zadejte celé číslo do políčka limitu!");
        }
    }

    @FXML
    protected void onAddProductClick() {
        TextField tfId = new TextField();
        tfId.setPromptText("P999");
        TextField tfNazev = new TextField();
        TextField tfKategorie = new TextField();
        TextField tfCena = new TextField();
        TextField tfMnozstvi = new TextField();

        ComboBox<enumSklad> cbSklad = new ComboBox<>(FXCollections.observableArrayList(enumSklad.values()));
        cbSklad.setValue(comboSklady.getValue() != null ? comboSklady.getValue() : enumSklad.values()[0]);

        ComboBox<enumPozice> cbPozice = new ComboBox<>(FXCollections.observableArrayList(
                enumPozice.PRVNI, enumPozice.POSLEDNI, enumPozice.PREDCHUDCE, enumPozice.NASLEDNIK));
        cbPozice.setValue(enumPozice.POSLEDNI);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("ID:"), tfId);
        grid.addRow(1, new Label("Název:"), tfNazev);
        grid.addRow(2, new Label("Kategorie:"), tfKategorie);
        grid.addRow(3, new Label("Cena:"), tfCena);
        grid.addRow(4, new Label("Množství:"), tfMnozstvi);
        grid.addRow(5, new Label("Sklad:"), cbSklad);
        grid.addRow(6, new Label("Pozice:"), cbPozice);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nový produkt");
        dialog.setHeaderText("Zadejte údaje produktu:");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> vysledek = dialog.showAndWait();
        if (vysledek.isEmpty() || vysledek.get() != ButtonType.OK) return;

        String id = tfId.getText().trim();
        String nazev = tfNazev.getText().trim();
        String kategorie = tfKategorie.getText().trim();
        if (id.isEmpty() || nazev.isEmpty()) {
            showAlert("Chyba", "ID a název produktu musí být vyplněny!");
            return;
        }

        float cena;
        int mnozstvi;
        try {
            cena = Float.parseFloat(tfCena.getText().trim().replace(',', '.'));
            mnozstvi = Integer.parseInt(tfMnozstvi.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Chyba", "Neplatný formát číselných údajů!");
            return;
        }
        if (cena < 0 || mnozstvi < 0) {
            showAlert("Chyba", "Cena a množství nesmí být záporné!");
            return;
        }

        Produkt novy = new Produkt(id, nazev, kategorie, cena, mnozstvi);
        enumSklad sklad = cbSklad.getValue();

        nastavAktualniPodleVyberu(sklad);
        if (!agenda.vloz(novy, cbPozice.getValue(), sklad)) {
            showAlert("Chyba", "Produkt s ID " + id + " už existuje!");
            return;
        }

        comboSklady.setValue(sklad);
        nastavZobrazeni(() -> zobrazSklad(sklad), "vloženo: " + id);
        highlightProduct(novy);
    }

    @FXML
    protected void onRemoveProductClick() {
        Produkt selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Upozornění", "Vyberte produkt v tabulce!");
            return;
        }

        Produkt odebrany = agenda.odeber(selected.getId());
        if (odebrany == null) {
            showAlert("Chyba", "Produkt se nepodařilo odstranit.");
        } else {
            obnov("odebráno: " + odebrany.getId());
        }
    }

    @FXML
    protected void onMoveProductClick() {
        Produkt selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Upozornění", "Vyberte produkt pro přesun!");
            return;
        }

        ObservableList<enumSklad> cile = FXCollections.observableArrayList(enumSklad.values());
        cile.remove(selected.getSklad());

        ChoiceDialog<enumSklad> dialog = new ChoiceDialog<>(cile.get(0), cile);
        dialog.setTitle("Přesun produktu");
        dialog.setHeaderText("Vyberte cílový sklad pro " + selected.getId() + ":");
        dialog.setContentText("Sklad:");

        Optional<enumSklad> cil = dialog.showAndWait();
        if (cil.isEmpty()) return;

        if (agenda.presun(selected.getId(), cil.get())) {
            obnov("přesunuto: " + selected.getId() + " do " + cil.get());
        } else {
            showAlert("Chyba", "Produkt se nepodařilo přesunout.");
        }
    }

    @FXML
    protected void onClearClick() {
        enumSklad sklad = comboSklady.getValue();

        if (sklad == null) {
            Alert potvrzeni = new Alert(Alert.AlertType.CONFIRMATION);
            potvrzeni.setTitle("Zrušit vše");
            potvrzeni.setHeaderText(null);
            potvrzeni.setContentText("Opravdu zrušit všechny sklady i strom?");
            Optional<ButtonType> vysledek = potvrzeni.showAndWait();
            if (vysledek.isEmpty() || vysledek.get() != ButtonType.OK) return;
        }

        agenda.zrus(sklad);
        obnov(sklad == null ? "vše zrušeno" : "zrušen " + sklad);
    }

    @FXML
    protected void onFirstClick() {
        zpristupni(enumPozice.PRVNI);
    }

    @FXML
    protected void onPreviousClick() {
        zpristupni(enumPozice.PREDCHUDCE);
    }

    @FXML
    protected void onNextClick() {
        zpristupni(enumPozice.NASLEDNIK);
    }

    @FXML
    protected void onLastClick() {
        zpristupni(enumPozice.POSLEDNI);
    }

    private void zpristupni(enumPozice pozice) {
        enumSklad sklad = comboSklady.getValue();
        if (sklad == null) {
            showAlert("Upozornění", "Nejprve vyberte sklad!");
            return;
        }
        nastavAktualniPodleVyberu(sklad);
        highlightProduct(agenda.zpristupniProdukt(pozice, sklad));
    }

    private void nastavAktualniPodleVyberu(enumSklad sklad) {
        Produkt vybrany = tableProducts.getSelectionModel().getSelectedItem();
        if (vybrany != null && vybrany.getSklad() == sklad) {
            agenda.nastavAktualni(vybrany.getId());
        }
    }

    @FXML
    protected void onNajdiClick() {
        String id = txtHledaneId.getText().trim();
        if (id.isEmpty()) {
            showAlert("Upozornění", "Zadejte ID produktu!");
            return;
        }

        Produkt nalezeny = agenda.najdi(id);
        if (nalezeny == null) {
            lblDetail.setText("Produkt s ID " + id + " nenalezen.");
            return;
        }

        enumSklad sklad = nalezeny.getSklad();
        comboSklady.setValue(sklad);
        nastavZobrazeni(() -> zobrazSklad(sklad), "nalezeno: " + id);
        highlightProduct(nalezeny);
        lblDetail.setText(popisProduktu(nalezeny));
    }

    @FXML
    protected void onNajdiVRozsahuClick() {
        String idOd = txtIdOd.getText().trim();
        String idDo = txtIdDo.getText().trim();
        if (idOd.isEmpty() || idDo.isEmpty()) {
            showAlert("Upozornění", "Zadejte obě meze rozsahu!");
            return;
        }
        if (idOd.compareTo(idDo) > 0) {
            showAlert("Upozornění", "Dolní mez je větší než horní!");
            return;
        }

        comboSklady.setValue(null);
        nastavZobrazeni(() -> zobrazRozsah(idOd, idDo), null);
    }

    @FXML
    protected void onHloubkaClick() {
        comboSklady.setValue(null);
        nastavZobrazeni(() -> zobrazStrom(eTypProhl.HLOUBKA), null);
    }

    @FXML
    protected void onSirkaClick() {
        comboSklady.setValue(null);
        nastavZobrazeni(() -> zobrazStrom(eTypProhl.SIRKA), null);
    }

    @FXML
    protected void onVybudujClick() {
        int vyskaPred = agenda.vyskaStromu();
        agenda.vybuduj();
        int vyskaPo = agenda.vyskaStromu();

        comboSklady.setValue(null);
        nastavZobrazeni(() -> zobrazStrom(eTypProhl.SIRKA), "strom vybudován, výška: " + vyskaPred + " → " + vyskaPo);
    }

    @FXML
    protected void onZobrazStromClick() {
        if (stromOkno == null) {
            stromOkno = new StromOkno(agenda, () -> obnov(null));
        }
        stromOkno.zobraz();
    }

    private void highlightProduct(Produkt p) {
        if (p != null) {
            tableProducts.getSelectionModel().select(p);
            tableProducts.scrollTo(p);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
