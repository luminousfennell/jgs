package analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import security.ILevel;
import security.ILevelMediator;
import soot.Local;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.toolkits.thread.EncapsulatedMethodAnalysis;
import soot.util.Chain;

/**
 * <h1>Locals map</h1>
 * 
 * The {@link LocalsMap} stores the {@link Local} of a specific analyzed method.
 * The {@link LocalsMap} provides the possibility to map a {@link Local} to a
 * <em>security level</em>, i.e. the locals provide their corresponding
 * <em>security level</em>. Also the {@link LocalsMap} provides the possibility
 * to record the start of an implicit flow, i.e. there is a method to add an
 * {@link IfStmt} together with the corresponding condition level. Additional
 * there a methods for getting the strongest stored <em>program counter</em>
 * <em>security level</em> as well as for finishing an implicit flow for a given
 * {@link IfStmt}.<br />
 * The {@link LocalsMap} will be used in the {@link SecurityLevelAnalysis}
 * analysis as flow object.
 * 
 * <hr />
 * 
 * @author Thomas Vogel
 * @version 0.2
 * @see SecurityLevelAnalysis
 * @see LevelMediator
 */
public class LocalsMap {

    /**
     * <h1>Extended local</h1>
     * 
     * The {@link LevelLocal} encapsulates a {@link Local} object and extends
     * this object with a <em>security level</em>. This <em>security level</em>
     * represents the level of the encapsulated {@link Local}.
     * 
     * <hr />
     * 
     * @author Thomas Vogel
     * @version 0.1
     */
    protected static class LevelLocal {

        /**
         * The <em>security level</em> that the encapsulated {@link Local}
         * object has.
         */
        private ILevel level = null;
        /** The encapsulated {@link Local} object. */
        private final Local local;

        /**
         * Constructor of an {@link LevelLocal} for a given {@link Local}, that
         * should be encapsulated.
         * 
         * @param local
         *            The {@link Local} which should be
         *            {@link EncapsulatedMethodAnalysis} by created
         *            {@link LevelLocal}.
         */
        protected LevelLocal(Local local) {
            this.local = local;
        }

        /**
         * Constructor of an {@link LevelLocal} for a given {@link Local}, that
         * should be encapsulated and it corresponding <em>security level</em>.
         * I.e. given <em>security level</em> is the level of the given
         * {@link Local}.
         * 
         * @param local
         *            The {@link Local} which should be
         *            {@link EncapsulatedMethodAnalysis} by created
         *            {@link LevelLocal}.
         * @param level
         *            The <em>security level</em> of the given {@link Local}.
         */
        protected LevelLocal(Local local, ILevel level) {
            this(local);
            this.level = level;
        }

        /**
         * Returns the <em>security level</em> that corresponds to this
         * {@link Local}.
         * 
         * @return The <em>security level</em> of the {@link Local}.
         */
        protected ILevel getLevel() {
            return level;
        }

        /**
         * Returns the {@link Local} that was extended with a
         * <em>security level</em>.
         * 
         * @return The encapsulated {@link Local}.
         */
        protected Local getLocal() {
            return local;
        }

        /**
         * Sets the given <em>security level</em> to the <em>security level</em>
         * of the encapsulated {@link Local}.
         * 
         * @param level
         *            The <em>security level</em> to which the level of this
         *            {@link LevelLocal} should be set.
         */
        protected void setLevel(ILevel level) {
            this.level = level;
        }

    }

    /**
     * Map that maps a specific {@link Local} to a <em>security level</em>. This
     * level is the <em>security level</em> of the local variable.
     */
    private Map<Local, ILevel> localMap = new HashMap<Local, ILevel>();
    /**
     * {@link LevelMediator} object that provides the possibility to handle and
     * to compare <em>security levels</em>.
     */
    private final ILevelMediator mediator;
    /**
     * Map that maps a specific {@link IfStmt} to a <em>security level</em>. The
     * level represents the <em>security level</em> of the condition.
     */
    private Map<IfStmt, ILevel> programCounter = new HashMap<IfStmt, ILevel>();

    /**
     * Constructor of a {@link LocalsMap} object that requires a {@link Chain}
     * of locals, which are all used {@link Local} of the analyzed method. Also
     * the constructor requires an {@link LevelMediator} object that provides
     * the possibility to compare and to validate <em>security levels</em>.
     * 
     * @param chain
     *            Chain of all used {@link Local} in the currently analyzed
     *            method.
     * @param mediator
     *            Object that provides the possibility to compare and to
     *            validate <em>security levels</em>.
     * @see LocalsMap#addAll(Collection, Map)
     */
    protected LocalsMap(Chain<Local> chain, ILevelMediator mediator) {
        super();
        this.mediator = mediator;
        addAll(convertLocals(chain), null);
    }

    /**
     * Adds the given collection of {@link LevelLocal}s as well as the given
     * <em>program counter</em> map to the {@link LocalsMap}. If an
     * {@link LevelLocal} of the given collection doesn't has an
     * <em>security level</em> the default <em>security level</em> for local
     * variables will be taken for this local variable.
     * 
     * @param locals
     *            The collection of {@link LevelLocal} which should be added.
     * @param programCounter
     *            The <em>program counter</em> map which should be added.
     * @see LevelLocal
     */
    public void addAll(Collection<LevelLocal> locals,
                       Map<IfStmt, ILevel> programCounter) {
        if (programCounter != null)
            this.programCounter = programCounter;
        for (LevelLocal extendedLocal : locals) {
            if (extendedLocal.getLevel() == null) {
                localMap.put(extendedLocal.getLocal(),
                             mediator.getDefaultVariableSecurityLevel());
            } else {
                localMap.put(extendedLocal.getLocal(), extendedLocal.getLevel());
            }
        }
    }

    /**
     * Method which takes a list of {@link LevelLocal} and updates every
     * <em>security level</em> in the {@link LocalsMap#localMap} of those locals
     * which are in the list and also in the {@link LocalsMap} store. The update
     * will be done only if the <em>security level</em> given by an
     * {@link LevelLocal} is stronger than the <em>security level</em> of the
     * stored {@link Local}. Note, if the <em>security level</em> of an extended
     * local is {@code null}, the default <em>security level</em> for local
     * variables will be taken instead.
     * 
     * @param locals
     *            List of {@link LevelLocal} whose <em>security levels</em>
     *            should be updated in the {@link LocalsMap}, if they are
     *            stronger than the existing <em>security level</em>.
     */
    public void addAllStronger(List<LevelLocal> locals) {
        for (LevelLocal extendedLocal : locals) {
            if (extendedLocal.getLevel() == null) {
                extendedLocal.setLevel(mediator.getDefaultVariableSecurityLevel());
            }
            if (localMap.containsKey(extendedLocal.getLocal())) {
                ILevel existingLevel = localMap.get(extendedLocal.getLocal());
                ILevel possibleNewLevel = extendedLocal.getLevel();
                if (existingLevel == null)
                    existingLevel = mediator.getDefaultVariableSecurityLevel();
                if (mediator.isWeakerOrEquals(existingLevel, possibleNewLevel)) {
                    localMap.put(extendedLocal.getLocal(), possibleNewLevel);
                }
            }
        }
    }

    /**
     * Adds a new <em>program counter</em> with the given {@link IfStmt} and the
     * given <em>security level</em> of the condition to the
     * <em>program counter</em> map. I.e. a new implicit flow will be started by
     * adding those values to the map.
     * 
     * @param ifStmt
     *            The {@link IfStmt} at which the implicit flow starts.
     * @param level
     *            The corresponding <em>security level</em> of the if-condition.
     */
    public void addProgramCounterLevel(IfStmt ifStmt, ILevel level) {
        programCounter.put(ifStmt, level);
    }

    /**
     * Clears the map of locals. After calling this method the {@link LocalsMap}
     * doesn't store any {@link Local} together with its <em>security level</em>
     * .
     */
    public void clear() {
        this.localMap.clear();
    }

    /**
     * Checks whether the given {@link Local} is contain by the
     * {@link LocalsMap#localMap}, thus stores also a <em>security level</em>
     * for this given local variable.
     * 
     * @param local
     *            {@link Local} for which should be checked whether the store
     *            contains this local variable.
     * @return {@code true} if the store contains the given local variable,
     *         otherwise {@code false}.
     */
    public boolean containsLocal(Local local) {
        return localMap.containsKey(local);
    }

    /**
     * Indicates whether some other object is "equal to" this one. <br/>
     * An other object is only equals to this if the other object is this object
     * or if it is a {@link LocalsMap} and the map of {@link Local} as well as
     * the map of <em>program counter</em> are equals.
     * 
     * @param obj
     *            Object for which should be checked whether it is equals to
     *            this {@link LocalsMap}.
     * @return {@code true} if the given object is a {@link LocalsMap} and if
     *         the map of {@link Local} as well as the map of
     *         <em>program counter</em> are equals, otherwise {@code false}.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        LocalsMap other = (LocalsMap) obj;
        return localMap.equals(other.localMap)
               && programCounter.equals(other.programCounter);
    }

    /**
     * Creates a list of {@link LevelLocal} that contains all contained locals
     * of {@link LocalsMap#localMap} together with their corresponding
     * <em>security level</em>.
     * 
     * @return List of all contained {@link Local}s of this map as
     *         {@link LevelLocal} including the corresponding
     *         <em>security level</em>.
     */
    public List<LevelLocal> getExtendedLocals() {
        List<LevelLocal> extendedLocals = new ArrayList<LevelLocal>();
        for (Local key : localMap.keySet()) {
            extendedLocals.add(new LevelLocal(key, localMap.get(key)));
        }
        return extendedLocals;
    }

    /**
     * Returns the <em>security level</em> of the given {@link Local} by a
     * lookup in the {@link LocalsMap#localMap}.
     * 
     * @param local
     *            The {@link Local} for which the <em>security level</em> should
     *            be returned.
     * @return The <em>security level</em> of the given local variable.
     */
    public ILevel getLevelOfLocal(Local local) {
        return localMap.get(local);
    }

    /**
     * Returns the <em>program counter</em> map that contains for all stored
     * {@link IfStmt} the <em>security level</em> of the if-condition.
     * 
     * @return The <em>program counter</em> map.
     */
    public Map<IfStmt, ILevel> getProgramCounter() {
        return this.programCounter;
    }

    /**
     * Returns the strongest <em>security level</em> of the
     * <em>program counter</em> map, i.e. if the analysis checks a {@link Unit}
     * inside of multiple implicit flows, the <em>security level</em> of the
     * implicit flow with the strongest {@link IfStmt} condition will be
     * returned.
     * 
     * @return The strongest <em>program counter</em> level.
     */
    public ILevel getStrongestProgramCounterLevel() {
        List<ILevel> levels = new ArrayList<ILevel>(programCounter.values());
        return mediator.getLeastUpperBoundLevelOf(levels);
    }

    /**
     * Checks whether the <em>program counter</em> contains a {@link IfStmt}.
     * I.e. if the map doesn't contain an {@link IfStmt} then the analysis
     * doesn't handle a implicit flow for the current checked {@link Unit}.
     * 
     * @return {@code true} if the <em>program counter</em> map contains an
     *         {@link IfStmt}, means the analysis checks an {@link Unit} inside
     *         of an implicit flow, otherwise {@code false}.
     */
    public boolean hasProgramCounterLevel() {
        return !programCounter.isEmpty();
    }

    /**
     * Removes the <em>program counter</em> with the given {@link IfStmt}, i.e.
     * finishes the implicit flow which starts at the given {@link IfStmt}.
     * 
     * @param ifStmt
     *            {@link IfStmt} which should be removed from the
     *            <em>program counter</em>, i.e. the {@link IfStmt} at which the
     *            implicit flow starts that should be removed.
     */
    public void removeProgramCounterLevel(IfStmt ifStmt) {
        if (!programCounter.isEmpty())
            programCounter.remove(ifStmt);
    }

    /**
     * Method that sets the <em>security level</em> of the given {@link Local}
     * in the store to the given level.
     * 
     * @param local
     *            {@link Local} for which the <em>security level</em> should be
     *            changed.
     * @param level
     *            The new <em>security level</em> of the given local varibale.
     * @return
     */
    public boolean update(Local local, ILevel level) {
        return localMap.put(local, level) != null;
    }

    /**
     * Converts the given collection of {@link Local} to a list of
     * {@link LevelLocal}. Note, that none {@link LevelLocal} has a
     * <em>security level</em> after this conversion.
     * 
     * @param locals
     *            List of {@link Local} which should be converted.
     * @return A list of {@link LevelLocal} that contains those {@link Local}
     *         objects of the given list.
     */
    private List<LevelLocal> convertLocals(Collection<Local> locals) {
        List<LevelLocal> extendedLocals = new ArrayList<LevelLocal>();
        for (Local local : locals) {
            extendedLocals.add(new LevelLocal(local));
        }
        return extendedLocals;
    }

}
