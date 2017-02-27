package utils.staticResults.superfluousInstrumentation;

import utils.exceptions.SuperfluousInstrumentation.*;
import utils.exceptions.InternalAnalyzerException;

/**
 * A Controller to check if superfluous instrumentation is present. Extends {@link PassivController}, which is its
 * passive counterpart that does nothing.
 */
public class ActiveController extends PassivController {
    private int kindOfException;
    ActiveController(int kindOfException) {
        this.kindOfException = kindOfException;
    }

    /**
     * If called, aborts execution with {@link LocalPcCalledException}. Overwrites method in {@link PassivController}.
     * expectedException is where the caller specifies which exception we expect, see {@link ExpectedException}.
     * Exception is only thrown if expectation of caller matches the kind of expection this controller is looking for
     * If controller is not expecting any exception at all (e.g. this.kindOfexception == 0),
     * it throws exceptions allways.
     */
    @Override
    public void abortIfActiveAndExceptionIsType(int expectedException) {

        if (expectedException == this.kindOfException || this.kindOfException == 0) {
            switch (expectedException) {
                case 0:
                    throw new InternalAnalyzerException("Some method in the HandleStmt requested to" +
                            " throw an exception of type 0 - which is no exception!!");
                case 1:
                    throw new InternalAnalyzerException("Some method in the HandleStmt requested to" +
                            "throw an exception of type 1 - which is the IllegalFlowException, which" +
                            "should not be thrown by the controller at all!");
                case 2:
                    throw new LocalPcCalledException("Check local PC was called somewhere with an active controller");
                case 3:
                    throw new AssignArgumentToLocalExcpetion("A sec value of an argument was assigned needlessly" +
                            "to a local while the controller was active");
                case 4:
                    throw new joinLevelOfLocalAndAssignmentLevelException("A joinLevelOfLocalAndAssignment statement" +
                            "was needlessly called while controller was active");
                case 5:
                    throw new setReturnAfterInvokeException("Superfluous Stmt!");
                case 6:
                    throw new checkThatLeException("superfluous stmt!");
                case 7:
                    throw new checkThatPcLeException("superfluous stmt!");
                default:
                    throw new InternalAnalyzerException("Unknown type of exception" + expectedException);
            }
        }
    }
}
