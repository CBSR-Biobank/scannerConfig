package edu.ualberta.med.scannerconfig;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scanlib.ScanLibFactory;

public class SelectSourceHandler extends AbstractHandler implements IHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		int scanlibReturn = ScanLibFactory.getScanLib()
				.slSelectSourceAsDefault();
		switch (scanlibReturn) {
		case (ScanLib.SC_SUCCESS):
			break;
		case (ScanLib.SC_INVALID_VALUE): // user canceled dialog box
			break;
		}
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setFocus();
		return null;
	}

}
