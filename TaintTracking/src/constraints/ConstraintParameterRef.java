package constraints;

public class ConstraintParameterRef implements IConstraintComponent {

	private final int parameterPos;
	
	public ConstraintParameterRef(int parameterPos) {
		this.parameterPos = parameterPos;
	}

	protected int getParameterPos() {
		return parameterPos;
	}
	
	public String toString() {
		return "ParameterReference[" + parameterPos + "]";
	}

}
