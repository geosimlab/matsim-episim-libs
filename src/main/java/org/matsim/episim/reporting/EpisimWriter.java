package org.matsim.episim.reporting;

import com.google.common.base.Joiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.events.Event;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Utility class to write reported data into csv files.
 */
public class EpisimWriter {

	protected static final Logger log = LogManager.getLogger(EpisimWriter.class);

	protected static final String SEPARATOR = "\t";

	/**
	 * Create one row in csv files.
	 */
	public static final Joiner JOINER = Joiner.on(SEPARATOR);

	/**
	 * Creates a csv writer and write the header using enum definition.s
	 */
	public static BufferedWriter prepare(String filename, Class<? extends Enum<?>> enumClass) {
		BufferedWriter writer = IOUtils.getBufferedWriter(filename);
		try {
			writer.write(JOINER.join(enumClass.getEnumConstants()));
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return writer;
	}

	/**
	 * Creates a csv writer and writes the header according to {@link Joiner#join(Object, Object, Object...)}
	 */
	public static BufferedWriter prepare(String filename, Object first, Object second, Object... rest) {
		BufferedWriter writer = IOUtils.getBufferedWriter(filename);
		try {
			writer.write(JOINER.join(first, second, rest));
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return writer;
	}

	/**
	 * Writes an event as xml representation to {@code out}.
	 */
	public static void writeEvent(final Appendable out, final Event event) throws IOException {
		out.append("\t<event ");
		Map<String, String> attr = event.getAttributes();
		for (Map.Entry<String, String> entry : attr.entrySet()) {
			out.append(entry.getKey());
			out.append("=\"");

			writeAttributeValue(out, entry.getValue());

			out.append("\" ");
		}
		out.append(" />\n");
	}

	private static void writeAttributeValue(final Appendable out, final String attributeValue) throws IOException {
		if (attributeValue == null) {
			return;
		}

		int len = attributeValue.length();
		boolean encode = false;
		for (int pos = 0; pos < len; pos++) {
			char ch = attributeValue.charAt(pos);
			if (ch == '<') {
				encode = true;
				break;
			} else if (ch == '>') {
				encode = true;
				break;
			} else if (ch == '\"') {
				encode = true;
				break;
			} else if (ch == '&') {
				encode = true;
				break;
			}
		}
		if (encode) {
			for (int pos = 0; pos < len; pos++) {
				char ch = attributeValue.charAt(pos);
				if (ch == '<') {
					out.append("&lt;");
				} else if (ch == '>') {
					out.append("&gt;");
				} else if (ch == '\"') {
					out.append("&quot;");
				} else if (ch == '&') {
					out.append("&amp;");
				} else {
					out.append(ch);
				}
			}
		}

		out.append(attributeValue);
	}

	/**
	 * Append a new row to the writer, columns separated by separator.
	 */
	public void append(BufferedWriter writer, String[] array) {
		try {
			writer.write(JOINER.join(array));
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			log.error("Could not write content", e);
		}
	}

	/**
	 * Appends plain string to the writer.
	 */
	public void append(BufferedWriter writer, String string) {
		try {
			writer.write(string);
			writer.flush();
		} catch (IOException e) {
			log.error("Could not write content", e);
		}
	}

	/**
	 * Appends an event as xml representation to the output.
	 */
	public void append(BufferedWriter writer, Event event) {
		try {
			writeEvent(writer, event);
		} catch (IOException e) {
			log.error("Could not write event");
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Close a writer for writing.
	 */
	public void close(BufferedWriter writer) {
		try {
			writer.close();
		} catch (IOException e) {
			log.error("Could not close writer", e);
			throw new UncheckedIOException(e);
		}
	}
}