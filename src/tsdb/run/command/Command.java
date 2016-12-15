package tsdb.run.command;

public interface Command {
	String getName();
	boolean run(String[] parameters);
	CommandType getType();
	default boolean isInternal() {
		return getType()==CommandType.INTERNAL;
	}
	default String getShortDescription() {
		return "";
	}
	default String getDetailedDescription() {
		return getShortDescription();
	}
	default String getShortHelp() {
		return getName()+" : "+getShortDescription();
	}
	default String getDetailedHelp() {
		return "----\n"+getName()+"\n----\n"+getDetailedDescription();
	}
}