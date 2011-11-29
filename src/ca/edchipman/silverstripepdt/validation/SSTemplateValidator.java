package ca.edchipman.silverstripepdt.validation;

import java.util.Locale;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.wst.html.internal.validation.HTMLValidator;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;
import org.eclipse.wst.validation.ValidatorMessage;
import org.eclipse.wst.validation.internal.core.Message;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;

public class SSTemplateValidator extends HTMLValidator {
    protected class LocalizedMessage extends Message {
        private String _message = null;
        public LocalizedMessage(int severity, String messageText) {
            this(severity, messageText, null);
        }
        
        public LocalizedMessage(int severity, String messageText, IResource targetObject) {
            this(severity, messageText, (Object) targetObject);
        }
        
        public LocalizedMessage(int severity, String messageText, Object targetObject) {
            super(null, severity, null);
            setLocalizedMessage(messageText);
            setTargetObject(targetObject);
        }
        
        public void setLocalizedMessage(String message) {
            _message = message;
        }
        
        public String getLocalizedMessage() {
            return _message;
        }
        
        public String getText() {
            return getLocalizedMessage();
        }
        
        public String getText(ClassLoader cl) {
            return getLocalizedMessage();
        }
        
        public String getText(Locale l) {
            return getLocalizedMessage();
        }
        
        public String getText(Locale l, ClassLoader cl) {
            return getLocalizedMessage();
        }
    }
    
    
    IDocument fDocument=null;
    
    @Override
    public void connect(IDocument document) {
        fDocument = document;
        super.connect(document);
    }

    @Override
    public void disconnect(IDocument document) {
        fDocument = null;
        super.disconnect(document);
    }
}
