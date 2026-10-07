package com.example.sklady.gui;

import com.example.sklady.enums.enumPozice;
import com.example.sklady.enums.enumSklad;
import com.example.sklady.managers.Sklady;
import com.example.sklady.models.Produkt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.util.Optional;

public class HelloController {

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
    private ComboBox<enumSklad> comboSklady;
    @FXML
    private Label lblAveragePrice;
    @FXML
    private TextField txtLimit;
    @FXML
    private Button btnMove;

    private Sklady skladyManager;
    private final ObservableList<Produkt> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nazev"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("kategorie"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("cena"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("mnozstvi"));

        skladyManager = new Sklady();
        tableProducts.setItems(productList);

        comboSklady.getItems().addAll(enumSklad.values());

        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (btnMove != null) {
                btnMove.setDisable(newVal == null);
            }
        });
    }

    @FXML
    protected void onLoadDataClick() {
        String filePath = "sklady.csv";
        File f = new File(filePath);
        if (!f.exists()) {
            filePath = "src/main/java/com/example/sklady/sklady.csv";
        }

        int loaded = skladyManager.importData(filePath);
        System.out.println("Načteno řádků z CSV: " + loaded);

        if (enumSklad.values().length > 0) {
            comboSklady.setValue(enumSklad.values()[0]);
            onSkladSelected();
        }
    }

    @FXML
    protected void onSkladSelected() {
        enumSklad selectedSklad = comboSklady.getValue();
        if (selectedSklad != null) {
            productList.clear();
            var items = skladyManager.zobrazProduktyList(selectedSklad);
            if (items != null) {
                productList.addAll(items);
            }
        }
    }

    @FXML
    protected void onShowAllClick() {
        productList.clear();
        var items = skladyManager.zobrazProduktyList(null);
        if (items != null) {
            productList.addAll(items);
        }
        comboSklady.setValue(null);
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
            enumSklad selectedSklad = comboSklady.getValue();

            productList.clear();
            var items = skladyManager.zobrazProduktyPodLimitem(selectedSklad, limit);
            if (items != null) {
                productList.addAll(items);
            }
        } catch (NumberFormatException e) {
            showAlert("Chyba", "Zadejte celé číslo do políčka limitu!");
        }
    }

    @FXML
    protected void onAddProductClick() {
        enumSklad selectedSklad = comboSklady.getValue();
        if (selectedSklad == null) {
            showAlert("Upozornění", "Nejprve vyberte sklad, do kterého chcete přidat produkt!");
            return;
        }

        TextInputDialog dialogId = new TextInputDialog("P999");
        dialogId.setTitle("Nový produkt");
        dialogId.setHeaderText("Zadejte ID produktu:");
        Optional<String> resultId = dialogId.showAndWait();
        if (resultId.isEmpty()) return;

        TextInputDialog dialogName = new TextInputDialog("Nový produkt");
        dialogName.setTitle("Nový produkt");
        dialogName.setHeaderText("Zadejte název produktu:");
        Optional<String> resultName = dialogName.showAndWait();
        if (resultName.isEmpty()) return;

        TextInputDialog dialogCat = new TextInputDialog("Ostatní");
        dialogCat.setTitle("Nový produkt");
        dialogCat.setHeaderText("Zadejte kategorii:");
        Optional<String> resultCat = dialogCat.showAndWait();
        if (resultCat.isEmpty()) return;

        TextInputDialog dialogPrice = new TextInputDialog("100.0");
        dialogPrice.setTitle("Nový produkt");
        dialogPrice.setHeaderText("Zadejte cenu:");
        Optional<String> resultPrice = dialogPrice.showAndWait();
        if (resultPrice.isEmpty()) return;

        TextInputDialog dialogQty = new TextInputDialog("10");
        dialogQty.setTitle("Nový produkt");
        dialogQty.setHeaderText("Zadejte množství:");
        Optional<String> resultQty = dialogQty.showAndWait();
        if (resultQty.isEmpty()) return;

        try {
            String id = resultId.get();
            String name = resultName.get();
            String cat = resultCat.get();
            float price = Float.parseFloat(resultPrice.get());
            int qty = Integer.parseInt(resultQty.get());

            Produkt newP = new Produkt(id, name, cat, price, qty);

            // Відповідно до ТЗ додаємо на останню позицію за замовчуванням
            skladyManager.vlozProdukt(newP, enumPozice.POSLEDNI, selectedSklad);
            onSkladSelected();
        } catch (Exception e) {
            showAlert("Chyba", "Neplatný formát číselných údajů!");
        }
    }

    @FXML
    protected void onRemoveProductClick() {
        Produkt selected = tableProducts.getSelectionModel().getSelectedItem();
        enumSklad selectedSklad = comboSklady.getValue();

        if (selected == null || selectedSklad == null) {
            showAlert("Upozornění", "Vyberte produkt v tabulce a aktivní sklad!");
            return;
        }

        boolean removed = skladyManager.odeberProduktPodleId(selectedSklad, selected.getId());
        if (removed) {
            onSkladSelected(); // оновлюємо таблицю
        } else {
            showAlert("Chyba", "Produkt se nepodařilo odstranit.");
        }
    }

    @FXML
    protected void onMoveProductClick() {
        Produkt selected = tableProducts.getSelectionModel().getSelectedItem();
        enumSklad currentSklad = comboSklady.getValue();

        if (selected == null || currentSklad == null) {
            showAlert("Upozornění", "Vyberte produkt pro přesun a aktivní sklad!");
            return;
        }

        ChoiceDialog<enumSklad> dialog = new ChoiceDialog<>(null, enumSklad.values());
        dialog.setTitle("Přesun produktu");
        dialog.setHeaderText("Vyberte cílový sklad:");
        dialog.setContentText("Sklad:");

        Optional<enumSklad> targetSklad = dialog.showAndWait();
        if (targetSklad.isPresent() && targetSklad.get() != currentSklad) {
            skladyManager.synchronizujAktualniPodleId(currentSklad, selected.getId());

            skladyManager.presunProdukt(currentSklad, targetSklad.get());
            onSkladSelected();
        }
    }

    @FXML
    protected void onClearClick() {
        enumSklad selectedSklad = comboSklady.getValue();
        skladyManager.zrus(selectedSklad); // Якщо selectedSklad == null, очистить усі склади
        productList.clear();
    }

    @FXML
    protected void onFirstClick() {
        enumSklad selectedSklad = comboSklady.getValue();
        if (selectedSklad != null) {
            Produkt p = skladyManager.zpristupniProdukt(enumPozice.PRVNI, selectedSklad);
            highlightProduct(p);
        }
    }

    @FXML
    protected void onPreviousClick() {
        enumSklad selectedSklad = comboSklady.getValue();
        if (selectedSklad != null) {
            Produkt p = skladyManager.zpristupniProdukt(enumPozice.PREDCHUDCE, selectedSklad);
            highlightProduct(p);
        }
    }

    @FXML
    protected void onNextClick() {
        enumSklad selectedSklad = comboSklady.getValue();
        if (selectedSklad != null) {
            Produkt p = skladyManager.zpristupniProdukt(enumPozice.NASLEDNIK, selectedSklad);
            highlightProduct(p);
        }
    }

    @FXML
    protected void onLastClick() {
        enumSklad selectedSklad = comboSklady.getValue();
        if (selectedSklad != null) {
            Produkt p = skladyManager.zpristupniProdukt(enumPozice.POSLEDNI, selectedSklad);
            highlightProduct(p);
        }
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