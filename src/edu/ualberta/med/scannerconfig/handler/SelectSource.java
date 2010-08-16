package edu.ualberta.med.scannerconfig.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;

public class SelectSource extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		int scanlibReturn = ScanLib.getInstance().slSelectSourceAsDefault();

		if (scanlibReturn != ScanLib.SC_SUCCESS) {
			ScannerConfigPlugin.openError("Source Selection Error",
					ScanLib.getErrMsg(scanlibReturn));
		}
		return null;
	}
}
