package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utilidades;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Departamento;
import model.services.DepartamentoServices;

public class DepartamentoListController implements Initializable, DataChangeListener {

	private DepartamentoServices service;

	@FXML
	private TableView<Departamento> tableViewDepartamento;

	@FXML
	private TableColumn<Departamento, Integer> tableColunaId;

	@FXML
	private TableColumn<Departamento, String> tableColunaNome;

	@FXML
	private TableColumn<Departamento, Departamento> tableColumnEDIT;

	@FXML
	private TableColumn<Departamento, Departamento> tableColumnREMOVE;

	@FXML
	private Button btNovo;

	private ObservableList<Departamento> obsList;

	@FXML
	public void onBtNovoAction(ActionEvent event) {
		Stage parenteStage = Utilidades.stageAtual(event);
		Departamento obj = new Departamento();
		criarDialogFormulario(obj, "/gui/FormularioDepartamento.fxml", parenteStage);
	}

	public void setDepartamentoService(DepartamentoServices service) {
		this.service = service;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		tableColunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColunaNome.setCellValueFactory(new PropertyValueFactory<>("name"));

		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartamento.prefHeightProperty().bind(stage.heightProperty());
	}

	public void atualizarTableView() {
		if (service == null) {
			throw new IllegalStateException("Serviço null");
		}
		List<Departamento> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewDepartamento.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}

	private void criarDialogFormulario(Departamento obj, String absoluteName, Stage parenteStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			FormularioDepartamentoController controller = loader.getController();
			controller.setDepartamento(obj);
			controller.setDepartmentoService(new DepartamentoServices());
			controller.subscribeDataChangeListener(this);
			controller.atualizarDadosFormulario();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Entre com os dados do Departamento");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);
			dialogStage.initOwner(parenteStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();

		} catch (IOException e) {
			Alerts.mostrarAlerta("IO Exception", "Erro carregamento view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		atualizarTableView();
	}

	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Departamento, Departamento>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Departamento obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> criarDialogFormulario(obj, "/gui/FormularioDepartamento.fxml",
						Utilidades.stageAtual(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Departamento, Departamento>() {
			private final Button button = new Button("remove");
			@Override
			protected void updateItem(Departamento obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Departamento obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmação", "Tem certeza que deseja deletar?");
		
		if(result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Serviço null");
			}
			try {
				service.remove(obj);
				atualizarTableView();
			}
			catch (DbIntegrityException e) {
				Alerts.mostrarAlerta("Error ao remover objeto", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}
}
