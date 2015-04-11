package ca.edchipman.silverstripepdt.versioninterfaces;

import org.eclipse.jface.text.TextAttribute;

import ca.edchipman.silverstripepdt.style.LineStyleProviderForSS;

abstract public class SilverStripeVersionStyleProvider {
    protected LineStyleProviderForSS lineStyleProvider;
    
    /**
     * Constructor
     */
    public SilverStripeVersionStyleProvider() {}
    
    /**
     * Constructor
     * @param _lineStyleProviderRef Reference to the line style provider, this is used to add attributes to the provider
     */
    public SilverStripeVersionStyleProvider(LineStyleProviderForSS _lineStyleProviderRef) {
        this.lineStyleProvider=_lineStyleProviderRef;
    }
    
    /**
     * Sets the line style provider
     * @param _lineStyleProviderRef Reference to the line style provider, this is used to add attributes to the provider
     */
    public void setLineStyleProvider(LineStyleProviderForSS _lineStyleProviderRef) {
        this.lineStyleProvider=_lineStyleProviderRef;
    }

    /**
     * Loads the colors specific to this version of SilverStripe
     */
    abstract public void loadVersionColors();
    
    /**
     * Gets the text attribute for the specific region, return null if the region is to be handled by the default handlers
     * @param type
     * @return
     */
    abstract public TextAttribute getAttributeFor(String type);
}
