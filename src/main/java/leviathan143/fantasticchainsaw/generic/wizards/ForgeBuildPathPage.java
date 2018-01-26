package leviathan143.fantasticchainsaw.generic.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;

public class ForgeBuildPathPage extends NewJavaProjectWizardPageTwo
{
    public ForgeBuildPathPage(NewJavaProjectWizardPageOne mainPage)
    {
	super(mainPage);
    }

    @Override
    public void init(IJavaProject jproject, IPath defaultOutputLocation, IClasspathEntry[] defaultEntries,
	    boolean defaultsOverrideExistingClasspath)
    {
	IClasspathEntry[] forgeDefaultEntries = new IClasspathEntry[] {
		JavaCore.newSourceEntry(jproject.getPath().append("src/main/java")),
		JavaCore.newSourceEntry(jproject.getPath().append("src/main/resources")) };
	super.init(jproject, defaultOutputLocation, forgeDefaultEntries, defaultsOverrideExistingClasspath);
    }
}
