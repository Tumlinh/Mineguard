package mineguard.client.gui.troop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mineguard.client.ClientProxy;
import mineguard.client.gui.troop.button.GuiButtonSize;
import mineguard.init.ModConfigClient;
import mineguard.client.gui.troop.button.GuiButtonSetting;
import mineguard.client.gui.Shortcut;
import mineguard.client.gui.troop.button.GuiButtonBehaviour;
import mineguard.client.gui.troop.button.GuiButtonFormation;
import mineguard.client.gui.troop.button.GuiButtonHalt;
import mineguard.settings.Behaviour;
import mineguard.settings.Formation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Keyboard;

public class GuiTroopController extends GuiScreen
{
    public static Map<Integer, Shortcut> keyBindings;

    private int xSize = 90;
    private int ySize = 90;
    private int guiLeft;
    private int guiTop;

    @Override
    public void initGui()
    {
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;

        // Pre-initialise buttons
        GuiButtonHalt HALT = new GuiButtonHalt(0, "Halt (H)");
        GuiButtonFormation FORMATION_CIRCLE = new GuiButtonFormation(1, "Circle formation (C)", Formation.CIRCLE);
        GuiButtonFormation FORMATION_SQUARE = new GuiButtonFormation(2, "Square formation (S)", Formation.SQUARE);
        GuiButtonBehaviour BEHAVIOUR_AGGRESSIVE = new GuiButtonBehaviour(3, "Aggressive mode (A)",
                Behaviour.AGGRESSIVE);
        GuiButtonBehaviour BEHAVIOUR_BERSERKER = new GuiButtonBehaviour(4, "Berserker mode (B)", Behaviour.BERSERKER);
        GuiButtonBehaviour BEHAVIOUR_DEFENSIVE = new GuiButtonBehaviour(5, "Defensive mode (D)", Behaviour.DEFENSIVE);
        GuiButtonBehaviour BEHAVIOUR_STILL = new GuiButtonBehaviour(6, "Still mode (T)", Behaviour.STILL);
        GuiButtonSize SIZE = new GuiButtonSize(7, "Size", 0, 32);

        GuiButtonFormation[] formationButtons = { FORMATION_CIRCLE, FORMATION_SQUARE };
        GuiButtonBehaviour[] behaviourButtons = { BEHAVIOUR_AGGRESSIVE, BEHAVIOUR_BERSERKER, BEHAVIOUR_DEFENSIVE,
                BEHAVIOUR_STILL };

        keyBindings = new HashMap<Integer, Shortcut>();
        keyBindings.put(Keyboard.KEY_H, new Shortcut(HALT, true));
        keyBindings.put(Keyboard.KEY_C, new Shortcut(FORMATION_CIRCLE, true));
        keyBindings.put(Keyboard.KEY_S, new Shortcut(FORMATION_SQUARE, true));
        keyBindings.put(Keyboard.KEY_A, new Shortcut(BEHAVIOUR_AGGRESSIVE, true));
        keyBindings.put(Keyboard.KEY_B, new Shortcut(BEHAVIOUR_BERSERKER, true));
        keyBindings.put(Keyboard.KEY_D, new Shortcut(BEHAVIOUR_DEFENSIVE, true));
        keyBindings.put(Keyboard.KEY_T, new Shortcut(BEHAVIOUR_STILL, true));
        keyBindings.put(Keyboard.KEY_ADD, new Shortcut(SIZE, false));
        keyBindings.put(Keyboard.KEY_SUBTRACT, new Shortcut(SIZE, false));

        // Post-initialise buttons: set position and texture
        HALT.x = guiLeft + 5;
        HALT.y = guiTop + 5;
        HALT.setTexture(0, 0);
        buttonList.add(HALT);

        int i = 0;
        for (GuiButtonFormation button : formationButtons) {
            button.x = guiLeft + 5 + i * 20;
            button.y = guiTop + 25;
            button.width = 20;
            button.height = 18;
            button.setTexture(i * 21, 57);
            buttonList.add(button);
            i++;
        }

        i = 0;
        for (GuiButtonBehaviour button : behaviourButtons) {
            button.x = guiLeft + 5 + i * 20;
            button.y = guiTop + 45;
            button.width = 20;
            button.height = 18;
            button.setTexture(i * 21, 114);
            buttonList.add(button);
            i++;
        }

        SIZE.x = guiLeft + 5;
        SIZE.y = guiTop + 65;
        SIZE.width = 80;
        SIZE.height = 20;
        buttonList.add(SIZE);

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == ClientProxy.TROOP_CONTROLLER_KEYBINDING.getKeyCode())
            this.closeGui();

        // Check registered key bindings
        for (Integer keyCode2 : keyBindings.keySet()) {
            if (keyCode2.equals(keyCode)) {
                keyBindings.get(keyCode).button.onKeyTyped(keyCode);

                if (keyBindings.get(keyCode).closeWindow)
                    this.closeGui();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int alpha = ModConfigClient.TROOP_CONTROLLER_ALPHA;
        int color = alpha << 24;
        Gui.drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, color);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GuiButtonSetting hoveredButton = null;
        for (GuiButton button : buttonList) {
            button.drawButton(this.mc, mouseX, mouseY, partialTicks);
            if (button instanceof GuiButtonSetting && ((GuiButtonSetting) button).isHovered())
                hoveredButton = (GuiButtonSetting) button;
        }
        for (GuiLabel label : labelList)
            label.drawLabel(this.mc, mouseX, mouseY);

        // Draw tooltip for settings
        if (hoveredButton != null) {
            List<String> lines = new ArrayList<String>();
            lines.add(hoveredButton.getHoveringText());
            GuiUtils.drawHoveringText(lines, mouseX, mouseY, mc.displayWidth, mc.displayHeight, -1, mc.fontRenderer);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Close GUI if clicked outside
        if (mouseX < guiLeft || mouseX > guiLeft + xSize || mouseY < guiTop || mouseY > guiTop + ySize)
            this.closeGui();
    }

    private void closeGui()
    {
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
