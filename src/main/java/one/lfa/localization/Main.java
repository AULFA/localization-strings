/*
 * Copyright © 2020 Library For All
 *
 * Apache 2.0 license.
 */

package one.lfa.localization;

import com.io7m.jaffirm.core.Preconditions;
import org.apache.commons.csv.CSVFormat;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Main
{
  private Main()
  {

  }

  private static final class Content implements ContentHandler
  {
    private final Path path;
    private final Map<String, TranslatedString> strings;
    private Locator locator;
    private Optional<String> id = Optional.empty();
    private String module;
    private String file;

    Content(
      final Path inPath,
      final Map<String, TranslatedString> inStrings)
    {
      this.path =
        Objects.requireNonNull(inPath, "path");
      this.strings =
        Objects.requireNonNull(inStrings, "strings");
    }

    @Override
    public void setDocumentLocator(
      final Locator in_locator)
    {
      this.locator = in_locator;
    }

    @Override
    public void startDocument()
    {
      this.file = this.path.getFileName().toString();

      final var values = this.path.getParent();
      Preconditions.checkPreconditionV(
        Objects.equals(values.getFileName().toString(), "values"),
        "Parent directory must be 'values' and not '%s'",
        values.getFileName()
      );

      final var res = values.getParent();
      Preconditions.checkPreconditionV(
        Objects.equals(res.getFileName().toString(), "res"),
        "Parent directory must be 'res' and not '%s'",
        res.getFileName()
      );

      final var main = res.getParent();
      Preconditions.checkPreconditionV(
        Objects.equals(main.getFileName().toString(), "main"),
        "Parent directory must be 'main' and not '%s'",
        main.getFileName()
      );

      final var src = main.getParent();
      Preconditions.checkPreconditionV(
        Objects.equals(src.getFileName().toString(), "src"),
        "Parent directory must be 'src' and not '%s'",
        src.getFileName()
      );

      final var mod = src.getParent().getFileName();
      this.module = mod.toString();
    }

    @Override
    public void endDocument()
    {

    }

    @Override
    public void startPrefixMapping(
      final String prefix,
      final String uri)
    {

    }

    @Override
    public void endPrefixMapping(final String prefix)
    {

    }

    @Override
    public void startElement(
      final String uri,
      final String localName,
      final String qName,
      final Attributes atts)
    {
      if (Objects.equals(localName, "string")) {
        this.id = Optional.of(atts.getValue("name"));
      } else {
        this.id = Optional.empty();
      }
    }

    @Override
    public void endElement(
      final String uri,
      final String localName,
      final String qName)
    {

    }

    @Override
    public void characters(
      final char[] ch,
      final int start,
      final int length)
      throws SAXException
    {
      if (this.id.isPresent()) {
        final var idText =
          this.id.get();
        final var text =
          textFromCharacters(ch, start, length);

        final var translated =
          TranslatedString.builder()
            .setId(idText)
            .setFile(this.file)
            .setModule(this.module)
            .setEnglish(text)
            .build();

        this.strings.put(idText, translated);
        this.id = Optional.empty();
      }
    }

    private static String textFromCharacters(
      final char[] ch,
      final int start,
      final int length)
    {
      // CHECKSTYLE:OFF
      final var text = new String(ch, start, length);
      // CHECKSTYLE:ON
      return text;
    }

    @Override
    public void ignorableWhitespace(
      final char[] ch,
      final int start,
      final int length)
    {

    }

    @Override
    public void processingInstruction(
      final String target,
      final String data)
    {

    }

    @Override
    public void skippedEntity(final String name)
    {

    }
  }

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length == 0) {
      throw new IllegalArgumentException("Usage: strings.xml [strings.xml ...]");
    }

    final var paths =
      Stream.of(args)
        .map(Paths::get)
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    try (var output = Files.newBufferedWriter(Paths.get("manifest.txt"))) {
      for (final var path : paths) {
        output.write(path.toString());
        output.newLine();
      }
      output.flush();
    }

    final var strings =
      new HashMap<String, TranslatedString>();

    for (final var path : paths) {
      final var parsers =
        SAXParserFactory.newInstance();
      final var parser =
        parsers.newSAXParser();
      final var reader =
        parser.getXMLReader();

      reader.setFeature(
        XMLConstants.FEATURE_SECURE_PROCESSING,
        true);
      reader.setFeature(
        "http://xml.org/sax/features/namespaces",
        true);

      try (InputStream stream = Files.newInputStream(path)) {
        final var content = new Content(path, strings);
        reader.setContentHandler(content);
        reader.parse(new InputSource(stream));
      }
    }

    try (var printer =
           CSVFormat.RFC4180.withHeader("Module", "File", "ID", "English")
             .printer()) {

      final var values =
        strings.values()
          .stream()
          .sorted()
          .collect(Collectors.toList());

      for (final var record : values) {
        printer.printRecord(
          record.module(),
          record.file(),
          record.id(),
          record.english()
        );
      }
    }
  }
}
