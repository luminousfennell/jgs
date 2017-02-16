package utils.staticResults.superfluousInstrumentation;

/**
 * Created by Nicolas Müller on 06.02.17.
 * The assigned number is necessary to hand over the expected exception to jimple,
 * see initHandleStmtUtils in class {@link analyzer.level1.JimpleInjector}.
 */
public enum ExpectedException {
    NONE(0),
    ILLEGAL_FLOW(1),
    CHECK_LOCAL_PC_CALLED(2),
    ASSIGN_ARG_TO_LOCAL(3),
    JOIN_LEVEL_OF_LOCAL_AND_ASSIGNMENT_LEVEL(4);

    private int number;

    ExpectedException(int number) {
        this.number = number;
    }

    public int getVal() {
        return this.number;
    }
}
