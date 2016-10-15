package org.daisy.dotify.impl.input.markdown;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.api.tasks.TaskOption;
import org.pegdown.PegDownProcessor;

public class MarkdownTask extends ReadWriteTask {
	private static final String SOURCE_ENCODING = "source-encoding";
	private static final String SOURCE_LANGUAGE = "source-language";
	private static final String DEFAULT_ENCODING = "utf-8";
	private static final String DEFAULT_LANGUAGE = Locale.getDefault().toLanguageTag();
	private static List<TaskOption> options = null;
	private final String language;
	private final String encoding;
	
	public MarkdownTask(Map<String, Object> params) {
		super("Markdown to HTML");
		this.language = getLanguage(params);
		this.encoding = getEncoding(params);
	}
	
	private static String getEncoding(Map<String, Object> params) {
		Object param = params.get(SOURCE_ENCODING);
		return (param!=null)?""+param:DEFAULT_ENCODING;
	}
	
	private static String getLanguage(Map<String, Object> params) {
		Object param = params.get(SOURCE_LANGUAGE);
		return (param!=null)?""+param:DEFAULT_LANGUAGE;
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		PegDownProcessor processor = new PegDownProcessor();
		try {
			byte[] data = Files.readAllBytes(input.getFile().toPath());
			String res = processor.markdownToHtml(new String(data, encoding));
			try (PrintWriter w = new PrintWriter(output)) {
				w.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\""+language+"\">");
				w.println("<body>");
				w.print(res);
				w.println("</body>");
				w.println("</html>");
			}
			return new DefaultAnnotatedFile.Builder(output).extension("html").mediaType("application/xhtml+xml").build();
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}
	}
	
	@Override
	public List<TaskOption> getOptions() {
		if (options==null) {
			options = new ArrayList<>();
			options.add(new TaskOption.Builder(SOURCE_ENCODING).description("The encoding of the input file").defaultValue(DEFAULT_ENCODING).build());
			options.add(new TaskOption.Builder(SOURCE_LANGUAGE).description("The language of the input file").defaultValue(DEFAULT_LANGUAGE).build());
		}
		return options;
	}

}