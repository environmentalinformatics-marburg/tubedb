package tsdb.run;

import tsdb.run.command.Command;
import tsdb.run.command.CommandType;

public abstract class AbstractCommand implements Command {
	private final String name;
	private final String shortDescription;
	private final String detailedDescription;
	private final CommandType commandType;
	public AbstractCommand(String name, CommandType commandType, String shortDescription, String detailedDescription) {
		this.name = name;
		this.commandType = commandType;	
		this.shortDescription = shortDescription;
		this.detailedDescription = detailedDescription;	
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public CommandType getType() {
		return commandType;
	}
	@Override
	public String getShortDescription() {
		return shortDescription;
	}
	@Override
	public String getDetailedDescription() {
		return detailedDescription;
	}
}