package yajhfc.customtrans;

import java.util.HashMap;
import java.util.Map;

import yajhfc.AbstractFaxOptions;

public class CustomTransOptions extends AbstractFaxOptions {
	public static final String DEF_MSGCAT_PARAMS = "--properties-output \"%s\"";
	
	/**
	 * Custom languages set by the user
	 */
	public final Map<String,String> languages = new HashMap<String,String>();
	
	/**
	 * Command to convert po to properties
	 */
	public String po2properties = "msgcat " + DEF_MSGCAT_PARAMS;
	
	/**
	 * Calls the super constructor with the prefix that should be prepended
	 * to the options name.
	 */
	public CustomTransOptions() {
		super("customtrans");
	}
}
