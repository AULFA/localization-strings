package one.lfa.localization;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public final class FixTetun
{
  private FixTetun()
  {

  }

  private static final class OutputRecord
  {
    String githubLine;
    String id;
    String text;
  }

  public static void main(final String args[])
    throws Exception
  {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: english.csv tetun.csv");
    }

    final var englishPath =
      Paths.get(args[0]).toAbsolutePath();
    final var tetunPath =
      Paths.get(args[1]).toAbsolutePath();

    final List<CSVRecord> englishRecords;
    try (var reader = Files.newBufferedReader(englishPath)) {
      try (var parser = CSVFormat.DEFAULT.parse(reader)) {
        englishRecords = parser.getRecords();
      }
    }

    final List<CSVRecord> tetunRecords;
    try (var reader = Files.newBufferedReader(tetunPath)) {
      try (var parser = CSVFormat.DEFAULT.parse(reader)) {
        tetunRecords = parser.getRecords();
      }
    }

    englishRecords.remove(0);
    tetunRecords.remove(0);

    final var outputRecords =
      new ArrayList<OutputRecord>(englishRecords.size());

    for (final var englishRecord : englishRecords) {
      final var tetunRecordOpt =
        tetunRecords.stream()
          .filter(r -> r.get(0).equals(englishRecord.get(0)))
          .findFirst();

      if (tetunRecordOpt.isPresent()) {
        final var tetunRecord = tetunRecordOpt.get();
        final OutputRecord outputRecord = new OutputRecord();
        outputRecord.githubLine = englishRecord.get(0);
        outputRecord.id = englishRecord.get(1);
        outputRecord.text = tetunRecord.get(2);
        outputRecords.add(outputRecord);
      }
    }

    writeOutputRecords(outputRecords);
  }

  private static void writeOutputRecords(
    final List<OutputRecord> outputRecords)
    throws Exception
  {
    final var factory = XMLOutputFactory.newInstance();
    final var writer = factory.createXMLStreamWriter(System.out);

    writer.writeStartDocument();
    writer.writeCharacters("\n");

    writer.writeStartElement("resources");
    writer.writeCharacters("\n");

    for (final var outputRecord : outputRecords) {
      writer.writeStartElement("string");
      writer.writeAttribute("name", outputRecord.id);
      writer.writeCharacters(outputRecord.text);
      writer.writeEndElement();
      writer.writeCharacters("\n");
    }

    writer.writeEndElement();

    writer.writeCharacters("\n");
    writer.writeEndDocument();

    writer.flush();
    writer.close();
  }
}
