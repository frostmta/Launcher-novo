package io.github.frostmta.zadrinth.client.gui;

import io.github.frostmta.zadrinth.client.ZadrinthKeys;
import io.github.frostmta.zadrinth.client.config.ZadrinthConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Base das telas do Zadrinth Client: desenho pelo {@link Ui} e entrada em
 * coordenadas inteiras (toque/mouse), com o ESC e a tecla do menu voltando
 * para a tela anterior.
 */
public abstract class ZScreen extends Screen {
	protected final @Nullable Screen parent;

	protected ZScreen(@Nullable Screen parent) {
		super(Component.literal("Zadrinth Client"));
		this.parent = parent;
	}

	@Override
	public final void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		Ui.begin(graphics);
		draw(mouseX, mouseY, delta);
		//Campos de texto e outros widgets por cima
		super.render(graphics, mouseX, mouseY, delta);
		Ui.begin(graphics);
		drawOver(mouseX, mouseY, delta);
	}

	/** O fundo é desenhado por cada tela (escurecido do client, sem o desfoque do jogo). */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
	}

	protected abstract void draw(int mouseX, int mouseY, float delta);

	/** Desenho por cima dos widgets (avisos, dicas). */
	protected void drawOver(int mouseX, int mouseY, float delta) {
	}

	protected void onMouseClicked(int mouseX, int mouseY, int button) {
	}

	protected void onMouseReleased(int mouseX, int mouseY, int button) {
	}

	protected void onMouseDragged(int mouseX, int mouseY, int button) {
	}

	/** direction: +1 = rolar para cima, -1 = para baixo */
	protected void onScroll(int mouseX, int mouseY, int direction) {
	}

	/** true = a tecla foi usada */
	protected boolean onKey(int key) {
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean widget = super.mouseClicked(event, doubleClick);
		//Toque fora do campo de texto tira o foco dele
		if (!widget && getFocused() instanceof EditBox box) {
			box.setFocused(false);
			setFocused(null);
		}
		if (!widget) onMouseClicked((int) event.x(), (int) event.y(), event.button());
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		super.mouseReleased(event);
		onMouseReleased((int) event.x(), (int) event.y(), event.button());
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		super.mouseDragged(event, dx, dy);
		onMouseDragged((int) event.x(), (int) event.y(), event.button());
		return true;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
		if (vertical != 0) onScroll((int) x, (int) y, vertical > 0 ? 1 : -1);
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		boolean typing = getFocused() instanceof EditBox box && box.isFocused();
		if (!typing && ZadrinthKeys.isMenuKey(event.key())) {
			onClose();
			return true;
		}
		if (!typing && onKey(event.key())) return true;
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return super.charTyped(event);
	}

	/** Campo de texto sem borda (o client desenha o próprio fundo). */
	protected EditBox textField(int x, int y, int w, int h, String value) {
		EditBox box = new EditBox(this.font, x, y, w, h, Component.literal("Buscar"));
		box.setBordered(false);
		box.setMaxLength(32);
		box.setValue(value);
		box.setTextColor(0xFFFFFFFF);
		return addRenderableWidget(box);
	}

	@Override
	public void onClose() {
		ZadrinthConfig.save();
		this.minecraft.setScreen(parent);
	}

	@Override
	public void removed() {
		ZadrinthConfig.save();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
