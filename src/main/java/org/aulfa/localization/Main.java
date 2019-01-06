package org.aulfa.localization;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

public final class Main
{
  private Main()
  {

  }

  private static final class StringResource
  {
    private final int line_number;
    private final String name;

    private StringResource(
      final int in_line_number,
      final String in_name)
    {
      this.line_number = in_line_number;
      this.name = in_name;
    }
  }

  private static final class Content implements ContentHandler
  {
    private final CSVPrinter printer;
    private Locator locator;
    private Optional<StringResource> element = Optional.empty();

    Content(final CSVPrinter printer)
    {
      this.printer = printer;
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
        this.element = Optional.of(
          new StringResource(
            this.locator.getLineNumber(),
            atts.getValue("name")));
      } else {
        this.element = Optional.empty();
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
      if (this.element.isPresent()) {
        final var resource = this.element.get();
        final var text = new String(ch, start, length);

        try {
          this.printer.print(Integer.valueOf(resource.line_number));
          this.printer.print(resource.name);
          this.printer.print(text);
          this.printer.println();
        } catch (final IOException e) {
          throw new SAXException(e);
        }

        this.element = Optional.empty();
      }
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

  public static void main(final String args[])
    throws Exception
  {
    if (args.length == 0) {
      throw new IllegalArgumentException("Usage: strings.xml");
    }

    final var path = Paths.get(args[0]).toAbsolutePath();
    final var parsers = SAXParserFactory.newInstance();
    final var parser = parsers.newSAXParser();
    final var reader = parser.getXMLReader();

    reader.setFeature(
      XMLConstants.FEATURE_SECURE_PROCESSING,
      true);
    reader.setFeature(
      "http://xml.org/sax/features/namespaces",
      true);

    try (final var printer =
           CSVFormat.RFC4180.withHeader("GitHub Line", "Name", "English")
             .printer()) {
      try (final InputStream stream = Files.newInputStream(path)) {
        final var content = new Content(printer);
        reader.setContentHandler(content);
        reader.parse(new InputSource(stream));
      }
    }
  }
}
