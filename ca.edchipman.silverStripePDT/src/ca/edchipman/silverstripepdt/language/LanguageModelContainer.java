package ca.edchipman.silverstripepdt.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathContainer;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.internal.core.BuildpathEntry;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.php.core.language.ILanguageModelProvider;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;

import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.versioninterfaces.ISilverStripeLanguageModelProvider;

@SuppressWarnings("restriction")
public class LanguageModelContainer implements IBuildpathContainer {

    private IPath containerPath;
    private IBuildpathEntry[] buildPathEntries;
    private IScriptProject fProject;
    private Job buildJob;

    public LanguageModelContainer(IPath containerPath, IScriptProject project) {
        this.containerPath = containerPath;
        this.fProject = project;
    }

    public IBuildpathEntry[] getBuildpathEntries(IScriptProject project) {
        if (buildPathEntries == null) {
            try {
                List<IBuildpathEntry> entries = new LinkedList<IBuildpathEntry>();

                for (ILanguageModelProvider provider:LanguageModelInitializer.getContributedProviders()) {

                    // Get the location where language model files reside
                    // in provider's plug-in:
                    IPath path = provider.getPath(project);
                    if (path != null) {

                        // Copy files (if target directory is older) to the
                        // plug-in state
                        // location:
                        path = copyToInstanceLocation(provider, path, project);
                        if (path != null) {

                            LanguageModelInitializer.addPathName(path, provider
                                    .getName());

                            IEnvironment environment = EnvironmentManager
                                    .getEnvironment(project);
                            if (environment != null) {
                                path = EnvironmentPathUtils.getFullPath(
                                        environment, path);
                            }
                            entries.add(DLTKCore.newLibraryEntry(path,
                                    BuildpathEntry.NO_ACCESS_RULES,
                                    BuildpathEntry.NO_EXTRA_ATTRIBUTES,
                                    BuildpathEntry.INCLUDE_ALL,
                                    BuildpathEntry.EXCLUDE_NONE, false, true));
                        }
                    }
                }
                buildPathEntries = (IBuildpathEntry[]) entries
                        .toArray(new IBuildpathEntry[entries.size()]);
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
        return buildPathEntries;
    }

    protected IPath copyToInstanceLocation(ILanguageModelProvider provider, IPath path, IScriptProject project) {
        try {
            ISilverStripeLanguageModelProvider ssLangProvider=((DefaultLanguageModelProvider) provider).getLanguageModelProvider(project);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("$nl$", Platform.getNL()); //$NON-NLS-1$
            URL url = FileLocator.find(((DefaultLanguageModelProvider) provider).getPlugin(project).getBundle(), provider.getPath(project), map);
            File sourceFile = new File(FileLocator.toFileURL(url).getPath());
            LocalFile sourceDir = new LocalFile(sourceFile);

            IPath targetPath = LanguageModelInitializer.getTargetLocation(provider, Path.fromOSString(sourceFile.getAbsolutePath()), project);
            IPath rootPath = (IPath) targetPath.clone();
            
            //If we already know this language is up to date return the target path here
            if(ssLangProvider.getPackedLangUpToDate()==true) {
                return targetPath;
            }
            
            LocalFile targetDir = new LocalFile(targetPath.toFile());
            
            String ssFrameworkModel=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_framework_model", SilverStripeVersion.FULL_CMS, project.getProject());
            if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
                sourceFile=sourceFile.getParentFile();
                sourceDir=new LocalFile(sourceFile);
                targetDir=new LocalFile(targetPath.toFile().getParentFile());
                rootPath=(IPath) targetPath.removeLastSegments(1);
            }
            
            
            //Lock file detection/creation
            File lockFile=Path.fromOSString(rootPath.toFile().getParentFile().getAbsolutePath()).append(rootPath.toFile().getName()+".lock").toFile();
            if(lockFile.exists()) {
                long timeDiff=new Date().getTime()-lockFile.lastModified();
                //If the lock file is less than 15 minutes old
                if(timeDiff<15*60*1000) {
                    //Folder is locked
                    return targetPath;
                }else {
                    //Reset the last modified and proceed
                    lockFile.setLastModified(System.currentTimeMillis());
                }
            }else {
                //Create the lock file
                try {
                    new FileOutputStream(lockFile).close();
                    lockFile.setLastModified(System.currentTimeMillis());
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
            
            
            IFileInfo targetInfo = targetDir.fetchInfo();
            boolean update = targetInfo.exists();
            if (update) {
                String sourceVersionString=null;
                String targetVersionString=null;
                
                File sourceVersionFile=new File(Path.fromOSString(sourceFile.getAbsolutePath()).append("version").toOSString());
                try {
                    BufferedReader versionFileReader=new BufferedReader(new FileReader(sourceVersionFile));
                    try {
                        //Read the version
                        sourceVersionString=versionFileReader.readLine();
                        
                        //Close the buffer
                        versionFileReader.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                File targetVersionFile;
                if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
                    targetVersionFile=new File(Path.fromOSString(targetPath.toFile().getParentFile().getAbsolutePath()).append("version").toOSString());
                }else {
                    targetVersionFile=new File(targetPath.append("version").toOSString());
                }
                
                if(targetVersionFile.exists()) {
                    try {
                        BufferedReader versionFileReader=new BufferedReader(new FileReader(targetVersionFile));
                        try {
                            //Read the version
                            targetVersionString=versionFileReader.readLine();
                            
                            //Close the buffer
                            versionFileReader.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    
                    update=(sourceVersionString==null || targetVersionString==null);
                    if(!update) {
                        update = !sourceVersionString.equals(targetVersionString);
                    }
                } else {
                    update=true;
                }
            }
            
            if (update) {
                targetDir.delete(EFS.NONE, new NullProgressMonitor());
                sourceDir.copy(targetDir, EFS.NONE, new NullProgressMonitor());
            }else if(!targetInfo.exists()) {
                sourceDir.copy(targetDir, EFS.NONE, new NullProgressMonitor());
            }
            
            //Update the language provider to say it is up-to-date
            ssLangProvider.setPackedLangUpToDate();
            
            
            //Build Project
            if(this.buildJob==null) {
                this.buildJob=CoreUtility.getBuildJob(project.getProject());
                this.buildJob.schedule();
            }
            
            
            //Delete the lock file
            if(lockFile.exists()) {
                lockFile.delete();
            }
            
            
            return targetPath;
        } catch (Exception e) {
            Logger.logException(e);
        }

        return null;
    }

    public String getDescription() {
        return LanguageModelInitializer.SILVERSTRIPE_LANGUAGE_LIBRARY;
    }

    public int getKind() {
        return K_SYSTEM;
    }

    public IPath getPath() {
        return containerPath;
    }

    public IBuildpathEntry[] getBuildpathEntries() {
        return getBuildpathEntries(fProject);
    }
}