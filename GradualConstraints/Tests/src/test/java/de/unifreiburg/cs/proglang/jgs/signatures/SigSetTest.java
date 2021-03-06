package de.unifreiburg.cs.proglang.jgs.signatures;

import de.unifreiburg.cs.proglang.jgs.constraints.TypeVars;
import de.unifreiburg.cs.proglang.jgs.constraints.secdomains.LowHigh;
import de.unifreiburg.cs.proglang.jgs.constraints.secdomains.LowHigh.Level;
import de.unifreiburg.cs.proglang.jgs.signatures.Literal;
import de.unifreiburg.cs.proglang.jgs.signatures.Return;
import de.unifreiburg.cs.proglang.jgs.signatures.Symbol$;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static de.unifreiburg.cs.proglang.jgs.TestDomain.*;
import static de.unifreiburg.cs.proglang.jgs.signatures.Effects.emptyEffect;
import static de.unifreiburg.cs.proglang.jgs.signatures.MethodSignatures.makeSignature;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

/**
 * Some tests for signatures.
 */
public class SigSetTest {

    private TypeVars tvars;

    @Before
    public void setUp() {
        tvars = new TypeVars();
    }

    private static Signature<Level> makeSig(List<SigConstraint<Level>> cs) {
        return makeSignature(0, cs, emptyEffect());
    }

    private static Signature<Level> makeSig(SigConstraint<Level>... cs) {
        return makeSig(asList(cs));
    }

    private static Signature<Level> makeSig(SigConstraint<Level> c) {
        return makeSig(singletonList(c));
    }

    private static Signature<Level> makeEmptySig() {
        return makeSig(emptyList());
    }


    @Test
    public void testRefinementOfEmpty() {
        SigConstraint<LowHigh.Level> sconstr = leS(new Literal<>(TLOW), new Return<>());
        Signature<LowHigh.Level>  sig = makeSig(sconstr);
        assertThat(sig, notRefines(makeEmptySig()));
        assertThat(makeEmptySig(), refines(makeSig(leS(Symbol$.MODULE$.literal(TLOW), Symbol$.MODULE$.ret()))));
    }

}
