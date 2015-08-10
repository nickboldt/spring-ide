/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.console;

import java.io.StringWriter;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;

public class CloudAppLogManager extends BootDashModelConsoleManager {

	static final String CONSOLE_TYPE = "org.springframework.ide.eclipse.boot.dash.cloudfoundry.console";
	static final String ATT_TARGET_PROPERTIES = "consoleTargetProperties";
	static final String ATT_APP_NAME = "consoleAppName";
	static final String APP_CONSOLE_ID = "consoleId";

	private IConsoleManager consoleManager;

	private final CloudFoundryRunTarget runTarget;

	public CloudAppLogManager(CloudFoundryRunTarget runTarget) {
		this.runTarget = runTarget;
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();

	}

	@Override
	public void writeToConsole(BootDashElement element, String message, LogType type) throws Exception {
		ApplicationLogConsole console = getApplicationConsole(runTarget.getTargetProperties(), element.getName());
		console.writeApplicationLog(message, type);
		consoleManager.showConsoleView(console);
	}

	@Override
	public void writeToConsole(String appName, String message, LogType type) throws Exception {
		ApplicationLogConsole console = getApplicationConsole(runTarget.getTargetProperties(), appName);
		console.writeApplicationLog(message, type);
		consoleManager.showConsoleView(console);
	}

	/**
	 *
	 * @param targetProperties
	 * @param appName
	 * @return existing console, or null if it does not exist.
	 */
	protected synchronized ApplicationLogConsole getExisitingConsole(TargetProperties targetProperties,
			String appName) {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if (consoles != null) {
			for (IConsole console : consoles) {
				if (console instanceof ApplicationLogConsole) {
					String id = (String) ((MessageConsole) console).getAttribute(APP_CONSOLE_ID);
					String idToCheck = getConsoleId(targetProperties, appName);
					if (idToCheck.equals(id)) {
						return (ApplicationLogConsole) console;
					}
				}
			}
		}

		return null;
	}

	public void stopConsole(String appName) throws Exception {
		ApplicationLogConsole console = getExisitingConsole(runTarget.getTargetProperties(), appName);
		if (console != null) {
			console.close();
			console.destroy();
		}
	}

	/**
	 *
	 * @param targetProperties
	 * @param appName
	 * @return non-null console for the given appname and target properties
	 * @throws Exception
	 *             if console was not created or found
	 */
	protected synchronized ApplicationLogConsole getApplicationConsole(TargetProperties targetProperties,
			String appName) throws Exception {
		if (appName == null) {
			throw BootDashActivator
					.asCoreException("INTERNAL ERROR: No application name specified when writing to console.");
		}
		if (targetProperties == null) {
			throw BootDashActivator.asCoreException("INTERNAL ERROR: No target properties specified for application : "
					+ appName + ". Unable to open console.");
		}
		ApplicationLogConsole appConsole = getExisitingConsole(targetProperties, appName);

		if (appConsole == null) {
			appConsole = new ApplicationLogConsole(getConsoleDisplayName(targetProperties, appName), CONSOLE_TYPE);
			appConsole.setAttribute(ATT_TARGET_PROPERTIES, targetProperties);
			appConsole.setAttribute(ATT_APP_NAME, appName);
			appConsole.setAttribute(APP_CONSOLE_ID, getConsoleId(targetProperties, appName));

			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { appConsole });
		}

		if (appConsole.getLoggregatorToken() == null) {
			CloudFoundryOperations client = runTarget.getClient();

			// Must verify that the application exists before attaching
			// loggregator listener
			CloudApplication app = null;
			try {
				app = client.getApplication(appName);
			} catch (Throwable e) {
				// Ignore.
			}
			if (app != null) {
				StreamingLogToken token = client.streamLogs(appName, appConsole);
				appConsole.setLoggregatorToken(token);
			}
		}

		return appConsole;
	}

	public static String getConsoleId(TargetProperties targetProperties, String appName) {
		return getConsoleDisplayName(targetProperties, appName) + '-' + targetProperties.getUrl();
	}

	public static String getConsoleDisplayName(TargetProperties targetProperties, String appName) {
		StringWriter writer = new StringWriter();
		writer.append(appName);
		writer.append(' ');
		writer.append('[');
		writer.append(targetProperties.get(CloudFoundryTargetProperties.ORG_PROP));
		writer.append(',');
		writer.append(' ');
		writer.append(targetProperties.get(CloudFoundryTargetProperties.SPACE_PROP));
		writer.append(']');

		return writer.toString();
	}

	@Override
	public void showConsole(BootDashElement element) throws Exception {
		ApplicationLogConsole console = getApplicationConsole(runTarget.getTargetProperties(), element.getName());

		if (console != null) {
			// TODO: enable when required.fetch recent.

			// CloudFoundryOperations client = runTarget.getClient();
			//
			// if (console.wasLoggregatorContentCleared()) {
			// List<ApplicationLog> recentLogs =
			// client.getRecentLogs(element.getName());
			// console.writeLoggregatorLogs(recentLogs);
			// }

			consoleManager.showConsoleView(console);
		}
	}
}
