package tsdb.explorer;

import static tsdb.util.AssumptionCheck.throwNull;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Base class for Views.
 * @author woellauer
 *
 */
public abstract class TsdbScene {

	private static final String ESCAPE = ""+(char)27;

	protected final Stage stage;

	protected TsdbScene(String title) {
		this(new Stage(StageStyle.DECORATED), title);
	}

	protected TsdbScene(Stage stage, String title) {
		throwNull(stage);
		this.stage = stage;
		stage.setTitle(title);
		stage.setWidth(640);
		stage.setHeight(480);
		stage.centerOnScreen();
		stage.setMaximized(true);

		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F12));
		stage.setFullScreenExitHint("");

		Parent root = createContent();
		Scene scene = new Scene(root);

		scene.setOnKeyPressed(value->{
			System.out.println(value);
			KeyCode code = value.getCode();
			switch(code) {
			/*case ESCAPE:
				onClose();
				stage.close();
				break;*/
			case F11:
				stage.setFullScreen(!stage.fullScreenProperty().get());
				break;
			default:
			}
		});

		scene.setOnKeyTyped(value->{
			String c = value.getCharacter();
			switch(c) {
			case ESCAPE:
				onClose();
				stage.close();
				break;
			default:
			}
		});
		
		//EventHandler<WindowEvent> value = null;
		stage.setOnCloseRequest(e->onClose());


		stage.setScene(scene);
	}

	protected abstract Parent createContent();

	public void show() {
		stage.show();
		onShown();
	}

	protected void onShown() {

	}
	
	protected void onClose() {
	}
}
