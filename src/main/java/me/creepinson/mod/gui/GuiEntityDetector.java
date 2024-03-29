/**
 * @author Creepinson
 * 
 */
package me.creepinson.mod.gui;

import java.io.IOException;
import java.util.HashSet;

import me.creepinson.mod.Main;
import me.creepinson.mod.packet.PacketEntityDetectorSync;
import me.creepinson.mod.tile.TileEntityEntityDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiEntityDetector extends GuiScreen {

	public TileEntityEntityDetector tile;

	public GuiSlider rangeSlider;

	private GuiEntityTypeList typeList;

	public GuiEntityDetector(TileEntityEntityDetector te) {
		this.tile = te;
		this.tile = (TileEntityEntityDetector) Minecraft.getMinecraft().world.getTileEntity(te.getPos());

		this.setWorldAndResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		
		rangeSlider = new GuiSlider(new GuiResponder() {

			@Override
			public void setEntryValue(int id, String value) {

			}

			@Override
			public void setEntryValue(int id, float value) {
				tile.radiusRange = (int) value;
				PacketEntityDetectorSync packet = new PacketEntityDetectorSync((int) value, tile.entityWhiteList, tile.getPos());
				Main.PACKET_INSTANCE.sendToServer(packet);
			}

			@Override
			public void setEntryValue(int id, boolean value) {

			}
		}, 0, 10, 5, "Detection Range", 1, this.tile.maxRadiusRange, this.tile.radiusRange,
				new GuiSlider.FormatHelper() {

					@Override
					public String getText(int id, String name, float value) {
						return name + ": " + (int)value;
					}
				});

		typeList = new GuiEntityTypeList(this, 160, 0);
		if (this.tile.entityWhiteList.size() > 0) {
			for (Class<? extends Entity> ce : this.tile.entityWhiteList) {
				if (ce == EntityPlayer.class) {
					typeList.selected.add("Player");
				} else {
					ResourceLocation rle = EntityList.getKey(ce);
					typeList.selected.add(rle.toString());
				}
			}
			typeList.scroll.setSelectedList(new HashSet<String>(typeList.selected));
			System.out.println(typeList.selected);
		}

	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			rangeSlider.mousePressed(mc, mouseX, mouseY);
		}
		typeList.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		rangeSlider.mouseReleased(mouseX, mouseY);
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		this.drawDefaultBackground();
		int iy = 45;
		if (typeList.scroll.getSelectedList() != null && typeList.scroll.getSelectedList().size() > 0) {
			for (String s : typeList.scroll.getSelectedList()) {
				if (s == "Player") {
					if (!this.tile.entityWhiteList.contains(EntityPlayer.class)) {
						this.tile.entityWhiteList.add(EntityPlayer.class);
					}
				} else {
					Class<? extends Entity> se = EntityList.getClassFromName(s);
					if (!this.tile.entityWhiteList.contains(se)) {
						this.tile.entityWhiteList.add(se);
					}
				}
			}
			PacketEntityDetectorSync packet = new PacketEntityDetectorSync((int) rangeSlider.getSliderValue(), tile.entityWhiteList, tile.getPos());
			Main.PACKET_INSTANCE.sendToServer(packet);
		}
		rangeSlider.drawButton(mc, mouseX, mouseY, partialTicks);
		typeList.drawScreen(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
