package dev.ikm.komet.layout.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import dev.ikm.komet.layout.LayoutKey;
import dev.ikm.tinkar.common.binary.*;
import dev.ikm.tinkar.common.service.PluggableService;
import io.soabase.recordbuilder.core.RecordBuilder;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Represents a layout record that defines grid-based layout constraints and properties.
 * This includes position-related settings such as row and column indices, span values,
 * alignment preferences, sizing constraints, and fill behavior.
 *
 * This class implements {@code Encodable}, enabling its serialization and deserialization
 * through custom encoding and decoding methods.
 */
@RecordBuilder
public record AreaGridSettings(
        String areaFactoryClassName,
        int columnIndex,
        int rowIndex,
        LayoutKey.ForArea layoutKeyForArea,
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
        boolean fillWidth,
        boolean visible
) implements AreaGridSettingsBuilder.With, Encodable {

    private static final Logger LOG = LoggerFactory.getLogger(AreaGridSettings.class);

    private static final int marshalVersion = 1;

    public static final AreaGridSettings DEFAULT = new AreaGridSettings();

    /**
     * Constructs an instance of {@code AreaGridSettings} with default column and row indices
     * set to 0, as well as default configurations for other properties.
     * <p>
     * This constructor initializes a {@code AreaGridSettings} with:
     * <p> - Column index: 0
     * <p> - Row index: 0
     * <p> - Column span: 1
     * <p> - Row span: 1
     * <p> - Horizontal grow priority: {@code Priority.SOMETIMES}
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
    private AreaGridSettings() {
        this(0, 0, LayoutKey.EMPTY, KlArea.Factory.class.getName());
    }

    /**
     * Constructs a {@code AreaGridSettings} with specified column and row indices.
     * Default values for other properties are applied, including column span, row span,
     * alignment, and grow priorities.
     *
     * @param columnIndex      the index of the column where the layout begins
     * @param rowIndex         the index of the row where the layout begins
     * @param layoutKeyForArea
     */
    public AreaGridSettings(int columnIndex, int rowIndex, LayoutKey.ForArea layoutKeyForArea,
                            String areaFactoryClassName) {
        this(areaFactoryClassName, columnIndex, rowIndex, layoutKeyForArea, 1, 1,
                Priority.SOMETIMES, Priority.NEVER, HPos.LEFT, VPos.TOP,
                new Insets(0),
                Double.MAX_VALUE, Double.MAX_VALUE,
                Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE,
                true, true, true);
    }

    /**
     * Constructs an instance of {@code AreaGridSettings} based on the provided {@code GridIncrementer}.
     * The initial column and row indices in the layout are derived from the current values
     * of the {@code column} and {@code row} fields within the {@code GridIncrementer}.
     * Default values are used for other layout properties, such as span, alignment,
     * grow priorities, margins, and size constraints.
     *  <p>
     * The caller is responsible for calling the
     *
     * @param incrementer the {@code GridIncrementer} instance used to determine the
     *                    initial column and row indices of the layout. The {@code GridIncrementer}'s
     *                    current column and row values are applied during initialization.
     */
    public AreaGridSettings(GridStepper incrementer, LayoutKey.ForArea layoutKeyForArea, String areaFactoryClassName) {
        this(areaFactoryClassName, incrementer.column(), incrementer.row(), layoutKeyForArea, 1, 1,
                Priority.SOMETIMES, Priority.NEVER, HPos.LEFT, VPos.TOP,
                new Insets(0),
                Double.MAX_VALUE, Double.MAX_VALUE,
                Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE,
                true, true, true
        );
    }

    public KlArea makeAndAddToParent(KlParent parentView) {
        KlArea.Factory factory = makeAreaFactory();
        LOG.debug("Adding {} to {}", factory.productClass().getSimpleName(), parentView.getClass().getSimpleName());
        return factory.createAndAddToParent(this, parentView);
    }

    public <F extends KlArea.Factory> F makeAreaFactory() {
        try {
            Class factoryClass = PluggableService.forName(areaFactoryClassName());
            Constructor constructor = factoryClass.getConstructor();
            return (F) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public AreaGridSettings with(int columnIndex, int rowIndex, LayoutKey.ForArea layoutKeyForArea, String areaFactoryClassName) {
        return new AreaGridSettings(areaFactoryClassName, columnIndex, rowIndex, layoutKeyForArea, columnSpan, rowSpan,
                hGrow, vGrow, hAlignment, vAlignment, margin, maxHeight, maxWidth,
                preferredHeight, preferredWidth, fillHeight, fillWidth, visible
        );
    }

    public AreaGridSettings with(Class factoryClass) {
        return this.withAreaFactoryClassName(factoryClass.getName());
    }



    /**
     * Encodes the properties of the layout record into the given {@code EncoderOutput}.
     *
     * @param out the {@code EncoderOutput} to which the layout record properties are written
     */
    @Override
    @Encoder
    public void encode(EncoderOutput out) {
//        String areaFactoryClassName,
        out.writeString(areaFactoryClassName);
//        int columnIndex,
        out.writeInt(columnIndex);
//        int rowIndex,
        out.writeInt(rowIndex);
//        LayoutKey.ForArea layoutKeyForArea,
        out.write(layoutKeyForArea);
//        int columnSpan,
        out.writeInt(columnSpan);
//        int rowSpan,
        out.writeInt(rowSpan);
//        Priority hGrow,
        out.writeString(hGrow.name());
//        Priority vGrow,
        out.writeString(vGrow.name());
//        HPos hAlignment,
        out.writeString(hAlignment.name());
//        VPos vAlignment,
        out.writeString(vAlignment.name());
//        Insets margin,
        out.writeDouble(margin.getTop());
        out.writeDouble(margin.getRight());
        out.writeDouble(margin.getBottom());
        out.writeDouble(margin.getLeft());
//        Double maxHeight,
        out.writeDouble(maxHeight);
//        Double maxWidth,
        out.writeDouble(maxWidth);
//        Double preferredHeight,
        out.writeDouble(preferredHeight);
//        Double preferredWidth,
        out.writeDouble(preferredWidth);
//        boolean fillHeight,
        out.writeBoolean(fillHeight);
//        boolean fillWidth,
        out.writeBoolean(fillWidth);
//        boolean visible
        out.writeBoolean(visible);
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
    public static AreaGridSettings decode(DecoderInput in) {
            return new AreaGridSettings(
                    in.readString(), // String areaFactoryClassName,
                    in.readInt(), // int columnIndex,
                    in.readInt(), // int rowIndex,
                    in.decode(), //LayoutKey.ForArea layoutKeyForArea,
                    in.readInt(), // int columnSpan,
                    in.readInt(), // int rowSpan,
                    Priority.valueOf(in.readString()), // Priority hGrow,
                    Priority.valueOf(in.readString()), // Priority vGrow,
                    HPos.valueOf(in.readString()), // HPos hAlignment,
                    VPos.valueOf(in.readString()), // VPos vAlignment,
// Insets margin,
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
                    in.readBoolean(), // boolean fillWidth
                    in.readBoolean() // boolean visible
            );
    }

    @Override
    public String toString() {
        String factoryForArea = areaFactoryClassName();
        factoryForArea = factoryForArea.substring(factoryForArea.lastIndexOf('.') + 1);
        String compactMargin = margin.toString()
                .replace("Insets ", "")
                .replace("top=", "t:")
                .replace("right=", "r:")
                .replace("bottom=", "b:")
                .replace("left=", "l:");

        return "AreaGridSettings{" +
                "" + factoryForArea +
                ", c: " + columnIndex +
                ", r: " + rowIndex +
                ", " + layoutKeyForArea +
                ", cs: " + columnSpan +
                ", rs: " + rowSpan +
                ", hg: " + hGrow +
                ", vg: " + vGrow +
                ", ha: " + hAlignment +
                ", va: " + vAlignment +
                ", margin: " + compactMargin +
                ", mh: " + processLayoutDouble(maxHeight) +
                ", mw: " + processLayoutDouble(maxWidth) +
                ", ph: " + processLayoutDouble(preferredHeight) +
                ", pw: " + processLayoutDouble(preferredWidth) +
                ", fh: " + fillHeight +
                ", fw: " + fillWidth +
                ", v: " + visible +
                '}';
    }

    private static String processLayoutDouble(Double value) {
        return Double.toString(value)
                .replace("1.7976931348623157E308", "Double.MAX_VALUE")
                .replace("-1.0", "USE_COMPUTED_SIZE");

    }
}
