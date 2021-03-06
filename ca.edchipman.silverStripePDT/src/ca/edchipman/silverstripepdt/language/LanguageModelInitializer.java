package ca.edchipman.silverstripepdt.language;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.dltk.core.*;
import org.eclipse.php.core.language.ILanguageModelProvider;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.core.preferences.IPreferencesPropagatorListener;
import org.eclipse.php.internal.core.preferences.PreferencesPropagatorEvent;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.core.util.project.observer.IProjectClosedObserver;
import org.eclipse.php.internal.core.util.project.observer.ProjectRemovedObserversAttacher;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripePreferences;
import ca.edchipman.silverstripepdt.SilverStripeVersion;

@SuppressWarnings("restriction")
public class LanguageModelInitializer extends BuildpathContainerInitializer {

    public static final String SILVERSTRIPE_LANGUAGE_LIBRARY = "SilverStripe Language Library";

    /**
     * Path of the language model for php projects
     */
    public static final String CONTAINER_PATH = SilverStripePDTPlugin.NATURE_ID; //$NON-NLS-1$
    public static final Path LANGUAGE_CONTAINER_PATH = new Path(LanguageModelInitializer.CONTAINER_PATH);

    /**
     * Listeners for PHP version change map (per project)
     */
    private Map<IProject, IPreferencesPropagatorListener> project2SilverStripeVerListener = new HashMap<IProject, IPreferencesPropagatorListener>();

    /**
     * Language model paths initializers
     */
    private static ILanguageModelProvider[] providers;

    /**
     * Holds nice names for the language model paths
     */
    private static Map<IPath, String> pathToName = Collections.synchronizedMap(new HashMap<IPath, String>());

    static void addPathName(IPath path, String name) {
        pathToName.put(path, name);
    }

    /**
     * Returns nice name for this language model path provided by the
     * {@link ILanguageModelProvider}. If the path doesn't refer to the language
     * model path - <code>null</code> is returned.
     * 
     * @return
     */
    public static String getPathName(IPath path) {
        return pathToName.get(path);
    }

    /**
     * Initialize version change listener for the given project
     * 
     * @param containerPath
     * @param scriptProject
     */
    private void initializeListener(final IPath containerPath, final IScriptProject scriptProject) {
        final IProject project = scriptProject.getProject();
        if (project2SilverStripeVerListener.containsKey(project)) {
            return;
        }
        IPreferencesPropagatorListener versionChangeListener = new IPreferencesPropagatorListener() {
            public void preferencesEventOccured(PreferencesPropagatorEvent event) {
                try {
                    // Re-initialize when PHP version changes
                    initialize(containerPath, scriptProject);
                } catch (CoreException e) {
                    Logger.logException(e);
                }
            }

            public IProject getProject() {
                return project;
            }
        };

        project2SilverStripeVerListener.put(project, versionChangeListener);
        SilverStripeVersionChangedHandler.getInstance().addSilverStripeVersionChangedListener(versionChangeListener);

        ProjectRemovedObserversAttacher.getInstance().addProjectClosedObserver(
                project, new IProjectClosedObserver() {
                    public void closed() {
                        SilverStripeVersionChangedHandler.getInstance().removeSilverStripeVersionChangedListener(project2SilverStripeVerListener.remove(project));
                    }
                });
    }

    public void initialize(IPath containerPath, IScriptProject scriptProject) throws CoreException {
        if (containerPath.segmentCount() > 0  && containerPath.segment(0).equals(CONTAINER_PATH)) {
            try {
                if(isPHPProject(scriptProject)) {
                    DLTKCore.setBuildpathContainer(containerPath, new IScriptProject[] { scriptProject }, new IBuildpathContainer[] { new LanguageModelContainer(containerPath, scriptProject) }, null);
                    initializeListener(containerPath, scriptProject);
                }
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
    }

    private static boolean isPHPProject(IScriptProject project) {
        String nature = getNatureFromProject(project);
        return PHPNature.ID.equals(nature);
    }

    private static String getNatureFromProject(IScriptProject project) {
        IDLTKLanguageToolkit languageToolkit = DLTKLanguageManager.getLanguageToolkit(project);
        if (languageToolkit != null) {
            return languageToolkit.getNatureId();
        }
        
        return null;
    }

    public static boolean isLanguageModelElement(IModelElement element) {
        if (element != null) {
            IProjectFragment fragment = (IProjectFragment) element.getAncestor(IModelElement.PROJECT_FRAGMENT);
            if (fragment != null && fragment.isExternal()) {
                IPath path = fragment.getPath();

                // see getTargetLocation() below for description:
                if (path.segmentCount() > 2) {
                    return "__language__".equals(path.segment(path .segmentCount() - 2));
                }
            }
        }
        return false;
    }

    /**
     * Modifies PHP project buildpath so it will contain path to the language
     * model library
     * 
     * @param project
     *            Project handle
     * @throws ModelException
     */
    public static void enableLanguageModelFor(IScriptProject project) throws ModelException {
        if (!isPHPProject(project)) {
            return;
        }

        boolean found = false;
        IBuildpathEntry[] rawBuildpath = project.getRawBuildpath();
        for (IBuildpathEntry entry : rawBuildpath) {
            if (entry.getEntryKind()==IBuildpathEntry.BPE_CONTAINER && entry.getPath().equals(LANGUAGE_CONTAINER_PATH)) {
                found = true;
                break;
            }
        }

        if (!found) {
            IBuildpathEntry containerEntry = DLTKCore.newContainerEntry(LANGUAGE_CONTAINER_PATH);
            int newSize = rawBuildpath.length + 1;
            List<IBuildpathEntry> newRawBuildpath = new ArrayList<IBuildpathEntry>(newSize);
            newRawBuildpath.addAll(Arrays.asList(rawBuildpath));
            newRawBuildpath.add(containerEntry);
            project.setRawBuildpath(newRawBuildpath.toArray(new IBuildpathEntry[newSize]), null);
        }
    }

    static ILanguageModelProvider[] getContributedProviders() {
        if (LanguageModelInitializer.providers == null) {
            List<ILanguageModelProvider> providers = new LinkedList<ILanguageModelProvider>();
            providers.add(new DefaultLanguageModelProvider()); // add default

            LanguageModelInitializer.providers = (ILanguageModelProvider[]) providers.toArray(new ILanguageModelProvider[providers.size()]);
        }
        return LanguageModelInitializer.providers;
    }
    
    static IPath getTargetLocation(ILanguageModelProvider provider, IPath sourcePath, IScriptProject project) {
        IPath destination=provider
                                .getPlugin()
                                .getStateLocation()
                                .append("__language__")
                                .append(sourcePath.lastSegment());
        
        return destination;
    }
}
