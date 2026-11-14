package io.github.frostmta.zadrinth.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

/** Botão no visual do Zadrinth (vidro escuro, borda clara, texto em caixa alta). */
public class ZadrinthButton extends ButtonWidget {
	private final boolean primary;

	public ZadrinthButton(int id, int x, int y, int width, int height, String message, boolean primary) {
		super(id, x, y, width, height, message);
		this.primary = primary;
	}

	@Override
	public void render(MinecraftClient client, int mouseX, int mouseY) {
		if (!this.visible) return;
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		int fill;
		int outline;
		if (primary) {
			fill = this.hovered ? 0xE0B49BFF : 0xC89D7BFF;
			outline = 0xFFE8DCFF;
		} else {
			fill = this.hovered ? 0x55FFFFFF : 0x30000000;
			outline = this.hovered ? 0xFFFFFFFF : 0xB0FFFFFF;
		}
		if (!this.active) {
			fill = 0x20000000;
			outline = 0x40FFFFFF;
		}
		Ui.card(this.x, this.y, this.width, this.height, fill, outline);
		int color = this.active ? 0xFFFFFFFF : 0xFF8C8C96;
		Ui.centered(client.textRenderer, "§l" + this.message, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
	}
}
