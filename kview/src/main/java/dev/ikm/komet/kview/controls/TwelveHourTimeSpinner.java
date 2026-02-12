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
package dev.ikm.komet.kview.controls;

// Source - https://stackoverflow.com/a/32617768
// https://stackoverflow.com/a/79881551/2430677
// Posted by James_D, modified by community. See post 'Timeline' for change history
// carldea has modified it further to support a 12-hour clock with AM/PM format.
// Retrieved 2026-02-02, License - CC BY-SA 3.0

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.*;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

public class TwelveHourTimeSpinner extends Spinner<LocalTime> {
    // Mode represents the unit that is currently being edited.
    // For convenience expose methods for incrementing and decrementing that
    // unit, and for selecting the appropriate portion in a spinner's editor
    enum Mode {

        HOURS {
            @Override
            LocalTime increment(LocalTime time, int steps) {
                if (time.getHour() == 23 && steps > 0) {
                    return time.withHour(0);
                } else if (time.getHour() == 0 && steps < 0) {
                    return time.withHour(23);
                }
                return time.plusHours(steps);
            }
            @Override
            void select(TwelveHourTimeSpinner spinner) {
                int index = spinner.getEditor().getText().indexOf(':');
                spinner.getEditor().selectRange(0, index);
            }
        },
        MINUTES {
            @Override
            LocalTime increment(LocalTime time, int steps) {
                if (time.getMinute() == 59 && steps > 0) {
                    return time.withMinute(0);
                } else if (time.getMinute() == 0 && steps < 0) {
                    return time.withMinute(59);
                }
                return time.plusMinutes(steps);
            }
            @Override
            void select(TwelveHourTimeSpinner spinner) {
                int hrIndex = spinner.getEditor().getText().indexOf(':');
                int minIndex = spinner.getEditor().getText().indexOf(':', hrIndex + 1);
                spinner.getEditor().selectRange(hrIndex+1, minIndex);
            }
        },
        SECONDS {
            @Override
            LocalTime increment(LocalTime time, int steps) {
                if (time.getSecond() == 59 && steps > 0) {
                    return time.withSecond(0);
                } else if (time.getSecond() == 0 && steps < 0) {
                    return time.withSecond(59);
                }
                return time.plusSeconds(steps);
            }
            @Override
            void select(TwelveHourTimeSpinner spinner) {
                int secIndex = spinner.getEditor().getText().lastIndexOf(':');
                spinner.getEditor().selectRange(secIndex+1, secIndex+3);
            }
        },
        AMPM {
            @Override
            LocalTime increment(LocalTime time, int steps) {
                // Toggle AM/PM on any increment or decrement
                if (time.getHour() < 12) {
                    time = time.plusHours(12);
                } else {
                    time = time.minusHours(12);
                }
                return time;
            }
            @Override
            void select(TwelveHourTimeSpinner spinner) {
                int index = spinner.getEditor().getText().lastIndexOf(' ');
                spinner.getEditor().selectRange(index+1, spinner.getEditor().getText().length());
            }
        };
        abstract LocalTime increment(LocalTime time, int steps);
        abstract void select(TwelveHourTimeSpinner spinner);
        LocalTime decrement(LocalTime time, int steps) {
            return increment(time, -steps);
        }
    }
    // lower case or upper case 'am' / 'pm' could be supported with more complex parsing
    private BooleanProperty amPmLowerCase = new SimpleBooleanProperty(false);
    public final BooleanProperty amPmLowerCaseProperty() {
        return amPmLowerCase;
    }
    public final boolean isAmPmLowerCase() {
        return amPmLowerCaseProperty().get();
    }
    // Property containing the current editing mode:
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.HOURS) ;

    public ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    public final Mode getMode() {
        return modeProperty().get();
    }

    public final void setMode(Mode mode) {
        modeProperty().set(mode);
    }

    public TwelveHourTimeSpinner(LocalTime time, boolean lowercaseAmPm){
        this(time);
        amPmLowerCaseProperty().set(lowercaseAmPm);
    }
    /**
     * Constructor with a specified local time to initialize the spinner with.
     * @param time A specified local time to initialize the spinner with.
     */
    public TwelveHourTimeSpinner(LocalTime time) {
        setEditable(true);
        getStyleClass().add("time-spinner");

        // Create a StringConverter for converting between the text in the
        // editor and the actual value:
        StringConverter<LocalTime> localTime12HourStringConverter = createStringConverter();

        // The textFormatter both manages the text <-> LocalTime conversion,
        // and vetoes any edits that are not valid. We just make sure we have
        // two colons and only digits in between:
        TextFormatter<LocalTime> textFormatter = createTextFormatter(localTime12HourStringConverter, time);

        // The spinner value factory defines increment and decrement by
        // delegating to the current editing mode:
        SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<>() {
            {
                setConverter(localTime12HourStringConverter);
                setValue(time);
            }

            @Override
            public void decrement(int steps) {
                setValue(mode.get().decrement(getValue(), steps));
                mode.get().select(TwelveHourTimeSpinner.this);
            }

            @Override
            public void increment(int steps) {
                setValue(mode.get().increment(getValue(), steps));
                mode.get().select(TwelveHourTimeSpinner.this);
            }

        };

        this.setValueFactory(valueFactory);
        this.getEditor().setTextFormatter(textFormatter);

        // Update the mode when the user interacts with the editor.
        // This is a bit of a hack, e.g. calling spinner.getEditor().positionCaret()
        // could result in incorrect state. Directly observing the caretPostion
        // didn't work well though; getting that to work properly might be
        // a better approach in the long run.
        this.getEditor().addEventHandler(InputEvent.ANY, e -> {
            if (e.getEventType() != MouseEvent.MOUSE_PRESSED && e.getEventType() != MouseEvent.MOUSE_PRESSED &&
                    e.getEventType() != KeyEvent.KEY_PRESSED && e.getEventType() != KeyEvent.KEY_RELEASED) {
                return;
            }
            int caretPos = this.getEditor().getCaretPosition();
            int hrIndex = this.getEditor().getText().indexOf(':');
            int minIndex = this.getEditor().getText().indexOf(':', hrIndex + 1);
            int secIndex = this.getEditor().getText().indexOf(' ', minIndex + 1);
            int amPmIndex = this.getEditor().getText().length() - 2;


//            System.out.println("caretPos=" + caretPos + " hrIndex=" + hrIndex + " minIndex=" + minIndex +
//                    " secIndex=" + secIndex + " amPmIndex=" + amPmIndex + "   event: " + e);

            if (caretPos <= hrIndex) {
                mode.set( Mode.HOURS );
            } else if (caretPos <= minIndex) {
                mode.set( Mode.MINUTES );
            } else if (caretPos <= secIndex){
                mode.set( Mode.SECONDS );
            } else if (caretPos >= amPmIndex && caretPos <= (this.getEditor().getText().length() -1)) {
                mode.set( Mode.AMPM );
            }
        });

        // When the mode changes, select the new portion:
        mode.addListener((obs, oldMode, newMode) -> newMode.select(this));
    }

    /**
     * Creates a TwelveHourTimeSpinner initialized to the current time.
     */
    public TwelveHourTimeSpinner() {
        this(LocalTime.now());
    }

    /**
     * Creates a StringConverter for converting between LocalTime and
     * a 12-hour format string with AM/PM.
     * @return StringConverter<LocalTime>
     */
    private StringConverter<LocalTime> createStringConverter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        StringConverter<LocalTime> localTime12HourConverter = new StringConverter<>() {

            @Override
            public String toString(LocalTime time) {
                if (isAmPmLowerCase()) {
                    return formatter.format(time).toLowerCase();
                }
                return formatter.format(time);
            }

            @Override
            public LocalTime fromString(String string) {

                String[] tokens = string.split("[:\\s]+");
                String ampm = "";
                if (amPmLowerCaseProperty().get()) {
                    ampm = tokens.length > 3 ? tokens[3].toLowerCase() : "";
                } else {
                    ampm = tokens.length > 3 ? tokens[3].toUpperCase() : "";
                }
                int hours = getIntField(tokens, 0);
                if (ampm.matches("[PM|pm]{2}") && hours < 12) hours += 12;
                if (ampm.matches("[AM|am]{2}") && hours == 12) hours = 0;
                int minutes = getIntField(tokens, 1);
                int seconds = getIntField(tokens, 2);

                int totalSeconds = (hours * 60 + minutes) * 60 + seconds ;
                return LocalTime.of((totalSeconds / 3600) % 24, (totalSeconds / 60) % 60, seconds % 60);
            }

            private int getIntField(String[] tokens, int index) {
                if (tokens.length <= index || tokens[index].isEmpty()) {
                    return 0 ;
                }
                return Integer.parseInt(tokens[index]);
            }

        };
        return localTime12HourConverter;
    }

    /**
     * Creates a TextFormatter that only allows valid time input in 12-hour format with AM/PM.
     * @param localTime12HourConverter
     * @param time Initial LocalTime value.
     * @return TextFormatter<LocalTime>
     */
    private TextFormatter<LocalTime> createTextFormatter(StringConverter<LocalTime> localTime12HourConverter, LocalTime time) {
        TextFormatter<LocalTime> textFormatter = new TextFormatter<>(localTime12HourConverter, time, c -> {
            String newText = c.getControlNewText();
            if (newText.matches("[0-9]{0,2}:[0-9]{0,2}:[0-9]{0,2}\\s[am|pm|AM|PM]{2}")) {
                return c ;
            }
            return null ;
        });
        return textFormatter;
    }
}
