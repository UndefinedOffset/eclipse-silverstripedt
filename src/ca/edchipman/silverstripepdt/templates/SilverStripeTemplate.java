package ca.edchipman.silverstripepdt.templates;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.templates.Template;

public class SilverStripeTemplate extends Template {
    /** The template pattern. */
    private /*final*/ String[] fSSVersions;

    /**
     * Creates a copy of a template.
     *
     * @param template the template to copy
     */
    public SilverStripeTemplate(SilverStripeTemplate template) {
        super(template.getName(), template.getDescription(), template.getContextTypeId(), template.getPattern(), template.isAutoInsertable());
        
        this.fSSVersions=template.ssVersions();
    }

    /**
     * Creates a template.
     *
     * @param name the name of the template
     * @param description the description of the template
     * @param contextTypeId the id of the context type in which the template can be applied
     * @param pattern the template pattern
     * @param isAutoInsertable the auto insertable property of the template
     * @since 3.1
     */
    public SilverStripeTemplate(String name, String description, String contextTypeId, String pattern, boolean isAutoInsertable, String[] ssVersions) {
        super(name, description, contextTypeId, pattern, isAutoInsertable);
        
        this.fSSVersions=ssVersions;
    }

    /**
     * Gets the ss versions for this template
     * @return array Array of SilverStripe Version codes
     */
    public String[] ssVersions() {
        return this.fSSVersions;
    }
}
