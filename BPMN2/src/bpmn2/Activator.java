package bpmn2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "bpmn2"; //$NON-NLS-1$

	// The shared instance
	private static BundleContext bundleContext;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		bundleContext = context;

	}

	public void stop(BundleContext context) throws Exception {
		bundleContext = null;
	}

}
