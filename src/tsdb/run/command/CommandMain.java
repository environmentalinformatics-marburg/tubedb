package tsdb.run.command;

import tsdb.run.AbstractCommand;

public class CommandMain extends AbstractCommand {		
	private final MainRunnable mainRunnable;

	public CommandMain(String name, CommandType commandType, String shortDescription, String detailedDescription, MainRunnable mainRunnable) {
		super(name, commandType, shortDescription, detailedDescription);
		this.mainRunnable = mainRunnable;
	}		

	@Override
	public boolean run(String[] parameters) {			
		try {
			mainRunnable.main(parameters);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}