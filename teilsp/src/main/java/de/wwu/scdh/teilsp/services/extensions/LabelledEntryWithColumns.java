package de.wwu.scdh.teilsp.services.extensions;

import java.util.List;
import java.util.Map;

public interface LabelledEntryWithColumns extends LabelledEntry {

    public Map<String, Comparable<?>> getColumns();

    public List<String> getColumnNames();

    public Comparable<?> getValue(String columnName);

    public <T extends Comparable<?>> T getValue(String columnName, T defaultValue);

    public <T extends List<Comparable<?>>> T getValuesInOrder(List<String> columnNames, T defaultValues);

    public List<Comparable<?>> getValuesInOrder(List<String> columnNames);

}
