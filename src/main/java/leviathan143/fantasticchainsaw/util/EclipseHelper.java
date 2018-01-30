package leviathan143.fantasticchainsaw.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.osgi.framework.Bundle;

public class EclipseHelper
{
	public static IProject getFirstSelectedProject()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable)
			{
				return ((IAdaptable) firstElement).getAdapter(IProject.class);
			}
		}
		return null;
	}

	public static List<ICompilationUnit> getCurrentSelectedCompilationUnits() throws JavaModelException
	{
		List<ICompilationUnit> compUnits = new ArrayList<ICompilationUnit>();
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection)
			{
				for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();)
				{
					Object obj = iter.next();
					if (obj instanceof ICompilationUnit) compUnits.add((ICompilationUnit) obj);
					else if (obj instanceof IPackageFragment)
						Collections.addAll(compUnits, ((IPackageFragment) obj).getCompilationUnits());
					else if (obj instanceof IPackageFragmentRoot)
					{
						IPackageFragmentRoot root = ((IPackageFragmentRoot) obj);
						for (IJavaElement child : root.getChildren())
						{
							if (child instanceof IPackageFragment)
								Collections.addAll(compUnits, ((IPackageFragment) child).getCompilationUnits());
						}
					}
					else if (obj instanceof IJavaProject)
					{
						for (IPackageFragment fragment : ((IJavaProject) obj).getPackageFragments())
							Collections.addAll(compUnits, fragment.getCompilationUnits());
					}
				}
			}
		}
		return compUnits;
	}

	public static IDocument getSharedWorkingCopy(ITextFileBufferManager manager, CompilationUnit comp)
			throws CoreException
	{
		IPath path = comp.getJavaElement().getPath();
		manager.connect(path, LocationKind.IFILE, null);
		ITextFileBuffer buffer = manager.getTextFileBuffer(path, LocationKind.IFILE);
		return buffer.getDocument();
	}

	public static Bundle getPluginBundle()
	{
		return Platform.getBundle("leviathan143.fantasticchainsaw");
	}

	public static IClasspathEntry getClasspathFile(IJavaProject project, String fileName) throws JavaModelException
	{
		for (IClasspathEntry entry : project.getRawClasspath())
		{
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
			{
				IPath entryPath = entry.getPath();
				if (entryPath.segment(entryPath.segmentCount() - 1).equals(fileName)) return entry;
			}
		}
		return null;
	}

	public static MessageConsole getOrCreateConsole(String name)
	{
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		for (IConsole console : manager.getConsoles())
			if (name.equals(console.getName())) return (MessageConsole) console;
		MessageConsole newConsole = new MessageConsole(name, null);
		manager.addConsoles(new IConsole[]{newConsole});
		return newConsole;
	}
}
