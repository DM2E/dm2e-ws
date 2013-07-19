package eu.dm2e.ws.api;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

@RDFClass(NS.FOAF.CLASS_PERSON)
public class UserPojo extends AbstractPersistentPojo<UserPojo> {
	
	public enum THEMES {
		  LIGHT("light")
		, DARK("dark")
		;
		
		private String name;
		public String getName() { return name; }
		THEMES(String val) {
			this.name = val;
		}
	}
	
	@RDFProperty(NS.FOAF.PROP_NAME)
	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	@RDFProperty(NS.OMNOM.PROP_PREFERRED_THEME)
	private String preferredTheme; // = THEMES.LIGHT.getName();
	public String getPreferredTheme() { return preferredTheme; }
	public void setPreferredTheme(String preferredTheme) { this.preferredTheme = preferredTheme; }
	
//	private Set<BookmarkPojo> bookmarks;

}
