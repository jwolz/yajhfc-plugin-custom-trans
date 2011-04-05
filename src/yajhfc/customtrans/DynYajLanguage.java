package yajhfc.customtrans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.YajLanguage;
import yajhfc.util.ExternalProcessExecutor;

public class DynYajLanguage extends YajLanguage {
	private static final Logger log = Logger.getLogger(DynYajLanguage.class.getName());

	protected final PropertyResourceBundle bundle;
	
	public DynYajLanguage(Locale locale, File file) throws IOException {
		super(locale);
		this.bundle = loadFile(file);
	}
	
	protected PropertyResourceBundle loadFile(File file) throws IOException {
		String fileNameLower = file.getName().toLowerCase();

		if (fileNameLower.endsWith(".properties")) {
            FileInputStream in = new FileInputStream(file);
            PropertyResourceBundle rv = new PropertyResourceBundle(in);
            in.close();
            return rv;
		} else {
		    if (!fileNameLower.endsWith(".po")) {
		        log.warning("Unknown file suffix, interpreting file " + file + " as po file.");
		    }
		    String cmdLine = EntryPoint.getOptions().po2properties;
		    if (cmdLine.contains("%s")) {
				cmdLine = cmdLine.replace("%s", file.getPath());
			} else {
				cmdLine = cmdLine + " \"" + file.getPath() + "\"";
			}
	        List<String> commandLineArgs = ExternalProcessExecutor.splitCommandLine(cmdLine);
	        ExternalProcessExecutor.quoteCommandLine(commandLineArgs);
	        if (Utils.debugMode) {
	            log.fine("Invoking " + commandLineArgs.get(0) + " with the following command line:");
	            for (String item : commandLineArgs) {
	                log.fine(item);
	            }
	        }
	        Process filter = new ProcessBuilder(commandLineArgs).start();
	        
			PropertyResourceBundle rv = new PropertyResourceBundle(filter.getInputStream());
			
			filter.getInputStream().close();
	        BufferedReader errReader = new BufferedReader(new InputStreamReader(filter.getErrorStream()));
	        String line;
	        LinkedList<String> tail = new LinkedList<String>();
	        while ((line = errReader.readLine()) != null) {
	            log.info(commandLineArgs.get(0) + " output: " + line);
	            tail.offer(line);
	            while (tail.size() > 10) {
	                tail.poll();
	            }
	        }
	        errReader.close();
	        filter.getOutputStream().close();
	        try {
	            int exitVal = filter.waitFor();
	            if (exitVal != 0) {
	                StringBuilder excText = new StringBuilder();
	                excText.append("Non-zero exit code of ").append(commandLineArgs.get(0)).append(" (").append(exitVal).append("):\n");
	                for (String text : tail) {
	                    excText.append(text).append('\n');
	                }
	                throw new IOException(excText.toString());
	            }
	        } catch (InterruptedException e) {
	            throw new IOException(e);
	        }
	        return rv;
		} 
	}

	@Override
	public ResourceBundle getMessagesResourceBundle() {
		return bundle;
	}
}
