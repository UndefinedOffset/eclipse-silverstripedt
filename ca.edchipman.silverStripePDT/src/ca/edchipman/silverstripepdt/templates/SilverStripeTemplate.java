package ca.edchipman.silverstripepdt.templates;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.templates.Template;

public class SilverStripeTemplate extends Template {
    /** The template pattern. */
    private /*final*/ String[] fSSVersions;

    /**
     * Creates a copy of a template.
     *
     * @param template The template to copy
     */
    public SilverStripeTemplate(SilverStripeTemplate template) {
        super(template.getName(), template.getDescription(), template.getContextTypeId(), template.getPattern(), template.isAutoInsertable());
        
        this.fSSVersions=template.ssVersions();
    }

    /**
     * Creates a template.
     *
     * @param name The name of the template
     * @param description The description of the template
     * @param contextTypeId The id of the context type in which the template can be applied
     * @param pattern The template pattern
     * @param isAutoInsertable The auto insertable property of the template
     * @since 3.1
     */
    public SilverStripeTemplate(String name, String description, String contextTypeId, String pattern, boolean isAutoInsertable, String[] ssVersions) {
        super(name, description, contextTypeId, pattern, isAutoInsertable);
        
        this.fSSVersions=ssVersions;
    }
    
    /**
     * Creates a copy of a template.
     *
     * @param template The template to copy
     * @param ssVersions SilverStripe versions supported
     */
	public SilverStripeTemplate(Template template, String[] ssVersions) {
		super(template.getName(), template.getDescription(), template.getContextTypeId(), template.getPattern(), template.isAutoInsertable());
        
        this.fSSVersions=ssVersions;
	}

	/**
     * Gets the ss versions for this template
     * @return array Array of SilverStripe Version codes
     */
    public String[] ssVersions() {
        return this.fSSVersions;
    }

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Template))
			return false;
		
		if(!(o instanceof SilverStripeTemplate))
			return super.equals(o);
		
		SilverStripeTemplate t=(SilverStripeTemplate) o;
		if (t == this)
			return true;
		
		return super.equals(o) && t.ssVersions().equals(this.fSSVersions);
	}
}
