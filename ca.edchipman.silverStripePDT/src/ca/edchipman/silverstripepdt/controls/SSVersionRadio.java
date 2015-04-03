package ca.edchipman.silverstripepdt.controls;

import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.swt.SWT;

@SuppressWarnings("restriction")
public class SSVersionRadio extends SelectionButtonDialogField {
    private String _ssVersion;
    private Boolean _supportsFrameworkOnly;
    
    /**
     * Initializes the radio pre-setting the SilverStripe version code
     * @param version Version code to use i.e SS3.1
     * @constructor
     */
    public SSVersionRadio(String version) {
        super(SWT.RADIO);
        
        this._ssVersion=version;
    }
    
    /**
     * Initializes the radio without setting the SilverStripe version code
     * @constructor
     */
    public SSVersionRadio() {
        super(SWT.RADIO);
    }
    
    /**
     * Sets the SilverStripe version code
     * @param version Version code to use i.e SS3.1
     */
    public void setSSVersion(String version) {
        this._ssVersion=version;
    }
    
    /**
     * Gets the SilverStripe version code
     * @return Version code i.e SS3.1
     */
    public String getSSVersion() {
        return this._ssVersion;
    }
    
    /**
     * Sets the SilverStripe framework only is supported or not
     * @param value SilverStripe framework only is supported or not
     */
    public void setSupportsFrameworkOnly(Boolean value) {
        this._supportsFrameworkOnly=value;
    }
    
    /**
     * Gets whether the SilverStripe framework only is supported or not
     * @return value SilverStripe framework only is supported or not
     */
    public Boolean getSupportsFrameworkOnly() {
        return this._supportsFrameworkOnly;
    }
}
