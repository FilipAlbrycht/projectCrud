package projectcrud;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author Filip
 */
public class Controller implements Initializable {

    private Connection conn = null;
    private String sql;
    private ResultSet result;
    private ObservableList<ObservableList> data;
    private Integer lastId = 0;
    private String oldPrice;
    private String oldModel;
    private String oldQuantity;
    private String id;

    @FXML
    private TableView table;

    @FXML
    private TextField cena;

    @FXML
    private TextField model;

    @FXML
    private TextField ilosc;

    @FXML
    private Button dodaj;

    @FXML
    private Button wysylanieEdycja;
    
    @FXML
    private Label uzupelnionePola;
    
    @FXML
    private Label cenaIloscInt;

    private void errors(){
        if(cena.getText().isEmpty() || model.getText().isEmpty() || ilosc.getText().isEmpty()){
            cenaIloscInt.setVisible(false);
            uzupelnionePola.setVisible(true);
        }   else    {
           try{
            Integer.parseInt(cena.getText());
            Integer.parseInt(ilosc.getText());
        }   catch(Exception e)  {
            uzupelnionePola.setVisible(false);
            cenaIloscInt.setVisible(true);
        } 
        }
        
    }
    
    public void setVisibleOnLabels(){
        uzupelnionePola.setVisible(false);
        cenaIloscInt.setVisible(false);
    }
    
    private void selectAll(TableView table) throws SQLException {
        data = FXCollections.observableArrayList();

        while (result.next()) {
            if (this.lastId < Integer.parseInt(result.getString(1))) {
                this.lastId = Integer.parseInt(result.getString(1));
            }
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                row.add(result.getString(i));
            }
            data.add(row);
        }

        table.setItems(data);
    }

    @FXML
    private void edit(ActionEvent event) {
        if (cena.getText() != this.oldPrice) {
            String sql = "UPDATE phones SET price=" + "'" + cena.getText() + "'" + " WHERE (phone_id = " + this.id.substring(1) + ")";
            try {
                conn.createStatement().executeUpdate(sql);
                setVisibleOnLabels();
            } catch (Exception e) {
                errors();
            }
        }

        if (ilosc.getText() != this.oldQuantity) {
            String sql = "UPDATE phones SET quantity=" + "'" + ilosc.getText() + "'" + " WHERE (phone_id = " + this.id.substring(1) + ")";
            try {
                conn.createStatement().executeUpdate(sql);
                setVisibleOnLabels();
            } catch (Exception e) {
                errors();
            }
        }

        if (model.getText() != this.oldModel) {
            String sql = "UPDATE phones SET model=" + "'" + model.getText() + "'" + " WHERE (phone_id = " + this.id.substring(1) + ")";
            try {
                conn.createStatement().executeUpdate(sql);
                setVisibleOnLabels();
            } catch (Exception e) {
                errors();
            }
        }

        try {
            sql = "SELECT * FROM phones";
            result = conn.createStatement().executeQuery(sql);
            selectAll(table);
            this.dodaj.setVisible(true);
            this.wysylanieEdycja.setVisible(false);
            clear();
            setVisibleOnLabels();
        } catch (Exception e) {
            errors();
        }

    }

    @FXML
    private void clear() {
        model.setText("");
        ilosc.setText("");
        cena.setText("");
    }

    @FXML
    private void fillEditForm(ActionEvent event) {
        if (!table.getSelectionModel().isEmpty()) {
            wysylanieEdycja.setVisible(true);
            Object selected = table.getSelectionModel().getSelectedItem();
            String id = selected.toString().split(", ")[0];
            String cena = selected.toString().split(", ")[1];
            String model = selected.toString().split(", ")[2];
            String ilosc = selected.toString().split(", ")[3];

            this.id = id;
            this.oldPrice = cena;
            this.oldModel = model;
            this.oldQuantity = ilosc;

            this.cena.setText(cena);
            this.model.setText(model);
            this.ilosc.setText(ilosc.substring(0, ilosc.length() - 1));
            dodaj.setVisible(false);
        }
    }

    @FXML
    private void delete(ActionEvent event) {
        Object selected = table.getSelectionModel().getSelectedItem();
        String id = selected.toString().split(",")[0].substring(1);
        String sql = "DELETE FROM phones WHERE (phone_id=" + id + ")";
        try {
            conn.createStatement().executeUpdate(sql);
            sql = "SELECT * FROM phones";
            result = conn.createStatement().executeQuery(sql);
            selectAll(table);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void insert(ActionEvent event) {
        final String ilosc = this.ilosc.getText();
        final String model = this.model.getText();
        final String cena = this.cena.getText();

        try {
            Integer lastId = this.lastId + 1;
            String sql = "INSERT INTO phones (phone_id, price, model, quantity) VALUES (" + "'" + Integer.toString(lastId) + "'" + "," + "'" + cena + "'" + "," + "'" + model + "'" + "," + "'" + ilosc + "'" + ")";
            conn.createStatement().executeUpdate(sql);

            sql = "SELECT * FROM phones";

            result = conn.createStatement().executeQuery(sql);
            selectAll(table);
            setVisibleOnLabels();
        } catch (Exception e) {
            errors();
        }

    }

    private void setColumns(TableView table) throws SQLException {
        for (int i = 0; i < result.getMetaData().getColumnCount(); i++) {
            final int j = i;
            TableColumn col = new TableColumn(result.getMetaData().getColumnName(i + 1));
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });

            table.getColumns().addAll(col);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/shop?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "Haslo12.");
            sql = "SELECT * FROM phones";

            result = conn.createStatement().executeQuery(sql);
            setColumns(table);
            selectAll(table);
        } catch (SQLException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
