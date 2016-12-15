package tsdb.run.command;

import tsdb.run.AbstractCommand;

public class CommandSequence extends AbstractCommand {
	private final Command[] commands;
	public CommandSequence(String name, CommandType commandType, String shortDescription, String detailedDescription, Command... commands) {
		super(name, commandType, shortDescription, detailedDescription);
		this.commands = commands;
	}
	@Override
	public boolean run(String[] parameters) {
		for(Command command:commands) {
			if(!command.run(EMPTY)) {
				return false;
			}
		}
		return true;
	}
	
	static final String[] EMPTY = new String[]{};
}