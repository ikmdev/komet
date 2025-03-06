package dev.ikm.komet.layout;

import dev.ikm.tinkar.common.binary.*;
import io.soabase.recordbuilder.core.RecordBuilder;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Represents a layout record that defines grid-based layout constraints and properties.
 * This includes position-related settings such as row and column indices, span values,
 * alignment preferences, sizing constraints, and fill behavior.
 *
 * This class implements {@code Encodable}, enabling its serialization and deserialization
 * through custom encoding and decoding methods.
 */
@RecordBuilder
public record GridLayout(
        int columnIndex,
        int rowIndex,
        int columnSpan,
        int rowSpan,
        Priority hGrow,
        Priority vGrow,
        HPos hAlignment,
        VPos vAlignment,
        Insets margin,
        Double maxHeight,
        Double maxWidth,
        Double preferredHeight,
        Double preferredWidth,
        boolean fillHeight,
        boolean fillWidth) implements Encodable, GridLayoutBuilder.With {

    private static final int marshalVersion = 1;

    public static final GridLayout DEFAULT = new GridLayout();

    /**
     * Constructs an instance of {@code GridLayout} with default column and row indices
     * set to 0, as well as default configurations for other properties.
     * <p>
     * This constructor initializes a {@code GridLayout} with:
     * <p> - Column index: 0
     * <p> - Row index: 0
     * <p> - Column span: 1
     * <p> - Row span: 1
     * <p> - Horizontal grow priority: {@code Priority.NEVER}
     * <p> - Vertical grow priority: {@code Priority.NEVER}
     * <p> - Horizontal alignment: {@code HPos.LEFT}
     * <p> - Vertical alignment: {@code VPos.TOP}
     * <p> - Margins: {@code new Insets(0)}
     * <p> - Maximum height: {@code Double.MAX_VALUE}
     * <p> - Maximum width: {@code Double.MAX_VALUE}
     * <p> - Preferred height: {@code Region.USE_COMPUTED_SIZE}
     * <p> - Preferred width: {@code Region.USE_COMPUTED_SIZE}
     * <p> - Fill height: true
     * <p> - Fill width: true
     */
    private GridLayout() {
        this(0, 0);
    }

    /**
     * Constructs a {@code GridLayout} with specified column and row indices.
     * Default values for other properties are applied, including column span, row span,
     * alignment, and grow priorities.
     *
     * @param columnIndex the index of the column where the layout begins
     * @param rowIndex the index of the row where the layout begins
     */
    public GridLayout(int columnIndex, int rowIndex) {
        this(columnIndex, rowIndex, 1, 1,
                Priority.NEVER, Priority.NEVER, HPos.LEFT, VPos.TOP,
                new Insets(0),
                Double.MAX_VALUE, Double.MAX_VALUE,
                Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE,
                true, true);
    }

    /**
     * Encodes the properties of the layout record into the given {@code EncoderOutput}.
     *
     * @param out the {@code EncoderOutput} to which the layout record properties are written
     */
    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeInt(marshalVersion);
        out.writeInt(columnIndex);
        out.writeInt(rowIndex);
        out.writeInt(columnSpan);
        out.writeInt(rowSpan);
        out.writeString(hGrow.name());
        out.writeString(vGrow.name());
        out.writeString(hAlignment.name());
        out.writeString(vAlignment.name());
        out.writeDouble(margin.getTop());
        out.writeDouble(margin.getRight());
        out.writeDouble(margin.getBottom());
        out.writeDouble(margin.getLeft());
        out.writeDouble(maxHeight);
        out.writeDouble(maxWidth);
        out.writeDouble(preferredHeight);
        out.writeDouble(preferredWidth);
        out.writeBoolean(fillHeight);
        out.writeBoolean(fillWidth);
    }

    /**
     * Decodes a {@code DecoderInput} to reconstruct a {@code LayoutRecord} instance.
     * The method reads the object version and deserializes the contained properties
     * if the version matches the supported marshal version.
     *
     * @param in the {@code DecoderInput} from which the layout record properties
     *           are read and reconstructed
     * @return a {@code LayoutRecord} instance containing the deserialized properties
     * @throws UnsupportedOperationException if the object version is unsupported
     */
    @Decoder
    public static GridLayout decode(DecoderInput in) {
        int objectMarshalVersion = in.readInt();
        if (objectMarshalVersion == marshalVersion) {
            return new GridLayout(
                    in.readInt(), // int columnIndex,
                    in.readInt(), // int rowIndex,
                    in.readInt(), // int columnSpan,
                    in.readInt(), // int rowSpan,
                    Priority.valueOf(in.readString()), // Priority hGrow,
                    Priority.valueOf(in.readString()), // Priority vGrow,
                    HPos.valueOf(in.readString()), // HPos hAlignment,
                    VPos.valueOf(in.readString()), // VPos vAlignment,
                    new Insets(in.readDouble(), // Insets margin top,
                            in.readDouble(), // Insets margin right,
                            in.readDouble(), // Insets margin bottom,
                            in.readDouble() // Insets margin left,
                    ),
                    in.readDouble(), // Double maxHeight,
                    in.readDouble(), // Double maxWidth,
                    in.readDouble(), // Double preferredHeight,
                    in.readDouble(), // Double preferredWidth
                    in.readBoolean(), // boolean fillHeight
                    in.readBoolean() // boolean fillWidth
             );
        } else {
            throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }

    }
}
