package tsdb.run.command;

@FunctionalInterface
public interface MainRunnable extends Runnable {
	void main(String[] args) throws Exception;
	@Override
	default void run() {
		try {
			main(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}