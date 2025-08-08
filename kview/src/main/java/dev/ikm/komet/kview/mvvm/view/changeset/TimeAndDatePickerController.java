/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.changeset;

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.events.ExportDateTimePopOverEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.APPLY_POP_OVER;
import static dev.ikm.komet.kview.events.ExportDateTimePopOverEvent.CANCEL_POP_OVER;

public class TimeAndDatePickerController implements BasicController {

    private static final String PM =  "PM";
    private static final String AM = "AM";
    private int rangeType;

    @FXML
    private BorderPane dateTimePickerBorderPane;

    @FXML
    private TextField hourField;

    @FXML
    private TextField minuteField;
    @FXML
    private Button amButton;
    @FXML
    private Button pmButton;

    private EvtBus eventBus;

    private UUID exportTopic;

    private String amOrPm;

    public TimeAndDatePickerController(UUID exportTopic, int rangeType) {
        this.exportTopic = exportTopic;
        this.rangeType = rangeType;
    }

    @Override
    @FXML
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();
        hourField.setText("12");
        minuteField.setText("00");
        handleHourAndMinuteTextFieldInputLimit();
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        hourField.setText("");
        minuteField.setText("");
    }

    @Override
    public void cleanup() {

    }

    @FXML
    private void handleApplyButton(ActionEvent event){
        try {
            int hour = Integer.parseInt(hourField.getText());
            int minute = Integer.parseInt(minuteField.getText());
            if (hour < 0 || hour > 12 || minute < 0 || minute > 59){
                throw new Exception("Invalid time input");
            }

            if (PM.equals(amOrPm) && hour < 12) {
                hour += 12;
            } else if (AM.equals(amOrPm) && hour == 12) {
                hour = 0;
            }

            LocalDate localDate = (LocalDate) dateTimePickerBorderPane.getLeft().getUserData();

            LocalDateTime localDateTime = localDate.atTime(hour, minute);
            eventBus.publish(exportTopic, new ExportDateTimePopOverEvent(event.getSource(), APPLY_POP_OVER, this.rangeType, transformLocalDateTimeToLong(localDateTime)));

        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAMAndPMButton(ActionEvent event) {
        Button clickedAMorPMButton = (Button) event.getSource();
        amOrPm = clickedAMorPMButton.getText();
        if (clickedAMorPMButton.getText().equals("AM")){
            amButton.setStyle("-fx-background-color: #7ADE3D");
            pmButton.setStyle("-fx-background-color: #CFD8E4");
        }else if (clickedAMorPMButton.getText().equals("PM")){
            pmButton.setStyle("-fx-background-color: #7ADE3D");
            amButton.setStyle("-fx-background-color: #CFD8E4");
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event){
        eventBus.publish(exportTopic, new ExportDateTimePopOverEvent(event.getSource(), CANCEL_POP_OVER, this.rangeType));
    }

    private long transformLocalDateTimeToLong(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        return zonedDateTime.toInstant().toEpochMilli();
    }
    private void handleHourAndMinuteTextFieldInputLimit(){
        hourField.textProperty().addListener((observable,oldValue,newValue) ->{
           if (!newValue.matches("\\d*")){
               hourField.setText(newValue.replaceAll("\\D",""));
           }
           if (newValue.length() > 2){
               hourField.setText(oldValue);
           }
        } );

        minuteField.textProperty().addListener((observable,oldValue,newValue) ->{
            if (!newValue.matches("\\d*")){
                minuteField.setText(newValue.replaceAll("\\D",""));
            }
            if (newValue.length() > 2){
                minuteField.setText(oldValue);
            }
        } );
    }

}
