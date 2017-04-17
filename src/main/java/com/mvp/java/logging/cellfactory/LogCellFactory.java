package com.mvp.java.logging.cellfactory;

import com.mvp.java.logging.LogRecord;
import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Custom CEll Factory to display logging info. with a cell. determine if the
 * message is longer than allowed and if so, break it up into in the table
 * (without scrolling horizontally)
 */
public class LogCellFactory implements Callback<TableColumn<LogRecord, String>, TableCell<LogRecord, String>> {
    final int MAX_CHARS_PER_LINE ;

    public LogCellFactory(int MAX_CHARS_PER_LINE) {
        this.MAX_CHARS_PER_LINE = MAX_CHARS_PER_LINE;
    }

    @Override
    public TableCell<LogRecord, String> call(TableColumn<LogRecord, String> param) {
        return new TableCell<LogRecord, String>() {

            @Override
            protected void updateItem(String message, boolean isCellEmpty) {
                super.updateItem(message, isCellEmpty);
                if (Objects.isNull(message) || isCellEmpty) {
                    setText("");
                    return;
                }
                setText(formatString(message));
            }
        };
    }

    private String formatString(String textToFormat) {
        StringBuilder text = new StringBuilder(textToFormat);
        int positionOfInsert = 0;
        for (int currentLineNum = 1; ((text.length() - positionOfInsert) > MAX_CHARS_PER_LINE); currentLineNum++) {
            positionOfInsert = MAX_CHARS_PER_LINE * currentLineNum;
            text.insert(positionOfInsert, '\n');
        }
        return text.toString();

    }
    
}
