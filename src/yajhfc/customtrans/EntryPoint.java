package yajhfc.customtrans;

import java.io.File;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.YajLanguage;
import yajhfc.launch.Launcher2;
import yajhfc.options.PanelTreeNode;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;

/**
 * Initialization class for the dynamic language plugin.
 * 
 * @author jonas
 *
 */
public class EntryPoint {
	private static final Logger log = Logger.getLogger(EntryPoint.class.getName());
	
	/**
	 * Plugin initialization method.
	 * The name and signature of this method must be exactly as follows 
	 * (i.e. it must always be "public static boolean init(int)" )
	 * @param startupMode the mode YajHFC is starting up in. The possible
	 *    values are one of the STARTUP_MODE_* constants defined in yajhfc.plugin.PluginManager
	 * @return true if the initialization was successful, false otherwise.
	 */
	public static boolean init(int startupMode) {

		PluginManager.pluginUIs.add(new PluginUI() {
			@Override
			public int getOptionsPanelParent() {
				// This method sets where the options panel will be put.
				// Currently OPTION_PANEL_ADVANCED and OPTION_PANEL_ROOT are supported
				return OPTION_PANEL_ADVANCED;
			}

			@Override
			public PanelTreeNode createOptionsPanel(PanelTreeNode parent) {
				/*
				 * This method must return a PanelTreeNode as shown below
				 * or null to not create an options page
				 */
				return new PanelTreeNode(
						parent, // Always pass the parent as first parameter
						new CustomTransOptionsPanel(), // The actual UI component that implements the options panel. 
						                        // This object *must* implement the OptionsPage interface.
						"Custom translations", // The text displayed in the tree view for this options page
						null);            // The icon displayed in the tree view for this options page
			}

			@Override
			public void saveOptions(Properties p) {
				getOptions().storeToProperties(p);
			}
			
		});

		return true;
	}
	
	static {
		loadCustomLanguages();
	}

    public static void loadCustomLanguages() {
        for (Map.Entry<String, String> lang : getOptions().languages.entrySet()) {
			try {
				addLanguageUnique(YajLanguage.supportedLanguages, 
						new DynYajLanguage(new Locale(lang.getKey()), new File(lang.getValue())));
			} catch (Exception e) {
				log.log(Level.WARNING, "Could not add language " + lang.getKey() + " from file " + lang.getValue(), e);
			}
		}
    }
	
	private static void addLanguageUnique(List<YajLanguage> list, YajLanguage lang) {
		// Try to find if the language has already been added:
		ListIterator<YajLanguage> it = list.listIterator();
		while (it.hasNext()) {
			YajLanguage l = it.next();
			if (l.getLocale().equals(lang.getLocale())) {
				it.set(lang);
				return;
			}
		}
		// If the language is not already present, add it
		list.add(lang);
	}
	
	private static CustomTransOptions options;
	/**
	 * Load some options 
	 * @return
	 */
    public static CustomTransOptions getOptions() {
        if (options == null) {
            options = new CustomTransOptions();
            options.loadFromProperties(Utils.getSettingsProperties());
            //options.po2properties = "/opt/local/bin/msgcat --properties-output \"%s\"";
            //options.languages.put("am", "/Users/jonas/Java/workspace/yajhfc/src/yajhfc/i18n/messages_it.po");
        }
        return options;
    }
    
    /**
     * Launches YajHFC including this plugin (for debugging purposes)
     * @param args
     */
    public static void main(String[] args) {
		PluginManager.internalPlugins.add(EntryPoint.class);
		Launcher2.main(args);
	}
}
