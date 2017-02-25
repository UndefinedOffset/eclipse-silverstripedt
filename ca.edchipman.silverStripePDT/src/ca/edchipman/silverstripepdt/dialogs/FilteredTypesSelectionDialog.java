/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ca.edchipman.silverstripepdt.dialogs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.core.Flags;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.WorkingCopyOwner;
import org.eclipse.dltk.core.index2.search.ModelAccess;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.NopTypeNameRequestor;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.TypeNameMatch;
import org.eclipse.dltk.core.search.TypeNameMatchRequestor;
import org.eclipse.dltk.internal.core.search.DLTKSearchTypeNameMatch;
import org.eclipse.dltk.internal.corext.util.Messages;
import org.eclipse.dltk.internal.corext.util.OpenTypeHistory;
import org.eclipse.dltk.internal.corext.util.Strings;
import org.eclipse.dltk.internal.corext.util.TypeFilter;
import org.eclipse.dltk.internal.corext.util.TypeInfoRequestorAdapter;
import org.eclipse.dltk.internal.ui.DLTKUIMessages;
import org.eclipse.dltk.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.dltk.internal.ui.search.DLTKSearchScopeFactory;
import org.eclipse.dltk.internal.ui.util.TypeNameMatchLabelProvider;
import org.eclipse.dltk.internal.ui.workingsets.WorkingSetFilterActionGroup;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.ui.DLTKUILanguageManager;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
import org.eclipse.dltk.ui.ScriptElementImageProvider;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.dltk.ui.dialogs.*;
import org.eclipse.dltk.ui.util.ExceptionHandler;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.SearchPattern;

import ca.edchipman.silverstripepdt.search.ISilverStripePDTSearchConstants;

/**
 * Shows a list of Java types to the user with a text entry field for a string
 * pattern used to filter the list of types.
 * 
 * @since 3.3
 */
@SuppressWarnings("restriction")
public class FilteredTypesSelectionDialog extends FilteredItemsSelectionDialog implements ITypeSelectionComponent {
    private static final String DIALOG_SETTINGS = "org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog"; //$NON-NLS-1$

    private static final String WORKINGS_SET_SETTINGS = "WorkingSet"; //$NON-NLS-1$

    private WorkingSetFilterActionGroup fFilterActionGroup;

    private final TypeItemLabelProvider fTypeInfoLabelProvider;

    private String fTitle;

    private IDLTKSearchScope fSearchScope;

    private boolean fAllowScopeSwitching;

    private final int fElementKinds;

    private final ITypeInfoFilterExtension fFilterExtension;

    private final TypeSelectionExtension fExtension;

    private ISelectionStatusValidator fValidator;

    private final TypeInfoUtil fTypeInfoUtil;

    private static boolean fgFirstTime = true;

    private final TypeItemsComparator fTypeItemsComparator;

    private int fTypeFilterVersion = 0;

    private IDLTKLanguageToolkit fToolkit;

    /**
     * Creates new FilteredTypesSelectionDialog instance
     * 
     * @param parent
     *            shell to parent the dialog on
     * @param multi
     *            <code>true</code> if multiple selection is allowed
     * @param context
     *            context used to execute long-running operations associated
     *            with this dialog
     * @param scope
     *            scope used when searching for types
     * @param elementKinds
     *            flags defining nature of searched elements; the only valid
     *            values are: <code>IJavaSearchConstants.TYPE</code>
     *            <code>IJavaSearchConstants.ANNOTATION_TYPE</code>
     *            <code>IJavaSearchConstants.INTERFACE</code>
     *            <code>IJavaSearchConstants.ENUM</code>
     *            <code>IJavaSearchConstants.CLASS_AND_INTERFACE</code>
     *            <code>IJavaSearchConstants.CLASS_AND_ENUM</code>. Please note
     *            that the bitwise OR combination of the elementary constants is
     *            not supported.
     */
    public FilteredTypesSelectionDialog(Shell parent, boolean multi,
            IRunnableContext context, IDLTKSearchScope scope, int elementKinds,
            IDLTKLanguageToolkit toolkit) {
        this(parent, multi, context, scope, elementKinds, null, toolkit);
    }

    /**
     * Creates new FilteredTypesSelectionDialog instance.
     * 
     * @param shell
     *            shell to parent the dialog on
     * @param multi
     *            <code>true</code> if multiple selection is allowed
     * @param context
     *            context used to execute long-running operations associated
     *            with this dialog
     * @param scope
     *            scope used when searching for types. If the scope is
     *            <code>null</code>, then workspace is scope is used as default,
     *            and the user can choose a working set as scope.
     * @param elementKinds
     *            flags defining nature of searched elements; the only valid
     *            values are: <code>IJavaSearchConstants.TYPE</code>
     *            <code>IJavaSearchConstants.ANNOTATION_TYPE</code>
     *            <code>IJavaSearchConstants.INTERFACE</code>
     *            <code>IJavaSearchConstants.ENUM</code>
     *            <code>IJavaSearchConstants.CLASS_AND_INTERFACE</code>
     *            <code>IJavaSearchConstants.CLASS_AND_ENUM</code>. Please note
     *            that the bitwise OR combination of the elementary constants is
     *            not supported.
     * @param extension
     *            an extension of the standard type selection dialog; See
     *            {@link TypeSelectionExtension}
     */
    public FilteredTypesSelectionDialog(Shell shell, boolean multi,
            IRunnableContext context, IDLTKSearchScope scope, int elementKinds,
            TypeSelectionExtension extension, IDLTKLanguageToolkit toolkit) {
        super(shell, multi);

        this.fToolkit = toolkit;

        setSelectionHistory(new TypeSelectionHistory());

        if (scope == null) {
            fAllowScopeSwitching = true;
            scope = SearchEngine.createWorkspaceScope(toolkit);
        }
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
        // IJavaHelpContextIds.TYPE_SELECTION_DIALOG2);

        fElementKinds = elementKinds;
        fExtension = extension;
        fFilterExtension = (extension == null) ? null : extension
                .getFilterExtension();
        fSearchScope = scope;

        if (extension != null) {
            fValidator = extension.getSelectionValidator();
        }

        fTypeInfoUtil = new TypeInfoUtil(extension != null ? extension
                .getImageProvider() : null);

        fTypeInfoLabelProvider = new TypeItemLabelProvider();

        setListLabelProvider(fTypeInfoLabelProvider);
        setListSelectionLabelDecorator(fTypeInfoLabelProvider);
        setDetailsLabelProvider(new TypeItemDetailsLabelProvider(fTypeInfoUtil));

        fTypeItemsComparator = new TypeItemsComparator();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.SelectionDialog#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        super.setTitle(title);
        fTitle = title;
    }

    /**
     * Adds or replaces subtitle of the dialog
     * 
     * @param text
     *            the new subtitle for this dialog
     */
    private void setSubtitle(String text) {
        if (text == null || text.length() == 0) {
            getShell().setText(fTitle);
        } else {
            getShell()
                    .setText(
                            Messages
                                    .format(
                                            DLTKUIMessages.FilteredTypeSelectionDialog_titleFormat,
                                            new String[] { fTitle, text }));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.AbstractSearchDialog#getDialogSettings()
     */
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = DLTKUIPlugin.getDefault()
                .getDialogSettings().getSection(DIALOG_SETTINGS);

        if (settings == null) {
            settings = DLTKUIPlugin.getDefault().getDialogSettings()
                    .addNewSection(DIALOG_SETTINGS);
        }

        return settings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.AbstractSearchDialog#storeDialog(org.eclipse.jface
     * .dialogs.IDialogSettings)
     */
    protected void storeDialog(IDialogSettings settings) {
        super.storeDialog(settings);

        if (fFilterActionGroup != null) {
            XMLMemento memento = XMLMemento.createWriteRoot("workingSet"); //$NON-NLS-1$
            fFilterActionGroup.saveState(memento);
            fFilterActionGroup.dispose();
            StringWriter writer = new StringWriter();
            try {
                memento.save(writer);
                settings.put(WORKINGS_SET_SETTINGS, writer.getBuffer()
                        .toString());
            } catch (IOException e) {
                // don't do anything. Simply don't store the settings
                DLTKUIPlugin.log(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.AbstractSearchDialog#restoreDialog(org.eclipse
     * .jface.dialogs.IDialogSettings)
     */
    protected void restoreDialog(IDialogSettings settings) {
        super.restoreDialog(settings);

        fTypeInfoLabelProvider.setContainerInfo(true);

        if (fAllowScopeSwitching) {
            String setting = settings.get(WORKINGS_SET_SETTINGS);
            if (setting != null) {
                try {
                    IMemento memento = XMLMemento
                            .createReadRoot(new StringReader(setting));
                    fFilterActionGroup.restoreState(memento);
                } catch (WorkbenchException e) {
                    // don't do anything. Simply don't restore the settings
                    DLTKUIPlugin.log(e);
                }
            }
            IWorkingSet ws = fFilterActionGroup.getWorkingSet();
            if (ws == null || (ws.isAggregateWorkingSet() && ws.isEmpty())) {
                setSearchScope(SearchEngine.createWorkspaceScope(fToolkit));
                setSubtitle(null);
            } else {
                setSearchScope(DLTKSearchScopeFactory.getInstance()
                        .createSearchScope(ws, true, fToolkit));
                setSubtitle(ws.getLabel());
            }
        }

        // TypeNameMatch[] types = OpenTypeHistory.getInstance().getTypeInfos();
        //
        // for (int i = 0; i < types.length; i++) {
        // TypeNameMatch type = types[i];
        // accessedHistoryItem(type);
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.AbstractSearchDialog#fillViewMenu(org.eclipse.
     * jface.action.IMenuManager)
     */
    protected void fillViewMenu(IMenuManager menuManager) {
        super.fillViewMenu(menuManager);

        if (fAllowScopeSwitching) {
            fFilterActionGroup = new WorkingSetFilterActionGroup(getShell(),
                    DLTKUIPlugin.getActivePage(),
                    new IPropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            IWorkingSet ws = (IWorkingSet) event.getNewValue();
                            if (ws == null
                                    || (ws.isAggregateWorkingSet() && ws
                                            .isEmpty())) {
                                setSearchScope(SearchEngine
                                        .createWorkspaceScope(fToolkit));
                                setSubtitle(null);
                            } else {
                                setSearchScope(DLTKSearchScopeFactory
                                        .getInstance().createSearchScope(ws,
                                                true, fToolkit));
                                setSubtitle(ws.getLabel());
                            }

                            applyFilter();
                        }
                    });
            fFilterActionGroup.fillViewMenu(menuManager);
        }

        // menuManager.add(new Separator());
        // menuManager.add(new TypeFiltersPreferencesAction());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea
     * (org.eclipse.swt.widgets.Composite)
     */
    protected Control createExtendedContentArea(Composite parent) {
        Control addition = null;

        if (fExtension != null) {

            addition = fExtension.createContentArea(parent);
            if (addition != null) {
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.horizontalSpan = 2;
                addition.setLayoutData(gd);

            }

            fExtension.initialize(this);
        }

        return addition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.SelectionDialog#setResult(java.util.List)
     */
    protected void setResult(List newResult) {

        List resultToReturn = new ArrayList();

        for (int i = 0; i < newResult.size(); i++) {
            if (newResult.get(i) instanceof TypeNameMatch) {
                IType type = ((TypeNameMatch) newResult.get(i)).getType();
                if (type.exists()) {
                    // items are added to history in the
                    // org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#computeResult()
                    // method
                    resultToReturn.add(type);
                } else {
                    TypeNameMatch typeInfo = (TypeNameMatch) newResult.get(i);
                    IProjectFragment root = typeInfo.getProjectFragment();
                    IDLTKUILanguageToolkit uiToolkit = DLTKUILanguageManager
                            .getLanguageToolkit(fToolkit.getNatureId());
                    ScriptElementLabels labels = uiToolkit
                            .getScriptElementLabels();
                    String containerName = labels.getElementLabel(root,
                            ScriptElementLabels.ALL_FULLY_QUALIFIED);
                    String message = Messages
                            .format(
                                    DLTKUIMessages.FilteredTypesSelectionDialog_dialogMessage,
                                    new String[] {
                                            typeInfo.getFullyQualifiedName(),
                                            containerName });
                    MessageDialog.openError(getShell(), fTitle, message);
                    getSelectionHistory().remove(typeInfo);
                }
            }
        }

        super.setResult(resultToReturn);
    }

    /*
     * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#create()
     */
    public void create() {
        super.create();
        Control patternControl = getPatternControl();
        if (patternControl instanceof Text) {
            TextFieldNavigationHandler.install((Text) patternControl);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#open()
     */
    public int open() {
        if (getInitialPattern() == null) {
            IWorkbenchWindow window = DLTKUIPlugin.getActiveWorkbenchWindow();
            if (window != null) {
                ISelection selection = window.getSelectionService()
                        .getSelection();
                if (selection instanceof ITextSelection) {
                    String text = ((ITextSelection) selection).getText();
                    if (text != null) {
                        text = text.trim();
                        if (text.length() > 0) {
                            setInitialPattern(text, FULL_SELECTION);
                        }
                    }
                }
            }
        }
        return super.open();
    }

    /**
     * Sets a new validator.
     * 
     * @param validator
     *            the new validator
     */
    public void setValidator(ISelectionStatusValidator validator) {
        fValidator = validator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
     */
    protected ItemsFilter createFilter() {
        return new TypeItemsFilter(fSearchScope, fElementKinds,
                fFilterExtension);
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        // if (ColoredViewersManager.showColoredLabels()) {
        // if (contents instanceof Composite) {
        // Table listControl = findTableControl((Composite) contents);
        // if (listControl != null) {
        // installOwnerDraw(listControl);
        // }
        // }
        // }
        return contents;
    }

    // private void installOwnerDraw(Table tableControl) {
    // new OwnerDrawSupport(tableControl) { // installs the owner draw
    // // listeners
    // public ColoredString getColoredLabel(Item item) {
    // String text = item.getText();
    // ColoredString str = new ColoredString(text);
    // int index = text.indexOf('-');
    // if (index != -1) {
    // str.colorize(index, str.length() - index,
    // ColoredJavaElementLabels.QUALIFIER_STYLE);
    // }
    // return str;
    // }
    //
    // public Color getColor(String foregroundColorName, Display display) {
    // return PlatformUI.getWorkbench().getThemeManager()
    // .getCurrentTheme().getColorRegistry().get(
    // foregroundColorName);
    // }
    // };
    // }
    //
    // private Table findTableControl(Composite composite) {
    // Control[] children = composite.getChildren();
    // for (int i = 0; i < children.length; i++) {
    // Control curr = children[i];
    // if (curr instanceof Table) {
    // return (Table) curr;
    // } else if (curr instanceof Composite) {
    // Table res = findTableControl((Composite) curr);
    // if (res != null) {
    // return res;
    // }
    // }
    // }
    // return null;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider
     * (org
     * .eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider,
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void fillContentProvider(AbstractContentProvider provider,
            ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
            throws CoreException {
        TypeItemsFilter typeSearchFilter = (TypeItemsFilter) itemsFilter;
        TypeSearchRequestor requestor = new TypeSearchRequestor(provider,
                typeSearchFilter);
        String typePattern = itemsFilter.getPattern();

        progressMonitor
                .setTaskName(DLTKUIMessages.FilteredTypesSelectionDialog_searchJob_taskName);

        IType[] types = new ModelAccess().findTypes(typePattern, ModelAccess
                .convertSearchRule(itemsFilter.getMatchRule()), 0,
                Modifiers.AccNameSpace, typeSearchFilter.getSearchScope(),
                progressMonitor);
        if (types != null) {
            for (IType type : types) {
                requestor.acceptTypeNameMatch(new DLTKSearchTypeNameMatch(type,
                        type.getFlags()));
            }
        } else {

            SearchEngine engine = new SearchEngine((WorkingCopyOwner) null);
            String packPattern = typeSearchFilter.getPackagePattern();

            /*
             * Setting the filter into match everything mode avoids filtering
             * twice by the same pattern (the search engine only provides
             * filtered matches). For the case when the pattern is a camel case
             * pattern with a terminator, the filter is not set to match
             * everything mode because jdt.core's SearchPattern does not support
             * that case.
             */
            int matchRule = typeSearchFilter.getMatchRule();
            if (matchRule == SearchPattern.RULE_CAMELCASE_MATCH) {
                // If the pattern is empty, the RULE_BLANK_MATCH will be chosen,
                // so
                // we don't have to check the pattern length
                char lastChar = typePattern.charAt(typePattern.length() - 1);

                if (lastChar == '<' || lastChar == ' ') {
                    typePattern = typePattern.substring(0,
                            typePattern.length() - 1);
                } else {
                    typeSearchFilter.setMatchEverythingMode(true);
                }
            } else {
                typeSearchFilter.setMatchEverythingMode(true);
            }

            try {
                engine.searchAllTypeNames(
                        packPattern == null ? null : packPattern.toCharArray(),
                        typeSearchFilter.getPackageFlags(),
                        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=176017
                        typePattern.toCharArray(),
                        matchRule,
                        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=176017
                        typeSearchFilter.getElementKind(), typeSearchFilter
                                .getSearchScope(), requestor,
                        IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                        progressMonitor);
            } finally {
                typeSearchFilter.setMatchEverythingMode(false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
     */
    protected Comparator getItemsComparator() {
        return fTypeItemsComparator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java
     * .lang.Object)
     */
    public String getElementName(Object item) {
        TypeNameMatch type = (TypeNameMatch) item;
        return fTypeInfoUtil.getText(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java
     * .lang.Object)
     */
    protected IStatus validateItem(Object item) {

        if (item == null)
            return new Status(IStatus.ERROR, DLTKUIPlugin.getPluginId(),
                    IStatus.ERROR, "", null); //$NON-NLS-1$

        if (fValidator != null) {
            IType type = ((TypeNameMatch) item).getType();
            if (!type.exists())
                return new Status(
                        IStatus.ERROR,
                        DLTKUIPlugin.getPluginId(),
                        IStatus.ERROR,
                        Messages
                                .format(
                                        DLTKUIMessages.FilteredTypesSelectionDialog_error_type_doesnot_exist,
                                        ((TypeNameMatch) item)
                                                .getFullyQualifiedName()), null);
            Object[] elements = { type };
            return fValidator.validate(elements);
        } else
            return new Status(IStatus.OK, DLTKUIPlugin.getPluginId(),
                    IStatus.OK, "", null); //$NON-NLS-1$
    }

    /**
     * Sets search scope used when searching for types.
     * 
     * @param scope
     *            the new scope
     */
    private void setSearchScope(IDLTKSearchScope scope) {
        fSearchScope = scope;
    }

    /*
     * We only have to ensure history consistency here since the search engine
     * takes care of working copies.
     */
    private static class ConsistencyRunnable implements IRunnableWithProgress {
        private IDLTKUILanguageToolkit tookit;

        ConsistencyRunnable(IDLTKLanguageToolkit toolkit) {
            this.tookit = DLTKUILanguageManager.getLanguageToolkit(toolkit
                    .getNatureId());
        }

        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            if (fgFirstTime) {
                // Join the initialize after load job.
                IJobManager manager = Job.getJobManager();
                manager.join(DLTKUIPlugin.PLUGIN_ID, monitor);
            }
            OpenTypeHistory history = OpenTypeHistory.getInstance(tookit);
            if (fgFirstTime || history.isEmpty()) {
                if (history.needConsistencyCheck()) {
                    monitor
                            .beginTask(
                                    DLTKUIMessages.TypeSelectionDialog_progress_consistency,
                                    100);
                    refreshSearchIndices(new SubProgressMonitor(monitor, 90));
                    history
                            .checkConsistency(new SubProgressMonitor(monitor,
                                    10));
                } else {
                    refreshSearchIndices(monitor);
                }
                monitor.done();
                fgFirstTime = false;
            } else {
                history.checkConsistency(monitor);
            }
        }

        public static boolean needsExecution(IDLTKLanguageToolkit toolkit) {
            OpenTypeHistory history = OpenTypeHistory
                    .getInstance(DLTKUILanguageManager
                            .getLanguageToolkit(toolkit.getNatureId()));
            return fgFirstTime || history.isEmpty()
                    || history.needConsistencyCheck();
        }

        private void refreshSearchIndices(IProgressMonitor monitor)
                throws InvocationTargetException {
            try {
                new SearchEngine().searchAllTypeNames(
                        null,
                        0,
                        // make sure we search a concrete name. This is faster
                        // according to Kent
                        "_______________".toCharArray(), //$NON-NLS-1$
                        SearchPattern.RULE_EXACT_MATCH
                                | SearchPattern.RULE_CASE_SENSITIVE,
                        IDLTKSearchConstants.TYPE, SearchEngine
                                .createWorkspaceScope(tookit.getCoreToolkit()),
                        new NopTypeNameRequestor(),
                        IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                        monitor);
            } catch (ModelException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    /*
     * @see
     * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#reloadCache(boolean,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    public void reloadCache(boolean checkDuplicates, IProgressMonitor monitor) {
        IProgressMonitor remainingMonitor;
        if (ConsistencyRunnable.needsExecution(fToolkit)) {
            monitor
                    .beginTask(
                            DLTKUIMessages.TypeSelectionDialog_progress_consistency,
                            10);
            try {
                ConsistencyRunnable runnable = new ConsistencyRunnable(fToolkit);
                runnable.run(new SubProgressMonitor(monitor, 1));
            } catch (InvocationTargetException e) {
                ExceptionHandler.handle(e,
                        DLTKUIMessages.TypeSelectionDialog_error3Title,
                        DLTKUIMessages.TypeSelectionDialog_error3Message);
                close();
                return;
            } catch (InterruptedException e) {
                // cancelled by user
                close();
                return;
            }
            remainingMonitor = new SubProgressMonitor(monitor, 9);
        } else {
            remainingMonitor = monitor;
        }
        super.reloadCache(checkDuplicates, remainingMonitor);
        monitor.done();
    }

    /*
     * @see org.eclipse.jdt.ui.dialogs.ITypeSelectionComponent#triggerSearch()
     */
    public void triggerSearch() {
        fTypeFilterVersion++;
        applyFilter();
    }

    // private class TypeFiltersPreferencesAction extends Action {
    //
    // public TypeFiltersPreferencesAction() {
    // super(
    // DLTKUIMessages.FilteredTypesSelectionDialog_TypeFiltersPreferencesAction_label);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.eclipse.jface.action.Action#run()
    // */
    // public void run() {
    // String typeFilterID = TypeFilterPreferencePage.TYPE_FILTER_PREF_PAGE_ID;
    // PreferencesUtil.createPreferenceDialogOn(getShell(), typeFilterID,
    // new String[] { typeFilterID }, null).open();
    // triggerSearch();
    // }
    // }

    /**
     * A <code>LabelProvider</code> for (the table of) types.
     */
    private class TypeItemLabelProvider extends LabelProvider implements
            ILabelDecorator {

        private boolean fContainerInfo;

        /**
         * Construct a new <code>TypeItemLabelProvider</code>. F
         */
        public TypeItemLabelProvider() {

        }

        public void setContainerInfo(boolean containerInfo) {
            fContainerInfo = containerInfo;
            fireLabelProviderChanged(new LabelProviderChangedEvent(this));
        }

        private boolean isInnerType(TypeNameMatch match) {
            return match.getTypeQualifiedName().indexOf('.') != -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            if (!(element instanceof TypeNameMatch)) {
                return super.getImage(element);
            }

            TypeNameMatch type = (TypeNameMatch) element;

            ImageDescriptor iD = ScriptElementImageProvider
                    .getTypeImageDescriptor(type.getModifiers(), false);

            return DLTKUIPlugin.getImageDescriptorRegistry().get(iD);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (!(element instanceof TypeNameMatch)) {
                return super.getText(element);
            }

            if (fContainerInfo && isDuplicateElement(element)) {
                return fTypeInfoUtil
                        .getFullyQualifiedText((TypeNameMatch) element);
            }

            if (!fContainerInfo && isDuplicateElement(element)) {
                return fTypeInfoUtil.getQualifiedText((TypeNameMatch) element);
            }

            return fTypeInfoUtil.getText(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse
         * .swt.graphics.Image, java.lang.Object)
         */
        public Image decorateImage(Image image, Object element) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.
         * String, java.lang.Object)
         */
        public String decorateText(String text, Object element) {
            if (!(element instanceof TypeNameMatch)) {
                return null;
            }

            if (fContainerInfo && isDuplicateElement(element)) {
                return fTypeInfoUtil
                        .getFullyQualifiedText((TypeNameMatch) element);
            }

            return fTypeInfoUtil.getQualifiedText((TypeNameMatch) element);
        }

    }

    /**
     * A <code>LabelProvider</code> for the label showing type details.
     */
    private class TypeItemDetailsLabelProvider extends LabelProvider {

        private TypeNameMatchLabelProvider fLabelProvider;

        private final TypeInfoUtil fTypeInfoUtil;

        public TypeItemDetailsLabelProvider(TypeInfoUtil typeInfoUtil) {
            fTypeInfoUtil = typeInfoUtil;

            fLabelProvider = new TypeNameMatchLabelProvider(
                    TypeNameMatchLabelProvider.SHOW_TYPE_ONLY
                            + TypeNameMatchLabelProvider.SHOW_FULLYQUALIFIED,
                    DLTKUILanguageManager.getLanguageToolkit(fToolkit
                            .getNatureId()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            if (element instanceof TypeNameMatch) {
                return fLabelProvider.getImage((element));
            }

            return super.getImage(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (element instanceof TypeNameMatch) {
                return fTypeInfoUtil
                        .getQualificationText((TypeNameMatch) element);
            }

            return super.getText(element);
        }
    }

    private class TypeInfoUtil {

        private final ITypeInfoImageProvider fProviderExtension;

        private final TypeInfoRequestorAdapter fAdapter = new TypeInfoRequestorAdapter();

        private final Map fLib2Name = new HashMap();

        private final String[] fInstallLocations;

        private final String[] fVMNames;

        private boolean fFullyQualifyDuplicates;

        public TypeInfoUtil(ITypeInfoImageProvider extension) {
            fProviderExtension = extension;
            List locations = new ArrayList();
            List labels = new ArrayList();
            IInterpreterInstallType[] installs = ScriptRuntime
                    .getInterpreterInstallTypes(fToolkit.getNatureId());
            for (int i = 0; i < installs.length; i++) {
                processInterpreterInstallType(installs[i], locations, labels);
            }
            fInstallLocations = (String[]) locations
                    .toArray(new String[locations.size()]);
            fVMNames = (String[]) labels.toArray(new String[labels.size()]);

        }

        public void setFullyQualifyDuplicates(boolean value) {
            fFullyQualifyDuplicates = value;
        }

        private void processInterpreterInstallType(
                IInterpreterInstallType installType, List locations, List labels) {
            if (installType != null) {
                IInterpreterInstall[] installs = installType
                        .getInterpreterInstalls();
                boolean isMac = Platform.OS_MACOSX.equals(Platform.getOS());
                final String HOME_SUFFIX = "/Home"; //$NON-NLS-1$
                for (int i = 0; i < installs.length; i++) {
                    String label = getFormattedLabel(installs[i].getName());
                    LibraryLocation[] libLocations = installs[i]
                            .getLibraryLocations();
                    if (libLocations != null) {
                        processLibraryLocation(libLocations, label);
                    } else {
                        String filePath = installs[i].getInstallLocation()
                                .toOSString();
                        if (filePath != null) {
                            // on MacOS X install locations end in an additional
                            // "/Home" segment; remove it
                            if (isMac && filePath.endsWith(HOME_SUFFIX))
                                filePath = filePath.substring(0, filePath
                                        .length()
                                        - HOME_SUFFIX.length() + 1);
                            locations.add(filePath);
                            labels.add(label);
                        }
                    }
                }
            }
        }

        private void processLibraryLocation(LibraryLocation[] libLocations,
                String label) {
            for (int l = 0; l < libLocations.length; l++) {
                LibraryLocation location = libLocations[l];
                fLib2Name.put(location.getLibraryPath().toOSString(), label);
            }
        }

        private String getFormattedLabel(String name) {
            return Messages
                    .format(
                            DLTKUIMessages.FilteredTypesSelectionDialog_library_name_format,
                            name);
        }

        public String getText(Object element) {
            return ((TypeNameMatch) element).getSimpleTypeName();
        }

        public String getQualifiedText(TypeNameMatch type) {
            StringBuffer result = new StringBuffer();
            result.append(type.getSimpleTypeName());
            // String containerName = type.getTypeContainerName();
            // result.append(ScriptElementLabels.CONCAT_STRING);
            // if (containerName.length() > 0) {
            // result.append(containerName);
            // } else {
            // result.append("");
            // }
            return result.toString();
        }

        public String getFullyQualifiedText(TypeNameMatch type) {
            StringBuffer result = new StringBuffer();
            result.append(type.getSimpleTypeName());
            IType stype = type.getType();
            if (stype.getParent().getElementType() == IModelElement.TYPE) {
                result.append(ScriptElementLabels.CONCAT_STRING);
                IType parent = (IType) stype.getParent();
                result.append(parent.getTypeQualifiedName(".")); //$NON-NLS-1$
            }
            // String containerName = type.getTypeContainerName();
            // if (containerName.length() > 0) {
            // result.append(ScriptElementLabels.CONCAT_STRING);
            // result.append(containerName);
            // }
            // result.append(ScriptElementLabels.CONCAT_STRING);
            // result.append(getContainerName(type));
            return result.toString();
        }

        public String getText(TypeNameMatch last, TypeNameMatch current,
                TypeNameMatch next) {
            StringBuffer result = new StringBuffer();
            int qualifications = 0;
            String currentTN = current.getSimpleTypeName();
            result.append(currentTN);
            return result.toString();
        }

        public String getQualificationText(TypeNameMatch type) {
            StringBuffer result = new StringBuffer();
            result.append(type.getType().getTypeQualifiedName(".")); //$NON-NLS-1$
            result.append(ScriptElementLabels.CONCAT_STRING);
            result.append(getContainerName(type));
            return result.toString();
        }

        private boolean isInnerType(TypeNameMatch match) {
            return match.getTypeQualifiedName().indexOf('.') != -1;
        }

        public ImageDescriptor getImageDescriptor(Object element) {
            TypeNameMatch type = (TypeNameMatch) element;
            if (fProviderExtension != null) {
                fAdapter.setMatch(type);
                ImageDescriptor descriptor = fProviderExtension
                        .getImageDescriptor(fAdapter);
                if (descriptor != null)
                    return descriptor;
            }
            return ScriptElementImageProvider.getTypeImageDescriptor(type
                    .getModifiers(), false);
        }

        private String getTypeContainerName(TypeNameMatch info) {
            String result = info.getTypeContainerName();
            if (result.length() > 0)
                return result;
            return ""; //$NON-NLS-1$
        }

        private String getContainerName(TypeNameMatch type) {
            IProjectFragment root = type.getProjectFragment();
            if (root.isExternal()) {
                String name = root.getPath().toOSString();
                for (int i = 0; i < fInstallLocations.length; i++) {
                    if (name.startsWith(fInstallLocations[i])) {
                        return fVMNames[i];
                    }
                }
                String lib = (String) fLib2Name.get(name);
                if (lib != null)
                    return lib;
            }
            StringBuffer buf = new StringBuffer();
            ScriptElementLabels labels = DLTKUILanguageManager
                    .getLanguageToolkit(fToolkit.getNatureId())
                    .getScriptElementLabels();
            labels.getProjectFragmentLabel(root,
                    ScriptElementLabels.ROOT_QUALIFIED
                            | ScriptElementLabels.ROOT_VARIABLE, buf);
            return buf.toString();
        }
    }

    /**
     * Filters types using pattern, scope, element kind and filter extension.
     */
    private class TypeItemsFilter extends ItemsFilter {

        private static final int TYPE_MODIFIERS = Flags.AccAnnotation
                | Flags.AccInterface;

        private final IDLTKSearchScope fScope;

        private final boolean fIsWorkspaceScope;

        private final int fElemKind;

        private final ITypeInfoFilterExtension fFilterExt;

        private final TypeInfoRequestorAdapter fAdapter = new TypeInfoRequestorAdapter();

        private SearchPattern fPackageMatcher;

        private boolean fMatchEverything = false;

        private final int fMyTypeFilterVersion = fTypeFilterVersion;

        /**
         * Creates instance of TypeItemsFilter
         * 
         * @param scope
         * @param elementKind
         * @param extension
         */
        public TypeItemsFilter(IDLTKSearchScope scope, int elementKind,
                ITypeInfoFilterExtension extension) {
            super(new TypeSearchPattern());
            fScope = scope;
            fIsWorkspaceScope = scope == null ? false : scope
                    .equals(SearchEngine.createWorkspaceScope(fToolkit));
            fElemKind = elementKind;
            fFilterExt = extension;
            String stringPackage = ((TypeSearchPattern) patternMatcher)
                    .getPackagePattern();
            if (stringPackage != null) {
                fPackageMatcher = new SearchPattern();
                fPackageMatcher.setPattern(stringPackage);
            } else {
                fPackageMatcher = null;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#
         * isSubFilter
         * (org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter)
         */
        public boolean isSubFilter(ItemsFilter filter) {
            if (!super.isSubFilter(filter))
                return false;
            TypeItemsFilter typeItemsFilter = (TypeItemsFilter) filter;
            if (fScope != typeItemsFilter.getSearchScope())
                return false;
            if (fMyTypeFilterVersion != typeItemsFilter
                    .getMyTypeFilterVersion())
                return false;
            return getPattern().indexOf('.', filter.getPattern().length()) == -1;
        }

        public boolean equalsFilter(ItemsFilter iFilter) {
            if (!super.equalsFilter(iFilter))
                return false;
            if (!(iFilter instanceof TypeItemsFilter))
                return false;
            TypeItemsFilter typeItemsFilter = (TypeItemsFilter) iFilter;
            if (fScope != typeItemsFilter.getSearchScope())
                return false;
            if (fMyTypeFilterVersion != typeItemsFilter
                    .getMyTypeFilterVersion())
                return false;
            return true;
        }

        public int getElementKind() {
            return fElemKind;
        }

        public ITypeInfoFilterExtension getFilterExtension() {
            return fFilterExt;
        }

        public IDLTKSearchScope getSearchScope() {
            return fScope;
        }

        public int getMyTypeFilterVersion() {
            return fMyTypeFilterVersion;
        }

        public String getPackagePattern() {
            if (fPackageMatcher == null)
                return null;
            return fPackageMatcher.getPattern();
        }

        public int getPackageFlags() {
            if (fPackageMatcher == null)
                return SearchPattern.RULE_EXACT_MATCH;

            return fPackageMatcher.getMatchRule();
        }

        public boolean matchesRawNamePattern(TypeNameMatch type) {
            return Strings.startsWithIgnoreCase(type.getSimpleTypeName(),
                    getPattern());
        }

        public boolean matchesCachedResult(TypeNameMatch type) {
            if (!(matchesPackage(type) && matchesFilterExtension(type)))
                return false;
            return matchesName(type);
        }

        public boolean matchesHistoryElement(TypeNameMatch type) {
            if (!(matchesPackage(type) && matchesModifiers(type)
                    && matchesScope(type) && matchesFilterExtension(type)))
                return false;
            return matchesName(type);
        }

        public boolean matchesFilterExtension(TypeNameMatch type) {
            if (fFilterExt == null)
                return true;
            fAdapter.setMatch(type);
            return fFilterExt.select(fAdapter);
        }

        private boolean matchesName(TypeNameMatch type) {
            return matches(type.getSimpleTypeName());
        }

        private boolean matchesPackage(TypeNameMatch type) {
            if (fPackageMatcher == null)
                return true;
            return fPackageMatcher.matches(type.getPackageName());
        }

        private boolean matchesScope(TypeNameMatch type) {
            if (fIsWorkspaceScope)
                return true;
            return fScope.encloses(type.getType());

        }

        private boolean matchesModifiers(TypeNameMatch type) {
            if (fElemKind == IDLTKSearchConstants.TYPE)
                return true;
            int modifiers = type.getModifiers() & TYPE_MODIFIERS;
            if (fElemKind == ISilverStripePDTSearchConstants.INTERFACE && modifiers == ISilverStripePDTSearchConstants.INTERFACE) {
                return true;
            }else if (fElemKind == ISilverStripePDTSearchConstants.CLASS && modifiers == ISilverStripePDTSearchConstants.INTERFACE) {
                return false;
            }
            
            switch (fElemKind) {
            case ISilverStripePDTSearchConstants.CLASS:
                return modifiers == 0;
            case IDLTKSearchConstants.TYPE:
                return modifiers == 0;
            case IDLTKSearchConstants.ANNOTATION_TYPE:
                // return Flags.isAnnotation(modifiers);
                return false;
            }
            return false;
        }

        /**
         * Set filter to "match everything" mode.
         * 
         * @param matchEverything
         *            if <code>true</code>, {@link #matchItem(Object)} always
         *            returns true. If <code>false</code>, the filter is
         *            enabled.
         */
        public void setMatchEverythingMode(boolean matchEverything) {
            this.fMatchEverything = matchEverything;
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#
         * isConsistentItem(java.lang.Object)
         */
        public boolean isConsistentItem(Object item) {
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#matchItem
         * (java.lang.Object)
         */
        public boolean matchItem(Object item) {

            if (fMatchEverything)
                return true;

            TypeNameMatch type = (TypeNameMatch) item;
            if (!(matchesPackage(type) && matchesModifiers(type)
                    && matchesScope(type) && matchesFilterExtension(type)))
                return false;
            return matchesName(type);
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#
         * matchesRawNamePattern(java.lang.Object)
         */
        public boolean matchesRawNamePattern(Object item) {
            TypeNameMatch type = (TypeNameMatch) item;
            return matchesRawNamePattern(type);
        }

    }

    /**
     * Extends functionality of SearchPatterns
     */
    private static class TypeSearchPattern extends SearchPattern {

        private String packagePattern;

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.SearchPattern#setPattern(java.lang.String)
         */
        public void setPattern(String stringPattern) {
            String pattern = stringPattern;
            String packPattern = null;
            int index = stringPattern.lastIndexOf("."); //$NON-NLS-1$
            if (index != -1) {
                packPattern = evaluatePackagePattern(stringPattern.substring(0,
                        index));
                pattern = stringPattern.substring(index + 1);
                if (pattern.length() == 0)
                    pattern = "**"; //$NON-NLS-1$
            }
            super.setPattern(pattern);
            packagePattern = packPattern;
        }

        /*
         * Transforms o.e.j to o.e.j
         */
        private String evaluatePackagePattern(String s) {
            StringBuffer buf = new StringBuffer();
            boolean hasWildCard = false;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '.') {
                    if (!hasWildCard) {
                        buf.append('*');
                    }
                    hasWildCard = false;
                } else if (ch == '*' || ch == '?') {
                    hasWildCard = true;
                }
                buf.append(ch);
            }
            if (!hasWildCard) {
                buf.append('*');
            }
            return buf.toString();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.dialogs.SearchPattern#isNameCharAllowed(char)
         */
        protected boolean isNameCharAllowed(char nameChar) {
            return super.isNameCharAllowed(nameChar);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.dialogs.SearchPattern#isPatternCharAllowed(char)
         */
        protected boolean isPatternCharAllowed(char patternChar) {
            return super.isPatternCharAllowed(patternChar);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.dialogs.SearchPattern#isValidCamelCaseChar(char)
         */
        protected boolean isValidCamelCaseChar(char ch) {
            return super.isValidCamelCaseChar(ch);
        }

        /**
         * @return the packagePattern
         */
        public String getPackagePattern() {
            return packagePattern;
        }

    }

    /**
     * A <code>TypeSearchRequestor</code> collects matches filtered using
     * <code>TypeItemsFilter</code>. The attached content provider is filled on
     * the basis of the collected entries (instances of
     * <code>TypeNameMatch</code>).
     */
    private class TypeSearchRequestor extends TypeNameMatchRequestor {
        private volatile boolean fStop;

        private final AbstractContentProvider fContentProvider;

        private final TypeItemsFilter fTypeItemsFilter;
        Set addedNames = new HashSet();

        public TypeSearchRequestor(AbstractContentProvider contentProvider,
                TypeItemsFilter typeItemsFilter) {
            super();
            fContentProvider = contentProvider;
            fTypeItemsFilter = typeItemsFilter;
        }

        public void cancel() {
            fStop = true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jdt.core.search.TypeNameMatchRequestor#acceptTypeNameMatch
         * (org.eclipse.jdt.core.search.TypeNameMatch)
         */
        public void acceptTypeNameMatch(TypeNameMatch match) {
            if (fStop)
                return;
            if (new TypeFilter(DLTKUILanguageManager
                    .getLanguageToolkit(fToolkit.getNatureId()))
                    .isFiltered(match))
                return;
            if (!addedNames.contains(match.getTypeQualifiedName())) {
                addedNames.add(match.getTypeQualifiedName());
                if (fTypeItemsFilter.matchesFilterExtension(match))
                    fContentProvider.add(match, fTypeItemsFilter);
            }
        }
    }

    /**
     * Compares TypeItems is used during sorting
     */
    private class TypeItemsComparator implements Comparator {

        private final Map fLib2Name = new HashMap();

        private final String[] fInstallLocations;

        private final String[] fVMNames;

        /**
         * Creates new instance of TypeItemsComparator
         */
        public TypeItemsComparator() {
            List locations = new ArrayList();
            List labels = new ArrayList();
            IInterpreterInstallType[] installs = ScriptRuntime
                    .getInterpreterInstallTypes();
            for (int i = 0; i < installs.length; i++) {
                processVMInstallType(installs[i], locations, labels);
            }
            fInstallLocations = (String[]) locations
                    .toArray(new String[locations.size()]);
            fVMNames = (String[]) labels.toArray(new String[labels.size()]);
        }

        private void processVMInstallType(IInterpreterInstallType installType,
                List locations, List labels) {
            if (installType != null) {
                IInterpreterInstall[] installs = installType
                        .getInterpreterInstalls();
                boolean isMac = Platform.OS_MACOSX.equals(Platform.getOS());
                final String HOME_SUFFIX = "/Home"; //$NON-NLS-1$
                for (int i = 0; i < installs.length; i++) {
                    String label = getFormattedLabel(installs[i].getName());
                    LibraryLocation[] libLocations = installs[i]
                            .getLibraryLocations();
                    if (libLocations != null) {
                        processLibraryLocation(libLocations, label);
                    } else {
                        String filePath = installs[i].getInstallLocation()
                                .toOSString();
                        // on MacOS X install locations end in an additional
                        // "/Home" segment; remove it
                        if (isMac && filePath.endsWith(HOME_SUFFIX))
                            filePath = filePath.substring(0, filePath.length()
                                    - HOME_SUFFIX.length() + 1);
                        locations.add(filePath);
                        labels.add(label);
                    }
                }
            }
        }

        private void processLibraryLocation(LibraryLocation[] libLocations,
                String label) {
            for (int l = 0; l < libLocations.length; l++) {
                LibraryLocation location = libLocations[l];
                fLib2Name.put(location.getLibraryPath().toString(), label);
            }
        }

        private String getFormattedLabel(String name) {
            return NLS
                    .bind(
                            DLTKUIMessages.FilteredTypesSelectionDialog_library_name_format,
                            name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object left, Object right) {

            TypeNameMatch leftInfo = (TypeNameMatch) left;
            TypeNameMatch rightInfo = (TypeNameMatch) right;

            int result = compareName(leftInfo.getSimpleTypeName(), rightInfo
                    .getSimpleTypeName());
            if (result != 0)
                return result;
            result = compareTypeContainerName(leftInfo.getTypeContainerName(),
                    rightInfo.getTypeContainerName());
            if (result != 0)
                return result;

            int leftCategory = getElementTypeCategory(leftInfo);
            int rightCategory = getElementTypeCategory(rightInfo);
            if (leftCategory < rightCategory)
                return -1;
            if (leftCategory > rightCategory)
                return +1;
            return compareContainerName(leftInfo, rightInfo);
        }

        private int compareName(String leftString, String rightString) {
            int result = leftString.compareToIgnoreCase(rightString);
            if (result != 0 || rightString.length() == 0) {
                return result;
            } else if (Strings.isLowerCase(leftString.charAt(0))
                    && !Strings.isLowerCase(rightString.charAt(0))) {
                return +1;
            } else if (Strings.isLowerCase(rightString.charAt(0))
                    && !Strings.isLowerCase(leftString.charAt(0))) {
                return -1;
            } else {
                return leftString.compareTo(rightString);
            }
        }

        private int compareTypeContainerName(String leftString,
                String rightString) {
            int leftLength = leftString.length();
            int rightLength = rightString.length();
            if (leftLength == 0 && rightLength > 0)
                return -1;
            if (leftLength == 0 && rightLength == 0)
                return 0;
            if (leftLength > 0 && rightLength == 0)
                return +1;
            return compareName(leftString, rightString);
        }

        private int compareContainerName(TypeNameMatch leftType,
                TypeNameMatch rightType) {
            return getContainerName(leftType).compareTo(
                    getContainerName(rightType));
        }

        private String getContainerName(TypeNameMatch type) {
            IProjectFragment root = type.getProjectFragment();
            if (root.isExternal()) {
                String name = root.getPath().toOSString();
                for (int i = 0; i < fInstallLocations.length; i++) {
                    if (name.startsWith(fInstallLocations[i])) {
                        return fVMNames[i];
                    }
                }
                String lib = (String) fLib2Name.get(name);
                if (lib != null)
                    return lib;
            }
            StringBuffer buf = new StringBuffer();
            ScriptElementLabels labels = getUIToolkit()
                    .getScriptElementLabels();
            labels.getProjectFragmentLabel(root,
                    ScriptElementLabels.ROOT_QUALIFIED
                            | ScriptElementLabels.ROOT_VARIABLE, buf);
            return buf.toString();
        }

        private int getElementTypeCategory(TypeNameMatch type) {
            try {
                if (type.getProjectFragment().getKind() == IProjectFragment.K_SOURCE)
                    return 0;
            } catch (ModelException e) {
                DLTKUIPlugin.log(e);
            }
            return 1;
        }
    }

    private IDLTKUILanguageToolkit getUIToolkit() {
        return DLTKUILanguageManager.getLanguageToolkit(fToolkit.getNatureId());
    }

    /**
     * Extends the <code>SelectionHistory</code>, providing support for
     * <code>OpenTypeHistory</code>.
     */
    protected class TypeSelectionHistory extends SelectionHistory {

        /**
         * Creates new instance of TypeSelectionHistory
         */

        public TypeSelectionHistory() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.SelectionHistory
         * #accessed(java.lang.Object)
         */
        public synchronized void accessed(Object object) {
            super.accessed(object);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.SelectionHistory
         * #remove(java.lang.Object)
         */
        public synchronized boolean remove(Object element) {
            OpenTypeHistory.getInstance(getUIToolkit()).remove(
                    (TypeNameMatch) element);
            return super.remove(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.SelectionHistory
         * #load(org.eclipse.ui.IMemento)
         */
        public void load(IMemento memento) {
            TypeNameMatch[] types = OpenTypeHistory.getInstance(getUIToolkit())
                    .getTypeInfos();

            for (int i = 0; i < types.length; i++) {
                TypeNameMatch type = types[i];
                accessed(type);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.SelectionHistory
         * #save(org.eclipse.ui.IMemento)
         */
        public void save(IMemento memento) {
            persistHistory();
        }

        /**
         * Stores contents of the local history into persistent history
         * container.
         */
        private synchronized void persistHistory() {
            if (getReturnCode() == OK) {
                Object[] items = getHistoryItems();
                for (int i = 0; i < items.length; i++) {
                    OpenTypeHistory.getInstance(getUIToolkit()).accessed(
                            (TypeNameMatch) items[i]);
                }
            }
        }

        protected Object restoreItemFromMemento(IMemento element) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.SelectionHistory
         * #storeItemToMemento(java.lang.Object, org.eclipse.ui.IMemento)
         */
        protected void storeItemToMemento(Object item, IMemento element) {

        }
    }
}
