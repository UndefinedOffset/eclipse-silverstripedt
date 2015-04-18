package ca.edchipman.silverstripepdt.views.internal;

public class SSTaskDefinition {
    private String _title;
    private String _desc;
    private String _url;
    
    public SSTaskDefinition() {}
    
    public void setTitle(final String value) {
        this._title=value;
    }
    
    public void setDesc(final String value) {
        this._desc=value;
    }
    
    public void setURL(final String value) {
        this._url=value;
    }
    
    public String getTitle() {
        return this._title;
    }
    
    public String getDesc() {
        return this._desc;
    }
    
    public String getURL() {
        return this._url;
    }
}
