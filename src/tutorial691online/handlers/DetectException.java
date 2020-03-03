package tutorial691online.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import tutorial691online.patterns.DestructiveWrappingFinder;
import tutorial691online.patterns.NestedTryFinder;
import tutorial691online.patterns.OverCatchFinder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;


public class DetectException extends AbstractHandler {
	private static final String CONSOLE_NAME = "Antipattern plugin";
	private static MessageConsole myConsole;
	private static MessageConsoleStream out;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		DetectException.myConsole = findConsole(CONSOLE_NAME);
		DetectException.out = myConsole.newMessageStream();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		detectInProjects(projects);
		
		printMessage("DONE DETECTING");
		return null;
	}	
	
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		
		for (int i = 0; i < existing.length; i++)
		   if (name.equals(existing[i].getName()))
		      return (MessageConsole) existing[i];
		
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}

	// To print messages into the Debug view, not just in the console here.
	public static void printMessage(String message) {
		out.println(message);
	}
	
	private void detectInProjects(IProject[] projects) {
		for(IProject project : projects) {
			printMessage("DETECTING IN: " + project.getName());

			NestedTryFinder nestedTry = new NestedTryFinder();
			DestructiveWrappingFinder destructiveWrapping = new DestructiveWrappingFinder();
			OverCatchFinder overCatchFinder = new OverCatchFinder();
			
			try {
				// find the exceptions and print the methods that contain the exceptions
				
				destructiveWrapping.findExceptions(project);
				destructiveWrapping.printExceptions();
				nestedTry.findExceptions(project);
				nestedTry.printExceptions();
				overCatchFinder.findExceptions(project);
				overCatchFinder.printExceptions();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	}
}
