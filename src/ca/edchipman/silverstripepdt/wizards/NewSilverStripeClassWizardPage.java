package ca.edchipman.silverstripepdt.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.core.ScriptProject;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTag;
import org.eclipse.php.internal.core.documentModel.provisional.contenttype.ContentTypeIdForPHP;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.corext.codemanipulation.StubUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import ca.edchipman.silverstripepdt.SilverStripePluginImages;
import ca.edchipman.silverstripepdt.dialogs.FilteredTypesSelectionDialog;
import ca.edchipman.silverstripepdt.search.ISilverStripePDTSearchConstants;
import ca.edchipman.silverstripepdt.wizards.SilverStripeProjectWizardSecondPage.SilverStripeFileCreator;

@SuppressWarnings("restriction")
public class NewSilverStripeClassWizardPage extends WizardPage {
    private Text sourceFolder;
    private Text className;
    private Text superClass;
    private ISelection selection;
    private List interfaces;
    private Button btnSuperConstruct;
    private Button btnAbstractMethods;
    private IType superClassType;
    private Button[] btnClassModifiers;
    
    /**
     * Create the wizard.
     */
    public NewSilverStripeClassWizardPage(final ISelection selection) {
        super("wizardPage");
        setTitle("SilverStripe Class");
        setDescription("Create a new SilverStripe class.");
        setImageDescriptor(SilverStripePluginImages.DESC_ADD_SS_FILE);
        
        this.selection = selection;
    }

    /**
     * Create contents of the wizard.
     * @param parent
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        GridLayout gl_container = new GridLayout(3, false);
        gl_container.verticalSpacing = 9;
        container.setLayout(gl_container);
        
        Label lblSourceFolder = new Label(container, SWT.NONE);
        lblSourceFolder.setText("Source folder:");
        
        sourceFolder = new Text(container, SWT.BORDER);
        sourceFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        sourceFolder.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                dialogChanged(false);
            }
        });
        
        Button btnBrowse = new Button(container, SWT.NONE);
        btnBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                handleBrowse();
            }
        });
        GridData gd_btnBrowse = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnBrowse.widthHint = 80;
        btnBrowse.setLayoutData(gd_btnBrowse);
        btnBrowse.setText("Browse...");
        
        Label lblName = new Label(container, SWT.NONE);
        lblName.setText("Name:");
        
        className = new Text(container, SWT.BORDER);
        className.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        className.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                dialogChanged(false);
            }
        });
        new Label(container, SWT.NONE);
        
        Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label lblModifiers = new Label(container, SWT.NONE);
        lblModifiers.setText("Modifiers:");
        
        Composite modifiersComp = new Composite(container, SWT.NONE);
        GridData gd_modifiersComp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_modifiersComp.widthHint = 401;
        modifiersComp.setLayoutData(gd_modifiersComp);
        
        btnClassModifiers = new Button[3];
        modifiersComp.setLayout(new GridLayout(3, false));
        btnClassModifiers[0] = new Button(modifiersComp, SWT.RADIO);
        btnClassModifiers[0].setText("none");
        btnClassModifiers[0].setSelection(true);
        
        btnClassModifiers[1] = new Button(modifiersComp, SWT.RADIO);
        btnClassModifiers[1].setText("abstract");
        
        btnClassModifiers[2] = new Button(modifiersComp, SWT.RADIO);
        btnClassModifiers[2].setText("final");
        new Label(container, SWT.NONE);
        
        Label lblSuperclass = new Label(container, SWT.NONE);
        lblSuperclass.setText("Superclass:");
        
        superClass = new Text(container, SWT.BORDER);
        superClass.setEditable(false);
        superClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Button btnSuperBrowse = new Button(container, SWT.NONE);
        btnSuperBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                superClassType=chooseSuperClass();
                if(superClassType!=null) {
                    superClass.setText(superClassType.getElementName());
                }
            }
        });
        btnSuperBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSuperBrowse.setText("Browse...");
        
        Label lblInterfaces = new Label(container, SWT.NONE);
        lblInterfaces.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblInterfaces.setText("Interfaces:");
        
        interfaces = new List(container, SWT.BORDER);
        interfaces.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        
        Composite composite = new Composite(container, SWT.NONE);
        FillLayout fl_composite = new FillLayout(SWT.VERTICAL);
        fl_composite.spacing = 40;
        composite.setLayout(fl_composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        
        Button btnAdd = new Button(composite, SWT.NONE);
        final Button btnRemove = new Button(composite, SWT.NONE);
        
        btnAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                IType interfaceType=chooseInterface();
                if(interfaceType!=null) {
                    String interfaceName=interfaceType.getElementName();
                    
                    if(interfaces.indexOf(interfaceName)==-1) {
                        interfaces.add(interfaceName);
                        
                        btnRemove.setEnabled(true);
                    }
                }
            }
        });
        btnAdd.setText("Add...");
        
        
        btnRemove.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(final SelectionEvent e) {
                    int[] indexes = interfaces.getSelectionIndices();
                    interfaces.remove(indexes);
                    
                    if(interfaces.getItemCount()==0) {
                        btnRemove.setEnabled(false);
                    }
                }
            });
        btnRemove.setEnabled(false);
        btnRemove.setText("Remove");
        
        Label lblWhichMethodStubs = new Label(container, SWT.NONE);
        lblWhichMethodStubs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblWhichMethodStubs.setText("Which method stubs would you like to create?");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        btnSuperConstruct = new Button(container, SWT.CHECK);
        btnSuperConstruct.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        btnSuperConstruct.setText("Constructor from superclass");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        btnAbstractMethods = new Button(container, SWT.CHECK);
        btnAbstractMethods.setSelection(true);
        btnAbstractMethods.setText("Inherited abstract methods");
        new Label(container, SWT.NONE);
        
        initialize();
        dialogChanged(true);
        setPageComplete(false);
    }
    
    private String getFileName() {
        return getClassName()+".php";
    }

    protected void updateStatus(final String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    protected IContainer getContainer(final String text) {
        final Path path = new Path(text);

        final IResource resource = ResourcesPlugin.getWorkspace().getRoot()
                .findMember(path);
        return resource instanceof IContainer ? (IContainer) resource : null;
    }
    
    /**
     * Ensures that both text fields are set.
     */
    protected void dialogChanged(Boolean wizardInit) {
        final String container = getContainerName();
        final String fileName = getFileName();

        if (container.length() == 0) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
            return;
        }
        final IContainer containerFolder = getContainer(container);
        if (containerFolder == null || !containerFolder.exists()) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_11); //$NON-NLS-1$
            return;
        }
        if (!containerFolder.getProject().isOpen()) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_12); //$NON-NLS-1$
            return;
        }
        
        if(wizardInit) {
            updateStatus(null);
            return;
        }
        
        if (fileName != null && !fileName.equals("") && containerFolder.getFile(new Path(fileName)).exists()) { //$NON-NLS-1$
            updateStatus("Specified class already exists"); //$NON-NLS-1$
            return;
        }

        if (getClassName().length() == 0) {
            updateStatus("Class name must be specified"); //$NON-NLS-1$
            return;
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            String fileNameWithoutExtention = fileName.substring(0, dotIndex);
            if(fileNameWithoutExtention.matches("^(?=_*[A-z]+)[A-z0-9_]+$")==false) {
                updateStatus("Class name contains illegal characters"); //$NON-NLS-1$
                return;
            }
        }

        updateStatus(null);
    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize() {
        if (selection != null && selection.isEmpty() == false
                && selection instanceof IStructuredSelection) {
            final IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1) {
                return;
            }

            Object obj = ssel.getFirstElement();
            if (obj instanceof IAdaptable) {
                obj = ((IAdaptable) obj).getAdapter(IResource.class);
            }

            IContainer container = null;
            if (obj instanceof IResource) {
                if (obj instanceof IContainer) {
                    container = (IContainer) obj;
                } else {
                    container = ((IResource) obj).getParent();
                }
            }

            if (container != null) {
                sourceFolder.setText(container.getFullPath().toString());
            }
        }
    }
    
    /**
     * Uses the standard container selection dialog to choose the new value for
     * the container field.
     */
    private void handleBrowse() {
        final ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select New File Folder"); //$NON-NLS-1$
        dialog.showClosedProjects(false);
        
        if (dialog.open() == Window.OK) {
            final Object[] result = dialog.getResult();
            if (result.length == 1)
                sourceFolder.setText(((Path) result[0]).toOSString());
        }
    }

    public String getContainerName() {
        return sourceFolder.getText();
    }

    public IProject getProject() {
        String projectName = getContainerName();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(new Path(projectName));
        IProject project = null;
        if (resource instanceof IProject) {
            project = (IProject) resource;
        } else if (resource != null) {
            project = resource.getProject();
        }
        return project;
    }
    
    public IScriptProject getScriptProject() {
        return new ScriptProject(getProject(), null);
    }
    
    /**
     * Opens a selection dialog that allows to select a super class.
     *
     * @return returns the selected type or <code>null</code> if the dialog has been canceled.
     * The caller typically sets the result to the super class input field.
     *  <p>
     * Clients can override this method if they want to offer a different dialog.
     * </p>
     *
     * @since 3.2
     */
    @SuppressWarnings("restriction")
    protected IType chooseSuperClass() {
        IScriptProject project = getScriptProject();
        if (project == null) {
            return null;
        }
        
        //@TODO need to get the silverstripe classes here
        IModelElement[] elements= new IModelElement[] { project };
        IDLTKSearchScope scope= SearchEngine.createSearchScope(elements,IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES|IJavaSearchScope.SYSTEM_LIBRARIES|IJavaSearchScope.REFERENCED_PROJECTS, project.getLanguageToolkit());

        FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(), false, getWizard().getContainer(), scope, ISilverStripePDTSearchConstants.CLASS, project.getLanguageToolkit());
        dialog.setTitle("Superclass Selection");
        dialog.setMessage("Choose a class");
        dialog.setInitialPattern(getSuperClass());

        if (dialog.open() == Window.OK) {
            return (IType) dialog.getFirstResult();
        }
        return null;
    }

    /**
     * Opens a selection dialog that allows to select an interface
     *
     * @return returns the selected type or <code>null</code> if the dialog has been canceled.
     * The caller typically sets the result to the super class input field.
     *  <p>
     * Clients can override this method if they want to offer a different dialog.
     * </p>
     *
     * @since 3.2
     */
    @SuppressWarnings("restriction")
    protected IType chooseInterface() {
        IScriptProject project = getScriptProject();
        if (project == null) {
            return null;
        }
        
        //@TODO need to get the silverstripe classes here
        IModelElement[] elements= new IModelElement[] { project };
        IDLTKSearchScope scope= SearchEngine.createSearchScope(elements,IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES|IJavaSearchScope.SYSTEM_LIBRARIES|IJavaSearchScope.REFERENCED_PROJECTS, project.getLanguageToolkit());

        FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(), false, getWizard().getContainer(), scope, ISilverStripePDTSearchConstants.INTERFACE, project.getLanguageToolkit());
        dialog.setTitle("Interface Selection");
        dialog.setMessage("Choose an interface");

        if (dialog.open() == Window.OK) {
            return (IType) dialog.getFirstResult();
        }
        return null;
    }
    
    /**
     * Returns the content of the classname input field.
     * 
     * @return the classname name
     */
    public String getClassName() {
        return className.getText();
    }
    
    /**
     * Returns the content of the superclass input field.
     * 
     * @return the superclass name
     */
    public String getSuperClass() {
        return superClass.getText();
    }
    
    /**
     * Returns the selected interfaces
     * 
     * @return selected interfaces
     */
    public String[] getInterfaces() {
        return interfaces.getItems();
    }
    
    /**
     * Returns if the super construct checkbox is selected
     * 
     * @return returns true if the super construct is selected
     */
    public Boolean getSuperConstruct() {
        return btnSuperConstruct.getSelection();
    }
    
    /**
     * Returns if the abstract checkbox is selected
     * 
     * @return returns true if the abstract method is selected
     */
    public Boolean getAbstractMethods() {
        return btnAbstractMethods.getSelection();
    }
    
    /**
     * Creates the new type using the entered field values.
     *
     * @param monitor a progress monitor to report progress.
     * @throws CoreException Thrown when the creation failed.
     * @throws InterruptedException Thrown when the operation was canceled.
     */
    public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
        if (monitor == null) {
            monitor= new NullProgressMonitor();
        }

        monitor.beginTask("Generating Class", 8);


        boolean needsSave;
        //ICompilationUnit connectedCU= null;

        try {
            String typeName= getClassName();


            int indent= 0;

            String lineDelimiter = StubUtility.getLineDelimiterUsed(getScriptProject());
            String tabCharacter = getIndentPrefixes();
            
            String finalFile="<?php"+lineDelimiter;
            
            if(btnClassModifiers[2].getSelection()) {
                finalFile+="final ";
            }else if(btnClassModifiers[1].getSelection()) {
                finalFile+="abstract ";
            }
            
            finalFile+="class "+getClassName();
            
            if(superClassType!=null) {
                finalFile+=" extends "+getSuperClass();
            }
            
            String[] interfaceElements=getInterfaces();
            if(interfaceElements!=null && interfaceElements.length>0) {
                finalFile+=" implements";
                for(int i=0;i<interfaceElements.length;i++) {
                    if(i>0) {
                        finalFile+=",";
                    }
                    
                    finalFile+=" "+interfaceElements[i];
                }
            }
            
            finalFile+=" {"+lineDelimiter+tabCharacter;
            
            if(btnSuperConstruct.getSelection() && superClassType!=null) {
                IMethod[] constructor = PHPModelUtils.getTypeMethod(superClassType, "__construct", false);
                PHPDocBlock docBlock = PHPModelUtils.getDocBlock(constructor[0]);
                
                finalFile+=renderDocBlock(docBlock,lineDelimiter,tabCharacter)+tabCharacter+"public function __construct(";
                
                
                IParameter[] params=constructor[0].getParameters();
                String paramStr="";
                for(int i=0;i<params.length;i++) {
                    finalFile+=params[i].getName()+(params[i].getDefaultValue()!="" ? "="+params[i].getDefaultValue():"");
                    paramStr+=params[i].getName();
                    
                    if(i<params.length-1) {
                        finalFile+=", ";
                        paramStr+=", ";
                    }
                }
                
                finalFile+=") {"+lineDelimiter+tabCharacter+tabCharacter+"parent::__construct("+paramStr+");"+lineDelimiter+tabCharacter+tabCharacter+lineDelimiter+tabCharacter+"}";
            }else {
                finalFile+=lineDelimiter;
            }
            
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }
            
            
            if(getAbstractMethods()) {
                IFile file = new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getContainerName(), typeName+".php", monitor, finalFile+lineDelimiter+"}"+lineDelimiter+"?>"+lineDelimiter, true);
                
                ISourceModule sourceModule = DLTKCore.createSourceModuleFrom(file);
                IType createdType = sourceModule.getType(typeName);
                
                IMethod[] unimplemented=PHPModelUtils.getUnimplementedMethods(createdType, monitor);
                
                for(IMethod method : unimplemented) {
                    try {
                        PHPDocBlock docBlock = PHPModelUtils.getDocBlock(method);
                        
                        String source = method.getSource().trim();
                        source = lineDelimiter+tabCharacter+lineDelimiter+tabCharacter+renderDocBlock(docBlock,lineDelimiter,tabCharacter)+tabCharacter+source.substring(0, source.length()-1);
                        source += " {"+lineDelimiter+tabCharacter+tabCharacter+"//@TODO Automatically created abstract method stub"+lineDelimiter+tabCharacter+"}";
                        finalFile+=source;
                    } catch (ModelException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            
            finalFile+=lineDelimiter+"}"+lineDelimiter+"?>"+lineDelimiter;
            
            new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getContainerName(), typeName+".php", monitor, finalFile, true);
            
            
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }
    
    /**
     * Hook method that is called when evaluating the name of the compilation unit to create. By default, a file extension
     * <code>php</code> is added to the given type name, but implementors can override this behavior.
     *
     * @param typeName the name of the type to create the compilation unit for.
     * @return the name of the compilation unit to be created for the given name
     *
     * @since 3.2
     */
    protected String getCompilationUnitName(String typeName) {
        return typeName + ".php";
    }
    
    public String getIndentPrefixes() {
        StringBuffer result = new StringBuffer();

        // prefix[0] is either '\t' or ' ' x tabWidth, depending on preference
        char indentCharPref = getIndentationChar();
        int indentationSize = getIndentationSize();

        for (int i = 0; i <= indentationSize; i++) {
            StringBuffer prefix = new StringBuffer();
            boolean appendTab = false;

            for (int j = 0; j + i < indentationSize; j++)
                prefix.append(indentCharPref);

            if (i != 0) {
                appendTab = true;
            }

            if (appendTab) {
                prefix.append('\t');
                // remove the tab so that indentation - tab is also an indent
                // prefix
                prefix.deleteCharAt(prefix.length() - 1);
            }
            result.append(prefix.toString());
        }

        result.append(""); //$NON-NLS-1$

        return result.toString();
    }
    
    private int getIndentationSize() {
        String indentSize = CorePreferencesSupport.getInstance().getWorkspacePreferencesValue(PHPCoreConstants.FORMATTER_INDENTATION_SIZE);
        if (indentSize == null) {
            return 1;
        }
        return Integer.valueOf(indentSize).intValue();
    }

    private char getIndentationChar() {
        String useTab = CorePreferencesSupport.getInstance().getWorkspacePreferencesValue(PHPCoreConstants.FORMATTER_USE_TABS);
        if (useTab == null) {
            return '\t';
        }
        return (Boolean.valueOf(useTab).booleanValue()) ? '\t' : ' ';
    }
    
    private String renderDocBlock(PHPDocBlock docBlock, String lineDelemiter, String tabCharacter) {
        String result="/**"+lineDelemiter;
        
        result+=docFormatLine(docBlock.getShortDescription(), lineDelemiter, tabCharacter)+lineDelemiter+tabCharacter+" * "+lineDelemiter;
        
        PHPDocTag[] tags = docBlock.getTags();
        for(PHPDocTag tag : tags) {
            result+=docFormatLine("@"+PHPDocTag.getTagKind(tag.getTagKind())+tag.getValue(), lineDelemiter, tabCharacter)+lineDelemiter;
        }
        
        return result+tabCharacter+" */"+lineDelemiter;
    }
    
    private String docFormatLine(String content, String lineDelemiter, String tabCharacter) {
        content = content.trim();
        
        String[] lines = content.split("\r?\n|\r");
        
        for(int i=0;i<lines.length;i++) {
            lines[i]=tabCharacter+" * "+lines[i].trim();
        }
        
        return implodeArray(lines, lineDelemiter);
    }
    
    /**
     * Method to join array elements of type string
     * 
     * @author Hendrik Will, imwill.com
     * @param inputArray Array which contains strings
     * @param glueString String between each array element
     * @return String containing all array elements seperated by glue string
     */
    public static String implodeArray(String[] inputArray, String glueString) {

        /** Output variable */
        String output = "";

        if (inputArray.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(inputArray[0]);

            for (int i = 1; i < inputArray.length; i++) {
                sb.append(glueString);
                sb.append(inputArray[i]);
            }

            output = sb.toString();
        }

        return output;
    }
}
