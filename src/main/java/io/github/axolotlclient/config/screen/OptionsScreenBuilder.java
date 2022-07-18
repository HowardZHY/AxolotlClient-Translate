package io.github.axolotlclient.config.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.config.ConfigManager;
import io.github.axolotlclient.config.options.ColorOption;
import io.github.axolotlclient.config.options.OptionCategory;
import io.github.axolotlclient.config.options.Tooltippable;
import io.github.axolotlclient.config.screen.widgets.ColorOptionWidget;
import io.github.axolotlclient.config.screen.widgets.ColorSelectionWidget;
import io.github.axolotlclient.config.screen.widgets.StringOptionWidget;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

import java.util.Arrays;
import java.util.Objects;

public class OptionsScreenBuilder extends Screen {

    private final Screen parent;
    protected OptionCategory cat;

    protected ColorSelectionWidget picker;

    private ButtonWidgetList list;

    public OptionsScreenBuilder(Screen parent, OptionCategory category){
        this.parent=parent;
        this.cat=category;
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        if(AxolotlClient.someNiceBackground.get()) { // Credit to pridelib for the colors
            DrawUtil.fill(0, 0, width, height/6, 0x0Fff0018);
            DrawUtil.fill(0, height/6, width, height*2/6, 0x0Fffa52c);
            DrawUtil.fill(0, height*2/6, width, height/2, 0x0Fffff41);
            DrawUtil.fill(0, height*2/3, width, height*5/6, 0x0F0000f9);
            DrawUtil.fill(0, height/2, width, height*2/3, 0x0F008018);
            DrawUtil.fill(0, height*5/6, width, height, 0x0F86007d);
        } else {
            if(this.client.world!=null)DrawUtil.fill(0,0, width, height, 0xB0100E0E);
            else renderDirtBackground(0);
        }

        drawCenteredString(textRenderer, cat.getTranslatedName(), width/2, 25, -1);

        super.render(mouseX, mouseY, tickDelta);

        this.list.render(mouseX, mouseY, tickDelta);

        if(picker!=null){
            GlStateManager.disableDepthTest();
            picker.render(MinecraftClient.getInstance(), mouseX, mouseY);
            GlStateManager.enableDepthTest();
        } else {
            list.renderTooltips(mouseX, mouseY);
        }
    }

    public void openColorPicker(ColorOption option){
        picker = new ColorSelectionWidget(option);
    }

    public void closeColorPicker() {
        ConfigManager.save();
        picker=null;
    }

    public boolean isPickerOpen(){
        return picker!=null;
    }

    @Override
    protected void mouseDragged(int i, int j, int k, long l) {
        if(!isPickerOpen()) {
            super.mouseDragged(i, j, k, l);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if(isPickerOpen()){
            if(!picker.isMouseOver(MinecraftClient.getInstance(), mouseX, mouseY)) {
                closeColorPicker();
                this.list.mouseClicked(mouseX, mouseY, button);this.list.entries.forEach(pair -> {
                    if(pair.left instanceof StringOptionWidget && ((StringOptionWidget) pair.left).textField.isFocused()){
                        ((StringOptionWidget) pair.left).textField.mouseClicked(mouseX, mouseY, button);
                    }
                    if(pair.left instanceof ColorOptionWidget){
                        if(((ColorOptionWidget) pair.left).textField.isFocused()) {
                            ((ColorOptionWidget) pair.left).textField.mouseClicked(mouseX, mouseY, button);
                        }
                    }
                });

            } else {
                picker.onClick(mouseX, mouseY);
            }
        } else {
            this.list.mouseClicked(mouseX, mouseY, button);
            this.list.entries.forEach(pair -> {
                if(pair.left instanceof StringOptionWidget && ((StringOptionWidget) pair.left).textField.isFocused()){
                    ((StringOptionWidget) pair.left).textField.mouseClicked(mouseX, mouseY, button);
                }
                if(pair.left instanceof ColorOptionWidget){
                    if(((ColorOptionWidget) pair.left).textField.isFocused()) {
                        ((ColorOptionWidget) pair.left).textField.mouseClicked(mouseX, mouseY, button);
                    }
                }
            });
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        this.list.mouseReleased(mouseX, mouseY, button);
        if(isPickerOpen()) picker.mouseReleased(mouseX, mouseY);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if(button.id==0){
            if(isPickerOpen()){
                closeColorPicker();
            }
            ConfigManager.save();
            MinecraftClient.getInstance().openScreen(parent);
        } else if(button.id==99){
            MinecraftClient.getInstance().openScreen(new CreditsScreen(this));
        }
    }

    @Override
    public void tick() {
        this.list.tick();
        if(isPickerOpen()){
            picker.tick();
        }
    }

    @Override
    public void init() {
        this.list = new ButtonWidgetList(this.client, this.width, this.height, 50, this.height - 50, 25, cat);

        this.buttons.add(new ButtonWidget(0, this.width/2-100, this.height-40, 200, 20, I18n.translate("back")));
        if(Objects.equals(cat.getName(), "config")) this.buttons.add(new ButtonWidget(99, this.width-106, this.height-26, 100, 20, I18n.translate("credits")));
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
        if(!isPickerOpen()) {
            this.list.handleMouse();
        }
    }

    @Override
    protected void keyPressed(char character, int code) {
        super.keyPressed(character, code);
        if(!isPickerOpen()) {
            this.list.keyPressed(character, code);
        } else {
            picker.keyPressed(character, code);
        }
    }

    @Override
    public boolean shouldPauseGame() {
        return false;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if(picker!=null){
            picker.init();
        }
        super.resize(client, width, height);
    }

    public void renderTooltip(Tooltippable tooltippable, int x, int y){
        String[] tooltip = Objects.requireNonNull(tooltippable.getTooltip()).split("<br>");
        this.renderTooltip(Arrays.asList(tooltip), x, y);
    }
}
