package de.unifreiburg.cs.proglang.jgs;

import de.unifreiburg.cs.proglang.jgs.constraints.TypeVars;
import de.unifreiburg.cs.proglang.jgs.constraints.secdomains.LowHigh;
import de.unifreiburg.cs.proglang.jgs.jimpleutils.Var;
import de.unifreiburg.cs.proglang.jgs.signatures.MethodSignatures;
import de.unifreiburg.cs.proglang.jgs.signatures.SignatureTable;
import de.unifreiburg.cs.proglang.jgs.signatures.Symbol;
import de.unifreiburg.cs.proglang.jgs.typing.Environment;
import de.unifreiburg.cs.proglang.jgs.typing.Environments;
import org.apache.commons.lang3.tuple.Pair;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.unifreiburg.cs.proglang.jgs.TestDomain.*;
import static de.unifreiburg.cs.proglang.jgs.constraints.secdomains.LowHigh.Level;
import static de.unifreiburg.cs.proglang.jgs.signatures.MethodSignatures.*;
import static de.unifreiburg.cs.proglang.jgs.signatures.SignatureTable.makeTable;
import static de.unifreiburg.cs.proglang.jgs.signatures.Symbol.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Some example "code" data for unit testing
 * <p>
 * Created by fennell on 11/16/15.
 */
public class Code {

    public final Jimple j;
    public final Local localX, localY, localZ;
    public final Local localO;
    public final Var<?> varX, varY, varZ, varO;
    public final TypeVars.TypeVar tvarX, tvarY, tvarZ, tvarO;

    // classes and methods for tests
    public final SignatureTable<LowHigh.Level> signatures;
    public final SootClass testClass;
    public final SootMethod testCallee__int;
    public final SootField testLowField_int;

    public final SootMethod testCallee_int_int__int;
    public final SootMethod ignoreSnd_int_int__int;
    public final SootMethod writeToLowReturn0_int__int;

    public final Environment init;

    public Code(TypeVars tvars) {
        this.j = Jimple.v();

        this.localX = j.newLocal("x", IntType.v());
        this.localY = j.newLocal("y", IntType.v());
        this.localZ = j.newLocal("z", IntType.v());
        this.localO = j.newLocal("o", RefType.v("java.lang.Object"));
        this.varX = Var.fromLocal(localX);
        this.varY = Var.fromLocal(localY);
        this.varZ = Var.fromLocal(localZ);
        this.varO = Var.fromLocal(localO);
        this.tvarX = tvars.testParam(varX, "");
        this.tvarY = tvars.testParam(varY, "");
        this.tvarZ = tvars.testParam(varZ, "");
        this.tvarO = tvars.testParam(varO, "");

        this.init = Environments.makeEmpty()
                .add(varX, tvarX)
                .add(varY, tvarY)
                .add(varZ, tvarZ)
                .add(varO, tvarO)
        ;

        this.testClass = new SootClass("TestClass");
        this.testLowField_int = new SootField("testLowField", IntType.v());
        testClass.addField(this.testLowField_int);

        Map<SootMethod, MethodSignatures.Signature<LowHigh.Level>> sigMap = new HashMap<>();
        Symbol.Param<LowHigh.Level> param_x = param(IntType.v(), 0);
        Symbol.Param<LowHigh.Level> param_y = param(IntType.v(), 1);

        // Method:
        this.testCallee__int = new SootMethod("testCallee",
                Collections.emptyList(),
                IntType.v());
        this.testClass.addMethod(testCallee__int);
        sigMap.put(this.testCallee__int,
                makeSignature(signatureConstraints(singleton(leS(
                        Symbol.literal(PUB),
                        ret()))), emptyEffect()));

        // Method:
        this.testCallee_int_int__int = new SootMethod("testCallee",
                asList(IntType.v(),
                        IntType.v()),
                IntType.v());
        this.testClass.addMethod(testCallee_int_int__int);
        SigConstraintSet<Level> sigCstrs =
                signatureConstraints(asList(leS(param_x, ret()),
                        leS(param_y, ret())));
        sigMap.put(this.testCallee_int_int__int,
                makeSignature(sigCstrs, emptyEffect()));

        // Method:
        this.ignoreSnd_int_int__int = new SootMethod("ignoreSnd",
                asList(IntType.v(),
                        IntType.v()),
                IntType.v());
        this.testClass.addMethod(ignoreSnd_int_int__int);
        sigCstrs = signatureConstraints(asList(leS(param_x, ret()), leS(param_y, param_y)));
        sigMap.put(this.ignoreSnd_int_int__int,
                makeSignature(sigCstrs, emptyEffect()));

        //Method:
        this.writeToLowReturn0_int__int = new SootMethod("writeToLow",
                singletonList(IntType.v()),
                IntType.v());
        this.testClass.addMethod(this.writeToLowReturn0_int__int);
        sigCstrs = signatureConstraints(asList((leS(param_x, literal(TLOW))), leS(ret(), ret())));
        sigMap.put(this.writeToLowReturn0_int__int,
                makeSignature(sigCstrs, effects(TLOW)));

        // freeze signatures
        this.signatures = makeTable(sigMap);
    }


    public SootMethod makeMethod(int modifier, String name, List<Local> params, soot.Type retType, List<Unit> bodyStmts) {
        SootMethod m = new SootMethod(name, params.stream().map(Local::getType).collect(toList()), retType, modifier);
        this.testClass.addMethod(m);
        Body body = Jimple.v().newBody(m);
        m.setActiveBody(body);

        // set the statements for the body.. first the identity statements, then the bodyStmts
        IntStream.range(0, params.size()).forEach(pos -> {
            Local l = params.get(pos);
            ParameterRef pr = Jimple.v().newParameterRef(l.getType(), pos);
            body.getUnits().add(Jimple.v().newIdentityStmt(l, pr));
        });
        body.getUnits().addAll(bodyStmts);

        // set the locals for the body
        Set<Local> locals = Stream.concat(
                params.stream(),
                body.getUseAndDefBoxes().stream()
                        .filter(b -> b.getValue() instanceof Local)
                        .map(b -> (Local)b.getValue())
                ).collect(toSet());
        body.getLocals().addAll(locals);

        return m;
    }

    public abstract class AdHocUnitGraph implements DirectedGraph<Unit> {
        protected abstract Collection<Unit> getUnits();

        @Override
        public final int size() {
            return getUnits().size();
        }

        @Override
        public final Iterator<Unit> iterator() {
            return getUnits().iterator();
        }

    }

    public class AdHocForwardUnitGraph extends AdHocUnitGraph {

        private Map<Unit, Set<Unit>> forwardEdges;

        protected AdHocForwardUnitGraph() {
        }

        protected void setForwardEdges(Map<Unit, Set<Unit>> forwardEdges) {
            this.forwardEdges = forwardEdges;
        }

        @Override
        public final List<Unit> getSuccsOf(Unit s) {
            if (this.forwardEdges.containsKey(s)) {
                return new ArrayList<>(this.forwardEdges.get(s));
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public final List<Unit> getHeads() {
            return this.getUnits().stream().filter(s -> this.getPredsOf(s).isEmpty()).collect(toList());
        }

        @Override
        public final List<Unit> getTails() {
            return this.getUnits().stream().filter(s -> this.getSuccsOf(s).isEmpty()).collect(toList());
        }

        @Override
        public final List<Unit> getPredsOf(Unit s) {
            return this.forwardEdges.entrySet().stream()
                    .filter(e -> e.getValue().contains(s))
                    .map(Map.Entry::getKey).collect(toList());
        }

        @Override
        protected final Collection<Unit> getUnits() {
            Set<Unit> result = new HashSet<>();
            result.addAll(this.forwardEdges.keySet());
            this.forwardEdges.values().forEach(result::addAll);
            return result;
        }

    }

    public class LoopWithReflexivePostdom extends AdHocUnitGraph {
        /*
        *     +-----------+
        *     v           |
        *  -> if -> exit  |
        *     +---------> n
        * */

        public final Unit nExit = j.newReturnStmt(IntConstant.v(42));
        public final Unit nIf = j.newIfStmt(j.newEqExpr(IntConstant.v(0), IntConstant.v(0)), nExit);
        public final Unit n = j.newGotoStmt(nIf);

        @Override
        protected List<Unit> getUnits() {
            return Arrays.asList(nIf, n, nExit);
        }

        @Override
        public List<Unit> getHeads() {
            return Collections.singletonList(nIf);
        }

        @Override
        public List<Unit> getTails() {
            return Collections.singletonList(nExit);
        }

        @Override
        public List<Unit> getPredsOf(Unit unit) {
            if (unit.equals(nIf)) {
                return Collections.emptyList();
            } else if (unit.equals(nExit)) {
                return Collections.singletonList(nIf);
            } else if (unit.equals(n)) {
                return Collections.singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

        @Override
        public List<Unit> getSuccsOf(Unit unit) {

            if (unit.equals(nIf)) {
                return asList(nExit, n);
            } else if (unit.equals(nExit)) {
                return Collections.emptyList();
            } else if (unit.equals(n)) {
                return Collections.singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

    }

    public class LoopWhereIfIsExitNode extends AdHocUnitGraph {
        /*
        *     +------+----+
        *     v      |    |
        *  -> if -> n2    |
        *     +---------> n1
        * */

        public final Unit n1 = j.newGotoStmt((Unit) null);
        public final Unit n2 = j.newGotoStmt((Unit) null);
        public final Unit nIf = j.newIfStmt(j.newEqExpr(IntConstant.v(0), IntConstant.v(0)), n1);

        public LoopWhereIfIsExitNode() {
            ((GotoStmt) n1).setTarget(nIf);
            ((GotoStmt) n2).setTarget(nIf);
        }

        @Override
        protected List<Unit> getUnits() {
            return Arrays.asList(nIf, n1, n2);
        }

        @Override
        public List<Unit> getHeads() {
            return Collections.singletonList(nIf);
        }

        @Override
        public List<Unit> getTails() {
            return Collections.singletonList(nIf);
        }

        @Override
        public List<Unit> getPredsOf(Unit unit) {
            if (unit.equals(nIf)) {
                return Collections.emptyList();
            } else if (unit.equals(n1)) {
                return Collections.singletonList(nIf);
            } else if (unit.equals(n2)) {
                return Collections.singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

        @Override
        public List<Unit> getSuccsOf(Unit unit) {

            if (unit.equals(nIf)) {
                return asList(n1, n2);
            } else if (unit.equals(n1)) {
                return Collections.singletonList(nIf);
            } else if (unit.equals(n2)) {
                return Collections.singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }
    }

    /*
    *  -> if -> nExit
    * */
    public class TrivialIf extends AdHocUnitGraph {

        public final Unit nExit = j.newReturnStmt(IntConstant.v(42));
        public final Unit nIf = j.newIfStmt(j.newEqExpr(IntConstant.v(0), IntConstant.v(0)), nExit);

        @Override
        protected List<Unit> getUnits() {
            return asList(nIf, nExit);
        }

        @Override
        public List<Unit> getHeads() {
            return singletonList(nIf);
        }

        @Override
        public List<Unit> getTails() {
            return singletonList(nExit);
        }

        @Override
        public List<Unit> getPredsOf(Unit s) {
            if (s.equals(nIf)) {
                return emptyList();
            } else if (s.equals(nExit)) {
                return singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

        @Override
        public List<Unit> getSuccsOf(Unit s) {
            if (s.equals(nIf)) {
                return singletonList(nExit);
            } else if (s.equals(nExit)) {
                return emptyList();
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }
    }

    public class SimpleDoWhile extends AdHocUnitGraph {
        public final Unit nSet = j.newAssignStmt(localX, localY);
        public final Unit nIf = j.newIfStmt(j.newEqExpr(localZ, IntConstant.v(0)), nSet);
        public final Unit nExit = j.newReturnStmt(IntConstant.v(42));


        @Override
        protected List<Unit> getUnits() {
            return asList(nSet, nIf, nExit);
        }

        @Override
        public List<Unit> getHeads() {
            return singletonList(nSet);
        }

        @Override
        public List<Unit> getTails() {
            return singletonList(nExit);
        }

        @Override
        public List<Unit> getPredsOf(Unit s) {
            if (s.equals(nSet)) {
                return singletonList(nIf);
            } else if (s.equals(nIf)) {
                return singletonList(nSet);
            } else if (s.equals(nExit)) {
                return singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

        @Override
        public List<Unit> getSuccsOf(Unit s) {
            if (s.equals(nSet)) {
                return singletonList(nIf);
            } else if (s.equals(nIf)) {
                return asList(nSet, nExit);
            } else if (s.equals(nExit)) {
                return emptyList();
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }
    }

    public class LoopIncrease extends AdHocUnitGraph {
        public final Unit nExit = j.newReturnStmt(IntConstant.v(42));
        public final Unit nSetZ = j.newAssignStmt(localZ, localY);
        public final Unit nSetX = j.newAssignStmt(localX, IntConstant.v(0));
        public final Unit nIf = j.newIfStmt(j.newEqExpr(localZ, IntConstant.v(0)), nSetZ);
        public final Unit gotoIf = j.newGotoStmt(nIf);

        @Override
        protected List<Unit> getUnits() {
            return asList(nIf, nSetZ, nSetX, gotoIf, nExit);
        }

        @Override
        public List<Unit> getHeads() {
            return singletonList(nIf);
        }

        @Override
        public List<Unit> getTails() {
            return singletonList(nExit);
        }

        @Override
        public List<Unit> getPredsOf(Unit s) {
            if (s.equals(nIf)) {
                return singletonList(gotoIf);
            } else if (s.equals(nSetZ)) {
                return singletonList(nIf);
            } else if (s.equals(nSetX)) {
                return singletonList(nSetZ);
            } else if (s.equals(gotoIf)) {
                return singletonList(nSetX);
            } else if (s.equals(nExit)) {
                return singletonList(nIf);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

        @Override
        public List<Unit> getSuccsOf(Unit s) {
            if (s.equals(nIf)) {
                return asList(nSetZ, nExit);
            } else if (s.equals(nSetZ)) {
                return singletonList(nSetX);
            } else if (s.equals(nSetX)) {
                return singletonList(gotoIf);
            } else if (s.equals(gotoIf)) {
                return singletonList(nIf);
            } else if (s.equals(nExit)) {
                return emptyList();
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }
    }

    public class Spaghetti1 extends AdHocUnitGraph {
        public final Unit setX = j.newAssignStmt(localX, IntConstant.v(0));
        public final Unit if1 = j.newIfStmt(j.newEqExpr(localY, localY), setX);
        public final Unit if2 = j.newIfStmt(j.newEqExpr(localZ, localZ), setX);
        public final Unit gotoIf2 = j.newGotoStmt(if2);
        public final Unit exit = j.newReturnStmt(IntConstant.v(42));
        public final Unit gotoEnd = j.newGotoStmt(exit);


        @Override
        protected List<Unit> getUnits() {
            return asList(if1, if2, setX, gotoIf2, gotoEnd, exit);
        }

        @Override
        public List<Unit> getHeads() {
            return singletonList(if1);
        }

        @Override
        public List<Unit> getTails() {
            return singletonList(exit);
        }

        @Override
        public List<Unit> getPredsOf(Unit s) {
            if (s.equals(if1)) {
                return emptyList();
            } else if (s.equals(if2)) {
                return singletonList(gotoIf2);
            } else if (s.equals(setX)) {
                return asList(if1, if2);
            } else if (s.equals(gotoIf2)) {
                return singletonList(setX);
            } else if (s.equals(exit)) {
                return asList(if1, gotoEnd);
            } else if (s.equals(gotoEnd)) {
                return singletonList(if2);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }

        @Override
        public List<Unit> getSuccsOf(Unit s) {
            if (s.equals(if1)) {
                return asList(setX, exit);
            } else if (s.equals(if2)) {
                return asList(setX, gotoEnd);
            } else if (s.equals(setX)) {
                return singletonList(gotoIf2);
            } else if (s.equals(gotoIf2)) {
                return singletonList(if2);
            } else if (s.equals(exit)) {
                return emptyList();
            } else if (s.equals(gotoEnd)) {
                return singletonList(exit);
            } else {
                throw new RuntimeException("UNEXPECTED CASE");
            }
        }
    }

    private Pair<Unit, Set<Unit>> makeEdge(Unit s) {
        return Pair.of(s, emptySet());
    }

    private Pair<Unit, Set<Unit>> makeEdge(Unit s, Unit... succs) {
        return Pair.of(s, new HashSet<>(asList(succs)));
    }

    @SafeVarargs
    private final Map<Unit, Set<Unit>> makeEdges(Pair<Unit, Set<Unit>> firstEntry, Pair<Unit, Set<Unit>>... restEntries) {
        return Stream.concat(Stream.of(firstEntry), Stream.of(restEntries)).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }


    public class MultipleReturns extends AdHocForwardUnitGraph {
        public final Unit returnX = j.newReturnStmt(localX);
        public final Unit returnZ = j.newReturnStmt(localZ);
        public final Unit ifY = j.newIfStmt(j.newEqExpr(localY, localY), returnX);
        public final Unit ifO = j.newIfStmt(j.newEqExpr(localO, localO), ifY);
        public final Unit returnZero = j.newReturnStmt(IntConstant.v(0));

        public MultipleReturns() {
            setForwardEdges(
                    makeEdges(
                            makeEdge(ifO, ifY, returnZ),
                            makeEdge(ifY, returnX, returnZero)
                    ));
        }
    }
}
