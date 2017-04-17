package com.mvp.java.logging.cellfactory;

import com.mvp.java.logging.LogRecord;
import java.util.Objects;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/**
 * Custom CEll Factory to display logging exception info within a cell.
 */
public class LogExceptionCellFactory implements Callback<TableColumn<LogRecord, String>, TableCell<LogRecord, String>> {

    @Override
    public TableCell<LogRecord, String> call(TableColumn<LogRecord, String> tableColumn) {
        final TableCell cell = new TableCell<LogRecord, String>() {

            @Override
            protected void updateItem(String exception, boolean isCellEmpty) {
                super.updateItem(exception, isCellEmpty);

                if (Objects.isNull(exception) || isCellEmpty) {
                    setGraphic(null);
                    setText("");
                    return;
                }
                LogRecord logRecordItem = (LogRecord) getTableRow().getItem();
                Button exceptionButton = new Button("View");
                exceptionButton.setOnAction(new ButtonHandler(exception, logRecordItem));
                setGraphic(exceptionButton);
                setText("");
            }
        };

        return cell;

    }

    private class ButtonHandler implements EventHandler<ActionEvent> {

        final private String exception;
        final private LogRecord selectedItem;

        ButtonHandler(String exception, LogRecord selectedItem) {
            this.exception = exception;
            this.selectedItem = selectedItem;
        }

        @Override
        public void handle(ActionEvent event) {

            if (Objects.isNull(selectedItem)) {
                return;
            }
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Exception Info");
            alert.setHeaderText(exception);
            String stacktrace = String.join("\n", selectedItem.getStacktrace());

            TextArea stacktraceTextArea = createTextArea(stacktrace);
            GridPane expandableStackTrace = createExpandableContent(stacktraceTextArea, alert);
            alert.getDialogPane().setExpandableContent(expandableStackTrace);

            alert.showAndWait();
        }

        private GridPane createExpandableContent(TextArea stacktraceTextArea, Alert alert) {
            GridPane expandableStackTrace = new GridPane();
            expandableStackTrace.setMaxWidth(Double.MAX_VALUE);
            expandableStackTrace.add(new Label("Stacktrace ..."), 0, 0);
            expandableStackTrace.add(stacktraceTextArea, 0, 1);
            return expandableStackTrace;
        }

        private TextArea createTextArea(String stacktrace) {
            TextArea stacktraceTextArea = new TextArea(stacktrace);
            stacktraceTextArea.setEditable(false);
            stacktraceTextArea.setWrapText(true);
            stacktraceTextArea.setMaxWidth(Double.MAX_VALUE);
            stacktraceTextArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(stacktraceTextArea, Priority.ALWAYS);
            GridPane.setHgrow(stacktraceTextArea, Priority.ALWAYS);
            return stacktraceTextArea;
        }
    }
}
