package logging;

import java.io.Serializable;
import java.util.logging.Level;

/**
 * <h1>Customized logger levels for the {@link SootLogger}</h1>
 * 
 * The {@link AnalysisLogLevel} class provides various level in order to distinguish between different types of logged messages. E.g. to
 * distinguish between incorrect or missing annotations and violations of the security regulation.
 * 
 * <hr />
 * 
 * @author Thomas Vogel
 * @version 0.2
 * @see AnalysisLog
 * @see SootLogger
 * @see SideEffectLogger
 */
final public class AnalysisLogLevel extends Level {

	/**
	 * Level, to be used for printing configuration information such as what levels should be logged.
	 */
	public static final Level CONFIGURATION = new AnalysisLogLevel("Configuration", FINE.intValue());
	/** Name of the logger level {@link AnalysisLogLevel#CONFIGURATION}. */
	public static final String CONFIGURATION_NAME = CONFIGURATION.getLocalizedName();
	/** Level, to be used for printing debug information. */
	public static final Level DEBUG = new AnalysisLogLevel("Debug", FINER.intValue());
	/** Name of the logger level {@link AnalysisLogLevel#DEBUG}. */
	public static final String DEBUG_NAME = DEBUG.getLocalizedName();
	/**
	 * Level, to be used for printing errors. Such an error which occur during the analysis and were caused by erroneous annotations,
	 * incorrect levels and other incorrect conditions. But errors are not caused by a caught exception or by internal Java exceptions. Note:
	 * this level not represents errors triggered by side effects, <em>security level</em> violations or the implementation checking of the
	 * {@code SecurityLevel} subclass.
	 */
	public static final Level ERROR = new AnalysisLogLevel("Error", Level.WARNING.intValue());
	/** Name of the logger level {@link AnalysisLogLevel#ERROR}. */
	public static final String ERROR_NAME = ERROR.getLocalizedName();
	/**
	 * Level, to be used for printing exceptions. Such an exception are errors which occur during the analysis and were caused by erroneous
	 * annotations, incorrect levels, switch exceptions or by internal Java exceptions. Note: this level not represents errors triggered by
	 * side effects, <em>security level</em> violations or the implementation checking of the {@code SecurityLevel} subclass.
	 */
	public static final Level EXCEPTION = new AnalysisLogLevel("Exception", Level.SEVERE.intValue());
	/** Name of the logger level {@link AnalysisLogLevel#EXCEPTION}. */
	public static final String EXCEPTION_NAME = EXCEPTION.getLocalizedName();
	/**
	 * Level, to be used for printing informations. Such information has no importance and are only of informational nature.
	 */
	public static final Level INFORMATION = new AnalysisLogLevel("Information", Level.CONFIG.intValue());
	/** Name of the logger level {@link AnalysisLogLevel#INFORMATION}. */
	public static final String INFORMATION_NAME = INFORMATION.getLocalizedName();
	/**
	 * Level, to be used for security violations, e.g. if a strong <em>security level</em> will be returned by a method body, but the
	 * corresponding annotation expects a weaker <em>security level</em>.
	 */
	public static final Level SECURITY = new AnalysisLogLevel("Security", 100);
	/** Name of the logger level {@link AnalysisLogLevel#SECURITY}. */
	public static final String SECURITY_NAME = SECURITY.getLocalizedName();
	/**
	 * Level, to be used for printing violations during the check of the {@code SecurityLevel} implementation, e.g. if an id function for a
	 * specific <em>security level</em> does not exist.
	 */
	public static final Level SECURITYCHECKER = new AnalysisLogLevel("Security-Checker", 99);
	/** Name of the logger level {@link AnalysisLogLevel#SECURITYCHECKER}. */
	public static final String SECURITYCHECKER_NAME = SECURITYCHECKER.getLocalizedName();
	/**
	 * Level, to be used for side effect violations, e.g. if a <em>write effect</em> occurs inside of a method body, but the annotation of
	 * this method doesn't take this effect into account.
	 */
	public static final Level SIDEEFFECT = new AnalysisLogLevel("Side-Effect", 200);
	/** Name of the logger level {@link AnalysisLogLevel#SIDEEFFECT}. */
	public static final String SIDEEFFECT_NAME = SIDEEFFECT.getLocalizedName();
	/**
	 * Level, to be used for printing structure elements such as headings, e.g. the analyzed method names during the analysis.
	 */
	public static final Level STRUCTURE = new AnalysisLogLevel("STRUCTURE", Level.CONFIG.intValue() - 100);
	/**
	 * Level, to be used for printing warnings. Such a warning is an important information about the analysis but is not caused by an error or
	 * an exception, rather if the analysis makes specific assumptions. E.g. if a library method was used (the library is not annotated, thus
	 * the analysis has to handle this fact somehow).
	 */
	public static final Level WARNING = new AnalysisLogLevel("Warning", Level.INFO.intValue());
	/** Name of the logger level {@link AnalysisLogLevel#WARNING}. */
	public static final String WARNING_NAME = WARNING.getLocalizedName();
	/**
	 * Version number, which is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes
	 * for that object that are compatible with respect to serialization (see {@link Serializable}).
	 */
	private static final long serialVersionUID = 4614063859303032668L;
	/** Level, to be used <u>only internally</u> for printing headings. */
	protected static final Level HEADING = new AnalysisLogLevel("HEADING", 42);

	/**
	 * Constructor of a SootLoggerLevel.
	 * 
	 * @param name
	 *          Name of the Level.
	 * @param value
	 *          Integer value of the Level.
	 */
	protected AnalysisLogLevel(String name, int value) {
		super(name, value);
	}

}
