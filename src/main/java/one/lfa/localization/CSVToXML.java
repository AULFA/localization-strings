/*
 * Copyright © 2020 Library For All
 *
 * Apache 2.0 license.
 */

package one.lfa.localization;

import org.apache.commons.csv.CSVFormat;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class CSVToXML
{
  private CSVToXML()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length == 0) {
      throw new IllegalArgumentException("Usage: strings.csv");
    }

    final var path =
      Paths.get(args[0])
        .toAbsolutePath();

    try (var reader = Files.newBufferedReader(path)) {
      try (var parser = CSVFormat.RFC4180.parse(reader)) {
        final var iter = parser.iterator();
        iter.next();

        while (iter.hasNext()) {
          final var record = iter.next();
          System.out.printf(
            "<string name=\"%s\">%s</string>%n",
            record.get(0),
            record.get(2)
          );
        }
      }
    }
  }
}
