package asmCodeGenerator;

public class Labeller {
	private static int labelSequenceNumber = 0;

	private int labelNumber;
	private String prefix;

	public Labeller(String userPrefix) {
		labelSequenceNumber++;
		labelNumber = labelSequenceNumber;
		this.prefix = makePrefix(userPrefix);
	}
	private String makePrefix(String prefix) {
		return "-" + prefix + "-" + labelNumber + "-";
	}

	public String newLabel(String suffix) {
		return prefix + suffix;
	}
}
