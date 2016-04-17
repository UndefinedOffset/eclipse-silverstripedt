package ca.edchipman.silverstripepdt.controls;

public class SSVersionOption {
    private String _label;
    private String _ssVersion;
    private Boolean _supportsFrameworkOnly=false;
    
    /**
     * Initializes the radio pre-setting the SilverStripe version code
     * @param version Version code to use i.e SS3.1
     * @constructor
     */
    public SSVersionOption(String version, String label) {
        this._ssVersion=version;
        this._label=label;
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
    
    public String toString() {
        return this._label;
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
