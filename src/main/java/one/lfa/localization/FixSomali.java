/*
 * Copyright © 2020 Library For All
 *
 * Apache 2.0 license.
 */

package one.lfa.localization;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import javax.xml.stream.XMLOutputFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FixSomali
{
  private FixSomali()
  {

  }

  private static final class OutputRecord
  {
    OutputRecord()
    {

    }

    private String githubLine;
    private String id;
    private String text;
  }

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: english.csv somali.csv");
    }

    final var englishPath =
      Paths.get(args[0]).toAbsolutePath();
    final var somaliPath =
      Paths.get(args[1]).toAbsolutePath();

    final List<CSVRecord> englishRecords;
    try (var reader = Files.newBufferedReader(englishPath)) {
      try (var parser = CSVFormat.DEFAULT.parse(reader)) {
        englishRecords = parser.getRecords();
      }
    }

    final List<CSVRecord> somaliRecords;
    try (var reader = Files.newBufferedReader(somaliPath)) {
      try (var parser = CSVFormat.DEFAULT.parse(reader)) {
        somaliRecords = parser.getRecords();
      }
    }

    final var englishMap = new HashMap<String, CSVRecord>();
    for (final var record : englishRecords) {
      englishMap.put(record.get(3).trim(), record);
    }

    final var badRecords = new ArrayList<CSVRecord>();

    try (var printer =
           CSVFormat.RFC4180.withHeader("ID", "English", "Somali")
             .printer()) {
      for (final var somaliRecord : somaliRecords) {
        final var english = somaliRecord.get(0).trim();
        final var englishRecord = englishMap.get(english);
        if (englishRecord == null) {
          badRecords.add(somaliRecord);
          continue;
        }

        printer.printRecord(
          englishRecord.get(2).trim(),
          english,
          somaliRecord.get(1).trim()
        );
      }
    }

    for (final var badRecord : badRecords) {
      System.err.printf("No record for english: \"%s\"%n", badRecord.get(0).trim());
    }
  }
}
