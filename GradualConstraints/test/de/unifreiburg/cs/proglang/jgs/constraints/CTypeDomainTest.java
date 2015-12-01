package de.unifreiburg.cs.proglang.jgs.constraints;

import java.util.HashMap;
import java.util.Map;

import de.unifreiburg.cs.proglang.jgs.jimpleutils.Var;
import org.junit.Before;
import org.junit.Test;

import de.unifreiburg.cs.proglang.jgs.constraints.CTypes.CType;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeDomain.Type;
import de.unifreiburg.cs.proglang.jgs.constraints.TypeVars.TypeVar;
import de.unifreiburg.cs.proglang.jgs.constraints.secdomains.LowHigh.Level;
import soot.IntType;
import soot.jimple.Jimple;

import static de.unifreiburg.cs.proglang.jgs.TestDomain.*;
import static org.junit.Assert.*;
import static de.unifreiburg.cs.proglang.jgs.constraints.CTypes.*;

public class CTypeDomainTest {
    
    private TypeVars tvars;
    private Assignment<Level> a1;
    private TypeVar vh1, vh2, vl, vd, vb;
    private CType<Level> h1, h2;
    
    @Before
    public void setUp() {
       this.tvars = new TypeVars();
       Map<TypeVar, Type<Level>> m = new HashMap<>();
       Var<?> lh1, lh2, ll, ld, lb;
       lh1 = Var.fromLocal(Jimple.v().newLocal("", IntType.v()));
       lh2 = Var.fromLocal(Jimple.v().newLocal("", IntType.v()));
       ll  = Var.fromLocal(Jimple.v().newLocal("", IntType.v()));
       ld  = Var.fromLocal(Jimple.v().newLocal("", IntType.v()));
       lb  = Var.fromLocal(Jimple.v().newLocal("", IntType.v()));

       vh1 = tvars.testParam(lh1, "");
       vh2 = tvars.testParam(lh2, "");
       vl =  tvars.testParam(ll, "");
       vd =  tvars.testParam(ld, "");
       vb =  tvars.testParam(lb, "");

       h1 = variable(vh1);
       h2 = variable(vh2);
       variable(vl);
       variable(vd);
       variable(vb);

       m.put(vh1, THIGH);
       m.put(vh2, THIGH);
       m.put(vl, TLOW);
       m.put(vd, DYN);
       m.put(vb, PUB);
       this.a1 = new Assignment<Level>(m);
    }

    @Test
    public void test() {
        
        assertEquals(h1.apply(a1), THIGH);
        assertEquals(h1, variable(vh1));
        assertNotEquals(h1, h2);
        assertEquals(h1.apply(a1), h2.apply(a1));

    }

}
