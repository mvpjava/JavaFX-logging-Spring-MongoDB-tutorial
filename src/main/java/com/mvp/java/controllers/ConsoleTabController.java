package com.mvp.java.controllers;

import com.mvp.java.services.MissionsService;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsoleTabController {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleTabController.class);

    @FXML
    private TextArea missionOverviewText;
    @FXML
    private ListView<String> missionsList;

    @Autowired
    MissionsService service;

    public void initialize() {
        ObservableList<String> missions = FXCollections.observableArrayList("Apollo", "Shuttle", "Skylab", "BoomMission!");
        missionsList.setItems(missions);
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        missionOverviewText.clear();
        String selectedItem = missionsList.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            String msg = "User clicked on bad area of list";
           LOG.warn(msg, new NullPointerException(msg));
           return;
        }
        
        missionOverviewText.positionCaret(0);
        missionOverviewText.appendText(getInfo(selectedItem));
    }

    public String getInfo(String selectedItem) {
        String missionInfo = null;

        try {
            missionInfo = service.getMissionInfo(selectedItem);
            LOG.info("Sucessfully retrieved mission info for " + selectedItem + "\n");
        } catch (Exception exception) {
            LOG.error("Could not retrieve mission info!", exception);
        }

        return missionInfo;
    }

    public TextArea getMissionOverviewText() {
        return missionOverviewText;
    }

    public ListView<String> getMissionsList() {
        return missionsList;
    }

}
