package com.mvp.java.controllers;

import com.mvp.java.logging.cellfactory.LogCellFactory;
import com.mvp.java.logging.cellfactory.LogExceptionCellFactory;
import com.mvp.java.logging.LogRecord;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;

@Controller
public class MongoLoggerController {

    private final MongoTemplate template;
    private final ObservableList<LogRecord> tableViewData = FXCollections.observableArrayList();

    @FXML
    private ComboBox<String> loggerLevelComboBox;
    @FXML
    private TextField logMessageTextField;
    @FXML
    private TextField loggerNameTextField;
    @FXML
    private TextField exceptionTextField;
    @FXML
    private TextField threadTextField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<LogRecord> logTableView;
    @FXML
    private TableColumn<LogRecord, Date> timestampCol;
    @FXML
    private TableColumn<LogRecord, String> messageCol;
    @FXML
    private TableColumn<LogRecord, String> loggerCol;
    @FXML
    private TableColumn<LogRecord, String> exceptionCol;
    @FXML
    private TableColumn<LogRecord, String> threadCol;
    @FXML
    private Label totalLogCountLabel;
    @FXML
    private Button clearAllButton;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField startTime;
    @FXML
    private AnchorPane queryAnchorPane;
    @FXML
    private TextField endTime;

    public void initialize() {
        logTableView.setItems(tableViewData);

        loggerLevelComboBox.getItems().addAll("ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR");
        loggerLevelComboBox.getSelectionModel().select("INFO");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        loggerCol.setCellValueFactory(new PropertyValueFactory<>("logger"));
        exceptionCol.setCellValueFactory(new PropertyValueFactory<>("exception"));
        threadCol.setCellValueFactory(new PropertyValueFactory<>("thread"));

        messageCol.setCellFactory(new LogCellFactory(100));
        exceptionCol.setCellFactory(new LogExceptionCellFactory());

    }

    @Autowired
    public MongoLoggerController(MongoTemplate mongoTemplate) {
        this.template = mongoTemplate;
    }

    @FXML
    private void searchAction(ActionEvent event) {
        logTableView.getItems().clear();
        Query queryWithCriteria = buildQuery();
        findAllWithCriteria(queryWithCriteria);
    }

    private Query buildQuery() {
        final Query query = new Query();

        getUserCriteriaFromTextField(logMessageTextField).ifPresent((String messageSearch) -> {
            query.addCriteria(Criteria.where("message").regex(messageSearch, "i")); // i= Case-insensitive
        });
        getUserCriteriaFromTextField(loggerNameTextField).ifPresent((String loggerSearch) -> {
            query.addCriteria(Criteria.where("logger").regex(loggerSearch, "i"));
        });
        getUserCriteriaFromTextField(threadTextField).ifPresent((String threadSearch) -> {
            query.addCriteria(Criteria.where("thread").regex(threadSearch, "i"));
        });
        getUserCriteriaFromTextField(exceptionTextField).ifPresent((String exceptionSearch) -> {
            query.addCriteria(Criteria.where("exception").regex(exceptionSearch, "i"));
        });
        getLogLevelUserInput().ifPresent((String logLevelSearch) -> {
            if (!logLevelSearch.equals("ALL")) {
                query.addCriteria(Criteria.where("level").is(logLevelSearch));
            }
        });

        getDateTimeUserCriteria(startDatePicker, startTime).ifPresent((LocalDateTime fromDateTime) -> {
            Criteria dateRangeCriteria = Criteria.where("timestamp").gte(fromDateTime);
            getDateTimeUserCriteria(endDatePicker, endTime).ifPresent((LocalDateTime toDateTime) -> {
                dateRangeCriteria.lt(toDateTime);
            });
            query.addCriteria(dateRangeCriteria);
        });

        return query;
    }

    private void findAllWithCriteria(final Query queryWithCriteria) {
        final List<LogRecord> results = template.find(queryWithCriteria, LogRecord.class, "log");
        writeMongoQueryToLabel(results, queryWithCriteria);

        for (LogRecord log : results) {
            tableViewData.add(log);
        }
    }

    private void writeMongoQueryToLabel(final List<LogRecord> results, final Query queryWithCriteria) {
        final StringBuffer statusLabelText = new StringBuffer(300);
        
        statusLabelText.append("Record Count: ")
                .append(results.size())
                .append("  [ for query string: ")
                .append(queryWithCriteria)
                .append("]");
        totalLogCountLabel.setText(statusLabelText.toString());
    }

    private Optional<String> getLogLevelUserInput() {
        int selectedIndex = loggerLevelComboBox.getSelectionModel().getSelectedIndex();
        SingleSelectionModel<String> selectionModel = loggerLevelComboBox.getSelectionModel();

        if (selectionModel.getSelectedIndex() < 0) {
            return Optional.empty();
        }

        return Optional.of(selectionModel.getSelectedItem());
    }

    private Optional<String> getUserCriteriaFromTextField(final TextField textField) {
        String text = textField.getText();
        if (Objects.nonNull(text) && !text.isEmpty()) {
            return Optional.of(text);
        }
        return Optional.empty();
    }

    private Optional<LocalDate> getUserCriteriaFromDateField(final DatePicker dateTimeTextField) {
        LocalDate date = dateTimeTextField.getValue();
        if (Objects.nonNull(date)) {
            return Optional.of(date);
        }
        return Optional.empty();
    }

    private Optional<LocalTime> getUserCriteriaFromTimeField(final TextField timeTextField) {
        String time = timeTextField.getText();

        if (Objects.nonNull(time) && !time.isEmpty()) {
            LocalTime localTime = null;
            try {
                localTime = LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (DateTimeException ex) {
                totalLogCountLabel.setText("Invalid Time format [" + time + "] specified");
                return Optional.empty();
            }
            return Optional.of(localTime);
        }
        return Optional.empty();
    }

    private Optional<LocalDateTime> getDateTimeUserCriteria(DatePicker datePicker, TextField timeTextField) {
        Optional<LocalDate> dateOptional = getUserCriteriaFromDateField(datePicker);
        Optional<LocalTime> timeOptional = getUserCriteriaFromTimeField(timeTextField);
        LocalDate date;
        LocalTime time;

        if (dateOptional.isPresent()) {
            date = dateOptional.get();
            if (timeOptional.isPresent()) {
                time = timeOptional.get();
                return Optional.of(date.atTime(time));
            }
            return Optional.of(date.atTime(LocalTime.MIDNIGHT));
        }
        return Optional.empty();
    }

    @FXML
    private void clearAllonAction(ActionEvent event) {
        this.logMessageTextField.clear();
        this.loggerNameTextField.clear();
        this.exceptionTextField.clear();
        this.exceptionTextField.clear();
        this.threadTextField.clear();
        this.startDatePicker.setValue(null);
        this.endDatePicker.setValue(null);
        this.startTime.clear();
        this.endTime.clear();
        this.loggerLevelComboBox.getSelectionModel().clearSelection();
    }

}
