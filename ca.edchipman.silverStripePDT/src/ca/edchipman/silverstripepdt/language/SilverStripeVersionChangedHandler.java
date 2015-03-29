package ca.edchipman.silverstripepdt.language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.php.internal.core.PHPCorePlugin;
import org.eclipse.php.internal.core.preferences.IPreferencesPropagatorListener;
import org.eclipse.php.internal.core.preferences.PreferencePropagatorFactory;
import org.eclipse.php.internal.core.preferences.PreferencesPropagator;
import org.eclipse.php.internal.core.preferences.PreferencesPropagatorEvent;
import org.eclipse.php.internal.core.preferences.PreferencesSupport;

@SuppressWarnings("restriction")
public class SilverStripeVersionChangedHandler implements IResourceChangeListener {

    private static final String SILVERSTRIPE_VERSION = "silverstripe_version";
    private static final String SILVERSTRIPE_FRAMEWORK_MODEL = "silverstripe_framework_model";

    private HashMap<IProject, HashSet> projectListeners = new HashMap<IProject, HashSet>();
    private HashMap<IProject, PreferencesPropagatorListener> preferencesPropagatorListeners = new HashMap<IProject, PreferencesPropagatorListener>();

    private PreferencesPropagator preferencesPropagator;
    private static final String NODES_QUALIFIER = PHPCorePlugin.ID;
    private static final Preferences store = PHPCorePlugin.getDefault().getPluginPreferences();

    private static SilverStripeVersionChangedHandler instance = new SilverStripeVersionChangedHandler();

    private SilverStripeVersionChangedHandler() {
        preferencesPropagator = PreferencePropagatorFactory.getPreferencePropagator(NODES_QUALIFIER, store);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    public static SilverStripeVersionChangedHandler getInstance() {
        return instance;
    }

    private void projectVersionChanged(IProject project,
            PreferencesPropagatorEvent event) {
        HashSet listeners = projectListeners.get(project);
        if (listeners != null) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                IPreferencesPropagatorListener listener = (IPreferencesPropagatorListener) iter
                        .next();
                listener.preferencesEventOccured(event);
            }
        }
    }

    private class PreferencesPropagatorListener implements IPreferencesPropagatorListener {

        private IProject project;

        public PreferencesPropagatorListener(IProject project) {
            this.project = project;
        }

        public void preferencesEventOccured(PreferencesPropagatorEvent event) {
            if (event.getNewValue() == null) {
                // We take the workspace settings since there was a move from
                // project-specific to workspace setings.
                String newValue = PreferencesSupport.getWorkspacePreferencesValue((String) event.getKey(),store);
                if (newValue == null || newValue.equals(event.getOldValue())) {
                    return; // No need to send a notification
                }
                event = new PreferencesPropagatorEvent(event.getSource(), event
                        .getOldValue(), newValue, event.getKey());
            } else if (event.getOldValue() == null) {
                // In this case there was a move from the workspace setting to a
                // project-specific setting.
                // At this stage the new value of the project-specific will
                // always be as the workspace, so there is
                // no need to send a notification.
                String preferencesValue = PreferencesSupport.getWorkspacePreferencesValue((String) event.getKey(),store);
                if (preferencesValue != null
                        && preferencesValue.equals(event.getNewValue())) {
                    return; // No need to send a notification
                }
            }
            
            if(project.isAccessible()) {
                projectVersionChanged(project, event);
            }
        }

        public IProject getProject() {
            return project;
        }

    }

    public void addSilverStripeVersionChangedListener(IPreferencesPropagatorListener listener) {
        IProject project = listener.getProject();
        HashSet<IPreferencesPropagatorListener> listeners = projectListeners.get(project);
        if (listeners == null) {
            projectAdded(project);
            listeners = projectListeners.get(project);
        }
        listeners.add(listener);
    }

    public void removeSilverStripeVersionChangedListener(IPreferencesPropagatorListener listener) {
        if (listener == null) {// this was added since when working with RSE
            // project model, listener was NULL
            return;
        }
        IProject project = listener.getProject();
        HashSet listeners = projectListeners.get(project);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void projectAdded(IProject project) {
        if (project == null || projectListeners.get(project) != null) {
            return;
        }
        projectListeners.put(project, new HashSet());

        // register as a listener to the PP on this project
        PreferencesPropagatorListener listener = new PreferencesPropagatorListener(project);
        preferencesPropagatorListeners.put(project, listener);
        preferencesPropagator.addPropagatorListener(listener, SILVERSTRIPE_VERSION);
        preferencesPropagator.addPropagatorListener(listener, SILVERSTRIPE_FRAMEWORK_MODEL);
    }

    public void projectRemoved(IProject project) {
        PreferencesPropagatorListener listener = preferencesPropagatorListeners.get(project);
        if (listener == null) {
            return;
        }
        preferencesPropagator.removePropagatorListener(listener, SILVERSTRIPE_VERSION);
        preferencesPropagator.removePropagatorListener(listener, SILVERSTRIPE_FRAMEWORK_MODEL);
        preferencesPropagatorListeners.remove(project);

        projectListeners.remove(project);
    }

    private void checkProjectsBeingAddedOrRemoved(IResourceDelta delta) {
        if (delta == null) {
            return;
        }
        IResource resource = delta.getResource();
        IResourceDelta[] children = null;

        switch (resource.getType()) {
        case IResource.ROOT:
            children = delta.getAffectedChildren();
            break;
        case IResource.PROJECT:
            IProject project = (IProject) resource;
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
                projectAdded(project);
                break;
            case IResourceDelta.CHANGED:
                if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
                    // project opened or closed
                    if (project.isOpen()) {
                        projectAdded(project);
                    } else {
                        projectRemoved(project);
                    }
                }
                break;
            case IResourceDelta.REMOVED:
                projectRemoved(project);
                break;
            }
            break;
        }
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                this.checkProjectsBeingAddedOrRemoved(children[i]);
            }
        }
    }

    public void resourceChanged(IResourceChangeEvent event) {
        checkProjectsBeingAddedOrRemoved(event.getDelta());
    }
}