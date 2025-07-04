package com.shynieke.playerstatues.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Locale;

public class NumberFieldBox extends EditBox {

	public float scrollMultiplier = 1;

	public float modValue = 360;
	public int decimalPoints = 0;

	public NumberFieldBox(Font font, int x, int y, int width, int height, Component defaultValue) {
		super(font, x, y, width, height, defaultValue);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void insertText(String textToWrite) {
		if (this.isNumeric(textToWrite)) super.insertText(textToWrite);

		float currentValue = getFloat();
		if (currentValue > 360 || currentValue < -360) {
			this.setValue("0");
		}
	}

	@Override
	public String getValue() {
		return (this.isNumeric(super.getValue()) ? super.getValue() : "0");
	}

	@Override
	public void setValue(String value) {
		super.setValue(String.format(Locale.ROOT, ("%." + decimalPoints + "f"), Float.parseFloat(value)));
	}

	public float getFloat() {
		return NumberUtils.toFloat(super.getValue(), 0.0F);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setHighlightPos(this.getValue().length());
			this.moveCursorToEnd();
		}
	}

	protected boolean isNumeric(String value) {
		return value.equals("-") || NumberUtils.isParsable(value);
	}
}
